package com.example.circalux

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.circalux.ui.components.*
import com.example.circalux.ui.screens.*
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Description
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import com.example.circalux.ui.theme.CircaLuxTheme
import com.example.circalux.ui.theme.SolarYellow
import com.example.circalux.ui.viewmodel.MainViewModel
import com.example.circalux.util.*
import androidx.compose.foundation.clickable
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CircaLogger.init(filesDir)
        CircaLogger.i("MainActivity onCreate started", "Lifecycle")
        enableEdgeToEdge()
        setContent {
            CircaLuxTheme {
                val viewModel: MainViewModel = viewModel()
                var showUviLegend by remember { mutableStateOf(false) }
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                var currentScreen by remember { mutableStateOf("home") }
                var activeTipKey by remember { mutableStateOf<String?>(null) }
                var logoTapCount by remember { mutableStateOf(0) }
                var showEasterEgg by remember { mutableStateOf(false) }

                if (viewModel.showOnboarding) {
                    OnboardingScreen(onFinished = { viewModel.completeOnboarding() })
                } else {
                    val permissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestMultiplePermissions()
                    ) { permissions ->
                        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                        ) {
                            viewModel.refreshLocationAndWeather()
                        }
                    }

                    LaunchedEffect(Unit) {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        gesturesEnabled = currentScreen != "manifesto", // Disable swipe gesture on blog to avoid scroll conflicts
                        drawerContent = {
                            ModalDrawerSheet(
                                drawerContainerColor = Color(0xFF050B14),
                                drawerContentColor = Color.White
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .padding(24.dp),
                                    contentAlignment = Alignment.BottomStart
                                ) {
                                    Column {
                                        CircaLuxAppIcon(size = 64.dp)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            "CircaLux", 
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Black,
                                            color = SolarYellow
                                        )
                                    }
                                }
                                
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.1f))
                                Spacer(modifier = Modifier.height(16.dp))

                                val navItems = listOf(
                                    Triple("Home", "home", Icons.Default.Home),
                                    Triple("Historial y Gráficas", "history", Icons.Default.History),
                                    Triple("Mi Perfil", "profile", Icons.Default.Person),
                                    Triple("Blog Jota Manifesto", "manifesto", Icons.Default.Description),
                                    Triple("Estado de Sensores", "sensors", Icons.Default.CompassCalibration),
                                    Triple("Ajustes", "settings", Icons.Default.Settings)
                                )

                                navItems.forEach { (label, route, icon) ->
                                    NavigationDrawerItem(
                                        label = { Text(label, fontWeight = FontWeight.SemiBold) },
                                        selected = currentScreen == route,
                                        onClick = { currentScreen = route; scope.launch { drawerState.close() } },
                                        icon = { Icon(icon, null) },
                                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                        colors = NavigationDrawerItemDefaults.colors(
                                            selectedContainerColor = SolarYellow.copy(alpha = 0.15f),
                                            selectedIconColor = SolarYellow,
                                            selectedTextColor = SolarYellow,
                                            unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                            unselectedTextColor = Color.White.copy(alpha = 0.6f)
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                val context = LocalContext.current
                                NavigationDrawerItem(
                                    label = { Text("Acerca de", fontWeight = FontWeight.SemiBold) },
                                    selected = currentScreen == "about",
                                    onClick = { currentScreen = "about"; scope.launch { drawerState.close() } },
                                    icon = { Icon(Icons.Default.Info, null) },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                    colors = NavigationDrawerItemDefaults.colors(
                                        selectedContainerColor = SolarYellow.copy(alpha = 0.15f),
                                        selectedIconColor = SolarYellow,
                                        selectedTextColor = SolarYellow,
                                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                        unselectedTextColor = Color.White.copy(alpha = 0.6f)
                                    )
                                )

                                NavigationDrawerItem(
                                    label = { Text("Reportar Error", fontWeight = FontWeight.SemiBold) },
                                    selected = false,
                                    onClick = { viewModel.sendLogsByEmail(context) },
                                    icon = { Icon(Icons.Default.BugReport, null) },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                    colors = NavigationDrawerItemDefaults.colors(
                                        unselectedIconColor = Color(0xFFE57373),
                                        unselectedTextColor = Color(0xFFE57373)
                                    )
                                )

                                NavigationDrawerItem(
                                    label = { Text("Apoyar en Ko-fi", fontWeight = FontWeight.SemiBold) },
                                    selected = false,
                                    onClick = {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://ko-fi.com/cu0uz"))
                                        context.startActivity(intent)
                                    },
                                    icon = { Icon(Icons.Default.Coffee, null) },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                                    colors = NavigationDrawerItemDefaults.colors(
                                        unselectedIconColor = Color(0xFF29ABE2),
                                        unselectedTextColor = Color(0xFF29ABE2)
                                    )
                                )

                                Column(
                                    modifier = Modifier
                                        .padding(24.dp)
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "CircaLux v${viewModel.getAppVersion()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.4f)
                                    )
                                    Text(
                                        "Created with AI by cu0uz in 2026",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    ) {
                            Scaffold(
                            modifier = Modifier.fillMaxSize().imePadding(),
                            containerColor = Color(0xFF050B14),
                            contentWindowInsets = WindowInsets.systemBars,
                            topBar = {
                                CircaLuxTopBar(
                                    onMenuClick = { scope.launch { drawerState.open() } },
                                    onLogoClick = {
                                        currentScreen = "home"
                                        logoTapCount++
                                        if (logoTapCount >= 7) {
                                            showEasterEgg = true
                                            logoTapCount = 0
                                        }
                                    }
                                )
                            }
                        ) { innerPadding ->
                            Box(modifier = Modifier.padding(innerPadding)) {
                                when (currentScreen) {
                                    "home" -> {
                                        var showLuxExplanation by remember { mutableStateOf(false) }
                                        var showSunTimesExplanation by remember { mutableStateOf(false) }
                                        var showSunAngleExplanation by remember { mutableStateOf(false) }
                                        var showSunCompassExplanation by remember { mutableStateOf(false) }

                                        HomeScreen(
                                            viewModel,
                                            onUviClick = { showUviLegend = true },
                                            onLuxClick = { showLuxExplanation = true },
                                            onSunTimesClick = { showSunTimesExplanation = true },
                                            onSunAngleClick = { showSunAngleExplanation = true },
                                            onSunCompassClick = { showSunCompassExplanation = true },
                                            onShowTip = { activeTipKey = it }
                                        )

                                        if (showLuxExplanation) {
                                            MetricExplanationDialog(
                                                metricKey = "luminancia",
                                                currentValue = String.format("%.0f", viewModel.luxValue.collectAsState().value),
                                                unit = "Lux",
                                                onDismiss = { showLuxExplanation = false }
                                            )
                                        }
                                        if (showSunTimesExplanation) {
                                            val weather = viewModel.weatherData
                                            val sunrise = weather?.daily?.sunrise?.firstOrNull()?.split("T")?.lastOrNull() ?: "--:--"
                                            val sunset = weather?.daily?.sunset?.firstOrNull()?.split("T")?.lastOrNull() ?: "--:--"
                                            MetricExplanationDialog(
                                                metricKey = "amanecer_atardecer",
                                                currentValue = "$sunrise / $sunset",
                                                unit = "Horas",
                                                onDismiss = { showSunTimesExplanation = false }
                                            )
                                        }
                                        if (showSunAngleExplanation) {
                                            val sunElevation = SolarCalculator.calculateSunElevation(viewModel.latitude, viewModel.longitude)
                                            MetricExplanationDialog(
                                                metricKey = "angulo_solar",
                                                currentValue = "${String.format("%.1f", sunElevation)}°",
                                                unit = "Grados",
                                                onDismiss = { showSunAngleExplanation = false }
                                            )
                                        }
                                        if (showSunCompassExplanation) {
                                            val deviceHeading by viewModel.deviceHeading.collectAsState()
                                            val sunAzimuth = SolarCalculator.calculateSunAzimuth(viewModel.latitude, viewModel.longitude)
                                            val diff = (sunAzimuth - deviceHeading + 360) % 360
                                            MetricExplanationDialog(
                                                metricKey = "brujula_solar",
                                                currentValue = "${String.format("%.0f", diff)}°",
                                                unit = "Desvío Sol",
                                                onDismiss = { showSunCompassExplanation = false },
                                                visualContent = {
                                                    SolarCompass(
                                                        sunAzimuth = sunAzimuth.toFloat(),
                                                        deviceHeading = deviceHeading
                                                    )
                                                }
                                            )
                                        }
                                    }
                                    "history" -> HistoryScreen(viewModel)
                                    "profile" -> ProfileScreen(viewModel)
                                    "settings" -> SettingsScreen(viewModel)
                                    "manifesto" -> ManifestoScreen()
                                    "sensors" -> SensorStatusScreen(viewModel)
                                    "about" -> AboutScreen()
                                    "privacy" -> PrivacyPolicyScreen()
                                }
                            }

                            if (!viewModel.hasAcceptedConsent) {
                                ConsentDialog(onAccept = { viewModel.acceptConsent() })
                            }

                            val profileState by viewModel.profile.collectAsState()
                            val skinTypeForLegend = profileState?.skinType ?: viewModel.skinType

                            if (showUviLegend) {
                                UVILegendDialog(
                                    skinType = skinTypeForLegend,
                                    onDismiss = { showUviLegend = false }
                                )
                            }
                            
                            // Turn Around Reminder
                            if (viewModel.showTurnAroundReminder) {
                                AlertDialog(
                                    onDismissRequest = { viewModel.showTurnAroundReminder = false },
                                    title = { Text("¡Date la vuelta!") },
                                    text = { Text("Es hora de exponer el otro lado de tu cuerpo para un reparto equitativo de Vitamina D.") },
                                    confirmButton = {
                                        TextButton(onClick = { viewModel.showTurnAroundReminder = false }) {
                                            Text("ENTENDIDO")
                                        }
                                    }
                                )
                            }
                            
                            // Finish Session Dialog
                            if (viewModel.showFinishSessionDialog) {
                                FinishSessionDialog(
                                    onSave = { weather -> 
                                        viewModel.stopSolarSession(save = true, weatherCondition = weather)
                                        viewModel.showFinishSessionDialog = false
                                    },
                                    onDiscard = {
                                        viewModel.stopSolarSession(save = false)
                                        viewModel.showFinishSessionDialog = false
                                    },
                                    onDismiss = { viewModel.showFinishSessionDialog = false }
                                )
                            }
                            
                            if (viewModel.showFinishRLTDialog) {
                                AlertDialog(
                                    onDismissRequest = { viewModel.showFinishRLTDialog = false },
                                    title = { Text("Finalizar Sesión Luz Roja") },
                                    text = { Text("¿Deseas guardar esta sesión en tu historial?") },
                                    confirmButton = {
                                        Button(onClick = { 
                                            viewModel.stopRedLightSession(save = true)
                                            viewModel.showFinishRLTDialog = false
                                        }) { Text("GUARDAR") }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = {
                                            viewModel.stopRedLightSession(save = false)
                                            viewModel.showFinishRLTDialog = false
                                        }) { Text("DESCARTAR", color = MaterialTheme.colorScheme.error) }
                                    }
                                )
                            }
                            
                            if (showEasterEgg) {
                                AlertDialog(
                                    onDismissRequest = { showEasterEgg = false },
                                    title = { Text("☀️ ¡MODO FOTOSÍNTESIS ACTIVADO! 🌿") },
                                    text = { 
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Has desbloqueado el secreto de los maestros solares.", textAlign = TextAlign.Center)
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text("🌻\n¡Ahora eres técnicamente una planta!\nNo olvides regarte con mucha agua y buscar el sol.", fontSize = 20.sp, textAlign = TextAlign.Center)
                                        }
                                    },
                                    confirmButton = {
                                        Button(onClick = { showEasterEgg = false }) { Text("¡SOY UNA PLANTA!") }
                                    }
                                )
                            }
                            
                            if (viewModel.showTrialStatusDialog) {
                                TrialStatusPopup(
                                    sessionsRemaining = viewModel.sessionsRemaining,
                                    onDismiss = { viewModel.showTrialStatusDialog = false }
                                )
                            }

                            if (viewModel.showTrialInfoDialog) {
                                // Trial info hidden for now
                                viewModel.showTrialInfoDialog = false
                            }
                            
                            if (viewModel.showTrialExpiredDialog) {
                                // Trial expired modal hidden for now
                                viewModel.showTrialExpiredDialog = false
                            }

                            activeTipKey?.let { key ->
                                val tip = ScientificKnowledgeBase.getTip(key)
                                if (tip != null) {
                                    AlertDialog(
                                        onDismissRequest = { activeTipKey = null },
                                        title = { Text(tip.title, fontWeight = FontWeight.Bold) },
                                        text = {
                                            Column {
                                                Text(tip.content)
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Text(
                                                    "Fuente: ${tip.source}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White.copy(alpha = 0.5f)
                                                )
                                            }
                                        },
                                        confirmButton = {
                                            TextButton(onClick = { activeTipKey = null }) {
                                                Text("ENTENDIDO")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    viewModel: MainViewModel, 
    onUviClick: () -> Unit,
    onLuxClick: () -> Unit,
    onSunTimesClick: () -> Unit,
    onSunAngleClick: () -> Unit,
    onSunCompassClick: () -> Unit,
    onShowTip: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.profile.collectAsState()
    val skinType = profile?.skinType ?: viewModel.skinType
    val weather = viewModel.weatherData
    val currentUvi = viewModel.currentUvi
    val temp = weather?.current?.temperature ?: 0.0
    val lux by viewModel.luxValue.collectAsState()
    val deviceHeading by viewModel.deviceHeading.collectAsState()
    
    val sunElevation = SolarCalculator.calculateSunElevation(viewModel.latitude, viewModel.longitude)
    val sunAzimuth = SolarCalculator.calculateSunAzimuth(viewModel.latitude, viewModel.longitude)
    
    val sunrise = weather?.daily?.sunrise?.firstOrNull()?.split("T")?.lastOrNull() ?: "--:--"
    val sunset = weather?.daily?.sunset?.firstOrNull()?.split("T")?.lastOrNull() ?: "--:--"

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            HeaderSection(temp)
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UviCard(uvi = currentUvi, skinType = skinType, onClick = onUviClick, modifier = Modifier.weight(1f))
                    LuxCard(lux = lux, onClick = onLuxClick, modifier = Modifier.weight(1f))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Surface(
                    color = Color(0xFF0F1724),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onSunTimesClick() }
                        ) {
                            SolarInfoCard(label = "Amanecer", value = sunrise, modifier = Modifier.weight(1f))
                            SolarInfoCard(label = "Atardecer", value = sunset, modifier = Modifier.weight(1f))
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            SolarInfoCard(
                                label = "Ángulo Solar", 
                                value = "${String.format("%.1f", sunElevation)}°", 
                                modifier = Modifier.weight(1f).clickable { onSunAngleClick() }
                            )
                            SolarInfoCard(
                                label = "Brújula Sol", 
                                value = "", 
                                modifier = Modifier.weight(1f).clickable { onSunCompassClick() }
                            ) {
                                Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                                    SolarCompass(
                                        sunAzimuth = sunAzimuth.toFloat(),
                                        deviceHeading = deviceHeading
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Ubicación Actual
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn, 
                                contentDescription = null, 
                                tint = SolarYellow.copy(alpha = 0.5f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "UBICACIÓN: ${String.format("%.4f", viewModel.latitude)}, ${String.format("%.4f", viewModel.longitude)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.4f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item {
            val vitaminDPerMin = SolarCalculator.estimateVitaminD(currentUvi, 1.0, viewModel.skinExposurePercentage, viewModel.skinType)
            Column(modifier = Modifier.clickable { onShowTip("vitamina_d") }) {
                SolarSessionControls(
                    isActive = viewModel.isSolarSessionActive,
                    onStart = { viewModel.startSolarSession() },
                    onStop = { viewModel.showFinishSessionDialog = true },
                    exposure = viewModel.skinExposurePercentage,
                    onExposureChange = { viewModel.skinExposurePercentage = it },
                    vitaminDPerMin = vitaminDPerMin
                )
            }
        }
        
        if (viewModel.isSolarSessionActive || viewModel.isRedLightSessionActive) {
            item {
                SessionStats(viewModel.currentSessionMinutes, viewModel.dGeneratedInSession)
            }
        }

        item {
            Column(modifier = Modifier.clickable { onShowTip("luz_roja") }) {
                RedLightSessionCard(
                    isActive = viewModel.isRedLightSessionActive,
                    onStart = { viewModel.startRedLightSession() },
                    onStop = { viewModel.showFinishRLTDialog = true },
                    powerWatts = viewModel.rltPowerWatts,
                    onPowerChange = { viewModel.rltPowerWatts = it },
                    distanceCm = viewModel.rltDistanceCm,
                    onDistanceChange = { viewModel.rltDistanceCm = it }
                )
            }
        }
        
        item {
            ForecastList(weather?.daily)
        }
    }
}

@Composable
fun HeaderSection(temp: Double) {
    val date = SimpleDateFormat("EEEE, d 'DE' MMMM", Locale("es", "ES")).format(Date())
    Surface(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        color = Color(0xFF0F1724),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = date.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${String.format("%.0f", temp)}°C",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = SolarYellow
                )
                Text(
                    text = "DESPEJADO",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SessionStats(minutes: Int, dGenerated: Double) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = SolarYellow.copy(alpha = 0.1f),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SolarYellow.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("TIEMPO", style = MaterialTheme.typography.labelSmall, color = SolarYellow.copy(alpha = 0.6f))
                Text("$minutes min", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color.White)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("VITAMINA D", style = MaterialTheme.typography.labelSmall, color = SolarYellow.copy(alpha = 0.6f))
                Text("${String.format("%.0f", dGenerated)} IU", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = SolarYellow)
            }
        }
    }
}

@Composable
fun FinishSessionDialog(
    onSave: (String) -> Unit,
    onDiscard: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedWeather by remember { mutableStateOf("Despejado") }
    val weatherOptions = listOf("Despejado", "Nublado", "Lluvia")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Finalizar Sesión Solar") },
        text = {
            Column {
                Text("¿Cómo está el tiempo ahora?")
                Spacer(modifier = Modifier.height(8.dp))
                weatherOptions.forEach { option ->
                    Row(
                        Modifier.fillMaxWidth().clickable { selectedWeather = option }.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedWeather == option, onClick = { selectedWeather = option })
                        Text(option, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(selectedWeather) }) {
                Text("GUARDAR")
            }
        },
        dismissButton = {
            TextButton(onClick = onDiscard) {
                Text("DESCARTAR", color = MaterialTheme.colorScheme.error)
            }
        }
    )
}
