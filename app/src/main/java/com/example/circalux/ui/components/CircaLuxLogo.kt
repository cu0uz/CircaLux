package com.example.circalux.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.circalux.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun CircaLuxLogo(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    showCurrentTime: Boolean = true
) {
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Calendar.getInstance()
            delay(1000)
        }
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = size.toPx() * 0.08f
            val radius = (size.toPx() - strokeWidth * 2) / 2
            val center = Offset(size.toPx() / 2, size.toPx() / 2)

            // 1. The Anillo (Ring) with Gradient
            // 24 segments logic is handled by the sweep gradient representation
            // Colors: DeepBlue (00:00) -> NightViolet (Dawn) -> SolarOrange (Sunrise) -> SolarYellow (Noon)
            val sweepGradient = Brush.sweepGradient(
                0.0f to DeepBlue,      // 00:00
                0.15f to NightViolet,  // Madrugada
                0.25f to SolarOrange,  // Amanecer
                0.5f to SolarYellow,   // Mediodía
                0.75f to SolarOrange,  // Atardecer
                0.85f to NightViolet,  // Anochecer
                1.0f to DeepBlue,      // 24:00
                center = center
            )

            val gapAngle = 54f // ~15% of 360 is 54
            val startAngle = 90f + (gapAngle / 2f)
            val sweepAngle = 360f - gapAngle

            drawArc(
                brush = sweepGradient,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 2. Solar trajectory curve (The "Smile")
            val smileRadius = radius * 0.9f
            val smileSweep = 40f
            val smileStart = 90f - (smileSweep / 2f)
            
            // Adjust vertical position to float below the gap
            val smileCenterY = center.y + (radius * 0.15f)
            
            drawArc(
                color = SolarYellow.copy(alpha = 0.6f),
                startAngle = smileStart,
                sweepAngle = sweepSweep, // Fix: this was sweepAngle in previous version but should be smileSweep
                useCenter = false,
                topLeft = Offset(center.x - smileRadius, smileCenterY - smileRadius),
                size = Size(smileRadius * 2, smileRadius * 2),
                style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round)
            )

            // 3. Current Moment (Glowing Dot)
            if (showCurrentTime) {
                val hour = currentTime.get(Calendar.HOUR_OF_DAY)
                val minute = currentTime.get(Calendar.MINUTE)
                val totalMinutes = hour * 60 + minute
                
                // Angle 0 starts at 3 o'clock in Android Canvas, so 12:00 (Noon) is at 270 degrees
                // 00:00 (Midnight) is at 90 degrees
                val progressAngle = (totalMinutes / 1440f) * 360f
                val indicatorAngle = (90f + progressAngle) % 360f
                
                val angleRad = Math.toRadians(indicatorAngle.toDouble())
                val indicatorX = center.x + radius * Math.cos(angleRad).toFloat()
                val indicatorY = center.y + radius * Math.sin(angleRad).toFloat()

                // Glow effect
                drawCircle(
                    color = Color.White.copy(alpha = 0.2f),
                    radius = strokeWidth * 0.8f,
                    center = Offset(indicatorX, indicatorY)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.4f),
                    radius = strokeWidth * 0.6f,
                    center = Offset(indicatorX, indicatorY)
                )
                // Small dot
                drawCircle(
                    color = Color.White,
                    radius = strokeWidth * 0.3f,
                    center = Offset(indicatorX, indicatorY)
                )
            }
        }
    }
}

// Helper to keep the old signature compatible or fix variable name
private val sweepSweep = 40f

@Preview(showBackground = true, backgroundColor = 0xFF050B14)
@Composable
fun CircaLuxLogoPreview() {
    CircaLuxLogo(size = 200.dp)
}
