package com.example.circalux.util

import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Custom logger for CircaLux to provide detailed debugging information.
 * It also records logs to a local file for later analysis/sending.
 */
object CircaLogger {
    private const val TAG = "CircaLuxDebug"
    private var logFile: File? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    fun init(filesDir: File) {
        logFile = File(filesDir, "circalux_logs.txt")
        if (!logFile!!.exists()) {
            logFile!!.createNewFile()
        }
    }

    fun d(message: String, context: String? = null) {
        val fullMessage = formatMessage("DEBUG", message, context)
        Log.d(TAG, fullMessage)
        writeToFile(fullMessage)
    }

    fun i(message: String, context: String? = null) {
        val fullMessage = formatMessage("INFO", message, context)
        Log.i(TAG, fullMessage)
        writeToFile(fullMessage)
    }

    fun w(message: String, context: String? = null) {
        val fullMessage = formatMessage("WARNING", message, context)
        Log.w(TAG, fullMessage)
        writeToFile(fullMessage)
    }

    fun e(message: String, throwable: Throwable? = null, context: String? = null) {
        val fullMessage = formatMessage("ERROR", message, context)
        Log.e(TAG, fullMessage, throwable)
        writeToFile("$fullMessage - ${throwable?.message}")
        
        throwable?.let {
            val details = "Exception Details: ${it.javaClass.simpleName}: ${it.message}\n${Log.getStackTraceString(it)}"
            writeToFile(details)
        }
    }

    private fun formatMessage(level: String, message: String, context: String?): String {
        val timestamp = dateFormat.format(Date())
        val contextPart = if (context != null) " [$context]" else ""
        return "$timestamp [$level]$contextPart $message"
    }

    private fun writeToFile(text: String) {
        try {
            logFile?.let {
                val writer = FileWriter(it, true)
                writer.append(text).append("\n")
                writer.close()
                Log.v(TAG, "Log written to file: ${it.absolutePath}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write to log file", e)
        }
    }

    fun getLogs(): String {
        return try {
            logFile?.readText() ?: "No hay logs disponibles."
        } catch (e: Exception) {
            "Error al leer logs: ${e.message}"
        }
    }

    fun clearLogs() {
        try {
            logFile?.writeText("")
        } catch (e: Exception) {}
    }
}
