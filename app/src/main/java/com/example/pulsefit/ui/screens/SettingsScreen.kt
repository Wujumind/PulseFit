package com.example.pulsefit.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import com.example.pulsefit.HealthConnectManager

/**
 * Screen for managing application-wide preferences.
 * Includes Theme, Measurement Units, Health Connect integrations, and OTA Update settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isMetric: Boolean,
    onMetricChange: (Boolean) -> Unit,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    onLogout: () -> Unit,
    onIntegrateHealthClick: () -> Unit,
    onCheckForUpdatesClick: () -> Unit,
    healthConnectManager: HealthConnectManager,
) {
    // --- UI State ---
    var isHealthExpanded by remember { mutableStateOf(value = false) }
    var isConnected by remember { mutableStateOf(value = false) }
    var showInstructionsFor by remember { mutableStateOf<String?>(null) }

    // Check actual Health Connect permission status on load
    LaunchedEffect(Unit) {
        isConnected = healthConnectManager.hasAllPermissions()
    }
    
    // Instruction modal for specific 3rd party apps
    if (showInstructionsFor != null) {
        AlertDialog(
            onDismissRequest = { 
                showInstructionsFor = null 
            },
            title = { Text("Connect $showInstructionsFor") },
            text = { 
                Text(
                    text = "To sync data from $showInstructionsFor, please:\n\n" +
                        "1. Open the $showInstructionsFor app.\n" +
                        "2. Go to Settings/Sync.\n" +
                        "3. Enable 'Sync to Health Connect'.\n\n" +
                        "PulseFit will then automatically see your data!",
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val packageName = when(showInstructionsFor) {
                        "Samsung Health" -> "com.sec.android.app.shealth"
                        "Garmin Connect" -> "com.garmin.android.apps.connectmobile"
                        "Fitbit" -> "com.fitbit.FitbitMobile"
                        "Zepp (Amazfit)" -> "com.huami.watch.hmwatch"
                        else -> "com.google.android.apps.fitness"
                    }
                    healthConnectManager.openSpecificHealthApp(packageName)
                    showInstructionsFor = null
                }) {
                    Text("Open App")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInstructionsFor = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Measurement Units (Metric vs Imperial)
            ListItem(
                headlineContent = { Text("Units of Measurement") },
                supportingContent = { Text(if (isMetric) "Metric (cm, kg)" else "Imperial (in, lb)") },
                leadingContent = { Icon(Icons.Default.Straighten, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = !isMetric,
                        onCheckedChange = { onMetricChange(!it) }
                    )
                }
            )

            // Theme Management
            ListItem(
                headlineContent = { Text("Dark Mode") },
                supportingContent = { Text(if (isDarkMode) "Enabled" else "Disabled") },
                leadingContent = { Icon(Icons.Default.DarkMode, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { onDarkModeChange(it) }
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = "Integrations",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Health Connect Integration Hub
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Connect Health Platform") },
                        supportingContent = { Text(if (isConnected) "Already synced with Health Connect" else "Google, Samsung, Garmin, Fitbit, Zepp") },
                        leadingContent = { 
                            Icon(
                                Icons.Default.HealthAndSafety, 
                                contentDescription = null,
                                tint = if (isConnected) Color(0xFF4CAF50) else LocalContentColor.current
                            ) 
                        },
                        trailingContent = {
                            IconButton(onClick = { isHealthExpanded = !isHealthExpanded }) {
                                Icon(
                                    if (isHealthExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (isHealthExpanded) "Collapse" else "Expand"
                                )
                            }
                        },
                        modifier = Modifier.clickable { isHealthExpanded = !isHealthExpanded },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )

                    if (isHealthExpanded) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        
                        val trackers = listOf(
                            "Google Fit" to "Sync steps, heart rate, and more",
                            "Samsung Health" to "Connect your Galaxy Watch data",
                            "Garmin Connect" to "Import your Garmin activities",
                            "Fitbit" to "Sync your Fitbit tracker data",
                            "Zepp (Amazfit)" to "Connect your Zepp app data"
                        )

                        trackers.forEachIndexed { index, (name, desc) ->
                            ListItem(
                                headlineContent = { Text(name) },
                                supportingContent = { Text(desc) },
                                leadingContent = { 
                                    Icon(
                                        when(name) {
                                            "Google Fit" -> Icons.Default.HealthAndSafety
                                            "Samsung Health" -> Icons.Default.Watch
                                            "Garmin Connect" -> Icons.Default.Navigation
                                            "Fitbit" -> Icons.Default.Watch
                                            "Zepp (Amazfit)" -> Icons.Default.Bluetooth
                                            else -> Icons.Default.Straighten
                                        }, 
                                        contentDescription = null 
                                    ) 
                                },
                                trailingContent = {
                                    TextButton(onClick = {
                                        if (!isConnected) {
                                            onIntegrateHealthClick() // Start permission flow
                                        } else {
                                            showInstructionsFor = name // Show instructions
                                        }
                                    }) {
                                        Text(if (isConnected) "Settings" else "Connect")
                                    }
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                            if (index < (trackers.size - 1)) {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Note: These trackers sync through Android Health Connect.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // App Meta Information and Updates
            Text(
                text = "App Info",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            ListItem(
                headlineContent = { Text("Version") },
                supportingContent = { Text("1.14 (Build 31)") },
                leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
            )

            ListItem(
                headlineContent = { Text("Check for Updates") },
                supportingContent = { Text("Get the latest features and fixes") },
                leadingContent = { Icon(Icons.Default.HealthAndSafety, contentDescription = null) }, 
                modifier = Modifier.clickable { onCheckForUpdatesClick() },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            // Logout Action
            ListItem(
                headlineContent = { Text("Logout", color = MaterialTheme.colorScheme.error) },
                leadingContent = { 
                    Icon(
                        Icons.AutoMirrored.Filled.Logout, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.error
                    ) 
                },
                modifier = Modifier.clickable { onLogout() }
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Version 1.14 (Build 31)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
