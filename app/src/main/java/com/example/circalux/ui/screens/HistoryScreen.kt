package com.example.circalux.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.circalux.data.model.*
import com.example.circalux.ui.theme.SolarYellow
import com.example.circalux.ui.viewmodel.MainViewModel
import com.example.circalux.util.NumberUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.exp
import kotlin.math.ln

@Composable
fun HistoryScreen(viewModel: MainViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Vitamina D", "Salud", "Medidas")
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color(0xFF050B14),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
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
                0 -> VitaminDTab(viewModel, snackbarHostState, scope)
                1 -> HealthTab(viewModel, snackbarHostState, scope)
                2 -> BodyTab(viewModel, snackbarHostState, scope)
            }
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
fun VitaminDTab(
    viewModel: MainViewModel,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    val solarSessions by viewModel.solarSessions.collectAsState()
    val rltSessions by viewModel.redLightSessions.collectAsState()
    val supplementEntries by viewModel.supplementEntries.collectAsState()
    val profile by viewModel.profile.collectAsState(initial = null)
    
    var editingSolarSession by remember { mutableStateOf<SolarSession?>(null) }
    var editingRLTSession by remember { mutableStateOf<RedLightSession?>(null) }
    var editingSupplement by remember { mutableStateOf<SupplementEntry?>(null) }
    
    val baseLevel = profile?.bloodDLevel ?: 0.0
    val lastAnalyticDate = profile?.lastAnalyticDate ?: 0L
    val currentTime = System.currentTimeMillis()
    
    val halfLifeDays = 21.0 // 25(OH)D half-life is ~3 weeks
    val dailyDecay = exp(-ln(2.0) / halfLifeDays)
    val conversionPerIu = 0.00038 // Scientific approx: 1000 IU/day steady state yields ~10-12 ng/mL increase

    // Combine all sources of Vitamin D (Sun + Supplements)
    val allIntakes = (solarSessions.map { it.timestamp to it.vitaminDGenerated } + 
                     supplementEntries.map { it.timestamp to it.amountUI.toDouble() })
                     .filter { it.first > lastAnalyticDate && it.first <= currentTime }
                     .sortedBy { it.first }

    var currentEstimatedLevel = baseLevel
    var lastProcessedTime = if (lastAnalyticDate > 0) lastAnalyticDate else {
        if (allIntakes.isNotEmpty()) allIntakes.first().first else currentTime
    }
    
    allIntakes.forEach { (time, iu) ->
        val daysElapsed = (time - lastProcessedTime).toDouble() / (1000 * 60 * 60 * 24)
        if (daysElapsed > 0) {
            currentEstimatedLevel *= Math.pow(dailyDecay, daysElapsed)
        }
        currentEstimatedLevel += (iu * conversionPerIu)
        lastProcessedTime = time
    }
    
    // Final decay to now
    val finalDaysElapsed = (currentTime - lastProcessedTime).toDouble() / (1000 * 60 * 60 * 24)
    if (finalDaysElapsed > 0) {
        currentEstimatedLevel *= Math.pow(dailyDecay, finalDaysElapsed)
    }

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

    for (day in 1..projectionDays) {
        projectedLevel = (projectedLevel * dailyDecay) + (dailyIuFromSupplements * conversionPerIu)
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
            SwipeToDeleteContainer(item = session, onDelete = { 
                viewModel.deleteSolarSession(it) 
                scope.launch { snackbarHostState.showSnackbar("Sesión solar eliminada") }
            }) {
                SolarSessionItem(session, onClick = { editingSolarSession = it })
            }
        }

        item {
            if (rltSessions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("SESIONES LUZ ROJA", style = MaterialTheme.typography.labelMedium, color = Color(0xFFD32F2F), modifier = Modifier.padding(vertical = 8.dp))
            }
        }

        items(rltSessions, key = { it.id }) { session ->
            SwipeToDeleteContainer(item = session, onDelete = { 
                viewModel.deleteRedLightSession(it) 
                scope.launch { snackbarHostState.showSnackbar("Sesión RLT eliminada") }
            }) {
                RedLightSessionItem(session, onClick = { editingRLTSession = it })
            }
        }

        item {
            if (supplementEntries.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("SUPLEMENTOS REGISTRADOS", style = MaterialTheme.typography.labelMedium, color = Color(0xFF81C784), modifier = Modifier.padding(vertical = 8.dp))
            }
        }

        items(supplementEntries, key = { it.id }) { entry ->
            SwipeToDeleteContainer(item = entry, onDelete = { 
                viewModel.deleteSupplementEntry(it) 
                scope.launch { snackbarHostState.showSnackbar("Registro eliminado") }
            }) {
                SupplementItem(entry, onClick = { editingSupplement = it })
            }
        }
    }

    editingSolarSession?.let { session ->
        EditSolarSessionDialog(
            session = session,
            onDismiss = { editingSolarSession = null },
            onConfirm = { updated ->
                viewModel.updateSolarSession(updated)
                scope.launch { snackbarHostState.showSnackbar("Sesión actualizada") }
                editingSolarSession = null
            }
        )
    }

    editingRLTSession?.let { session ->
        EditRLTSessionDialog(
            session = session,
            onDismiss = { editingRLTSession = null },
            onConfirm = { updated ->
                viewModel.updateRedLightSession(updated)
                scope.launch { snackbarHostState.showSnackbar("Sesión actualizada") }
                editingRLTSession = null
            }
        )
    }

    editingSupplement?.let { entry ->
        EditSupplementDialog(
            entry = entry,
            onDismiss = { editingSupplement = null },
            onConfirm = { updated ->
                viewModel.updateSupplementEntry(updated)
                scope.launch { snackbarHostState.showSnackbar("Registro actualizado") }
                editingSupplement = null
            }
        )
    }
}

