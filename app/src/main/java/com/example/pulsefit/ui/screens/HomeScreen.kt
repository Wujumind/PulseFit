package com.example.pulsefit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulsefit.R
import com.example.pulsefit.WorkoutViewModel
import com.example.pulsefit.HealthConnectManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    username: String,
    workoutViewModel: WorkoutViewModel,
    healthConnectManager: HealthConnectManager,
    onSettingsClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Metrics", "Workouts", "Social")
    val icons = listOf(Icons.Default.MonitorHeart, Icons.Default.FitnessCenter, Icons.Default.People)

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
                    Text(
                        text = "Hi, $username",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
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
                0 -> HealthMetricsContent(workoutViewModel, healthConnectManager)
                1 -> WorkoutsContent(workoutViewModel)
                2 -> SocialContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthMetricsContent(workoutViewModel: WorkoutViewModel, healthConnectManager: HealthConnectManager) {
    val scrollState = rememberScrollState()
    
    var currentHeartRate by remember { mutableStateOf("--") }
    var restingHeartRate by remember { mutableStateOf("--") }
    var hrv by remember { mutableStateOf("--") }

    // Re-fetch data whenever the screen is visible
    LaunchedEffect(Unit) {
        while(true) {
            if (healthConnectManager.hasAllPermissions()) {
                currentHeartRate = healthConnectManager.readCurrentHeartRate()?.toString() ?: "--"
                restingHeartRate = healthConnectManager.readRestingHeartRate()?.toString() ?: "--"
                hrv = healthConnectManager.readHRV()?.let { "%.1f".format(it) } ?: "--"
            }
            kotlinx.coroutines.delay(15000) // Refresh every 15 seconds
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WeeklyWorkoutTracker(workoutViewModel)

        Spacer(modifier = Modifier.height(24.dp))

        MetricCard(
            title = "Current Heart Rate",
            value = currentHeartRate,
            unit = "BPM",
            color = Color.Red
        )

        Spacer(modifier = Modifier.height(16.dp))

        MetricCard(
            title = "Resting Heart Rate",
            value = restingHeartRate,
            unit = "BPM",
            color = Color(0xFFFF5722) // Orange-Red
        )

        Spacer(modifier = Modifier.height(16.dp))

        MetricCard(
            title = "Heart Rate Variability",
            value = hrv,
            unit = "ms",
            color = MaterialTheme.colorScheme.primary,
            statusMessage = if (hrv != "--") "Your HRV is within the healthy range." else "Connect health app to see data."
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
fun WeeklyWorkoutTracker(viewModel: WorkoutViewModel) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Weekly Workout Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                days.forEach { day ->
                    val isCompleted = viewModel.completionStatus[day] ?: false
                    val isRestDay = viewModel.schedule[day]?.contains("Rest", ignoreCase = true) ?: false
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = day, style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(
                                    when {
                                        isCompleted -> Color(0xFF4CAF50) // Green
                                        isRestDay -> MaterialTheme.colorScheme.surfaceVariant
                                        else -> Color(0xFFF44336) // Red
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Add, // Placeholder for Checkmark
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutsContent(workoutViewModel: WorkoutViewModel) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Weekly Schedule",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .align(Alignment.Start)
        )

        WeeklyScheduler(workoutViewModel)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Your Workouts",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .align(Alignment.Start)
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

@Composable
fun WeeklyScheduler(viewModel: WorkoutViewModel) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        days.forEach { day ->
            var isEditing by remember { mutableStateOf(value = false) }
            var textValue by remember { mutableStateOf(viewModel.schedule[day] ?: "") }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (textValue.contains("Rest", ignoreCase = true))
                        MaterialTheme.colorScheme.surfaceVariant
                    else
                        MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (isEditing) {
                            OutlinedTextField(
                                value = textValue,
                                onValueChange = { textValue = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            viewModel.updateSchedule(day, textValue)
                                            isEditing = false
                                        }
                                    ) {
                                        Text("Save", color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            )
                        } else {
                            Text(
                                text = textValue,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    if (!isEditing) {
                        IconButton(onClick = { isEditing = true }) {
                            Text("Edit", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SocialContent() {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Fitness Community",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Placeholder for social feed
        repeat(5) { index ->
            SocialPostCard(
                username = "Athlete ${index + 1}",
                activity = "Completed a 5km Run",
                time = "${index + 1}h ago"
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun SocialPostCard(username: String, activity: String, time: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = username, style = MaterialTheme.typography.titleMedium)
                Text(text = activity, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = time,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
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
