package com.example.circalux.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.circalux.ui.theme.SolarYellow
import kotlin.math.abs

@Composable
fun SolarCompass(
    sunAzimuth: Float,
    deviceHeading: Float,
    modifier: Modifier = Modifier
) {
    // Calculate relative deviation (-180 to 180)
    // 0 means sun is straight ahead
    var relativeAngle = sunAzimuth - deviceHeading
    while (relativeAngle <= -180f) relativeAngle += 360f
    while (relativeAngle > 180f) relativeAngle -= 360f

    val animatedAngle by animateFloatAsState(targetValue = relativeAngle, label = "arrowRotation")
    val isAligned = abs(relativeAngle) < 15f
    val indicatorColor by animateColorAsState(
        targetValue = if (isAligned) SolarYellow else Color.White.copy(alpha = 0.6f),
        label = "colorTransition"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(100.dp)) {
                val center = center
                val radius = size.minDimension / 2

                // Subtly draw a background ring
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = radius,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                )

                // Rotate arrow to point towards the sun
                rotate(animatedAngle) {
                    val arrowPath = Path().apply {
                        moveTo(center.x, center.y - radius * 0.95f) // Tip
                        lineTo(center.x - radius * 0.4f, center.y + radius * 0.5f) // Bottom Left
                        lineTo(center.x, center.y + radius * 0.2f) // Notch
                        lineTo(center.x + radius * 0.4f, center.y + radius * 0.5f) // Bottom Right
                        close()
                    }
                    
                    drawPath(
                        path = arrowPath,
                        color = indicatorColor
                    )

                    // Small sun circle at the tip
                    drawCircle(
                        color = if (isAligned) Color.White else SolarYellow.copy(alpha = 0.8f),
                        radius = 4.dp.toPx(),
                        center = Offset(center.x, center.y - radius * 0.95f)
                    )
                }
            }

            if (isAligned) {
                Icon(
                    Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = SolarYellow.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val guidanceText = when {
            isAligned -> "¡ORIENTACIÓN CORRECTA!"
            relativeAngle > 0 -> "GIRA A LA DERECHA ➔"
            else -> "⇽ GIRA A LA IZQUIERDA"
        }

        Text(
            text = guidanceText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = indicatorColor,
            letterSpacing = 1.sp
        )
    }
}
