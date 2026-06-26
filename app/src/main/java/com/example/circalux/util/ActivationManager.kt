package com.example.circalux.util

import android.content.Context
import java.util.UUID

object ActivationManager {
    
    fun getOrCreateUserId(context: Context): String {
        val prefs = context.getSharedPreferences("activation_prefs", Context.MODE_PRIVATE)
        var userId = prefs.getString("user_id", null)
        if (userId == null) {
            userId = UUID.randomUUID().toString().take(8).uppercase()
            prefs.edit().putString("user_id", userId).apply()
        }
        return userId
    }

    fun isCodeValid(userId: String, code: String): Boolean {
        if (!code.contains("-")) return false
        val parts = code.split("-")
        if (parts.size != 2) return false
        
        val providedId = parts[0]
        val checksum = parts[1]
        
        if (providedId != userId) return false
        
        val calculatedChecksum = calculateChecksum(userId)
        return checksum == calculatedChecksum
    }

    fun calculateChecksum(userId: String): String {
        val sum = userId.sumOf { it.code }
        val check = sum % 99
        return check.toString().padStart(2, '0')
    }
}
