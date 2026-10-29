package com.example.agropistachi.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TelemetryDao {
    @Query("SELECT * FROM telemetry_cache")
    fun getAllTelemetry(): Flow<List<TelemetryEntity>>

    @Query("SELECT * FROM telemetry_cache WHERE cuartelId = :cuartelId")
    suspend fun getTelemetryForCuartel(cuartelId: String): TelemetryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(telemetryList: List<TelemetryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(telemetry: TelemetryEntity)
}

@Dao
interface ActuatorDao {
    @Query("SELECT * FROM actuator_cache")
    fun getAllActuators(): Flow<List<ActuatorEntity>>

    @Query("SELECT * FROM actuator_cache WHERE cuartelId = :cuartelId")
    suspend fun getActuatorForCuartel(cuartelId: String): ActuatorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(actuators: List<ActuatorEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(actuator: ActuatorEntity)
}

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts WHERE isAcknowledged = 0 ORDER BY timestamp DESC")
    fun getActiveAlerts(): Flow<List<AlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity): Long

    @Update
    suspend fun updateAlert(alert: AlertEntity)

    @Query("DELETE FROM alerts")
    suspend fun clearAlerts()
}

@Dao
interface EventLogDao {
    @Query("SELECT * FROM event_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<EventLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: EventLogEntity)
}
