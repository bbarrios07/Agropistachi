package com.example.agropistachi.data.repository

import android.content.Context
import com.example.agropistachi.data.local.ActuatorEntity
import com.example.agropistachi.data.local.AgroAlertaDatabase
import com.example.agropistachi.data.local.AlertEntity
import com.example.agropistachi.data.local.EventLogEntity
import com.example.agropistachi.data.local.TelemetryEntity
import com.example.agropistachi.data.model.Cuartel
import com.example.agropistachi.data.model.UserRole
import com.example.agropistachi.data.remote.ActuatorControlRequest
import com.example.agropistachi.data.remote.AgroAlertaApiService
import com.example.agropistachi.data.remote.RetrofitClient
import com.example.agropistachi.notification.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class AgroAlertaRepository(private val context: Context) {

    private val db = AgroAlertaDatabase.getInstance(context)
    private val telemetryDao = db.telemetryDao()
    private val actuatorDao = db.actuatorDao()
    private val alertDao = db.alertDao()
    private val eventLogDao = db.eventLogDao()
    private val apiService: AgroAlertaApiService = RetrofitClient.apiService
    private val notificationHelper = NotificationHelper(context)

    // Predefined cuarteles for Agrícola FRUTIS (Buin)
    val cuarteles = listOf(
        Cuartel("C1", "Cuartel 1 - Paltos Hass", "Paltos", 12.5, isCritical = true),
        Cuartel("C2", "Cuartel 2 - Cerezos Lapins", "Cerezos", 8.0, isCritical = false),
        Cuartel("C3", "Cuartel 3 - Uvas Thompson", "Uvas de Mesa", 15.2, isCritical = false),
        Cuartel("C4", "Cuartel 4 - Cítricos Lemon", "Limoneros", 10.0, isCritical = true)
    )

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    val allTelemetry: Flow<List<TelemetryEntity>> = telemetryDao.getAllTelemetry()
    val allActuators: Flow<List<ActuatorEntity>> = actuatorDao.getAllActuators()
    val activeAlerts: Flow<List<AlertEntity>> = alertDao.getActiveAlerts()
    val allAlerts: Flow<List<AlertEntity>> = alertDao.getAllAlerts()
    val eventLogs: Flow<List<EventLogEntity>> = eventLogDao.getAllLogs()

    fun setOnlineMode(online: Boolean) {
        _isOnline.value = online
    }

    suspend fun syncWithBackend(): Result<String> = withContext(Dispatchers.IO) {
        _isSyncing.value = true
        try {
            if (!_isOnline.value) {
                _isSyncing.value = false
                return@withContext Result.failure(Exception("Modo Offline activo: mostrando datos cacheados en Room local."))
            }

            val response = apiService.getLatestTelemetryAndAlerts()
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!

                // Save to Room cache
                val telemetryEntities = data.telemetry.map { dto ->
                    TelemetryEntity(
                        cuartelId = dto.cuartelId,
                        soilHumidityPercent = dto.soilHumidityPercent,
                        temperatureCelsius = dto.temperatureCelsius,
                        relativeHumidityPercent = dto.relativeHumidityPercent,
                        solarRadiationWM2 = dto.solarRadiationWM2,
                        tankLevelPercent = dto.tankLevelPercent,
                        flowRateLps = dto.flowRateLps,
                        lastUpdatedTimestamp = dto.timestamp
                    )
                }
                telemetryDao.insertOrUpdateAll(telemetryEntities)

                val actuatorEntities = data.actuators.map { dto ->
                    ActuatorEntity(
                        cuartelId = dto.cuartelId,
                        electrovalvulaOpen = dto.electrovalvulaOpen,
                        bombaImpulsionActive = dto.bombaImpulsionActive,
                        sistemaAntiHeladasActive = dto.sistemaAntiHeladasActive,
                        lastUpdatedTimestamp = dto.timestamp
                    )
                }
                actuatorDao.insertOrUpdateAll(actuatorEntities)

                // Save alerts
                data.alerts.forEach { alertDto ->
                    val alertEntity = AlertEntity(
                        cuartelId = alertDto.cuartelId,
                        cuartelName = alertDto.cuartelName,
                        type = alertDto.type,
                        severity = alertDto.severity,
                        message = alertDto.message,
                        timestamp = alertDto.timestamp,
                        isAcknowledged = false
                    )
                    alertDao.insertAlert(alertEntity)
                }

                logEvent(UserRole.SUPERVISOR.displayName, "Sincronización SCADA exitosa desde servidor central Santiago")
                _isSyncing.value = false
                Result.success("Sincronización completada. Datos actualizados desde SCADA.")
            } else {
                _isSyncing.value = false
                Result.failure(Exception("Error de respuesta del servidor SCADA (${response.code()})"))
            }
        } catch (e: Exception) {
            _isSyncing.value = false
            Result.failure(e)
        }
    }

    suspend fun toggleActuator(
        cuartelId: String,
        actuatorType: String,
        currentState: Boolean,
        userRole: UserRole
    ): Boolean = withContext(Dispatchers.IO) {
        val newState = !currentState
        val currentActuator = actuatorDao.getActuatorForCuartel(cuartelId) ?: ActuatorEntity(
            cuartelId = cuartelId,
            electrovalvulaOpen = false,
            bombaImpulsionActive = false,
            sistemaAntiHeladasActive = false
        )

        val updatedEntity = when (actuatorType) {
            "electrovalvula" -> currentActuator.copy(electrovalvulaOpen = newState)
            "bomba" -> currentActuator.copy(bombaImpulsionActive = newState)
            "antihelada" -> currentActuator.copy(sistemaAntiHeladasActive = newState)
            else -> currentActuator
        }

        // Save locally to Room immediately (Offline-first approach)
        actuatorDao.insertOrUpdate(updatedEntity)

        // Try API REST sync if online
        if (_isOnline.value) {
            try {
                apiService.controlActuator(
                    ActuatorControlRequest(
                        cuartelId = cuartelId,
                        actuatorType = actuatorType,
                        newState = newState,
                        userRole = userRole.displayName
                    )
                )
            } catch (_: Exception) {}
        }

        val actionDesc = "Cambio estado actuador '$actuatorType' a ${if (newState) "ACTIVADO" else "DESACTIVADO"} en $cuartelId (${if (_isOnline.value) "vía API SCADA" else "Guardado en caché local"})"
        logEvent(userRole.displayName, actionDesc, cuartelId)
        true
    }

    suspend fun acknowledgeAlert(alert: AlertEntity, userRole: UserRole) = withContext(Dispatchers.IO) {
        val updated = alert.copy(isAcknowledged = true, resolvedBy = userRole.displayName)
        alertDao.updateAlert(updated)
        logEvent(userRole.displayName, "Atendió y registró acción sobre la alerta: ${alert.message}", alert.cuartelId)
    }

    suspend fun triggerSimulatedCriticalAlert(type: String, cuartelId: String, cuartelName: String) = withContext(Dispatchers.IO) {
        val (title, msg) = when (type) {
            "HELADA" -> Pair("❄️ ALERTA CRÍTICA: Helada en $cuartelName", "Temperatura cayó a -2.1°C. Se requiere activación del sistema anti-heladas inmediatamente.")
            "FUGA" -> Pair("💧 ALERTA CRÍTICA: Fuga o Falla de Riego", "Anomalía severa de flujo en $cuartelName. Caudal detectado 0.5 L/s con bomba encendida.")
            else -> Pair("⚠️ ALERTA CRÍTICA: Nivel Estanque en $cuartelName", "Nivel de agua del estanque acumulador por debajo del 10%.")
        }

        val alertEntity = AlertEntity(
            cuartelId = cuartelId,
            cuartelName = cuartelName,
            type = type,
            severity = "CRITICAL",
            message = msg,
            timestamp = System.currentTimeMillis()
        )
        alertDao.insertAlert(alertEntity)

        // Trigger system push notification
        notificationHelper.showCriticalNotification(title, msg)

        logEvent("SISTEMA TELEMETRÍA", "Evento crítico detectado y notificado: $title", cuartelId)
    }

    suspend fun logEvent(userRole: String, actionText: String, cuartelId: String = "GENERAL") = withContext(Dispatchers.IO) {
        eventLogDao.insertLog(
            EventLogEntity(
                userRole = userRole,
                actionText = actionText,
                cuartelId = cuartelId,
                timestamp = System.currentTimeMillis()
            )
        )
    }
}
