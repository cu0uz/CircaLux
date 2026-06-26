package com.example.circalux.data.db

import androidx.room.*
import com.example.circalux.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert
    suspend fun insertSolarSession(session: SolarSession)

    @Query("SELECT * FROM solar_sessions ORDER BY timestamp DESC")
    fun getAllSolarSessions(): Flow<List<SolarSession>>

    @Delete
    suspend fun deleteSolarSession(session: SolarSession)

    @Insert
    suspend fun insertRedLightSession(session: RedLightSession)

    @Query("SELECT * FROM red_light_sessions ORDER BY timestamp DESC")
    fun getAllRedLightSessions(): Flow<List<RedLightSession>>

    @Delete
    suspend fun deleteRedLightSession(session: RedLightSession)

    // Health Metrics
    @Insert
    suspend fun insertHealthMetric(metric: HealthMetric)

    @Query("SELECT * FROM health_metrics ORDER BY timestamp DESC")
    fun getAllHealthMetrics(): Flow<List<HealthMetric>>

    @Delete
    suspend fun deleteHealthMetric(metric: HealthMetric)

    // Body Measurements
    @Insert
    suspend fun insertBodyMeasurement(measurement: BodyMeasurement)

    @Query("SELECT * FROM body_measurements ORDER BY timestamp DESC")
    fun getAllBodyMeasurements(): Flow<List<BodyMeasurement>>

    @Delete
    suspend fun deleteBodyMeasurement(measurement: BodyMeasurement)

    // Supplements
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplementEntry(entry: SupplementEntry)

    @Query("SELECT * FROM supplement_entries ORDER BY timestamp DESC")
    fun getAllSupplementEntries(): Flow<List<SupplementEntry>>

    @Query("SELECT * FROM supplement_entries WHERE timestamp = :date")
    suspend fun getSupplementByDate(date: Long): SupplementEntry?

    @Delete
    suspend fun deleteSupplementEntry(entry: SupplementEntry)

    @Query("SELECT * FROM solar_sessions WHERE timestamp >= :start AND timestamp <= :end")
    fun getSolarSessionsInRange(start: Long, end: Long): Flow<List<SolarSession>>

    @Query("DELETE FROM solar_sessions")
    suspend fun clearSolarSessions()

    @Query("DELETE FROM red_light_sessions")
    suspend fun clearRedLightSessions()

    @Query("DELETE FROM health_metrics")
    suspend fun clearHealthMetrics()

    @Query("DELETE FROM body_measurements")
    suspend fun clearBodyMeasurements()

    @Query("DELETE FROM supplement_entries")
    suspend fun clearSupplementEntries()

    // Profile
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: UserProfile)

    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getProfile(): Flow<UserProfile?>
}

@Database(
    entities = [
        SolarSession::class, 
        RedLightSession::class, 
        HealthMetric::class, 
        BodyMeasurement::class, 
        SupplementEntry::class, 
        UserProfile::class
    ], 
    version = 4
)
abstract class SessionDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
}
