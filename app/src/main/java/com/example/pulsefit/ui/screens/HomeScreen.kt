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
import com.example.pulsefit.*
import com.example.pulsefit.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    username: String,
    workoutViewModel: WorkoutViewModel,
    socialViewModel: SocialViewModel,
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
                            icon = { Icon(icons[index], contentDescription = null) },
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
                2 -> SocialContent(socialViewModel)
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
                    val isPast = viewModel.isPastDay(day)
                    val isToday = viewModel.isToday(day)
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = day, 
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isToday) MaterialTheme.colorScheme.primary else Color.Unspecified,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(
                                    when {
                                        isCompleted -> Color(0xFF4CAF50) // Green (Completed/Rest)
                                        isPast && !isRestDay -> Color(0xFFF44336) // Red (Missed Workout)
                                        else -> MaterialTheme.colorScheme.surfaceVariant // Gray (Future/Pending Today)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Add, 
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

        Button(
            onClick = { workoutViewModel.startTodayWorkout() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Icon(Icons.Default.FitnessCenter, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Today's Workout")
        }

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
fun SocialContent(socialViewModel: SocialViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        socialViewModel.loadLeaderboard()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Fitness Community",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it
                socialViewModel.searchUsers(it)
            },
            label = { Text("Search usernames...") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                if (socialViewModel.isSearching) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (searchQuery.isNotEmpty()) {
            Text("Search Results", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
            
            if (!socialViewModel.isSearching && socialViewModel.searchResults.isEmpty()) {
                Text(
                    text = "No account with that username",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                socialViewModel.searchResults.forEach { user ->
                    UserResultCard(user) { socialViewModel.addFriend(user.uid) }
                }
            }
        } else {
            // Show Friends List when not searching
            if (socialViewModel.friendsList.isNotEmpty()) {
                Text("Friends", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
                socialViewModel.friendsList.forEach { friend ->
                    UserResultCard(friend, isFriend = true) { 
                        socialViewModel.loadFriendSchedule(friend.uid, friend.username)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Leaderboard", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
        Column(modifier = Modifier.verticalScroll(scrollState)) {
            socialViewModel.leaderboard.forEachIndexed { index, user ->
                LeaderboardCard(index + 1, user)
            }
        }
    }

    // Friend Schedule Dialog
    if (socialViewModel.selectedFriendName != null) {
        AlertDialog(
            onDismissRequest = { socialViewModel.selectedFriendName = null },
            title = { Text("${socialViewModel.selectedFriendName}'s Schedule") },
            text = {
                Column {
                    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    days.forEach { day ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text(day, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
                            Text(socialViewModel.friendSchedule[day] ?: "No plan")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { socialViewModel.selectedFriendName = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun UserResultCard(user: UserProfile, isFriend: Boolean = false, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(user.username)
            }
            if (isFriend) {
                TextButton(onClick = onClick) { Text("View Schedule") }
            } else {
                Button(onClick = onClick) { Text("Add") }
            }
        }
    }
}

@Composable
fun LeaderboardCard(rank: Int, user: UserProfile) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("#$rank", fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
            Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.username, fontWeight = FontWeight.Bold)
                Text("${user.streak} day streak", style = MaterialTheme.typography.bodySmall)
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
