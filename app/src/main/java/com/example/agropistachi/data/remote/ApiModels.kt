package com.example.agropistachi.data.remote

data class TelemetryDto(
    val cuartelId: String,
    val soilHumidityPercent: Double,
    val temperatureCelsius: Double,
    val relativeHumidityPercent: Double,
    val solarRadiationWM2: Double,
    val tankLevelPercent: Double,
    val flowRateLps: Double,
    val timestamp: Long
)

data class ActuatorDto(
    val cuartelId: String,
    val electrovalvulaOpen: Boolean,
    val bombaImpulsionActive: Boolean,
    val sistemaAntiHeladasActive: Boolean,
    val timestamp: Long
)

data class AlertDto(
    val id: Long,
    val cuartelId: String,
    val cuartelName: String,
    val type: String,
    val severity: String,
    val message: String,
    val timestamp: Long
)

data class SyncDataResponse(
    val status: String,
    val farmName: String,
    val location: String,
    val telemetry: List<TelemetryDto>,
    val actuators: List<ActuatorDto>,
    val alerts: List<AlertDto>
)

data class ActuatorControlRequest(
    val cuartelId: String,
    val actuatorType: String, // "electrovalvula", "bomba", "antihelada"
    val newState: Boolean,
    val userRole: String
)

data class ActuatorControlResponse(
    val success: Boolean,
    val message: String,
    val timestamp: Long
)
