package com.example.circalux.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.circalux.data.model.SolarSession
import com.example.circalux.data.model.RedLightSession
import com.example.circalux.ui.theme.SolarYellow
import com.example.circalux.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.exp
import kotlin.math.ln

@Composable
fun HistoryScreen(viewModel: MainViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Vitamina D", "Salud", "Medidas")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF050B14),
            contentColor = SolarYellow,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = SolarYellow
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = FontWeight.Bold) }
                )
            }
        }

        when (selectedTab) {
            0 -> VitaminDTab(viewModel)
            1 -> HealthTab(viewModel)
            2 -> BodyTab(viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SwipeToDeleteContainer(
    item: T,
    onDelete: (T) -> Unit,
    content: @Composable (T) -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete(item)
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> Color.Red.copy(alpha = 0.8f)
                    else -> Color.Transparent
                }, label = "color"
            )
            val scale by animateFloatAsState(
                if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) 0.75f else 1f, label = "scale"
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Borrar",
                    modifier = Modifier.scale(scale),
                    tint = Color.White
                )
            }
        }
    ) {
        content(item)
    }
}

@Composable
fun VitaminDTab(viewModel: MainViewModel) {
    val solarSessions by viewModel.solarSessions.collectAsState()
    val rltSessions by viewModel.redLightSessions.collectAsState()
    val supplementEntries by viewModel.supplementEntries.collectAsState()
    val profile by viewModel.profile.collectAsState(initial = null)
    
    val baseLevel = profile?.bloodDLevel ?: 0.0
    val lastAnalyticDate = profile?.lastAnalyticDate ?: 0L
    val currentTime = System.currentTimeMillis()
    
    val halfLifeDays = 15.0
    val decayConstant = ln(2.0) / halfLifeDays
    
    val sessionIuAfterAnalytic = solarSessions
        .filter { it.timestamp > lastAnalyticDate && it.timestamp <= currentTime }
        .sumOf { it.vitaminDGenerated }
        
    val supplementIuAfterAnalytic = supplementEntries
        .filter { it.timestamp > lastAnalyticDate && it.timestamp <= currentTime }
        .sumOf { it.amountUI.toDouble() }

    val daysSinceAnalytic = if (lastAnalyticDate > 0) {
        TimeUnit.MILLISECONDS.toDays(currentTime - lastAnalyticDate).toInt()
    } else 0
    
    val decayedBase = baseLevel * exp(-decayConstant * daysSinceAnalytic)
    val currentEstimatedLevel = decayedBase + ((sessionIuAfterAnalytic + supplementIuAfterAnalytic) / 1000.0)

    val projectionDays = 30
    val futureProjection = mutableListOf<Double>()
    var projectedLevel = currentEstimatedLevel
    
    val dailyIuFromSupplements = if (profile?.takesSupplements == true) {
        when (profile?.supplementFrequency) {
            "Semanal" -> (profile?.supplementAmount ?: 0) / 7.0
            "Mensual" -> (profile?.supplementAmount ?: 0) / 30.0
            else -> (profile?.supplementAmount ?: 0).toDouble()
        }
    } else 0.0

    for (i in 1..projectionDays) {
        projectedLevel = projectedLevel * exp(-decayConstant * 1) + (dailyIuFromSupplements / 1000.0)
        futureProjection.add(projectedLevel)
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("ESTADO ACTUAL", style = MaterialTheme.typography.labelSmall, color = SolarYellow.copy(alpha = 0.6f))
            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF141A26)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "${String.format("%.1f", currentEstimatedLevel)} ng/mL",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = SolarYellow
                    )
                    Text("Basado en analítica, sesiones y suplementos", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("PROYECCIÓN A 30 DÍAS (Si continúas así)", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                    com.example.circalux.ui.components.SparklineGraph(
                        data = futureProjection,
                        modifier = Modifier.fillMaxWidth().height(100.dp).padding(vertical = 12.dp),
                        color = Color(0xFF81C784)
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Hoy: ${String.format("%.1f", currentEstimatedLevel)}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                        Text("+30d: ${String.format("%.1f", futureProjection.lastOrNull() ?: 0.0)}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF81C784))
                    }
                }
            }
        }

        item {
            if (solarSessions.isNotEmpty()) {
                Text("SESIONES DE SOL", style = MaterialTheme.typography.labelMedium, color = SolarYellow, modifier = Modifier.padding(vertical = 12.dp))
            }
        }
        
        items(solarSessions, key = { it.id }) { session ->
            SwipeToDeleteContainer(item = session, onDelete = { viewModel.deleteSolarSession(it) }) {
                SolarSessionItem(it)
            }
        }

        item {
            if (rltSessions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("SESIONES LUZ ROJA", style = MaterialTheme.typography.labelMedium, color = Color(0xFFD32F2F), modifier = Modifier.padding(vertical = 8.dp))
            }
        }

        items(rltSessions, key = { it.id }) { session ->
            SwipeToDeleteContainer(item = session, onDelete = { viewModel.deleteRedLightSession(it) }) {
                RedLightSessionItem(it)
            }
        }

        item {
            if (supplementEntries.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("SUPLEMENTOS REGISTRADOS", style = MaterialTheme.typography.labelMedium, color = Color(0xFF81C784), modifier = Modifier.padding(vertical = 8.dp))
            }
        }

        items(supplementEntries, key = { it.id }) { entry ->
            SwipeToDeleteContainer(item = entry, onDelete = { viewModel.deleteSupplementEntry(it) }) {
                SupplementItem(it)
            }
        }
    }
}

