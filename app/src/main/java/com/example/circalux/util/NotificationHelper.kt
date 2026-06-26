package com.example.circalux.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.circalux.R

class NotificationHelper(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ALARMS = "circalux_alarms"
        const val CHANNEL_SUN = "circalux_sun"
        const val CHANNEL_UVI = "circalux_uvi"
    }

    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alarmChannel = NotificationChannel(
                CHANNEL_ALARMS,
                "Alarmas de Sesión",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones críticas de tiempo de exposición superado"
                enableLights(true)
                enableVibration(true)
            }

            val sunChannel = NotificationChannel(
                CHANNEL_SUN,
                "Avisos Solares",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Recordatorios de amanecer y atardecer"
            }

            val uviChannel = NotificationChannel(
                CHANNEL_UVI,
                "Ventana Vitamina D",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Avisos de apertura/cierre de generación de Vitamina D"
            }

            notificationManager.createNotificationChannel(alarmChannel)
            notificationManager.createNotificationChannel(sunChannel)
            notificationManager.createNotificationChannel(uviChannel)
        }
    }

    fun sendAlarm(title: String, message: String) {
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val builder = NotificationCompat.Builder(context, CHANNEL_ALARMS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    fun sendSunNotification(title: String, message: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_SUN)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    fun sendUviNotification(title: String, message: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_UVI)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
