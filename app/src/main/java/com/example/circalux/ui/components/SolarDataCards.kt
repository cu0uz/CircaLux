package com.example.circalux.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.circalux.ui.theme.*

@Composable
fun UviCard(uvi: Double, skinType: Int, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    val medTime = getMedTime(skinType, uvi)
    val description = getUviDescription(uvi)
    val color = getUviColor(uvi)

    Surface(
        onClick = onClick,
        modifier = modifier.padding(8.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF141A26),
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = String.format("%.1f", uvi),
                    style = MaterialTheme.typography.displaySmall,
                    color = color,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        "ÍNDICE UV", 
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        description,
                        style = MaterialTheme.typography.labelLarge,
                        color = color,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Límite (MED): $medTime min",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun LuxCard(lux: Float, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.padding(8.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF141A26)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = String.format("%.0f", lux),
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                fontWeight = FontWeight.Light
            )
            Text(
                "LUMINANCIA (LUX)", 
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun SolarInfoCard(
    label: String,
    value: String,
    unit: String = "",
    modifier: Modifier = Modifier,
    content: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = modifier.padding(4.dp).height(80.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1A2230)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                label.uppercase(), 
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (content != null) {
                content()
            } else {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        value, 
                        style = MaterialTheme.typography.titleLarge, 
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (unit.isNotEmpty()) {
                        Text(
                            unit, 
                            style = MaterialTheme.typography.labelSmall, 
                            modifier = Modifier.padding(start = 2.dp, bottom = 2.dp),
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

fun getUviColor(uvi: Double): Color {
    return when {
        uvi < 3 -> Color(0xFF81C784)
        uvi < 6 -> Color(0xFFFFF176)
        uvi < 8 -> Color(0xFFFFB74D)
        uvi < 11 -> Color(0xFFE57373)
        else -> Color(0xFFBA68C8)
    }
}

fun getUviDescription(uvi: Double): String {
    return when {
        uvi < 3 -> "BAJO"
        uvi < 6 -> "MODERADO"
        uvi < 8 -> "ALTO"
        uvi < 11 -> "MUY ALTO"
        else -> "EXTREMO"
    }
}
