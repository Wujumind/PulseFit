package com.example.pulsefit.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
) {

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

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
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
                                TextButton(onClick = onIntegrateHealthClick) {
                                    Text("Connect")
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                        )
                        if (index < trackers.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
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
        }
    }
}
