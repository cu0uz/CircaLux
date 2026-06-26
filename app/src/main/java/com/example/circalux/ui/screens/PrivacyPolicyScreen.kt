package com.example.circalux.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PrivacyPolicyScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Política de Privacidad",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        PrivacySection(
            title = "1. Transparencia y Localización",
            content = "CircaLux es una aplicación de salud local. Todos los datos que introduces (perfil, sesiones, medidas, glucosa) se almacenan exclusivamente en tu dispositivo. No tenemos servidores externos ni recolectamos tu información."
        )
        
        PrivacySection(
            title = "2. Datos Sensibles",
            content = "Los datos de salud y ubicación se utilizan únicamente para realizar los cálculos astronómicos y de síntesis de Vitamina D en tiempo real. No se comparten con terceros."
        )
        
        PrivacySection(
            title = "3. Pagos y Activación",
            content = "Para el sistema de fidelización, generamos un identificador único (User ID) basado en tu dispositivo. Este ID es el único dato que deberás facilitarnos para generar tu código de activación. No almacenamos información financiera ni tarjetas de crédito."
        )
        
        PrivacySection(
            title = "4. Derechos ARSULIPO",
            content = "Cumplimos con el RGPD y la LOPDGDD. Tienes total control sobre tus datos:\n- Acceso: Puedes ver todo en el Historial.\n- Rectificación: Puedes editar cualquier registro.\n- Supresión: Dispones de una opción en Ajustes para borrar todos los datos permanentemente.\n- Portabilidad: Puedes exportar tus datos a CSV en cualquier momento."
        )
        
        PrivacySection(
            title = "5. Seguridad",
            content = "Utilizamos almacenamiento seguro local y protocolos de cifrado estándar del sistema operativo Android para proteger tu información."
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Al usar CircaLux, aceptas estos términos bajo el marco legal vigente.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun PrivacySection(title: String, content: String) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 22.sp
        )
    }
}
