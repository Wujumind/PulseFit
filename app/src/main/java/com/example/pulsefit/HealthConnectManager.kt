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

    suspend fun hasAllPermissions(): Boolean {
        val client = healthConnectClient ?: return false
        val granted = client.permissionController.getGrantedPermissions()
        return granted.containsAll(permissions)
    }

    suspend fun readRestingHeartRate(): Long? {
        val client = healthConnectClient ?: return null
        if (!hasAllPermissions()) return null
        
        // Look back 7 days to get the most recent data
        val startTime = Instant.now().minus(java.time.Duration.ofDays(7))
        val endTime = Instant.now()

        try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = RestingHeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            // Return the most recent record
            return response.records.maxByOrNull { it.time }?.beatsPerMinute
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
