package com.example.circalux.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.circalux.data.model.UserProfile
import com.example.circalux.ui.theme.SolarYellow
import com.example.circalux.ui.viewmodel.MainViewModel
import java.util.Calendar

@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    val profile by viewModel.profile.collectAsState(initial = UserProfile())
    val scrollState = rememberScrollState()

    var nickname by remember(profile) { mutableStateOf(profile?.nickname ?: "") }
    var age by remember(profile) { mutableStateOf(profile?.age?.toString() ?: "0") }
    var gender by remember(profile) { mutableStateOf(profile?.gender ?: "Hombre") }
    var height by remember(profile) { mutableStateOf(profile?.height?.toString() ?: "0.0") }
    var weight by remember(profile) { mutableStateOf(profile?.weight?.toString() ?: "0.0") }
    var skinType by remember(profile) { mutableStateOf(profile?.skinType ?: 2) }
    
    var bloodDLevel by remember(profile) { mutableStateOf(profile?.bloodDLevel?.toString() ?: "0.0") }
    var lastAnalyticDate by remember(profile) { mutableStateOf(profile?.lastAnalyticDate ?: System.currentTimeMillis()) }
    
    var takesSupplements by remember(profile) { mutableStateOf(profile?.takesSupplements ?: false) }
    var supplementAmount by remember(profile) { mutableStateOf(profile?.supplementAmount?.toString() ?: "4000") }
    var supplementFrequency by remember(profile) { mutableStateOf(profile?.supplementFrequency ?: "Diario") }
    var supplementStartDate by remember(profile) { mutableStateOf(profile?.supplementStartDate ?: System.currentTimeMillis()) }
    var sunriseAlarmsEnabled by remember(profile) { mutableStateOf(profile?.sunriseSunsetAlarmsEnabled ?: false) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            "Mi Perfil",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = SolarYellow
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("Apodo o Nombre") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.White.copy(alpha = 0.2f))
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Edad") },
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.White.copy(alpha = 0.2f))
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text("Género", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = gender == "Hombre", onClick = { gender = "Hombre" })
                    Text("Hombre", style = MaterialTheme.typography.bodySmall)
                    RadioButton(selected = gender == "Mujer", onClick = { gender = "Mujer" })
                    Text("Mujer", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("Altura (cm)") },
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.White.copy(alpha = 0.2f))
            )
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Peso (kg)") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.White.copy(alpha = 0.2f))
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Fototipo de Piel (Fitzpatrick)", style = MaterialTheme.typography.labelLarge, color = SolarYellow)
        Slider(
            value = skinType.toFloat(),
            onValueChange = { skinType = it.toInt() },
            valueRange = 1f..6f,
            steps = 4,
            colors = SliderDefaults.colors(thumbColor = SolarYellow, activeTrackColor = SolarYellow)
        )
        Text("Tipo $skinType: " + when(skinType) {
            1 -> "Muy Clara (Siempre se quema)"
            2 -> "Clara (Suele quemarse)"
            3 -> "Media (A veces se quema)"
            4 -> "Morena (Raramente se quema)"
            5 -> "Oscura (Casi nunca se quema)"
            else -> "Muy Oscura (Nunca se quema)"
        }, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))

        Spacer(modifier = Modifier.height(32.dp))
        
        Text("Analítica de Vitamina D", style = MaterialTheme.typography.labelLarge, color = SolarYellow)
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = bloodDLevel,
                onValueChange = { bloodDLevel = it },
                label = { Text("Nivel en sangre (ng/mL)") },
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.White.copy(alpha = 0.2f))
            )
            
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = java.text.SimpleDateFormat("dd/MM/yy", java.util.Locale.getDefault()).format(java.util.Date(lastAnalyticDate)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha Analítica") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = SolarYellow) },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedBorderColor = SolarYellow,
                        disabledTextColor = Color.White,
                        disabledLabelColor = Color.White.copy(alpha = 0.6f),
                        disabledPlaceholderColor = Color.White.copy(alpha = 0.6f),
                        disabledBorderColor = Color.White.copy(alpha = 0.2f),
                        disabledTrailingIconColor = SolarYellow
                    ),
                    enabled = false
                )
                // Overlay for click detection since OutlinedTextField is disabled
                Box(modifier = Modifier.matchParentSize().clickable {
                    val calendar = Calendar.getInstance().apply { timeInMillis = lastAnalyticDate }
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val newCalendar = Calendar.getInstance()
                            newCalendar.set(year, month, dayOfMonth)
                            lastAnalyticDate = newCalendar.timeInMillis
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                })
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = takesSupplements, onCheckedChange = { takesSupplements = it })
            Text("Tomo suplementos de Vitamina D", fontWeight = FontWeight.Bold)
        }
        
        if (takesSupplements) {
            OutlinedTextField(
                value = supplementAmount,
                onValueChange = { supplementAmount = it },
                label = { Text("Cantidad (UI)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.White.copy(alpha = 0.2f))
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Frecuencia", style = MaterialTheme.typography.labelSmall)
            val frequencies = listOf("Diario", "Semanal", "Mensual")
            Row {
                frequencies.forEach { freq ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                        RadioButton(selected = supplementFrequency == freq, onClick = { supplementFrequency = freq })
                        Text(freq, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date(supplementStartDate)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha de inicio del suplemento") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = SolarYellow) },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedBorderColor = SolarYellow,
                        disabledTextColor = Color.White,
                        disabledLabelColor = Color.White.copy(alpha = 0.6f),
                        disabledPlaceholderColor = Color.White.copy(alpha = 0.6f),
                        disabledBorderColor = Color.White.copy(alpha = 0.2f),
                        disabledTrailingIconColor = SolarYellow
                    ),
                    enabled = false
                )
                Box(modifier = Modifier.matchParentSize().clickable {
                    val calendar = Calendar.getInstance().apply { timeInMillis = supplementStartDate }
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val newCalendar = Calendar.getInstance()
                            newCalendar.set(year, month, dayOfMonth)
                            supplementStartDate = newCalendar.timeInMillis
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                })
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = sunriseAlarmsEnabled, onCheckedChange = { sunriseAlarmsEnabled = it })
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Alarmas de Sol", fontWeight = FontWeight.Bold)
                Text("Avisar 10 min antes del amanecer/atardecer", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = {
                val newProfile = (profile ?: UserProfile()).copy(
                    nickname = nickname,
                    age = age.toIntOrNull() ?: 0,
                    gender = gender,
                    height = height.toDoubleOrNull() ?: 0.0,
                    weight = weight.toDoubleOrNull() ?: 0.0,
                    skinType = skinType,
                    bloodDLevel = bloodDLevel.toDoubleOrNull() ?: 0.0,
                    lastAnalyticDate = lastAnalyticDate,
                    takesSupplements = takesSupplements,
                    supplementAmount = supplementAmount.toIntOrNull() ?: 0,
                    supplementFrequency = supplementFrequency,
                    supplementStartDate = supplementStartDate,
                    sunriseSunsetAlarmsEnabled = sunriseAlarmsEnabled
                )
                viewModel.saveProfile(newProfile)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SolarYellow, contentColor = Color.Black)
        ) {
            Text("GUARDAR PERFIL", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = if (viewModel.isActivated) Color(0xFF1B2614) else Color(0xFF261414)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    if (viewModel.isActivated) "APP ACTIVADA" else "ESTADO: PRUEBA (TRIAL)",
                    fontWeight = FontWeight.Bold,
                    color = if (viewModel.isActivated) Color(0xFF81C784) else Color(0xFFE57373)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("USER ID: " + viewModel.userId, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                
                if (!viewModel.isActivated) {
                    Spacer(modifier = Modifier.height(16.dp))
                    var code by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Código de Activación") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { viewModel.activateApp(code) },
                        modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SolarYellow, contentColor = Color.Black)
                    ) {
                        Text("ACTIVAR AHORA")
                    }
                }
            }
        }
    }
}
