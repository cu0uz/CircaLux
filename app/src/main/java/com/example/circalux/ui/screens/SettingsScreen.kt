package com.example.circalux.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.circalux.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let { 
            viewModel.exportBackup(it)
            scope.launch { snackbarHostState.showSnackbar("Copia de seguridad exportada") }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { 
            viewModel.importBackup(it)
            scope.launch { snackbarHostState.showSnackbar("Datos importados con éxito") }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                "Ajustes", 
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1724))
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    SettingItem(
                        title = "Exportar Copia (CSV)",
                        subtitle = "Guarda tu historial en un archivo",
                        icon = Icons.Default.FileUpload,
                        onClick = { exportLauncher.launch("circalux_backup.csv") }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.1f))

                    SettingItem(
                        title = "Importar Copia (CSV)",
                        subtitle = "Restaura tus datos desde un archivo",
                        icon = Icons.Default.FileDownload,
                        onClick = { importLauncher.launch(arrayOf("*/*")) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.1f))

                    SettingItem(
                        title = "Notificaciones",
                        subtitle = "Alertas de amanecer y UVI",
                        icon = Icons.Default.Notifications,
                        onClick = {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(intent)
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.1f))

                    SettingItem(
                        title = "Optimización de Batería",
                        subtitle = "Asegurar avisos en segundo plano",
                        icon = Icons.Default.BatteryAlert,
                        onClick = {
                            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            context.startActivity(intent)
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.1f))

                    SettingItem(
                        title = "Privacidad y RGPD",
                        subtitle = "Consulta nuestra política de datos",
                        icon = Icons.Default.Lock,
                        onClick = { /* Could open PrivacyPolicyScreen */ }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.1f))

                    var showDeleteConfirm by remember { mutableStateOf(false) }

                    SettingItem(
                        title = "Borrar Todos los Datos",
                        subtitle = "Eliminar historial y perfil permanentemente",
                        icon = Icons.Default.DeleteForever,
                        onClick = { showDeleteConfirm = true }
                    )

                    if (showDeleteConfirm) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirm = false },
                            title = { Text("¿Estás seguro?") },
                            text = { Text("Esta acción es irreversible. Se borrarán todas tus sesiones, medidas corporales y configuración de perfil para cumplir con tu derecho de supresión (ARSULIPO).") },
                            confirmButton = {
                                Button(
                                    onClick = { 
                                        viewModel.deleteAllData()
                                        showDeleteConfirm = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("BORRAR TODO")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteConfirm = false }) {
                                    Text("CANCELAR")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xFF1A2230),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
            }
        }
    }
}
