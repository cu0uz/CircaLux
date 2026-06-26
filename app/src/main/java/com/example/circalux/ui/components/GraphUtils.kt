package com.example.circalux.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.circalux.ui.theme.SolarYellow

@Composable
fun SparklineGraph(
    data: List<Double>,
    modifier: Modifier = Modifier,
    color: Color = SolarYellow
) {
    if (data.size < 2) {
        Box(modifier = modifier, contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("No hay suficientes datos", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.3f))
        }
        return
    }

    val max = data.maxOrNull() ?: 1.0
    val min = data.minOrNull() ?: 0.0
    val range = if (max - min == 0.0) 1.0 else max - min

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val spacing = width / (data.size - 1)

        val path = Path().apply {
            data.forEachIndexed { index, value ->
                val x = index * spacing
                val y = height - ((value - min) / range * height).toFloat()
                if (index == 0) moveTo(x, y) else lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
