package com.example.circalux.ui.components

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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.circalux.ui.theme.SolarYellow
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SolarCompass(
    sunAzimuth: Float,
    deviceHeading: Float,
    modifier: Modifier = Modifier
) {
    // Calculate the relative angle to the sun
    // 0 means looking directly at the sun
    var relativeAngle = sunAzimuth - deviceHeading
    if (relativeAngle < 0) relativeAngle += 360f
    if (relativeAngle > 180) relativeAngle -= 360f

    val animatedRelativeAngle by animateFloatAsState(targetValue = relativeAngle, label = "relativeAngle")
    val animatedHeading by animateFloatAsState(targetValue = deviceHeading, label = "heading")

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension / 2f

                // Draw Compass Ring
                drawCircle(
                    color = Color.White.copy(alpha = 0.1f),
                    radius = radius,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                )

                // Draw Cardinal Points (Static relative to North)
                // North is at -90 degrees in Canvas coordinate system if we want 0 to be North
                rotate(degrees = -animatedHeading, pivot = center) {
                    val cardinalPoints = listOf("N" to 0f, "E" to 90f, "S" to 180f, "W" to 270f)
                    cardinalPoints.forEach { (text, angle) ->
                        val angleRad = Math.toRadians(angle.toDouble() - 90.0)
                        val x = center.x + (radius - 20.dp.toPx()) * cos(angleRad).toFloat()
                        val y = center.y + (radius - 20.dp.toPx()) * sin(angleRad).toFloat()
                        
                        drawContext.canvas.nativeCanvas.drawText(
                            text,
                            x,
                            y + 5.dp.toPx(),
                            android.graphics.Paint().apply {
                                color = if (text == "N") android.graphics.Color.RED else android.graphics.Color.WHITE
                                textAlign = android.graphics.Paint.Align.CENTER
                                textSize = 12.sp.toPx()
                                isFakeBoldText = true
                            }
                        )
                    }
                }

                // Draw Sun Indicator
                rotate(degrees = sunAzimuth - animatedHeading, pivot = center) {
                    drawLine(
                        color = SolarYellow,
                        start = center,
                        end = Offset(center.x, center.y - radius + 10.dp.toPx()),
                        strokeWidth = 4.dp.toPx()
                    )
                    drawCircle(
                        color = SolarYellow,
                        radius = 8.dp.toPx(),
                        center = Offset(center.x, center.y - radius + 5.dp.toPx())
                    )
                }

                // Draw "Forward" Arrow (Fixed at top)
                drawLine(
                    color = Color.White,
                    start = center,
                    end = Offset(center.x, center.y - radius + 30.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // Central Sun Icon if aligned
            if (abs(relativeAngle) < 15f) {
                Icon(
                    Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = SolarYellow,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val guidanceText = when {
            abs(relativeAngle) < 10f -> "¡ESTÁS DE CARA AL SOL!"
            relativeAngle > 0 -> "GIRA A LA DERECHA ➔"
            else -> "⇽ GIRA A LA IZQUIERDA"
        }

        Text(
            text = guidanceText,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Black,
            color = if (abs(relativeAngle) < 10f) SolarYellow else Color.White.copy(alpha = 0.7f)
        )
        
        Text(
            text = "Desvío: ${String.format("%.0f", relativeAngle)}°",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.4f)
        )
    }
}
