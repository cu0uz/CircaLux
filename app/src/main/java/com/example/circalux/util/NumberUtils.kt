package com.example.circalux.util

object NumberUtils {
    /**
     * Parses a string to Double, supporting both '.' and ',' as decimal separators.
     */
    fun parseDouble(value: String): Double {
        if (value.isEmpty()) return 0.0
        val sanitized = value.replace(',', '.')
        return sanitized.toDoubleOrNull() ?: 0.0
    }

    /**
     * Parses a string to Int, handling empty strings.
     */
    fun parseInt(value: String): Int {
        if (value.isEmpty()) return 0
        return value.toIntOrNull() ?: 0
    }
    
    /**
     * Formats a Double for display in input fields (optional, if needed).
     */
    fun formatInput(value: Double): String {
        if (value == 0.0) return ""
        return value.toString().replace(".0", "")
    }
}