@Composable
fun SolarSessionItem(session: SolarSession, onClick: () -> Unit) {
    val date = SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(Date(session.timestamp))
    Surface(
        onClick = onClick,
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
fun RedLightSessionItem(session: RedLightSession, onClick: () -> Unit) {
    val date = SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(Date(session.timestamp))
    Surface(
        onClick = onClick,
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
fun SupplementItem(entry: SupplementEntry, onClick: () -> Unit) {
    val date = SimpleDateFormat("d MMM, yyyy", Locale.getDefault()).format(Date(entry.timestamp))
    Surface(
        onClick = onClick,
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
fun HealthTab(
    viewModel: MainViewModel,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    val metrics by viewModel.healthMetrics.collectAsState()
    var editingMetric by remember { mutableStateOf<HealthMetric?>(null) }
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
                SwipeToDeleteContainer(item = metric, onDelete = { 
                    viewModel.deleteHealthMetric(it) 
                    scope.launch { snackbarHostState.showSnackbar("Métrica eliminada") }
                }) {
                    HealthMetricItem(metric, onClick = { editingMetric = metric })
                }
            }
        }
    }

    if (showAddDialog) {
        AddHealthDialog(onDismiss = { showAddDialog = false }, onAdd = { g, k, t -> 
            viewModel.addHealthMetric(g, k, t)
            scope.launch { snackbarHostState.showSnackbar("Métricas guardadas") }
            showAddDialog = false
        })
    }

    editingMetric?.let { metric ->
        AddHealthDialog(
            initialMetric = metric,
            onDismiss = { editingMetric = null },
            onAdd = { g, k, t ->
                viewModel.updateHealthMetric(metric.copy(glucose = g, ketones = k, timestamp = t))
                scope.launch { snackbarHostState.showSnackbar("Métrica actualizada") }
                editingMetric = null
            }
        )
    }
}

@Composable
fun HealthMetricItem(metric: HealthMetric, onClick: () -> Unit) {
    val date = SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(Date(metric.timestamp))
    Surface(
        onClick = onClick,
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
fun AddHealthDialog(
    initialMetric: HealthMetric? = null,
    onDismiss: () -> Unit, 
    onAdd: (Double, Double, Long) -> Unit
) {
    var glucose by remember { mutableStateOf(initialMetric?.glucose?.toString() ?: "") }
    var ketones by remember { mutableStateOf(initialMetric?.ketones?.toString() ?: "") }
    var selectedDate by remember { mutableLongStateOf(initialMetric?.timestamp ?: System.currentTimeMillis()) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialMetric == null) "Registrar Métricas" else "Editar Métrica") },
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
                Spacer(modifier = Modifier.height(16.dp))
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selectedDate)),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha de la toma") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Icon(Icons.Default.CalendarMonth, null, tint = SolarYellow) },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.White,
                            disabledBorderColor = Color.White.copy(alpha = 0.2f),
                            disabledLabelColor = Color.White.copy(alpha = 0.6f),
                            disabledTrailingIconColor = SolarYellow
                        )
                    )
                    Box(modifier = Modifier.matchParentSize().clickable {
                        val calendar = Calendar.getInstance().apply { timeInMillis = selectedDate }
                        DatePickerDialog(context, { _, y, m, d ->
                            calendar.set(y, m, d)
                            selectedDate = calendar.timeInMillis
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                    })
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                val g = NumberUtils.parseDouble(glucose)
                val k = NumberUtils.parseDouble(ketones)
                if (g > 0 && k > 0) onAdd(g, k, selectedDate)
            }) { Text("GUARDAR") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCELAR") } }
    )
}

