package com.example.circalux.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.circalux.ui.theme.SolarYellow

@Composable
fun UVILegendDialog(
    skinType: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F1724),
        titleContentColor = SolarYellow,
        textContentColor = Color.White,
        title = {
            Text(
                "Escala de Índice UV",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Tiempos MED para Fototipo $skinType",
                    style = MaterialTheme.typography.labelLarge,
                    color = SolarYellow,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Fototipo " + when(skinType) {
                        1 -> "I: Muy sensible (Céltico)"
                        2 -> "II: Sensible (Caucásico)"
                        3 -> "III: Normal (Europeo)"
                        4 -> "IV: Tolerante (Mediterráneo)"
                        5 -> "V: Muy tolerante (Oscuro)"
                        else -> "VI: Resistente (Negro)"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                UviLegendItem("Bajo", "1-2", getLegendTime(skinType, "Low"), Color(0xFF81C784), 
                    "Riesgo mínimo. Ideal para paseos largos.")
                UviLegendItem("Moderado", "3-5", getLegendTime(skinType, "Moderate"), Color(0xFFFFF176),
                    "Ventana de Vitamina D abierta. Busca sombra al mediodía.")
                UviLegendItem("Alto", "6-7", getLegendTime(skinType, "High"), Color(0xFFFFB74D),
                    "Riesgo de quemadura rápido. Usa protección si excedes el MED.")
                UviLegendItem("Muy Alto", "8-10", getLegendTime(skinType, "Very High"), Color(0xFFE57373),
                    "Radiación agresiva. Minimiza exposición directa.")
                UviLegendItem("Extremo", "11+", getLegendTime(skinType, "Extreme"), Color(0xFFBA68C8),
                    "Peligro extremo. Evita el sol central totalmente.")
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "MED (Dosis Eritematosa Mínima): Es el tiempo máximo estimado que puedes estar al sol antes de que tu piel comience a enrojecerse (quemarse).",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Nota: Los tiempos son estimaciones basadas en la escala Fitzpatrick y pueden variar según la altitud y humedad.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SolarYellow.copy(alpha = 0.5f),
                    lineHeight = 14.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("ENTENDIDO", color = SolarYellow, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun UviLegendItem(label: String, range: String, medTime: Int, color: Color, advice: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text("UVI $range", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
            }
            Text(
                if (medTime > 300) ">5h" else "$medTime min",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Black,
                color = color
            )
        }
        Text(
            advice,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.4f),
            modifier = Modifier.padding(start = 22.dp, top = 2.dp)
        )
    }
}

/**
 * Values normalized based on scientific evidence (Fitzpatrick scale)
 * Base reference: Type 2 burns in ~20 min at UVI 10.
 */
fun getLegendTime(skinType: Int, category: String): Int {
    val uviReference = when (category) {
        "Low" -> 2.0
        "Moderate" -> 4.0
        "High" -> 6.5
        "Very High" -> 9.0
        "Extreme" -> 12.0
        else -> 1.0
    }
    
    return getMedTime(skinType, uviReference)
}

/**
 * Dynamic MED time calculation based on Skin Type and UVI.
 * Formula based on: Time = (SkinFactor) / UVI
 */
fun getMedTime(skinType: Int, uvi: Double): Int {
    if (uvi <= 0) return 600 // Practically infinite
    
    // Skin factor constant: minutes * UVI before burning
    // Type 2 burns at ~200 (20 min * UVI 10)
    val skinFactorConstant = when (skinType) {
        1 -> 140.0  // Burns very fast
        2 -> 200.0  // Cauchy reference
        3 -> 300.0  
        4 -> 450.0  
        5 -> 650.0  
        6 -> 1000.0 // Very resistant
        else -> 200.0
    }

    return (skinFactorConstant / uvi).toInt()
}
