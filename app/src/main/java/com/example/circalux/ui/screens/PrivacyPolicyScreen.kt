package com.example.circalux.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.circalux.ui.theme.SolarYellow

@Composable
fun PrivacyPolicyScreen() {
    val scrollState = rememberScrollState()
    
    // Ensure the screen fills max height and is explicitly scrollable
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF050B14)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Política de Privacidad",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = SolarYellow
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            PrivacySection(
                title = "1. TRANSPARENCIA TOTAL",
                content = "CircaLux es una aplicación de salud de funcionamiento estrictamente LOCAL. Esto significa que todos los datos que introduces (perfil, sesiones de sol, medidas corporales, niveles de glucosa o cetonas) se almacenan única y exclusivamente en la base de datos de tu teléfono móvil."
            )
            
            PrivacySection(
                title = "2. SIN SERVIDORES EXTERNOS",
                content = "No recolectamos tu información. No existen servidores externos donde se envíen tus datos personales. La aplicación no utiliza rastreadores, analíticas de terceros ni servicios en la nube para procesar tu información financiera o de salud."
            )
            
            PrivacySection(
                title = "3. DATOS DE LOCALIZACIÓN",
                content = "CircaLux requiere acceso al GPS para calcular la posición del sol (ángulo de elevación y azimut) y la intensidad del índice UVI en tu posición exacta. Estos datos se procesan en tiempo real dentro de tu dispositivo y nunca se comparten con nosotros ni con terceros."
            )
            
            PrivacySection(
                title = "4. SISTEMA DE FIDELIZACIÓN",
                content = "Para el sistema opcional de activación, generamos un identificador único (User ID) basado en el hardware de tu dispositivo. Este ID es el único dato que el usuario facilita voluntariamente para la generación de su código de activación. No almacenamos nombres reales, correos electrónicos ni información bancaria."
            )
            
            PrivacySection(
                title = "5. DERECHOS ARSULIPO",
                content = "En cumplimiento con el RGPD y la LOPDGDD, garantizamos tus derechos:\n" +
                        "• ACCESO: Puedes ver todos tus datos en el historial.\n" +
                        "• RECTIFICACIÓN: Puedes editar cualquier registro manualmente.\n" +
                        "• SUPRESIÓN: En Ajustes dispones de un botón para BORRAR TODO permanentemente.\n" +
                        "• PORTABILIDAD: Puedes exportar tu historial a un archivo CSV.\n" +
                        "• OPOSICIÓN: Puedes retirar tu consentimiento en cualquier momento eliminando la app."
            )
            
            PrivacySection(
                title = "6. SEGURIDAD",
                content = "Tus datos están protegidos por el cifrado nativo del sistema operativo Android. Al ser una app local, la seguridad de tu información depende de la seguridad de tu propio dispositivo."
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Última actualización: Junio 2026\nCircaLux - El conocimiento es luz.",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.4f),
                lineHeight = 16.sp
            )
            
            Spacer(modifier = Modifier.height(48.dp)) // Extra space at bottom to ensure last section is visible
        }
    }
}

@Composable
fun PrivacySection(title: String, content: String) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
            color = SolarYellow.copy(alpha = 0.8f),
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            lineHeight = 22.sp
        )
    }
}