@Composable
fun BodyTab(
    viewModel: MainViewModel,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    val measurements by viewModel.bodyMeasurements.collectAsState()
    var editingItem by remember { mutableStateOf<BodyMeasurement?>(null) }
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
                SwipeToDeleteContainer(item = item, onDelete = { 
                    viewModel.deleteBodyMeasurement(item) 
                    scope.launch { snackbarHostState.showSnackbar("Medición eliminada") }
                }) {
                    BodyMeasurementItem(item, onClick = { editingItem = item })
                }
            }
        }
    }

    if (showAddDialog) {
        AddBodyDialog(onDismiss = { showAddDialog = false }, onAdd = { n, w, h, c, b, m, weight, timestamp ->
            viewModel.addBodyMeasurement(n, w, h, c, b, m, weight, timestamp)
            scope.launch { snackbarHostState.showSnackbar("Medición guardada") }
            showAddDialog = false
        })
    }

    editingItem?.let { item ->
        AddBodyDialog(
            initialItem = item,
            onDismiss = { editingItem = null },
            onAdd = { n, w, h, c, b, m, weight, timestamp ->
                viewModel.updateBodyMeasurement(item.copy(
                    neck = n, waist = w, hip = h, chest = c, biceps = b, thigh = m, weight = weight, timestamp = timestamp
                ))
                scope.launch { snackbarHostState.showSnackbar("Medición actualizada") }
                editingItem = null
            }
        )
    }
}

@Composable
fun BodyMeasurementItem(item: BodyMeasurement, onClick: () -> Unit) {
    val date = SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(Date(item.timestamp))
    Surface(
        onClick = onClick,
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
fun AddBodyDialog(
    initialItem: BodyMeasurement? = null,
    onDismiss: () -> Unit, 
    onAdd: (Double, Double, Double, Double, Double, Double, Double, Long) -> Unit
) {
    var neck by remember { mutableStateOf(initialItem?.neck?.toString() ?: "") }
    var waist by remember { mutableStateOf(initialItem?.waist?.toString() ?: "") }
    var hip by remember { mutableStateOf(initialItem?.hip?.toString() ?: "") }
    var chest by remember { mutableStateOf(initialItem?.chest?.toString() ?: "") }
    var biceps by remember { mutableStateOf(initialItem?.biceps?.toString() ?: "") }
    var thigh by remember { mutableStateOf(initialItem?.thigh?.toString() ?: "") }
    var weight by remember { mutableStateOf(initialItem?.weight?.toString() ?: "") }
    var selectedDate by remember { mutableLongStateOf(initialItem?.timestamp ?: System.currentTimeMillis()) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialItem == null) "Registrar Medidas" else "Editar Medidas") },
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
                    val value = when {
                        label.startsWith("Cuello") -> neck
                        label.startsWith("Cintura") -> waist
                        label.startsWith("Cadera") -> hip
                        label.startsWith("Pecho") -> chest
                        label.startsWith("Bíceps") -> biceps
                        label.startsWith("Muslo") -> thigh
                        else -> weight
                    }
                    OutlinedTextField(
                        value = value,
                        onValueChange = setter,
                        label = { Text(label) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    OutlinedTextField(
                        value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selectedDate)),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha de la medición") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Icon(Icons.Default.CalendarMonth, null, tint = SolarYellow) },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.White,
                            disabledBorderColor = Color.White.copy(alpha = 0.2f),
                            disabledLabelColor = Color.White.copy(alpha = 0.6f),
                            disabledTrailingIconColor = SolarYellow
                        )
                    )
                    Box(modifier = Modifier.matchParentSize().clickable {
                        val calendar = Calendar.getInstance().apply { timeInMillis = selectedDate }
                        DatePickerDialog(context, { _, y, m, d ->
                            calendar.set(y, m, d)
                            selectedDate = calendar.timeInMillis
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                    })
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                onAdd(
                    NumberUtils.parseDouble(neck),
                    NumberUtils.parseDouble(waist),
                    NumberUtils.parseDouble(hip),
                    NumberUtils.parseDouble(chest),
                    NumberUtils.parseDouble(biceps),
                    NumberUtils.parseDouble(thigh),
                    NumberUtils.parseDouble(weight),
                    selectedDate
                )
            }) { Text("GUARDAR") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCELAR") } }
    )
}

