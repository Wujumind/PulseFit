package com.example.pulsefit

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

@Serializable
data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val downloadUrl: String,
    val releaseNotes: String,
)

class UpdateManager(private val context: Context) {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    private val updateUrl = "https://raw.githubusercontent.com/Wujumind/PulseFit/main/docs/version.json"

    suspend fun checkForUpdate(): UpdateInfo? {
        android.util.Log.d("UpdateManager", "Checking for updates at: $updateUrl")
        return withContext(Dispatchers.IO) {
            try {
                // Add a timestamp to bypass any potential caching
                val urlWithCacheBuster = "$updateUrl?t=${System.currentTimeMillis()}"
                val request = Request.Builder().url(urlWithCacheBuster).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext null
                    val body = response.body?.string() ?: return@withContext null
                    val info = json.decodeFromString<UpdateInfo>(body)
                    
                    val packageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
                    } else {
                        @Suppress("DEPRECATION")
                        context.packageManager.getPackageInfo(context.packageName, 0)
                    }
                    val currentVersion = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        packageInfo.longVersionCode.toInt()
                    } else {
                        @Suppress("DEPRECATION")
                        packageInfo.versionCode
                    }
                    
                    // Priority check for faster updates
                    android.util.Log.d("UpdateManager", "Current version: $currentVersion, Cloud version: ${info.versionCode}")
                    if (info.versionCode > currentVersion) info else null
                }
            } catch (e: Exception) {
                android.util.Log.e("UpdateManager", "Update check failed", e)
                null
            }
        }
    }
}

@Composable
fun UpdateChecker(
    context: Context,
    updateInfo: UpdateInfo?,
    onUpdateDismiss: () -> Unit,
    onCheckForUpdate: suspend () -> Unit
) {
    // Check for update immediately on launch
    LaunchedEffect(Unit) {
        onCheckForUpdate()
    }

    updateInfo?.let { info ->
        AlertDialog(
            onDismissRequest = onUpdateDismiss,
            title = { Text("Update Available") },
            text = { Text("A newer version (${info.versionName}) of PulseFit is available. \n\nWhat's new:\n${info.releaseNotes}") },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(info.downloadUrl))
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                    }
                ) {
                    Text("Update Now")
                }
            },
            dismissButton = {
                TextButton(onClick = onUpdateDismiss) {
                    Text("Later")
                }
            }
        )
    }
}
