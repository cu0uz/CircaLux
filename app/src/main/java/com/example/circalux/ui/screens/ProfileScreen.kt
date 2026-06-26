package com.example.circalux.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.circalux.data.model.UserProfile
import com.example.circalux.ui.theme.SolarYellow
import com.example.circalux.ui.viewmodel.MainViewModel
import com.example.circalux.util.NumberUtils
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    val profile by viewModel.profile.collectAsState(initial = UserProfile())
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // UI States
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

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0) // Inherit from parent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.White.copy(alpha = 0.2f))
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Peso (kg)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        age = NumberUtils.parseInt(age),
                        gender = gender,
                        height = NumberUtils.parseDouble(height),
                        weight = NumberUtils.parseDouble(weight),
                        skinType = skinType,
                        bloodDLevel = NumberUtils.parseDouble(bloodDLevel),
                        lastAnalyticDate = lastAnalyticDate,
                        takesSupplements = takesSupplements,
                        supplementAmount = NumberUtils.parseInt(supplementAmount),
                        supplementFrequency = supplementFrequency,
                        supplementStartDate = supplementStartDate,
                        sunriseSunsetAlarmsEnabled = sunriseAlarmsEnabled
                    )
                    viewModel.saveProfile(newProfile)
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "¡Perfil guardado con éxito!",
                            duration = SnackbarDuration.Short
                        )
                    }
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
                    
                    // User ID Section with Copy Button
                    Surface(
                        onClick = {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("CircaLux ID", viewModel.userId)
                            clipboard.setPrimaryClip(clip)
                            scope.launch { snackbarHostState.showSnackbar("ID copiado al portapapeles") }
                        },
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("USER ID", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                                Text(viewModel.userId, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copiar ID",
                                tint = SolarYellow,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    if (!viewModel.isActivated) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        var showKofiInstructions by remember { mutableStateOf(false) }
                        
                        Text(
                            "ACTIVA LA VERSIÓN COMPLETA",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = SolarYellow
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Elimina el límite de sesiones y desbloquea todas las funciones por solo 1€.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        
                        Button(
                            onClick = { showKofiInstructions = true },
                            modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF29ABE0), contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("COMPRAR ACTIVACIÓN (1€)")
                        }

                        if (showKofiInstructions) {
                            AlertDialog(
                                onDismissRequest = { showKofiInstructions = false },
                                containerColor = Color(0xFF0F1724),
                                title = { Text("🚀 Pasos para la Activación", fontWeight = FontWeight.Bold, color = SolarYellow) },
                                text = {
                                    Column {
                                        Text("Para activar tu cuenta manualmente, sigue estos pasos:")
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        Surface(
                                            color = Color.White.copy(alpha = 0.05f),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text("PASO 1:", style = MaterialTheme.typography.labelSmall, color = SolarYellow)
                                                Text("Copia tu USER ID:", style = MaterialTheme.typography.bodySmall)
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(viewModel.userId, fontWeight = FontWeight.Bold, color = Color.White)
                                                    IconButton(onClick = {
                                                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                        val clip = android.content.ClipData.newPlainText("CircaLux ID", viewModel.userId)
                                                        clipboard.setPrimaryClip(clip)
                                                        scope.launch { snackbarHostState.showSnackbar("ID copiado") }
                                                    }) {
                                                        Icon(Icons.Default.ContentCopy, null, tint = SolarYellow, modifier = Modifier.size(20.dp))
                                                    }
                                                }
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        Text("PASO 2:", style = MaterialTheme.typography.labelSmall, color = SolarYellow)
                                        Text("Pulsa en 'Ir a Ko-fi' y realiza una donación de 1€.", style = MaterialTheme.typography.bodySmall)
                                        
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        Text("⚠️ MUY IMPORTANTE:", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE57373))
                                        Text(
                                            "Introduce tu USER ID en el campo 'Mensaje' o 'Comentario' de Ko-fi. Sin esto no podremos enviarte tu código.",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE57373)
                                        )
                                        
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text("Recibirás tu código de activación por email en un plazo de 24h.", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            showKofiInstructions = false
                                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://ko-fi.com/cu0uz"))
                                            context.startActivity(intent)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SolarYellow, contentColor = Color.Black)
                                    ) {
                                        Text("IR A KO-FI (1€)")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showKofiInstructions = false }) {
                                        Text("CANCELAR", color = Color.White.copy(alpha = 0.6f))
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(16.dp))

                        var code by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it },
                            label = { Text("Código de Activación") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = { 
                                if (viewModel.activateApp(code)) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("¡Aplicación activada con éxito!")
                                    }
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Código de activación inválido.")
                                    }
                                }
                            },
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
}
