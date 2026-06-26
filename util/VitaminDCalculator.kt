package com.example.circalux.util

import kotlin.math.exp
import kotlin.math.ln

/**
 * Utility to calculate Vitamin D biological decay and projections.
 * Half-life of 25(OH)D is approximately 15 days.
 */
object VitaminDCalculator {
    private const val HALF_LIFE_DAYS = 15.0
    private val DECAY_CONSTANT = ln(2.0) / HALF_LIFE_DAYS

    /**
     * Estimates current Vitamin D level based on a previous measurement and time passed.
     */
    fun estimateCurrentLevel(initialLevel: Double, daysPassed: Int): Double {
        return initialLevel * exp(-DECAY_CONSTANT * daysPassed)
    }

    /**
     * Calculates the increase in blood levels (ng/mL) from IU intake.
     * Rule of thumb: 1000 IU increases blood level by ~5-10 ng/mL over time, 
     * but for a single session we can estimate a smaller immediate contribution.
     * More realistic: 1000 IU ≈ 1 ng/mL increase in steady state.
     */
    fun iuToNgMl(iu: Double): Double {
        return iu / 1000.0 // Simplified model: 1000 IU = 1 ng/mL
    }
}
