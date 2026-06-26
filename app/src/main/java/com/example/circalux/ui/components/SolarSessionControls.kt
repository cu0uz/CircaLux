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
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "SESIÓN DE SOL", 
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = SolarYellow,
                    letterSpacing = 1.sp
                )
                Text(
                    "${String.format("%.0f", vitaminDPerMin)} UI/min",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val percentages = listOf(
                    0.1 to "Cara/Manos",
                    0.3 to "Camiseta",
                    0.5 to "Pantalón",
                    0.7 to "Bañador",
                    1.0 to "Total"
                )
                percentages.forEach { (pct, label) ->
                    ExposureChip(
                        pct = pct,
                        desc = label,
                        isSelected = exposure == pct,
                        onClick = { onExposureChange(pct) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = if (isActive) onStop else onStart,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActive) Color(0xFFE53935) else SolarYellow,
                    contentColor = if (isActive) Color.White else Color.Black
                )
            ) {
                Text(
                    if (isActive) "FINALIZAR" else "INICIAR",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

@Composable
fun ExposureChip(
    pct: Double,
    desc: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(
                    color = if (isSelected) SolarYellow else Color(0xFF232B39),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${(pct * 100).toInt()}%",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.6f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = desc,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) SolarYellow else Color.White.copy(alpha = 0.4f),
            maxLines = 1
        )
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
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                "SESIÓN LUZ ROJA", 
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = RedLightSession,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            if (!isActive) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Potencia (W)", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Slider(
                                value = powerWatts.toFloat(),
                                onValueChange = { onPowerChange(it.toInt()) },
                                valueRange = 10f..300f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(thumbColor = RedLightSession, activeTrackColor = RedLightSession)
                            )
                            Text("$powerWatts", modifier = Modifier.width(35.dp), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Distancia (cm)", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Slider(
                                value = distanceCm.toFloat(),
                                onValueChange = { onDistanceChange(it.toInt()) },
                                valueRange = 5f..100f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(thumbColor = RedLightSession, activeTrackColor = RedLightSession)
                            )
                            Text("$distanceCm", modifier = Modifier.width(35.dp), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    Text("P: ${powerWatts}W", color = Color.White.copy(alpha = 0.7f))
                    Text("D: ${distanceCm}cm", color = Color.White.copy(alpha = 0.7f))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedButton(
                onClick = if (isActive) onStop else onStart,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, RedLightSession.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = RedLightSession
                )
            ) {
                Text(
                    if (isActive) "PARAR RLT" else "INICIAR RLT",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}
