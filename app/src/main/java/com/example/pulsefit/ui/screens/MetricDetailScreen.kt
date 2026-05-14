package com.example.pulsefit.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.pulsefit.HealthConnectManager
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetricDetailScreen(
    metricName: String,
    healthConnectManager: HealthConnectManager,
    onBackClick: () -> Unit
) {
    var historyData by remember { mutableStateOf<List<Pair<Instant, Double>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(metricName) {
        isLoading = true
        historyData = when (metricName) {
            "HRV" -> healthConnectManager.readHRVHistory()
            "Resting Heart Rate" -> healthConnectManager.readRestingHeartRateHistory().map { it.first to it.second.toDouble() }
            else -> healthConnectManager.readHeartRateHistory().map { it.first to it.second.toDouble() }
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(metricName) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (historyData.isEmpty()) {
                Text("No data available for the last period.")
            } else {
                Text(
                    text = "History (Last 30 Days)",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                SimpleLineGraph(
                    data = historyData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .padding(bottom = 32.dp),
                    lineColor = if (metricName == "HRV") MaterialTheme.colorScheme.primary else Color.Red
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Stats Summary
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val avg = historyData.map { it.second }.average()
                        val max = historyData.maxOf { it.second }
                        val min = historyData.minOf { it.second }

                        Text("Average: ${"%.1f".format(avg)}")
                        Text("Highest: ${"%.1f".format(max)}")
                        Text("Lowest: ${"%.1f".format(min)}")
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleLineGraph(
    data: List<Pair<Instant, Double>>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color.Blue
) {
    if (data.size < 2) return

    val minVal = data.minOf { it.second }.toFloat()
    val maxVal = data.maxOf { it.second }.toFloat()
    val range = (maxVal - minVal).coerceAtLeast(1f)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val spacing = width / (data.size - 1)

        val path = Path()
        data.forEachIndexed { index, pair ->
            val x = index * spacing
            val y = height - ((pair.second.toFloat() - minVal) / range * height)
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx())
        )
    }
}
