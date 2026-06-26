package com.example.circalux.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun ConsentDialog(
    onAccept: () -> Unit,
    onViewFullPolicy: () -> Unit
) {
    Dialog(
        onDismissRequest = { /* Force explicit choice */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Bienvenido a CircaLux",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Tu privacidad es nuestra prioridad absoluta. CircaLux funciona 100% de forma local.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Para cumplir con el RGPD, necesitamos tu consentimiento para:\n\n" +
                                "1. Procesar datos de salud (glucosa, cetonas, medidas) localmente.\n" +
                                "2. Usar tu ubicación GPS para cálculos solares en tiempo real.\n" +
                                "3. Almacenar tus sesiones de forma permanente en este dispositivo.\n\n" +
                                "No enviamos datos a servidores externos. Puedes retirar este consentimiento borrando tus datos en Ajustes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onAccept,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("ACEPTO Y CONTINUO", fontWeight = FontWeight.Bold)
                }
                
                TextButton(onClick = onViewFullPolicy) {
                    Text("Ver Política completa")
                }
            }
        }
    }
}
