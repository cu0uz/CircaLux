package com.example.circalux.ui.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.circalux.data.db.SessionDatabase
import com.example.circalux.data.model.*
import com.example.circalux.data.network.WeatherRepository
import com.example.circalux.data.network.WeatherResponse
import com.example.circalux.util.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val locationManager = LocationManager(application)
    private val weatherRepository = WeatherRepository()
    private val luxSensorManager = LuxSensorManager(application)
    private val orientationManager = OrientationManager(application)
    private val notificationHelper = NotificationHelper(application)
    
    private val db = Room.databaseBuilder(
        application,
        SessionDatabase::class.java, "circalux-db"
    ).fallbackToDestructiveMigration().build()
    private val sessionDao = db.sessionDao()
    private val backupManager = BackupManager(application, sessionDao)

    var weatherData by mutableStateOf<WeatherResponse?>(null)
        private set

    val currentUvi: Double
        get() {
            val data = weatherData ?: return 0.0
            
            // Check if data is stale (older than 1 hour)
            // Time format: 2024-06-25T07:00
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
                val dataTime = sdf.parse(data.current?.time ?: "")?.time ?: 0L
                if (System.currentTimeMillis() - dataTime > 3600000) return 0.0
            } catch (e: Exception) { }

            val apiUvi = data.current?.uvIndex ?: 0.0
            val elevation = SolarCalculator.calculateSunElevation(latitude, longitude)
            if (elevation <= 0) return 0.0
            
            // Refined Solar Guard: UVI is physically limited by the sine of elevation 
            // due to atmospheric path length. Power of 1.5 accounts for extra scattering at low angles.
            val maxPossible = 12.5 * Math.pow(kotlin.math.sin(Math.toRadians(elevation)), 1.5)
            return apiUvi.coerceAtMost(maxPossible)
        }

    var latitude by mutableStateOf(0.0)
    var longitude by mutableStateOf(0.0)
    
    var userId by mutableStateOf("")
        private set
    
    var isActivated by mutableStateOf(false)
        private set

    var showOnboarding by mutableStateOf(true)
        private set
    
    var hasAcceptedConsent by mutableStateOf(false)
        private set

    var skinType by mutableStateOf(1) // Default to I
    var skinExposurePercentage by mutableStateOf(0.1) // Default 10%

    val luxValue: StateFlow<Float> = luxSensorManager.luxValue
    val deviceHeading: StateFlow<Float> = orientationManager.heading
    
    val solarSessions: StateFlow<List<SolarSession>> = sessionDao.getAllSolarSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val redLightSessions: StateFlow<List<RedLightSession>> = sessionDao.getAllRedLightSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val profile = sessionDao.getProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val healthMetrics = sessionDao.getAllHealthMetrics()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val supplementEntries: StateFlow<List<SupplementEntry>> = sessionDao.getAllSupplementEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        val prefs = application.getSharedPreferences("activation_prefs", Context.MODE_PRIVATE)
        isActivated = prefs.getBoolean("is_activated", false)
        showOnboarding = !prefs.getBoolean("onboarding_completed", false)
        hasAcceptedConsent = prefs.getBoolean("consent_accepted", false)
        userId = ActivationManager.getOrCreateUserId(application)
        
        // Initial fetch
        refreshLocationAndWeather()
        
        // Observe location changes continuously
        viewModelScope.launch {
            try {
                locationManager.getLocationUpdates().collect { loc ->
                    latitude = loc.latitude
                    longitude = loc.longitude
                    weatherData = weatherRepository.fetchWeather(loc.latitude, loc.longitude)
                    checkSunriseSunsetReminders()
                    CircaLogger.d("Auto-updated location and weather for: ${loc.latitude}, ${loc.longitude}", "MainViewModel")
                }
            } catch (e: Exception) {
                CircaLogger.e("Location updates flow failed", e, "MainViewModel")
            }
        }

        luxSensorManager.startListening()
        orientationManager.startListening()
        viewModelScope.launch {
            profile.collect { p ->
                p?.let {
                    skinType = it.skinType
                    if (it.supplementStartDate > 0) backfillSupplements(it)
                }
            }
        }
        startWeatherMonitoring()
    }

    fun activateApp(code: String): Boolean {
        if (ActivationManager.isCodeValid(userId, code)) {
            getApplication<Application>().getSharedPreferences("activation_prefs", Context.MODE_PRIVATE)
                .edit().putBoolean("is_activated", true).apply()
            isActivated = true
            return true
        }
        return false
    }

    fun completeOnboarding() {
        getApplication<Application>().getSharedPreferences("activation_prefs", Context.MODE_PRIVATE)
            .edit().putBoolean("onboarding_completed", true).apply()
        showOnboarding = false
        if (!isActivated) {
            showTrialInfoDialog = true
        }
    }

    fun acceptConsent() {
        hasAcceptedConsent = true
        getApplication<Application>().getSharedPreferences("activation_prefs", Context.MODE_PRIVATE)
            .edit().putBoolean("consent_accepted", true).apply()
    }

    fun deleteAllData() {
        viewModelScope.launch {
            db.clearAllTables()
            getApplication<Application>().getSharedPreferences("activation_prefs", Context.MODE_PRIVATE)
                .edit().clear().apply()
            getApplication<Application>().getSharedPreferences("circa_prefs", Context.MODE_PRIVATE)
                .edit().clear().apply()
            hasAcceptedConsent = false
            showOnboarding = true
        }
    }

    override fun onCleared() {
        luxSensorManager.stopListening()
        orientationManager.stopListening()
    }

    // Session State
    var isSolarSessionActive by mutableStateOf(false)
    var isRedLightSessionActive by mutableStateOf(false)
    
    var sessionStartTime by mutableStateOf(0L)
    var currentSessionMinutes by mutableStateOf(0)
    var dGeneratedInSession by mutableStateOf(0.0)
    
    var rltPowerWatts by mutableStateOf(100)
    var rltDistanceCm by mutableStateOf(30)
    
    var showTurnAroundReminder by mutableStateOf(false)
    var showFinishSessionDialog by mutableStateOf(false)
    var showFinishRLTDialog by mutableStateOf(false)
    var showTrialExpiredDialog by mutableStateOf(false)
    var showTrialStatusDialog by mutableStateOf(false)
    var showTrialInfoDialog by mutableStateOf(false)
    var sessionsRemaining by mutableStateOf(10)
    
    private var sessionJob: Job? = null

    fun refreshLocationAndWeather() {
        viewModelScope.launch {
            performWeatherRefresh()
        }
    }

    private suspend fun performWeatherRefresh() {
        val loc = locationManager.fetchLocation()
        if (loc != null) {
            latitude = loc.latitude
            longitude = loc.longitude
        }
        
        if (latitude != 0.0 || longitude != 0.0) {
            val response = weatherRepository.fetchWeather(latitude, longitude)
            if (response != null) {
                weatherData = response
                checkSunriseSunsetReminders()
            }
        }
    }

    private fun backfillSupplements(p: UserProfile) {
        viewModelScope.launch {
            if (p.takesSupplements && p.supplementStartDate > 0) {
                CircaLogger.i("Starting supplement backfill from ${p.supplementStartDate}", "MainViewModel")
                val now = System.currentTimeMillis()
                
                val endCal = Calendar.getInstance().apply {
                    timeInMillis = now
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val endTimestamp = endCal.timeInMillis

                val currentCal = Calendar.getInstance().apply {
                    timeInMillis = p.supplementStartDate
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                var count = 0
                while (currentCal.timeInMillis <= endTimestamp) {
                    val normalizedDay = currentCal.timeInMillis
                    val existing = sessionDao.getSupplementByDate(normalizedDay)
                    if (existing == null) {
                        sessionDao.insertSupplementEntry(SupplementEntry(
                            timestamp = normalizedDay,
                            amountUI = p.supplementAmount,
                            isAutomated = true,
                            isRecurring = true
                        ))
                        count++
                    }
                    
                    when (p.supplementFrequency) {
                        "Semanal" -> currentCal.add(Calendar.WEEK_OF_YEAR, 1)
                        "Mensual" -> currentCal.add(Calendar.MONTH, 1)
                        else -> currentCal.add(Calendar.DAY_OF_YEAR, 1) // "Diario"
                    }
                }
                CircaLogger.i("Backfill finished. Created $count new entries.", "MainViewModel")
            }
        }
    }

    private fun startWeatherMonitoring() {
        viewModelScope.launch {
            // Wait for initial data and set initial state without notifying
            while (weatherData == null) {
                performWeatherRefresh()
                if (weatherData == null) delay(30000) // Retry every 30s if no internet/gps
            }
            
            var wasUviHigh = currentUvi >= 3.0
            
            while (true) {
                delay(TimeUnit.MINUTES.toMillis(10))
                performWeatherRefresh()
                
                val uvi = currentUvi
                val isUviHigh = uvi >= 3.0
                
                if (isUviHigh && !wasUviHigh) {
                    val elevation = SolarCalculator.calculateSunElevation(latitude, longitude)
                    notificationHelper.sendUviNotification(
                        "¡Ventana de Vitamina D abierta!", 
                        "UVI: ${String.format("%.1f", uvi)} (Sol a ${String.format("%.0f", elevation)}°). Momento ideal para síntesis."
                    )
                } else if (!isUviHigh && wasUviHigh) {
                    notificationHelper.sendUviNotification(
                        "Ventana de Vitamina D cerrada", 
                        "El UVI ha bajado de 3.0."
                    )
                }
                
                wasUviHigh = isUviHigh
            }
        }
    }

    private fun checkSunriseSunsetReminders() {
        viewModelScope.launch {
            val p = profile.first()
            if (p?.sunriseSunsetAlarmsEnabled != true) return@launch
            
            val weather = weatherData ?: return@launch
            val sunriseStr = weather.daily?.sunrise?.firstOrNull() ?: return@launch
            val sunsetStr = weather.daily?.sunset?.firstOrNull() ?: return@launch
            
            scheduleSunAlarm(sunriseStr, "Quedan 10 min para el amanecer", 1001)
            scheduleSunAlarm(sunsetStr, "Quedan 10 min para el atardecer", 1002)
        }
    }

    private fun scheduleSunAlarm(timeStr: String, message: String, requestCode: Int) {
        val alarmManager = getApplication<Application>().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        try {
            // Time format: 2024-06-25T04:51
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
            val date = sdf.parse(timeStr) ?: return
            
            val calendar = Calendar.getInstance().apply {
                time = date
                add(Calendar.MINUTE, -10)
            }
            
            if (calendar.timeInMillis > System.currentTimeMillis()) {
                val intent = Intent(getApplication(), AlarmReceiver::class.java).apply {
                    putExtra("title", "Aviso Solar")
                    putExtra("message", message)
                }
                
                val pendingIntent = PendingIntent.getBroadcast(
                    getApplication(), 
                    requestCode, 
                    intent, 
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            CircaLogger.e("Error scheduling alarm", e)
        }
    }

    fun startSolarSession() {
        // Trial limitation removed for now
        /*
        if (!isActivated && solarSessions.value.size >= 10) {
            showTrialExpiredDialog = true
            return
        }
        */
        
        if (isRedLightSessionActive) stopRedLightSession()
        isSolarSessionActive = true
        sessionStartTime = System.currentTimeMillis()
        currentSessionMinutes = 0
        dGeneratedInSession = 0.0
        showTurnAroundReminder = false
        
        var medAlarmSent = false
        sessionJob = viewModelScope.launch {
            while (isSolarSessionActive) {
                delay(1000) // Changed to 1 second for real-time timer update
                val elapsedSeconds = (System.currentTimeMillis() - sessionStartTime) / 1000
                currentSessionMinutes = (elapsedSeconds / 60).toInt()
                
                val uvi = currentUvi
                // Calculate vitamin D generated so far based on elapsed time
                dGeneratedInSession = SolarCalculator.estimateVitaminD(
                    uvi = uvi,
                    minutes = elapsedSeconds / 60.0,
                    skinExposurePercentage = skinExposurePercentage,
                    skinType = skinType
                )
                
                // Reminder every 10 minutes (using seconds to be precise)
                if (elapsedSeconds > 0 && elapsedSeconds % 600 == 0L) {
                    showTurnAroundReminder = true
                    notificationHelper.sendAlarm("¡Date la vuelta!", "Han pasado 10 minutos, cambia de posición.")
                }
                
                // Alarm if MED exceeded (only once per session)
                val medTime = com.example.circalux.ui.components.getMedTime(skinType, uvi)
                if (medTime > 0 && currentSessionMinutes >= medTime && !medAlarmSent) {
                    notificationHelper.sendAlarm("¡Límite alcanzado!", "Has superado el tiempo recomendado (MED) para tu tipo de piel.")
                    medAlarmSent = true
                }
            }
        }
    }

    fun stopSolarSession(save: Boolean = false, weatherCondition: String = "") {
        if (save) {
            val uvi = currentUvi
            val session = SolarSession(
                timestamp = sessionStartTime,
                durationMinutes = currentSessionMinutes,
                vitaminDGenerated = dGeneratedInSession,
                uviAvg = uvi,
                locationName = "Lat: ${String.format("%.2f", latitude)} Lng: ${String.format("%.2f", longitude)}",
                skinExposurePercentage = skinExposurePercentage,
                skinType = skinType,
                weatherCondition = weatherCondition
            )
            viewModelScope.launch {
                sessionDao.insertSolarSession(session)
                
                // Trial status logic hidden for now
                /*
                if (!isActivated) {
                    val count = solarSessions.value.size + 1
                    sessionsRemaining = (10 - count).coerceAtLeast(0)
                    showTrialStatusDialog = true
                }
                */
            }
        }
        isSolarSessionActive = false
        sessionJob?.cancel()
        showTurnAroundReminder = false
    }

    fun startRedLightSession() {
        if (isSolarSessionActive) stopSolarSession()
        isRedLightSessionActive = true
        sessionStartTime = System.currentTimeMillis()
        currentSessionMinutes = 0
        
        sessionJob = viewModelScope.launch {
            while (isRedLightSessionActive) {
                delay(1000) // 1 second update
                val elapsedSeconds = (System.currentTimeMillis() - sessionStartTime) / 1000
                currentSessionMinutes = (elapsedSeconds / 60).toInt()
            }
        }
    }

    fun stopRedLightSession(save: Boolean = false) {
        if (save) {
            val session = RedLightSession(
                timestamp = sessionStartTime,
                durationMinutes = currentSessionMinutes,
                lampType = "Estándar",
                lampPowerWatts = rltPowerWatts,
                distanceCm = rltDistanceCm
            )
            viewModelScope.launch {
                sessionDao.insertRedLightSession(session)
            }
        }
        isRedLightSessionActive = false
        sessionJob?.cancel()
    }
    
    fun deleteSolarSession(session: SolarSession) {
        viewModelScope.launch {
            sessionDao.deleteSolarSession(session)
        }
    }

    fun deleteRedLightSession(session: RedLightSession) {
        viewModelScope.launch {
            sessionDao.deleteRedLightSession(session)
        }
    }

    fun deleteSupplementEntry(entry: SupplementEntry) {
        viewModelScope.launch {
            sessionDao.deleteSupplementEntry(entry)
        }
    }

    fun deleteHealthMetric(metric: HealthMetric) {
        viewModelScope.launch {
            sessionDao.deleteHealthMetric(metric)
        }
    }

    fun deleteBodyMeasurement(measurement: BodyMeasurement) {
        viewModelScope.launch {
            sessionDao.deleteBodyMeasurement(measurement)
        }
    }
    
    fun saveProfile(newProfile: UserProfile) {
        viewModelScope.launch {
            sessionDao.saveProfile(newProfile)
            backfillSupplements(newProfile)
        }
    }
    
    fun addHealthMetric(glucose: Double, ketones: Double, timestamp: Long = System.currentTimeMillis()) {
        val gki = if (ketones > 0) glucose / 18.0 / ketones else 0.0
        val metric = HealthMetric(
            timestamp = timestamp,
            glucose = glucose,
            ketones = ketones,
            gki = gki
        )
        viewModelScope.launch {
            sessionDao.insertHealthMetric(metric)
        }
    }

    fun addBodyMeasurement(
        neck: Double,
        waist: Double,
        hip: Double,
        chest: Double,
        biceps: Double,
        thigh: Double,
        weight: Double,
        timestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val measurement = calculateBodyMeasurement(
                timestamp = timestamp,
                neck = neck,
                waist = waist,
                hip = hip,
                chest = chest,
                biceps = biceps,
                thigh = thigh,
                weight = weight
            )
            sessionDao.insertBodyMeasurement(measurement)
        }
    }

    private fun calculateBodyMeasurement(
        id: Long = 0,
        timestamp: Long,
        neck: Double,
        waist: Double,
        hip: Double,
        chest: Double,
        biceps: Double,
        thigh: Double,
        weight: Double
    ): BodyMeasurement {
        val userProfile = profile.value ?: UserProfile()
        val height = if (userProfile.height > 0) userProfile.height else 170.0
        val whtr = waist / height
        
        var bodyFat: Double
        try {
            if (userProfile.gender == "Mujer") {
                bodyFat = 495 / (1.29579 - 0.35004 * log10(max(1.0, waist + hip - neck)) + 0.22100 * log10(height)) - 450
            } else {
                bodyFat = 495 / (1.0324 - 0.19077 * log10(max(1.0, waist - neck)) + 0.15456 * log10(height)) - 450
            }
        } catch (e: Exception) {
            bodyFat = 0.0
        }
        if (bodyFat.isNaN() || bodyFat.isInfinite() || bodyFat < 0) bodyFat = 0.0

        return BodyMeasurement(
            id = id,
            timestamp = timestamp,
            neck = neck,
            waist = waist,
            hip = hip,
            chest = chest,
            biceps = biceps,
            thigh = thigh,
            weight = weight,
            whtr = whtr,
            bodyFatNavy = bodyFat
        )
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            backupManager.exportToCsv(uri)
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            backupManager.importFromCsv(uri)
        }
    }

    fun sendLogsByEmail(context: Context) {
        val logs = CircaLogger.getLogs()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("myt8dolgj@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Error Report - CircaLux - $userId")
            putExtra(Intent.EXTRA_TEXT, "Logs:\n\n$logs")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Enviar reporte de errores"))
    }

    fun getAppVersion(): String {
        return try {
            val pInfo = getApplication<Application>().packageManager.getPackageInfo(getApplication<Application>().packageName, 0)
            pInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    fun testNotification() {
        notificationHelper.sendAlarm("Prueba de Alarma", "Esta es una notificación de prueba para verificar el sonido y la vibración.")
    }

    val bodyMeasurements = sessionDao.getAllBodyMeasurements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSolarSession(session: SolarSession) {
        viewModelScope.launch {
            sessionDao.insertSolarSession(session)
        }
    }

    fun updateRedLightSession(session: RedLightSession) {
        viewModelScope.launch {
            sessionDao.insertRedLightSession(session)
        }
    }

    fun updateSupplementEntry(entry: SupplementEntry) {
        viewModelScope.launch {
            sessionDao.insertSupplementEntry(entry)
        }
    }

    fun updateHealthMetric(metric: HealthMetric) {
        viewModelScope.launch {
            sessionDao.insertHealthMetric(metric)
        }
    }

    fun updateBodyMeasurement(measurement: BodyMeasurement) {
        viewModelScope.launch {
            val recalculated = calculateBodyMeasurement(
                id = measurement.id,
                timestamp = measurement.timestamp,
                neck = measurement.neck,
                waist = measurement.waist,
                hip = measurement.hip,
                chest = measurement.chest,
                biceps = measurement.biceps,
                thigh = measurement.thigh,
                weight = measurement.weight
            )
            sessionDao.insertBodyMeasurement(recalculated)
        }
    }
}
