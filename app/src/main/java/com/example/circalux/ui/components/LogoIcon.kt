package com.example.circalux.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.circalux.ui.theme.*

/**
 * A static, optimized version of the CircaLux logo designed for use as an icon.
 */
@Composable
fun CircaLuxIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color? = null
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = size.toPx() * 0.12f
            val radius = (size.toPx() - strokeWidth * 2) / 2
            val center = Offset(size.toPx() / 2, size.toPx() / 2)

            val sweepGradient = if (tint != null) {
                Brush.linearGradient(listOf(tint, tint))
            } else {
                Brush.sweepGradient(
                    0.0f to DeepBlue,
                    0.25f to SolarOrange,
                    0.5f to SolarYellow,
                    0.75f to SolarOrange,
                    1.0f to DeepBlue,
                    center = center
                )
            }

            val gapAngle = 60f
            val startAngle = 90f + (gapAngle / 2f)
            val sweepAngle = 360f - gapAngle

            drawArc(
                brush = sweepGradient,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            val smileRadius = radius * 0.7f
            val smileSweep = 45f
            val smileStart = 90f - (smileSweep / 2f)
            val smileCenterY = center.y + (radius * 0.2f)

            drawArc(
                color = if (tint != null) tint.copy(alpha = 0.6f) else SolarYellow.copy(alpha = 0.8f),
                startAngle = smileStart,
                sweepAngle = smileSweep,
                useCenter = false,
                topLeft = Offset(center.x - smileRadius, smileCenterY - smileRadius),
                size = Size(smileRadius * 2, smileRadius * 2),
                style = Stroke(width = (strokeWidth * 0.5f).coerceAtLeast(1.dp.toPx()), cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * A styled component that looks like a modern app icon.
 */
@Composable
fun CircaLuxAppIcon(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    Surface(
        modifier = modifier.size(size),
        shape = RoundedCornerShape(size * 0.22f),
        color = DarkBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircaLuxIcon(size = size * 0.65f)
        }
    }
}
