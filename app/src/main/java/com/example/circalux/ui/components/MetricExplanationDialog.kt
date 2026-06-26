package com.example.circalux.ui.components

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.circalux.ui.theme.SolarYellow
import com.example.circalux.util.ScientificKnowledgeBase

@Composable
fun MetricExplanationDialog(
    metricKey: String,
    currentValue: String,
    unit: String = "",
    onDismiss: () -> Unit,
    visualContent: @Composable (() -> Unit)? = null
) {
    val tip = ScientificKnowledgeBase.getTip(metricKey) ?: return

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F1724),
        titleContentColor = SolarYellow,
        textContentColor = Color.White,
        title = {
            Text(
                tip.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Visual value display
                Surface(
                    color = SolarYellow.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (visualContent != null) {
                            Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                                visualContent()
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        Text(
                            text = currentValue,
                            style = if (visualContent != null) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        if (unit.isNotEmpty()) {
                            Text(
                                text = unit.uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                color = SolarYellow,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = tip.content,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Evidencia Científica:",
                    style = MaterialTheme.typography.labelMedium,
                    color = SolarYellow,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = tip.source,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
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
