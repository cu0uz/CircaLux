package com.example.circalux.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.circalux.ui.components.CircaLuxAppIcon
import com.example.circalux.ui.components.CircaLuxLogo
import com.example.circalux.ui.theme.SolarYellow

@Composable
fun AboutScreen() {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        CircaLuxAppIcon(size = 120.dp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "CircaLux",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = SolarYellow
        )
        Text(
            "v1.0.0",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        AboutCard(
            title = "Evidencia Científica",
            icon = Icons.Default.Science,
            content = "CircaLux integra algoritmos basados en estudios de fotobiología y cronobiología. Los cálculos de síntesis de Vitamina D y exposición a Luz Roja (RLT) se derivan de modelos estándar de la industria (Fitzpatrick, MED, leyes de irradiancia).",
            iconColor = SolarYellow
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AboutCard(
            title = "Descargo de Responsabilidad",
            icon = Icons.Default.Warning,
            content = "Esta aplicación es una herramienta de monitorización y consulta informativa. NO constituye consejo médico profesional. CircaLux no sustituye el diagnóstico, tratamiento o asesoramiento de un profesional de la salud cualificado. El uso de esta aplicación y la exposición a la radiación solar son responsabilidad exclusiva del usuario.",
            iconColor = Color(0xFFE57373)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AboutCard(
            title = "Mejora Continua",
            icon = Icons.Default.Info,
            content = "Estamos en fase de desarrollo activo. Aunque buscamos la máxima precisión mediante la validación de sensores y APIs, el sistema puede contener errores técnicos o de cálculo que serán corregidos en futuras actualizaciones. Tu feedback es vital para nosotros.",
            iconColor = Color(0xFF81C784)
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Text(
            "© 2026 CircaLux Project\nCreado con IA por cu0uz",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.3f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun AboutCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: String, iconColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1724)),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                content,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                lineHeight = 20.sp
            )
        }
    }
}
