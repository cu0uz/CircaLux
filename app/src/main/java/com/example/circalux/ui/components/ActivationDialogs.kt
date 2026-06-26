package com.example.circalux.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.circalux.ui.theme.SolarYellow

@Composable
fun ActivationModal(
    userId: String,
    onActivate: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { },
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        containerColor = Color(0xFF0F1724),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = { 
            Text(
                "DESBLOQUEO PERMANENTE", 
                fontWeight = FontWeight.Black, 
                color = SolarYellow,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            ) 
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Has alcanzado el límite de 10 sesiones de la versión gratuita.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text("FACILITA ESTE ID PARA TU CÓDIGO:", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                
                Surface(
                    onClick = {
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("CircaLux ID", userId)
                        clipboard.setPrimaryClip(clip)
                    },
                    color = Color.White.copy(alpha = 0.05f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            userId, 
                            style = MaterialTheme.typography.headlineMedium, 
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(Icons.Default.ContentCopy, null, tint = SolarYellow, modifier = Modifier.size(20.dp))
                    }
                }
                
                Text("(Toca para copiar)", style = MaterialTheme.typography.labelSmall, color = SolarYellow.copy(alpha = 0.6f))

                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase().trim(); error = false },
                    label = { Text("Introduce tu código") },
                    placeholder = { Text("ID-XX") },
                    isError = error,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SolarYellow,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    )
                )
                if (error) {
                    Text(
                        "Código inválido. Revisa el formato ID-XX", 
                        color = MaterialTheme.colorScheme.error, 
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onActivate(code)
                    error = true
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SolarYellow, contentColor = Color.Black),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Text("ACTIVAR VERSIÓN COMPLETA", fontWeight = FontWeight.Bold)
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
