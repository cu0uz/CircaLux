package com.example.circalux.util

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.*

/**
 * Utility for solar calculations and Vitamin D synthesis estimation.
 */
object SolarCalculator {

    /**
     * Estimates Vitamin D synthesis in IU.
     * Formula: IU = (240 * UVI * TimeInMinutes * SkinAreaPercentage) / MED
     */
    fun estimateVitaminD(
        uvi: Double,
        minutes: Double,
        skinExposurePercentage: Double, // e.g., 0.1 for 10%
        skinType: Int // Fitzpatrick skin type 1-6
    ): Double {
        if (uvi <= 0) return 0.0
        
        val med = getMEDForSkinType(skinType)
        val iu = (240.0 * uvi * minutes * (skinExposurePercentage * 100.0)) / med
        return max(0.0, iu)
    }

    /**
     * Gets the Minimal Erythemal Dose (MED) for a skin type.
     */
    fun getMEDForSkinType(skinType: Int): Int {
        return when (skinType) {
            1 -> 200
            2 -> 250
            3 -> 350
            4 -> 500
            5 -> 750
            6 -> 1200
            else -> 250 // Default to Type 2
        }
    }

    /**
     * Calculates the sun's elevation angle (altitude) in degrees.
     * Uses UTC time for consistent global results.
     */
    fun calculateSunElevation(
        latitude: Double,
        longitude: Double,
        calendar: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    ): Double {
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val hourUTC = calendar.get(Calendar.HOUR_OF_DAY) + 
                     calendar.get(Calendar.MINUTE) / 60.0 + 
                     calendar.get(Calendar.SECOND) / 3600.0
        
        // 1. Solar Declination
        val declination = 23.45 * sin(Math.toRadians(360.0 / 365.0 * (dayOfYear - 81)))
        
        // 2. Equation of Time (EoT)
        val b = Math.toRadians(360.0 / 365.0 * (dayOfYear - 81))
        val eot = 9.87 * sin(2.0 * b) - 7.53 * cos(b) - 1.5 * sin(b)
        
        // 3. True Solar Time
        val solarTime = hourUTC + (longitude / 15.0) + (eot / 60.0)
        
        // 4. Hour Angle (HRA)
        val hra = 15.0 * (solarTime - 12.0)
        
        val latRad = Math.toRadians(latitude)
        val decRad = Math.toRadians(declination)
        val hraRad = Math.toRadians(hra)
        
        val sinElevation = sin(latRad) * sin(decRad) + cos(latRad) * cos(decRad) * cos(hraRad)
        val elevation = Math.toDegrees(asin(sinElevation))
        
        return elevation
    }

    /**
     * Calculates the sun's azimuth angle in degrees (0 = North, 90 = East).
     */
    fun calculateSunAzimuth(
        latitude: Double,
        longitude: Double,
        calendar: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    ): Double {
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val hourUTC = calendar.get(Calendar.HOUR_OF_DAY) + 
                     calendar.get(Calendar.MINUTE) / 60.0 + 
                     calendar.get(Calendar.SECOND) / 3600.0
        
        val declination = 23.45 * sin(Math.toRadians(360.0 / 365.0 * (dayOfYear - 81)))
        val b = Math.toRadians(360.0 / 365.0 * (dayOfYear - 81))
        val eot = 9.87 * sin(2.0 * b) - 7.53 * cos(b) - 1.5 * sin(b)
        val solarTime = hourUTC + (longitude / 15.0) + (eot / 60.0)
        val hra = 15.0 * (solarTime - 12.0)
        
        val latRad = Math.toRadians(latitude)
        val decRad = Math.toRadians(declination)
        val hraRad = Math.toRadians(hra)
        val elevation = calculateSunElevation(latitude, longitude, calendar)
        val elevRad = Math.toRadians(elevation)
        
        var cosAzimuth = (sin(decRad) * cos(latRad) - cos(decRad) * sin(latRad) * cos(hraRad)) / cos(elevRad)
        cosAzimuth = cosAzimuth.coerceIn(-1.0, 1.0)
        
        val azimuth = Math.toDegrees(acos(cosAzimuth))
        
        // Correct for solar time (AM/PM)
        return if (hra > 0) 360.0 - azimuth else azimuth
    }
}
