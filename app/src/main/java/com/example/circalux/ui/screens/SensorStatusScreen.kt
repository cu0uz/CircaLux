package com.example.circalux.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.circalux.ui.theme.SolarYellow
import com.example.circalux.ui.viewmodel.MainViewModel

@Composable
fun SensorStatusScreen(viewModel: MainViewModel) {
    val luxValue by viewModel.luxValue.collectAsState()
    val heading by viewModel.deviceHeading.collectAsState()
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            "Estado de Sensores",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = SolarYellow
        )
        Spacer(modifier = Modifier.height(24.dp))

        SensorItem(label = "Sensor de Luz (Lux)", value = String.format("%.1f lx", luxValue))
        SensorItem(label = "Orientación (Magnetómetro)", value = String.format("%.1f°", heading))
        SensorItem(label = "Latitud", value = String.format("%.5f", viewModel.latitude))
        SensorItem(label = "Longitud", value = String.format("%.5f", viewModel.longitude))
        
        val weather = viewModel.weatherData
        SensorItem(label = "UVI Actual (Validado)", value = String.format("%.1f", viewModel.currentUvi))
        SensorItem(label = "UVI Máximo Hoy", value = String.format("%.1f", weather?.daily?.uvIndexMax?.firstOrNull() ?: 0.0))
        SensorItem(label = "Temperatura Actual", value = "${weather?.current?.temperature ?: "--"} °C")
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { viewModel.testNotification() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = SolarYellow, contentColor = Color.Black)
        ) {
            Text("PROBAR ALARMA SONORA", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141A26)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(16.dp)) {
                Icon(Icons.Default.Info, contentDescription = null, tint = SolarYellow)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Esta pantalla muestra los datos en bruto recibidos por el sistema para verificar su correcto funcionamiento.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun SensorItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.White.copy(alpha = 0.6f))
        Text(value, fontWeight = FontWeight.Bold, color = Color.White)
    }
    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
}
