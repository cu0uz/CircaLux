package com.example.circalux.util

import com.example.circalux.data.model.SolarSession
import com.example.circalux.data.model.SupplementEntry
import kotlin.math.*

object HealthCalculator {

    /**
     * Índice Cintura-Altura (WHtR) = cintura/altura
     */
    fun calculateWHtR(waistCm: Double, heightCm: Double): Double {
        if (heightCm <= 0) return 0.0
        return waistCm / heightCm
    }

    /**
     * % Grasa Corporal (Navy) 
     * Hombres: 495 / (1.0324 - 0.19077 * log10(waist - neck) + 0.15456 * log10(height)) - 450
     * Mujeres: 495 / (1.29579 - 0.35004 * log10(waist + hip - neck) + 0.22100 * log10(height)) - 450
     */
    fun calculateNavyBodyFat(
        waist: Double,
        neck: Double,
        hip: Double,
        height: Double,
        gender: String
    ): Double {
        return try {
            if (gender.lowercase() == "mujer") {
                val denom = 1.29579 - 0.35004 * log10(waist + hip - neck) + 0.22100 * log10(height)
                495.0 / denom - 450.0
            } else {
                val denom = 1.0324 - 0.19077 * log10(waist - neck) + 0.15456 * log10(height)
                495.0 / denom - 450.0
            }
        } catch (e: Exception) {
            0.0
        }
    }

    /**
     * GKI = (Glucose / 18) / Ketones
     * Assuming glucose is in mg/dL and ketones in mmol/L
     */
    fun calculateGKI(glucoseMgDl: Double, ketonesMmolL: Double): Double {
        if (ketonesMmolL <= 0) return 0.0
        return (glucoseMgDl / 18.0) / ketonesMmolL
    }

    /**
     * Proyecta el nivel de Vitamina D teniendo en cuenta el decaimiento.
     * Vida media aproximada de 21 días (0.033 decaimiento diario).
     */
    fun projectVitaminDLevel(
        currentLevel: Double,
        days: Int,
        dailyIntakeIU: Int = 0,
        weightKg: Double = 70.0
    ): Double {
        var level = currentLevel
        val decayRate = 0.033 // ln(2)/21
        
        // 1000 IU increases blood level by approx 1-2 ng/mL depending on weight
        // Simple approximation: 1000 IU -> +1 ng/mL for a 70kg adult
        val conversionFactor = 1000.0 * (weightKg / 70.0)

        for (i in 1..days) {
            level *= (1.0 - decayRate)
            level += dailyIntakeIU / conversionFactor
        }
        return level
    }
}
