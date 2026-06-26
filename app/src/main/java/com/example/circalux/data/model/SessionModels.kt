package com.example.circalux.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "solar_sessions")
data class SolarSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val durationMinutes: Int,
    val vitaminDGenerated: Double,
    val uviAvg: Double,
    val locationName: String,
    val skinExposurePercentage: Double,
    val skinType: Int,
    val weatherCondition: String = ""
)

@Entity(tableName = "red_light_sessions")
data class RedLightSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val durationMinutes: Int,
    val lampType: String,
    val notes: String = "",
    val lampPowerWatts: Int = 100,
    val distanceCm: Int = 30
)

@Entity(tableName = "health_metrics")
data class HealthMetric(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val glucose: Double,
    val ketones: Double,
    val gki: Double
)

@Entity(tableName = "body_measurements")
data class BodyMeasurement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val neck: Double,
    val waist: Double,
    val hip: Double,
    val chest: Double,
    val biceps: Double,
    val thigh: Double,
    val weight: Double,
    val whtr: Double,
    val bodyFatNavy: Double
)

@Entity(tableName = "supplement_entries")
data class SupplementEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val amountUI: Int,
    val isRecurring: Boolean = false,
    val isAutomated: Boolean = false
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Single user app
    val nickname: String = "",
    val age: Int = 0,
    val gender: String = "Otro", // "Hombre", "Mujer", "Otro"
    val height: Double = 0.0,
    val weight: Double = 0.0,
    val skinType: Int = 2,
    val bloodDLevel: Double = 0.0,
    val lastAnalyticDate: Long = 0L,
    val takesSupplements: Boolean = false,
    val supplementAmount: Int = 0,
    val supplementFrequency: String = "Diario",
    val supplementStartDate: Long = 0L,
    val isActivated: Boolean = false,
    val userId: String = "",
    val sunriseSunsetAlarmsEnabled: Boolean = false
)
