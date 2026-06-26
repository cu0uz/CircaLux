package com.example.circalux.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.circalux.ui.theme.SolarYellow

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scrollState = rememberScrollState()
    
    var hasLocation by remember { mutableStateOf(false) }
    var hasNotifications by remember { mutableStateOf(false) }
    var isBatteryOptimizedIgnored by remember { mutableStateOf(false) }

    // Logic to check all permissions
    fun checkAllPermissions() {
        hasLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        hasNotifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        isBatteryOptimizedIgnored = powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    // Check permissions when returning to the app (e.g. from settings)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                checkAllPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        checkAllPermissions()
    }

    val batteryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        checkAllPermissions()
    }

    val allDone = hasLocation && hasNotifications && isBatteryOptimizedIgnored

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050B14))
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.WbSunny,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = SolarYellow
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Bienvenido a CircaLux",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            color = Color.White
        )
        Text(
            "Para funcionar correctamente, es obligatorio configurar estos tres ajustes antes de comenzar.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))

        OnboardingStep(
            title = "1. Ubicación",
            description = "Indispensable para calcular la posición del sol y el UVI real.",
            icon = Icons.Default.LocationOn,
            isDone = hasLocation,
            onClick = {
                permissionLauncher.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ))
            }
        )

        OnboardingStep(
            title = "2. Notificaciones",
            description = "Necesarias para las alarmas de Vitamina D y avisos solares.",
            icon = Icons.Default.Notifications,
            isDone = hasNotifications,
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
                }
            }
        )

        OnboardingStep(
            title = "3. Optimización de Batería",
            description = "Desactívala para asegurar que las alarmas suenen en segundo plano.",
            icon = Icons.Default.BatteryChargingFull,
            isDone = isBatteryOptimizedIgnored,
            onClick = {
                try {
                    // Try to open the direct "Ignore Battery Optimizations" request dialog
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:" + context.packageName)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Fallback to the general list if the direct request is not allowed/fails
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    context.startActivity(intent)
                }
            }
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onFinished,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SolarYellow, 
                contentColor = Color.Black,
                disabledContainerColor = Color.White.copy(alpha = 0.1f),
                disabledContentColor = Color.White.copy(alpha = 0.3f)
            ),
            enabled = allDone
        ) {
            Text("COMENZAR", fontWeight = FontWeight.Bold)
        }
        
        if (!allDone) {
            Text(
                "Configura los 3 pasos anteriores para continuar",
                style = MaterialTheme.typography.labelSmall,
                color = SolarYellow.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Composable
fun OnboardingStep(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isDone: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = if (isDone) ({}) else onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        color = if (isDone) SolarYellow.copy(alpha = 0.15f) else Color(0xFF141A26),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp, 
            color = if (isDone) SolarYellow else Color.White.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon, 
                contentDescription = null, 
                tint = if (isDone) SolarYellow else Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title, 
                    fontWeight = FontWeight.Bold, 
                    color = if (isDone) SolarYellow else Color.White
                )
                Text(
                    description, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = if (isDone) Color.White.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.5f)
                )
            }
            if (isDone) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SolarYellow)
            } else {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.3f))
            }
        }
    }
}
