package com.example.circalux.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Aviso Solar"
        val message = intent.getStringExtra("message") ?: "Recordatorio"
        
        val notificationHelper = NotificationHelper(context)
        notificationHelper.sendSunNotification(title, message)
    }
}
