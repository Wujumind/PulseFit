package com.example.pulsefit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onIntegrateHealthClick: () -> Unit,
) {
    var isMetric by remember { mutableStateOf(true) }
    var isDarkMode by remember { mutableStateOf(false) }

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
        }
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
                        onCheckedChange = { isMetric = !it }
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
                        onCheckedChange = { isDarkMode = it }
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
                modifier = Modifier.fillMaxWidth(),
                onClick = onIntegrateHealthClick
            ) {
                ListItem(
                    headlineContent = { Text("Connect Health App") },
                    supportingContent = { Text("Sync your heart rate and activity data") },
                    leadingContent = { Icon(Icons.Default.HealthAndSafety, contentDescription = null) },
                    trailingContent = {
                        Text("Connect", color = MaterialTheme.colorScheme.primary)
                    },
                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                )
            }
        }
    }
}