@Composable
fun SolarSessionItem(session: SolarSession) {
    val date = SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(Date(session.timestamp))
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF141A26),
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Sesión Solar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SolarYellow)
            }
            Text(date, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Vitamina D", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                    Text("${session.vitaminDGenerated.toInt()} IU", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Duración", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                    Text("${session.durationMinutes} min", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun RedLightSessionItem(session: RedLightSession) {
    val date = SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(Date(session.timestamp))
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF261414),
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Sesión Luz Roja", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
            }
            Text(date, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Detalles", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                    Text("${session.lampPowerWatts}W @ ${session.distanceCm}cm", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Duración", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                    Text("${session.durationMinutes} min", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun SupplementItem(entry: com.example.circalux.data.model.SupplementEntry) {
    val date = SimpleDateFormat("d MMM, yyyy", Locale.getDefault()).format(Date(entry.timestamp))
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1B2614),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81C784).copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(date, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                Text("Vitamina D", fontWeight = FontWeight.Bold, color = Color.White)
            }
            Text("+${entry.amountUI} UI", fontWeight = FontWeight.Black, color = Color(0xFF81C784), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun HealthTab(viewModel: MainViewModel) {
    val metrics by viewModel.healthMetrics.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Métricas de Salud", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SolarYellow)
            Button(onClick = { showAddDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = SolarYellow, contentColor = Color.Black)) {
                Text("Añadir")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (metrics.isNotEmpty()) {
            Text("TENDENCIA GKI", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
            com.example.circalux.ui.components.SparklineGraph(
                data = metrics.map { it.gki }.reversed(),
                modifier = Modifier.fillMaxWidth().height(150.dp).padding(vertical = 16.dp),
                color = Color(0xFF81C784)
            )
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(metrics, key = { it.id }) { metric ->
                SwipeToDeleteContainer(item = metric, onDelete = { viewModel.deleteHealthMetric(it) }) {
                    HealthMetricItem(it)
                }
            }
        }
    }

    if (showAddDialog) {
        AddHealthDialog(onDismiss = { showAddDialog = false }, onAdd = { g, k -> 
            viewModel.addHealthMetric(g, k)
            showAddDialog = false
        })
    }
}

@Composable
fun HealthMetricItem(metric: com.example.circalux.data.model.HealthMetric) {
    val date = SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(Date(metric.timestamp))
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF141A26)
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(date, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                Text("GKI: ${String.format("%.2f", metric.gki)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF81C784))
            }
            Row {
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 16.dp)) {
                    Text("Glucosa", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                    Text("${metric.glucose.toInt()} mg/dL", style = MaterialTheme.typography.bodyMedium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Cetonas", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                    Text("${metric.ketones} mmol/L", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun AddHealthDialog(onDismiss: () -> Unit, onAdd: (Double, Double) -> Unit) {
    var glucose by remember { mutableStateOf("") }
    var ketones by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Métricas") },
        text = {
            Column {
                OutlinedTextField(
                    value = glucose,
                    onValueChange = { glucose = it },
                    label = { Text("Glucosa (mg/dL)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = ketones,
                    onValueChange = { ketones = it },
                    label = { Text("Cetonas (mmol/L)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { 
                val g = glucose.toDoubleOrNull() ?: 0.0
                val k = ketones.toDoubleOrNull() ?: 0.0
                if (g > 0 && k > 0) onAdd(g, k)
            }) { Text("GUARDAR") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCELAR") } }
    )
}

@Composable
fun BodyTab(viewModel: MainViewModel) {
    val measurements by viewModel.bodyMeasurements.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Progresión Física", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SolarYellow)
            Button(onClick = { showAddDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = SolarYellow, contentColor = Color.Black)) {
                Text("Añadir")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (measurements.isNotEmpty()) {
            Text("GRASA CORPORAL (%)", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
            com.example.circalux.ui.components.SparklineGraph(
                data = measurements.map { it.bodyFatNavy }.reversed(),
                modifier = Modifier.fillMaxWidth().height(150.dp).padding(vertical = 16.dp),
                color = Color(0xFFE57373)
            )
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(measurements, key = { it.id }) { item ->
                SwipeToDeleteContainer(item = item, onDelete = { viewModel.deleteBodyMeasurement(it) }) {
                    BodyMeasurementItem(item)
                }
            }
        }
    }

    if (showAddDialog) {
        AddBodyDialog(onDismiss = { showAddDialog = false }, onAdd = { neck, waist, hip, chest, biceps, thigh, weight ->
            viewModel.addBodyMeasurement(neck, waist, hip, chest, biceps, thigh, weight)
            showAddDialog = false
        })
    }
}

@Composable
fun BodyMeasurementItem(item: com.example.circalux.data.model.BodyMeasurement) {
    val date = SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(Date(item.timestamp))
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF141A26)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(date, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                Text("${String.format("%.1f", item.bodyFatNavy)}% Grasa", fontWeight = FontWeight.Bold, color = Color(0xFFE57373))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Peso", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                    Text("${item.weight} kg", style = MaterialTheme.typography.bodyMedium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("WHtR", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                    Text(String.format("%.2f", item.whtr), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun AddBodyDialog(onDismiss: () -> Unit, onAdd: (Double, Double, Double, Double, Double, Double, Double) -> Unit) {
    var neck by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }
    var hip by remember { mutableStateOf("") }
    var chest by remember { mutableStateOf("") }
    var biceps by remember { mutableStateOf("") }
    var thigh by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Medidas") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                val fields = listOf(
                    "Cuello (cm)" to { v: String -> neck = v },
                    "Cintura (cm)" to { v: String -> waist = v },
                    "Cadera (cm)" to { v: String -> hip = v },
                    "Pecho (cm)" to { v: String -> chest = v },
                    "Bíceps (cm)" to { v: String -> biceps = v },
                    "Muslo (cm)" to { v: String -> thigh = v },
                    "Peso (kg)" to { v: String -> weight = v }
                )
                fields.forEach { (label, setter) ->
                    OutlinedTextField(
                        value = if (label.startsWith("Cuello")) neck else if (label.startsWith("Cintura")) waist else if (label.startsWith("Cadera")) hip else if (label.startsWith("Pecho")) chest else if (label.startsWith("Bíceps")) biceps else if (label.startsWith("Muslo")) thigh else weight,
                        onValueChange = setter,
                        label = { Text(label) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                onAdd(
                    neck.toDoubleOrNull() ?: 0.0,
                    waist.toDoubleOrNull() ?: 0.0,
                    hip.toDoubleOrNull() ?: 0.0,
                    chest.toDoubleOrNull() ?: 0.0,
                    biceps.toDoubleOrNull() ?: 0.0,
                    thigh.toDoubleOrNull() ?: 0.0,
                    weight.toDoubleOrNull() ?: 0.0
                )
            }) { Text("GUARDAR") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCELAR") } }
    )
}
