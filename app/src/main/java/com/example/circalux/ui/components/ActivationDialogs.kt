package com.example.circalux.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.circalux.ui.theme.SolarYellow

@Composable
fun ActivationModal(
    userId: String,
    onActivate: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { },
        containerColor = Color(0xFF0F1724),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = { Text("Activación Requerida", fontWeight = FontWeight.Black) },
        text = {
            Column {
                Text("Has alcanzado el límite de 10 sesiones gratuitas.")
                Spacer(modifier = Modifier.height(16.dp))
                Text("Tu USER ID es:", style = MaterialTheme.typography.labelSmall, color = SolarYellow)
                Text(userId, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Introduce el código de activación para desbloqueo permanente:")
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it; error = false },
                    label = { Text("Código (ID-XX)") },
                    isError = error,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error) {
                    Text("Código no válido", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onActivate(code)
                    error = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = SolarYellow, contentColor = Color.Black)
            ) {
                Text("ACTIVAR")
            }
        }
    )
}

@Composable
fun TrialStatusPopup(
    sessionsRemaining: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF141A26),
        title = { Text("Sesión Guardada") },
        text = {
            Text("Te quedan $sessionsRemaining sesiones gratuitas disponibles.")
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("ENTENDIDO", color = SolarYellow)
            }
        }
    )
}
