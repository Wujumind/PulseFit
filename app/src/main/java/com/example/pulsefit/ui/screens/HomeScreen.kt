package com.example.pulsefit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulsefit.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onSettingsClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Metrics", "Workouts")
    val icons = listOf(Icons.Default.MonitorHeart, Icons.Default.FitnessCenter)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_pulsefit_logo),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Color.Unspecified, // Keep original red
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("PulseFit", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            Column {
                HorizontalDivider()
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) },
                            icon = { Icon(icons[index], contentDescription = null) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (selectedTab) {
                0 -> HealthMetricsContent()
                1 -> WorkoutsContent()
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            TextButton(onClick = onLogout) {
                Text("Logout", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun HealthMetricsContent() {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MetricCard(
            title = "Resting Heart Rate",
            value = "72",
            unit = "BPM",
            color = Color.Red
        )

        Spacer(modifier = Modifier.height(16.dp))

        MetricCard(
            title = "Heart Rate Variability",
            value = "45",
            unit = "ms",
            color = MaterialTheme.colorScheme.primary,
            statusMessage = "Your HRV is within the healthy range."
        )
        
        // Add more spacers at the bottom for better scroll feel
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    unit: String,
    color: Color,
    statusMessage: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = " $unit",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            statusMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun WorkoutsContent() {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Your Workouts",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        WorkoutOptionCard(
            title = "Prebuilt Workout",
            description = "Choose from our professionally designed routines",
            icon = Icons.Default.FitnessCenter,
        ) { /* Handle Prebuilt */ }

        Spacer(modifier = Modifier.height(16.dp))

        WorkoutOptionCard(
            title = "Create Your Own",
            description = "Build a custom routine that fits your needs",
            icon = Icons.Default.Add,
        ) { /* Handle Create Custom */ }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                Text(text = description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
