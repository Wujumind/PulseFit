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
import androidx.compose.ui.platform.LocalContext

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.lifecycleScope
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.GraphRequest
import android.content.Intent
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callbackManager = CallbackManager.Factory.create()
        enableEdgeToEdge()
        setContent {
            val userViewModel: UserViewModel = viewModel()
            val systemDarkMode = isSystemInDarkTheme()
            var isDarkMode by remember { mutableStateOf(systemDarkMode) }
            var isMetric by remember { mutableStateOf(value = true) }
            
            PulseFitTheme(darkTheme = isDarkMode) {
                UpdateChecker(LocalContext.current)
                PulseFitApp(
                    isDarkMode = isDarkMode,
                    onDarkModeChange = { isDarkMode = it },
                    isMetric = isMetric,
                    onMetricChange = { isMetric = it },
                    userViewModel = userViewModel,
                    onGoogleSignIn = { signInWithGoogle(userViewModel) },
                    onFacebookSignIn = { signInWithFacebook(userViewModel) }
                )
            }
        }
    }

    private fun signInWithGoogle(userViewModel: UserViewModel) {
        val credentialManager = CredentialManager.create(this)
        
        // TODO: REPLACE "YOUR_GOOGLE_WEB_CLIENT_ID" with your actual Web Client ID from Google Cloud Console
        // 1. Go to https://console.cloud.google.com/
        // 2. Create an OAuth 2.0 Client ID for "Web application"
        // 3. Add your debug SHA-1 fingerprint to the Android Client ID
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("YOUR_GOOGLE_WEB_CLIENT_ID")
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(this@MainActivity, request)
                val credential = result.credential
                if (credential is GoogleIdTokenCredential) {
                    userViewModel.updateUserInfo(
                        name = credential.displayName ?: "Google User",
                        photoUrl = credential.profilePictureUri?.toString(),
                        userEmail = credential.id
                    )
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun signInWithFacebook(userViewModel: UserViewModel) {
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                val request = GraphRequest.newMeRequest(result.accessToken) { obj, _ ->
                    val name = obj?.getString("name") ?: "Facebook User"
                    val email = obj?.getString("email") ?: ""
                    val id = obj?.getString("id")
                    val photoUrl = "https://graph.facebook.com/$id/picture?type=large"
                    userViewModel.updateUserInfo(name, photoUrl, email)
                }
                val parameters = Bundle()
                parameters.putString("fields", "id,name,email")
                request.parameters = parameters
                request.executeAsync()
            }
            override fun onCancel() {}
            override fun onError(error: FacebookException) {}
        })
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("email", "public_profile"))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}

@Composable
fun PulseFitApp(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    isMetric: Boolean,
    onMetricChange: (Boolean) -> Unit,
    userViewModel: UserViewModel,
    onGoogleSignIn: () -> Unit,
    onFacebookSignIn: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginClick = { navController.navigate("home") },
                onSignUpClick = { navController.navigate("signup") },
                onGoogleSignInClick = onGoogleSignIn,
                onFacebookSignInClick = onFacebookSignIn
            )
        }
        composable("signup") {
            SignUpScreen(
                onSignUpClick = { navController.navigate("home") },
            ) { navController.popBackStack() }
        }
        composable("home") {
            HomeScreen(
                username = userViewModel.username,
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
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            ) { /* Handle Health Integration */ }
        }
        composable("profile") {
            ProfileScreen(
                userViewModel = userViewModel,
                isMetric = isMetric
            ) { navController.popBackStack() }
        }
    }
}
