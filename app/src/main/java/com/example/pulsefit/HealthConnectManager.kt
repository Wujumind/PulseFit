package com.example.pulsefit

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import android.os.Build
import android.content.Intent
import android.net.Uri
import java.time.Instant
import java.time.ZonedDateTime

class HealthConnectManager(private val context: Context) {
    private val healthConnectClient by lazy { 
        if (isHealthConnectAvailable()) HealthConnectClient.getOrCreate(context) else null 
    }

    val permissions = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(RestingHeartRateRecord::class),
        HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class)
    )

    fun isHealthConnectAvailable(): Boolean {
        return HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    }

    fun openHealthConnectStore() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("market://details?id=com.google.android.apps.healthdata")
            setPackage("com.android.vending")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openSpecificHealthApp(packageName: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            context.startActivity(intent)
        } else {
            // If app not installed, open Play Store
            val storeIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(storeIntent)
        }
    }

    fun isAppInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun hasAllPermissions(): Boolean {
        val client = healthConnectClient ?: return false
        val granted = client.permissionController.getGrantedPermissions()
        return granted.containsAll(permissions)
    }

    suspend fun readRestingHeartRate(): Long? {
        val client = healthConnectClient ?: return null
        if (!hasAllPermissions()) return null
        
        val startTime = Instant.now().minus(java.time.Duration.ofDays(7))
        val endTime = Instant.now()

        try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = RestingHeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            return response.records.maxByOrNull { it.time }?.beatsPerMinute
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun readCurrentHeartRate(): Long? {
        val client = healthConnectClient ?: return null
        if (!hasAllPermissions()) return null

        // Look back 1 hour for the most recent heart rate sample
        val startTime = Instant.now().minus(java.time.Duration.ofHours(1))
        val endTime = Instant.now()

        try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            // HeartRateRecord contains a list of samples
            return response.records.lastOrNull()?.samples?.lastOrNull()?.beatsPerMinute
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun readHRV(): Double? {
        val client = healthConnectClient ?: return null
        if (!hasAllPermissions()) return null

        val startTime = Instant.now().minus(java.time.Duration.ofDays(7))
        val endTime = Instant.now()

        try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateVariabilityRmssdRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            return response.records.maxByOrNull { it.time }?.heartRateVariabilityMillis
        } catch (e: Exception) {
            return null
        }
    }
}
