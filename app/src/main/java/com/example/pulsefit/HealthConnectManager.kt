package com.example.pulsefit

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
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
            val storeIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(storeIntent)
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

        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = RestingHeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records.maxByOrNull { it.time }?.beatsPerMinute
        } catch (e: Exception) {
            null
        }
    }

    suspend fun readCurrentHeartRate(): Long? {
        val client = healthConnectClient ?: return null
        if (!hasAllPermissions()) return null

        val startTime = Instant.now().minus(java.time.Duration.ofHours(1))
        val endTime = Instant.now()

        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records.lastOrNull()?.samples?.lastOrNull()?.beatsPerMinute
        } catch (e: Exception) {
            null
        }
    }

    suspend fun readHRV(): Double? {
        val client = healthConnectClient ?: return null
        if (!hasAllPermissions()) return null

        val startTime = Instant.now().minus(java.time.Duration.ofDays(7))
        val endTime = Instant.now()

        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateVariabilityRmssdRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records.maxByOrNull { it.time }?.heartRateVariabilityMillis
        } catch (e: Exception) {
            null
        }
    }

    suspend fun readHeartRateHistory(): List<Pair<Instant, Long>> {
        val client = healthConnectClient ?: return emptyList()
        if (!hasAllPermissions()) return emptyList()

        val startTime = Instant.now().minus(java.time.Duration.ofDays(1))
        val endTime = Instant.now()

        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records.flatMap { record ->
                record.samples.map { sample -> sample.time to sample.beatsPerMinute }
            }.sortedBy { it.first }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun readRestingHeartRateHistory(): List<Pair<Instant, Long>> {
        val client = healthConnectClient ?: return emptyList()
        if (!hasAllPermissions()) return emptyList()

        val startTime = Instant.now().minus(java.time.Duration.ofDays(30))
        val endTime = Instant.now()

        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = RestingHeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records.map { it.time to it.beatsPerMinute }.sortedBy { it.first }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun readHRVHistory(): List<Pair<Instant, Double>> {
        val client = healthConnectClient ?: return emptyList()
        if (!hasAllPermissions()) return emptyList()

        val startTime = Instant.now().minus(java.time.Duration.ofDays(30))
        val endTime = Instant.now()

        return try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateVariabilityRmssdRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            response.records.map { it.time to it.heartRateVariabilityMillis }.sortedBy { it.first }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