@Composable
fun EditSolarSessionDialog(
    session: SolarSession,
    onDismiss: () -> Unit,
    onConfirm: (SolarSession) -> Unit
) {
    var duration by remember { mutableStateOf(session.durationMinutes.toString()) }
    var uvi by remember { mutableStateOf(session.uviAvg.toString()) }
    var selectedDate by remember { mutableLongStateOf(session.timestamp) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Sesión Solar") },
        text = {
            Column {
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duración (min)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uvi,
                    onValueChange = { uvi = it },
                    label = { Text("UVI Promedio") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(selectedDate)),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha y Hora") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Icon(Icons.Default.CalendarMonth, null, tint = SolarYellow) },
                        enabled = false
                    )
                    Box(modifier = Modifier.matchParentSize().clickable {
                        val calendar = Calendar.getInstance().apply { timeInMillis = selectedDate }
                        DatePickerDialog(context, { _, y, m, d ->
                            calendar.set(y, m, d)
                            selectedDate = calendar.timeInMillis
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                    })
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val d = NumberUtils.parseInt(duration)
                val u = NumberUtils.parseDouble(uvi)
                onConfirm(session.copy(durationMinutes = d, uviAvg = u, timestamp = selectedDate))
            }) { Text("GUARDAR") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCELAR") } }
    )
}

@Composable
fun EditRLTSessionDialog(
    session: RedLightSession,
    onDismiss: () -> Unit,
    onConfirm: (RedLightSession) -> Unit
) {
    var duration by remember { mutableStateOf(session.durationMinutes.toString()) }
    var power by remember { mutableStateOf(session.lampPowerWatts.toString()) }
    var distance by remember { mutableStateOf(session.distanceCm.toString()) }
    var selectedDate by remember { mutableLongStateOf(session.timestamp) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Sesión Luz Roja") },
        text = {
            Column {
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duración (min)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = power,
                    onValueChange = { power = it },
                    label = { Text("Potencia (W)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = distance,
                    onValueChange = { distance = it },
                    label = { Text("Distancia (cm)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(selectedDate)),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha y Hora") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Icon(Icons.Default.CalendarMonth, null, tint = SolarYellow) },
                        enabled = false
                    )
                    Box(modifier = Modifier.matchParentSize().clickable {
                        val calendar = Calendar.getInstance().apply { timeInMillis = selectedDate }
                        DatePickerDialog(context, { _, y, m, d ->
                            calendar.set(y, m, d)
                            selectedDate = calendar.timeInMillis
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                    })
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(session.copy(
                    durationMinutes = NumberUtils.parseInt(duration),
                    lampPowerWatts = NumberUtils.parseInt(power),
                    distanceCm = NumberUtils.parseInt(distance),
                    timestamp = selectedDate
                ))
            }) { Text("GUARDAR") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCELAR") } }
    )
}

@Composable
fun EditSupplementDialog(
    entry: SupplementEntry,
    onDismiss: () -> Unit,
    onConfirm: (SupplementEntry) -> Unit
) {
    var amountUI by remember { mutableStateOf(entry.amountUI.toString()) }
    var selectedDate by remember { mutableLongStateOf(entry.timestamp) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Registro Suplemento") },
        text = {
            Column {
                OutlinedTextField(
                    value = amountUI,
                    onValueChange = { amountUI = it },
                    label = { Text("Cantidad (UI)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selectedDate)),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Icon(Icons.Default.CalendarMonth, null, tint = SolarYellow) },
                        enabled = false
                    )
                    Box(modifier = Modifier.matchParentSize().clickable {
                        val calendar = Calendar.getInstance().apply { timeInMillis = selectedDate }
                        DatePickerDialog(context, { _, y, m, d ->
                            calendar.set(y, m, d)
                            selectedDate = calendar.timeInMillis
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                    })
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(entry.copy(
                    amountUI = NumberUtils.parseInt(amountUI),
                    timestamp = selectedDate
                ))
            }) { Text("GUARDAR") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCELAR") } }
    )
}
