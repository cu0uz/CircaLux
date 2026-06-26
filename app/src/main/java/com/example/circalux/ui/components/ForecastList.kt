package com.example.circalux.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.circalux.data.network.DailyData

@Composable
fun ForecastList(daily: DailyData?) {
    if (daily == null) return

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Próximos 7 días",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow {
            val count = daily.time.size
            items(count) { index ->
                ForecastItem(
                    day = daily.time[index],
                    uvi = daily.uvIndexMax[index],
                    temp = daily.temperatureMax[index]
                )
            }
        }
    }
}

@Composable
fun ForecastItem(day: String, uvi: Double, temp: Double) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .width(100.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(day.substringAfterLast("-"), style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "UVI ${String.format("%.0f", uvi)}",
                color = getUviColor(uvi),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall
            )
            Text("${String.format("%.1f", temp)}°C", style = MaterialTheme.typography.bodySmall)
        }
    }
}
