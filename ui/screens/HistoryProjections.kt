package com.example.circalux.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.circalux.ui.theme.SolarYellow
import com.example.circalux.ui.viewmodel.MainViewModel
import com.example.circalux.util.VitaminDCalculator
import java.util.concurrent.TimeUnit

@Composable
fun ProjectionsTab(viewModel: MainViewModel) {
    val solarSessions by viewModel.solarSessions.collectAsState()
    val profile by viewModel.profile.collectAsState(initial = null)
    
    val baseLevel = profile?.bloodDLevel ?: 0.0
    val lastAnalyticDate = profile?.lastAnalyticDate ?: 0L
    
    val currentTime = System.currentTimeMillis()
    val daysSinceLastAnalytic = if (lastAnalyticDate > 0) {
        TimeUnit.MILLISECONDS.toDays(currentTime - lastAnalyticDate).toInt()
    } else 0
    
    val decayedBase = VitaminDCalculator.estimateCurrentLevel(baseLevel, daysSinceLastAnalytic)
    
    // Sum all vitamin D from sessions since last analytic
    val totalIuFromSessions = solarSessions
        .filter { it.timestamp > lastAnalyticDate }
        .sumOf { it.vitaminDGenerated }
    
    val currentEstimatedLevel = decayedBase + VitaminDCalculator.iuToNgMl(totalIuFromSessions)

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text(
                "Proyección Vitamina D",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = SolarYellow
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141A26))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("NIVEL ESTIMADO ACTUAL", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                    Text(
                        "${String.format("%.1f", currentEstimatedLevel)} ng/mL",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = SolarYellow
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Basado en última analítica y sesiones registradas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Proyección a 30 días", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            val levelIn30Days = VitaminDCalculator.estimateCurrentLevel(currentEstimatedLevel, 30)
            
            Text(
                "Si no tomas más el sol, en 30 días tu nivel bajará a ~${String.format("%.1f", levelIn30Days)} ng/mL.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}
