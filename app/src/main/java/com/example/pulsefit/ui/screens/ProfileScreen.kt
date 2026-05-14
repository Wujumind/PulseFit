package com.example.pulsefit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.pulsefit.UserViewModel

/**
 * Allows the user to view and edit their profile details.
 * User changes are stored locally in the ViewModel and synchronized with Firestore.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userViewModel: UserViewModel,
    isMetric: Boolean,
    onBackClick: () -> Unit,
) {
    // Local state for editable fields to avoid UI lag
    var height by remember { mutableStateOf(if (isMetric) "175" else "69") }
    var weight by remember { mutableStateOf(if (isMetric) "70" else "154") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
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
            // Profile Picture - Tapping allows user to change it (placeholder)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { /* Handle photo change integration */ },
                contentAlignment = Alignment.Center
            ) {
                if (userViewModel.profilePictureUrl != null) {
                    AsyncImage(
                        model = userViewModel.profilePictureUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            TextButton(onClick = { /* Handle photo change integration */ }) {
                Text("Change Photo")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Editable Username Field
            OutlinedTextField(
                value = userViewModel.username,
                onValueChange = { userViewModel.username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Measurement Fields (Height and Weight)
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it },
                    label = { Text(if (isMetric) "Height (cm)" else "Height (in)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text(if (isMetric) "Weight (kg)" else "Weight (lb)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Persistence Trigger
            Button(
                onClick = { 
                    userViewModel.height = height
                    userViewModel.weight = weight
                    userViewModel.saveUserData() // Pushes updates to Firestore
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Profile")
            }
        }
    }
}
