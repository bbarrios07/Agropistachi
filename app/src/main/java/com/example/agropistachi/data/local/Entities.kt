package com.example.agropistachi.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "telemetry_cache")
data class TelemetryEntity(
    @PrimaryKey val cuartelId: String,
    val soilHumidityPercent: Double,      // Humedad de suelo (%)
    val temperatureCelsius: Double,       // Temperatura (°C)
    val relativeHumidityPercent: Double,  // Humedad relativa (%)
    val solarRadiationWM2: Double,        // Radiación solar (W/m²)
    val tankLevelPercent: Double,         // Nivel de estanque (%)
    val flowRateLps: Double,              // Caudal (L/s)
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "actuator_cache")
data class ActuatorEntity(
    @PrimaryKey val cuartelId: String,
    val electrovalvulaOpen: Boolean,      // Electroválvulas
    val bombaImpulsionActive: Boolean,    // Bombas de impulsión
    val sistemaAntiHeladasActive: Boolean,// Sistema anti-heladas
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cuartelId: String,
    val cuartelName: String,
    val type: String,               // HELADA, FUGA_OBSTRUCCION, NIVEL_CRITICO_AGUA
    val severity: String,           // CRITICAL, WARNING, INFO
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isAcknowledged: Boolean = false,
    val resolvedBy: String? = null
)

@Entity(tableName = "event_logs")
data class EventLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userRole: String,
    val actionText: String,
    val cuartelId: String,
    val timestamp: Long = System.currentTimeMillis()
)
