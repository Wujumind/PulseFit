package com.example.pulsefit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pulsefit.ui.screens.*
import com.example.pulsefit.ui.theme.PulseFitTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var healthConnectManager: HealthConnectManager
    private val auth = FirebaseAuth.getInstance()
    
    private val requestPermissions = registerForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { _ ->
        // Permissions granted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        healthConnectManager = HealthConnectManager(this)
        enableEdgeToEdge()
        setContent {
            val userViewModel: UserViewModel = viewModel()
            val workoutViewModel: WorkoutViewModel = viewModel()
            val socialViewModel: SocialViewModel = viewModel()
            val systemDarkMode = isSystemInDarkTheme()
            val context = LocalContext.current
            
            // Update Management State
            val updateManager = remember { UpdateManager(context) }
            var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
            val scope = rememberCoroutineScope()
            
            // Keep track if user has manually toggled dark mode
            var hasUserToggledDarkMode by remember { mutableStateOf(false) }
            var isDarkMode by remember { mutableStateOf(systemDarkMode) }
            var isMetric by remember { mutableStateOf(value = true) }
            
            // Sync with system dark mode if user hasn't overridden it
            LaunchedEffect(systemDarkMode) {
                if (!hasUserToggledDarkMode) {
                    isDarkMode = systemDarkMode
                }
            }

            PulseFitTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    UpdateChecker(
                        context = context,
                        updateInfo = updateInfo,
                        onUpdateDismiss = { updateInfo = null },
                        onCheckForUpdate = { 
                            updateInfo = updateManager.checkForUpdate()
                        }
                    )
                    PulseFitApp(
                        isDarkMode = isDarkMode,
                        onDarkModeChange = { 
                            isDarkMode = it
                            hasUserToggledDarkMode = true
                        },
                        isMetric = isMetric,
                        onMetricChange = { isMetric = it },
                        userViewModel = userViewModel,
                        workoutViewModel = workoutViewModel,
                        socialViewModel = socialViewModel,
                        healthConnectManager = healthConnectManager,
                        onGoogleSignIn = { signInWithGoogle(userViewModel) },
                        onRequestHealthPermissions = { 
                            requestPermissions.launch(healthConnectManager.permissions)
                        },
                        onManualUpdateCheck = {
                            scope.launch {
                                updateInfo = updateManager.checkForUpdate()
                                if (updateInfo == null) {
                                    android.widget.Toast.makeText(context, "App is up to date", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    private fun signInWithGoogle(userViewModel: UserViewModel) {
        val credentialManager = CredentialManager.create(this)
        
        // TODO: REPLACE "YOUR_GOOGLE_WEB_CLIENT_ID" with your actual Web Client ID from Google Cloud Console
        val serverClientId = "904782209269-n115kq86uceqcfav4ukassnpil9tegaf.apps.googleusercontent.com"
        
        if (serverClientId == "904782209269-n115kq86uceqcfav4ukassnpil9tegaf.apps.googleusercontent.com") {
            android.widget.Toast.makeText(this, "Setup Required: Please set YOUR_GOOGLE_WEB_CLIENT_ID in MainActivity.kt", android.widget.Toast.LENGTH_LONG).show()
            return
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(this@MainActivity, request)
                val credential = result.credential
                if (credential is GoogleIdTokenCredential) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(credential.idToken, null)
                    auth.signInWithCredential(firebaseCredential).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            userViewModel.updateUserInfo(
                                name = credential.displayName ?: "Google User",
                                photoUrl = credential.profilePictureUri?.toString(),
                                userEmail = credential.id
                            )
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }
}

@Composable
fun PulseFitApp(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    isMetric: Boolean,
    onMetricChange: (Boolean) -> Unit,
    userViewModel: UserViewModel,
    workoutViewModel: WorkoutViewModel,
    socialViewModel: SocialViewModel,
    healthConnectManager: HealthConnectManager,
    onGoogleSignIn: () -> Unit,
    onRequestHealthPermissions: () -> Unit,
    onManualUpdateCheck: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("home") },
                onSignUpClick = { navController.navigate("signup") },
                onGoogleSignInClick = onGoogleSignIn
            )
        }
        composable("signup") {
            SignUpScreen(
                onSignUpSuccess = { navController.navigate("home") },
            ) { navController.popBackStack() }
        }
        composable("home") {
            HomeScreen(
                username = userViewModel.username,
                workoutViewModel = workoutViewModel,
                socialViewModel = socialViewModel,
                healthConnectManager = healthConnectManager,
                onSettingsClick = { navController.navigate("settings") },
            ) { navController.navigate("profile") }
        }
        composable("settings") {
            SettingsScreen(
                isDarkMode = isDarkMode,
                onDarkModeChange = onDarkModeChange,
                isMetric = isMetric,
                onMetricChange = onMetricChange,
                onBackClick = { navController.popBackStack() },
                onLogout = {
                    userViewModel.signOut()
                    workoutViewModel.clearData()
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onIntegrateHealthClick = onRequestHealthPermissions,
                healthConnectManager = healthConnectManager,
                onCheckForUpdatesClick = onManualUpdateCheck
            )
        }
        composable("profile") {
            ProfileScreen(
                userViewModel = userViewModel,
                isMetric = isMetric
            ) { navController.popBackStack() }
        }
    }
}
