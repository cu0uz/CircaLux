package com.example.circalux.util

import android.content.Context
import android.net.Uri
import com.example.circalux.data.db.SessionDao
import com.example.circalux.data.model.*
import kotlinx.coroutines.flow.first
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

/**
 * Robust Backup and Restore system for CircaLux.
 * Handles export/import of all app data via CSV with detailed logging.
 */
class BackupManager(private val context: Context, private val dao: SessionDao) {

    private val TAG = "BackupManager"

    suspend fun exportToCsv(uri: Uri): Boolean {
        CircaLogger.i("Starting full data export to: $uri", TAG)
        return try {
            val solar = dao.getAllSolarSessions().first()
            val redLight = dao.getAllRedLightSessions().first()
            val health = dao.getAllHealthMetrics().first()
            val body = dao.getAllBodyMeasurements().first()
            val supplements = dao.getAllSupplementEntries().first()
            val profile = dao.getProfile().first()

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write("CIRCALUX_BACKUP_VERSION,2\n")
                    
                    writer.write("---PROFILE---\n")
                    profile?.let { p ->
                        writer.write("${p.nickname},${p.age},${p.gender},${p.height},${p.weight},${p.skinType},${p.bloodDLevel},${p.lastAnalyticDate},${p.takesSupplements},${p.supplementAmount},${p.supplementFrequency},${p.supplementStartDate},${p.isActivated},${p.userId}\n")
                    }
                    
                    writer.write("---SOLAR---\n")
                    solar.forEach { writer.write("${it.timestamp},${it.durationMinutes},${it.vitaminDGenerated},${it.uviAvg},${it.locationName},${it.skinExposurePercentage},${it.skinType},${it.weatherCondition}\n") }
                    
                    writer.write("---REDLIGHT---\n")
                    redLight.forEach { writer.write("${it.timestamp},${it.durationMinutes},${it.lampType},${it.notes},${it.lampPowerWatts},${it.distanceCm}\n") }
                    
                    writer.write("---HEALTH---\n")
                    health.forEach { writer.write("${it.timestamp},${it.glucose},${it.ketones},${it.gki}\n") }
                    
                    writer.write("---BODY---\n")
                    body.forEach { writer.write("${it.timestamp},${it.neck},${it.waist},${it.hip},${it.chest},${it.biceps},${it.thigh},${it.weight},${it.whtr},${it.bodyFatNavy}\n") }
                    
                    writer.write("---SUPPLEMENTS---\n")
                    supplements.forEach { writer.write("${it.timestamp},${it.amountUI},${it.isRecurring},${it.isAutomated}\n") }
                    
                    writer.flush()
                }
            }
            CircaLogger.i("Export successful. Total records: Solar=${solar.size}, RedLight=${redLight.size}, Health=${health.size}", TAG)
            true
        } catch (e: Exception) {
            CircaLogger.e("Export failed critically", e, TAG)
            false
        }
    }

    suspend fun importFromCsv(uri: Uri): Boolean {
        CircaLogger.i("Starting full data import from: $uri", TAG)
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var currentSection = ""
                    val lines = reader.readLines()
                    
                    if (lines.isEmpty() || !lines[0].startsWith("CIRCALUX_BACKUP")) {
                        CircaLogger.e("Import failed: Invalid backup file format or empty file", null, TAG)
                        return false
                    }

                    // Clear database before restore to avoid duplicates and conflicts
                    CircaLogger.d("Cleaning database before import", TAG)
                    dao.clearSolarSessions()
                    dao.clearRedLightSessions()
                    dao.clearHealthMetrics()
                    dao.clearBodyMeasurements()
                    dao.clearSupplementEntries()

                    lines.forEachIndexed { index, rawLine ->
                        val line = rawLine.trim()
                        if (line.isEmpty()) return@forEachIndexed
                        
                        if (line.startsWith("---") && line.endsWith("---")) {
                            currentSection = line
                            CircaLogger.d("Parsing section: $currentSection", TAG)
                            return@forEachIndexed
                        }

                        val parts = line.split(",")
                        try {
                            when (currentSection) {
                                "---PROFILE---" -> {
                                    if (parts.size >= 12) {
                                        dao.saveProfile(UserProfile(
                                            nickname = parts[0],
                                            age = parts[1].toInt(),
                                            gender = parts[2],
                                            height = parts[3].toDouble(),
                                            weight = parts[4].toDouble(),
                                            skinType = parts[5].toInt(),
                                            bloodDLevel = parts[6].toDouble(),
                                            lastAnalyticDate = parts[7].toLong(),
                                            takesSupplements = parts[8].toBoolean(),
                                            supplementAmount = parts[9].toInt(),
                                            supplementFrequency = parts[10],
                                            supplementStartDate = parts[11].toLong(),
                                            isActivated = parts.getOrNull(12)?.toBoolean() ?: false,
                                            userId = parts.getOrNull(13) ?: ""
                                        ))
                                    }
                                }
                                "---SOLAR---" -> {
                                    if (parts.size >= 8) {
                                        dao.insertSolarSession(SolarSession(
                                            timestamp = parts[0].toLong(),
                                            durationMinutes = parts[1].toInt(),
                                            vitaminDGenerated = parts[2].toDouble(),
                                            uviAvg = parts[3].toDouble(),
                                            locationName = parts[4],
                                            skinExposurePercentage = parts[5].toDouble(),
                                            skinType = parts[6].toInt(),
                                            weatherCondition = parts[7]
                                        ))
                                    }
                                }
                                "---REDLIGHT---" -> {
                                    if (parts.size >= 4) {
                                        dao.insertRedLightSession(RedLightSession(
                                            timestamp = parts[0].toLong(),
                                            durationMinutes = parts[1].toInt(),
                                            lampType = parts[2],
                                            notes = parts[3],
                                            lampPowerWatts = parts.getOrNull(4)?.toInt() ?: 100,
                                            distanceCm = parts.getOrNull(5)?.toInt() ?: 30
                                        ))
                                    }
                                }
                                "---HEALTH---" -> {
                                    if (parts.size >= 4) {
                                        dao.insertHealthMetric(HealthMetric(
                                            timestamp = parts[0].toLong(),
                                            glucose = parts[1].toDouble(),
                                            ketones = parts[2].toDouble(),
                                            gki = parts[3].toDouble()
                                        ))
                                    }
                                }
                                "---BODY---" -> {
                                    if (parts.size >= 10) {
                                        dao.insertBodyMeasurement(BodyMeasurement(
                                            timestamp = parts[0].toLong(),
                                            neck = parts[1].toDouble(),
                                            waist = parts[2].toDouble(),
                                            hip = parts[3].toDouble(),
                                            chest = parts[4].toDouble(),
                                            biceps = parts[5].toDouble(),
                                            thigh = parts[6].toDouble(),
                                            weight = parts[7].toDouble(),
                                            whtr = parts[8].toDouble(),
                                            bodyFatNavy = parts[9].toDouble()
                                        ))
                                    }
                                }
                                "---SUPPLEMENTS---" -> {
                                    if (parts.size >= 4) {
                                        dao.insertSupplementEntry(SupplementEntry(
                                            timestamp = parts[0].toLong(),
                                            amountUI = parts[1].toInt(),
                                            isRecurring = parts[2].toBoolean(),
                                            isAutomated = parts[3].toBoolean()
                                        ))
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            CircaLogger.e("Skip line $index due to parse error: $line", e, TAG)
                        }
                    }
                }
            }
            CircaLogger.i("Import finished successfully", TAG)
            true
        } catch (e: Exception) {
            CircaLogger.e("Import failed critically", e, TAG)
            false
        }
    }
}
