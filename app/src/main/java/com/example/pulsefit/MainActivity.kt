package com.example.pulsefit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pulsefit.ui.screens.HomeScreen
import com.example.pulsefit.ui.screens.LoginScreen
import com.example.pulsefit.ui.screens.ProfileScreen
import com.example.pulsefit.ui.screens.SettingsScreen
import com.example.pulsefit.ui.screens.SignUpScreen
import com.example.pulsefit.ui.theme.PulseFitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PulseFitTheme {
                PulseFitApp()
            }
        }
    }
}

@Composable
fun PulseFitApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginClick = { navController.navigate("home") },
                onSignUpClick = { navController.navigate("signup") },
                onGoogleSignInClick = { /* Handle Google Sign In */ },
            ) { /* Handle Facebook Sign In */ }
        }
        composable("signup") {
            SignUpScreen(
                onSignUpClick = { navController.navigate("home") },
            ) { navController.popBackStack() }
        }
        composable("home") {
            HomeScreen(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onSettingsClick = { navController.navigate("settings") },
            ) { navController.navigate("profile") }
        }
        composable("settings") {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
            ) { /* Handle Health Integration */ }
        }
        composable("profile") {
            ProfileScreen { navController.popBackStack() }
        }
    }
}
