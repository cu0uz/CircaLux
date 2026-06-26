package com.example.circalux.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.circalux.ui.theme.RedLightSession
import com.example.circalux.ui.theme.SolarYellow

@Composable
fun SolarSessionControls(
    isActive: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    exposure: Double,
    onExposureChange: (Double) -> Unit,
    vitaminDPerMin: Double = 0.0
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF141A26),
        border = androidx.compose.foundation.BorderStroke(1.dp, SolarYellow.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "SESIÓN DE SOL", 
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = SolarYellow,
                    letterSpacing = 1.sp
                )
                
                Surface(
                    color = SolarYellow.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "${String.format("%.0f", vitaminDPerMin)} IU/min",
                        style = MaterialTheme.typography.labelMedium,
                        color = SolarYellow,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                "¿QUÉ PARTE DE TU CUERPO RECIBE EL SOL?",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // CLEARER EXPOSURE SELECTOR - Focused on what is EXPOSED
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val percentages = listOf(
                    0.1 to "Solo Cara/Manos",
                    0.3 to "Brazos Desc.",
                    0.5 to "Torso Desc.",
                    0.7 to "En Bañador",
                    1.0 to "Piel Total"
                )
                percentages.forEach { (pct, label) ->
                    ExposureCard(
                        pct = pct,
                        label = label,
                        isSelected = exposure == pct,
                        onClick = { onExposureChange(pct) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = if (isActive) onStop else onStart,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActive) Color(0xFFE53935) else SolarYellow,
                    contentColor = if (isActive) Color.White else Color.Black
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(if (isActive) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    if (isActive) "FINALIZAR SESIÓN" else "INICIAR EXPOSICIÓN",
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun ExposureCard(
    pct: Double,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(75.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) SolarYellow else Color(0xFF232B39),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${(pct * 100).toInt()}%",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = if (isSelected) Color.Black else Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 8.sp,
                lineHeight = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = if (isSelected) Color.Black.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun RedLightSessionCard(
    isActive: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    powerWatts: Int,
    onPowerChange: (Int) -> Unit,
    distanceCm: Int,
    onDistanceChange: (Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF261414),
        border = androidx.compose.foundation.BorderStroke(1.dp, RedLightSession.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                "SESIÓN LUZ ROJA", 
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = RedLightSession,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(20.dp))

            if (!isActive) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("POTENCIA: ${powerWatts}W", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                        Slider(
                            value = powerWatts.toFloat(),
                            onValueChange = { onPowerChange(it.toInt()) },
                            valueRange = 10f..300f,
                            colors = SliderDefaults.colors(thumbColor = RedLightSession, activeTrackColor = RedLightSession)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("DISTANCIA: ${distanceCm}cm", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                        Slider(
                            value = distanceCm.toFloat(),
                            onValueChange = { onDistanceChange(it.toInt()) },
                            valueRange = 5f..100f,
                            colors = SliderDefaults.colors(thumbColor = RedLightSession, activeTrackColor = RedLightSession)
                        )
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Surface(color = RedLightSession.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                        Text("POTENCIA: ${powerWatts}W", modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.labelMedium, color = RedLightSession, fontWeight = FontWeight.Bold)
                    }
                    Surface(color = RedLightSession.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                        Text("DISTANCIA: ${distanceCm}cm", modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.labelMedium, color = RedLightSession, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = if (isActive) onStop else onStart,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActive) Color(0xFFE53935) else RedLightSession.copy(alpha = 0.8f),
                    contentColor = Color.White
                )
            ) {
                Icon(if (isActive) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    if (isActive) "PARAR LUZ ROJA" else "INICIAR LUZ ROJA",
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
