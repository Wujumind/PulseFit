package com.example.pulsefit.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulsefit.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Handles new user registration via Email and Password.
 * Ensures usernames are unique before allowing account creation.
 */
@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onLoginClick: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(value = false) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Official PulseFit Logo
        Image(
            painter = painterResource(id = R.drawable.ic_pulsefit_logo),
            contentDescription = "PulseFit Logo",
            modifier = Modifier.size(80.dp)
        )
        
        Text(
            text = "PulseFit",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Create Account",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Feedback for registration errors
        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Username Input - Used for social features and must be unique
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        // Email Input
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        // Password Input
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            // Registration trigger
            Button(
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {
                        isLoading = true
                        
                        // Step 1: Verify username availability in Firestore
                        db.collection("users")
                            .whereEqualTo("username", name)
                            .get()
                            .addOnSuccessListener { documents ->
                                if (documents.isEmpty()) {
                                    // Step 2: Create the auth account
                                    auth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                // Step 3: Initialize user profile in Firestore
                                                val userId = auth.currentUser?.uid ?: ""
                                                val userData = mapOf(
                                                    "username" to name,
                                                    "email" to email,
                                                    "streak" to 0,
                                                    "totalWorkouts" to 0,
                                                    "friends" to emptyList<String>()
                                                )
                                                db.collection("users").document(userId).set(userData)
                                                    .addOnSuccessListener {
                                                        isLoading = false
                                                        onSignUpSuccess()
                                                    }
                                            } else {
                                                isLoading = false
                                                error = task.exception?.message ?: "Sign up failed"
                                            }
                                        }
                                } else {
                                    isLoading = false
                                    error = "Username is already taken"
                                }
                            }
                    } else {
                        error = "Please fill in all fields"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign Up")
            }
        }

        // Return to Login Screen
        TextButton(onClick = onLoginClick) {
            Text("Already have an account? Login")
        }
    }
}
