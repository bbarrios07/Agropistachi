package com.example.agropistachi.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agropistachi.data.local.ActuatorEntity
import com.example.agropistachi.data.local.AlertEntity
import com.example.agropistachi.data.local.EventLogEntity
import com.example.agropistachi.data.local.TelemetryEntity
import com.example.agropistachi.data.model.Cuartel
import com.example.agropistachi.data.model.UserRole
import com.example.agropistachi.data.repository.AgroAlertaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CuartelStatus(
    val cuartel: Cuartel,
    val telemetry: TelemetryEntity?,
    val actuator: ActuatorEntity?,
    val activeAlertsCount: Int
)

class AgroAlertaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AgroAlertaRepository(application)

    private val _currentUserRole = MutableStateFlow(UserRole.SUPERVISOR)
    val currentUserRole: StateFlow<UserRole> = _currentUserRole.asStateFlow()

    private val _selectedCuartelId = MutableStateFlow<String?>(null)
    val selectedCuartelId: StateFlow<String?> = _selectedCuartelId.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    val isOnline: StateFlow<Boolean> = repository.isOnline
    val isSyncing: StateFlow<Boolean> = repository.isSyncing
    val activeAlerts: StateFlow<List<AlertEntity>> = repository.activeAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allAlerts: StateFlow<List<AlertEntity>> = repository.allAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val eventLogs: StateFlow<List<EventLogEntity>> = repository.eventLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cuartelesStatus: StateFlow<List<CuartelStatus>> = combine(
        repository.allTelemetry,
        repository.allActuators,
        repository.activeAlerts
    ) { telemetries, actuators, alerts ->
        val telemetryMap = telemetries.associateBy { it.cuartelId }
        val actuatorMap = actuators.associateBy { it.cuartelId }
        val alertCountMap = alerts.groupBy { it.cuartelId }.mapValues { it.value.size }

        repository.cuarteles.map { cuartel ->
            CuartelStatus(
                cuartel = cuartel,
                telemetry = telemetryMap[cuartel.id],
                actuator = actuatorMap[cuartel.id],
                activeAlertsCount = alertCountMap[cuartel.id] ?: 0
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Initial sync to populate Room database with initial readings & alerts
        syncData()
    }

    fun setRole(role: UserRole) {
        _currentUserRole.value = role
        viewModelScope.launch {
            repository.logEvent(role.displayName, "Cambió perfil de usuario a ${role.displayName}")
        }
    }

    fun setOnlineMode(online: Boolean) {
        repository.setOnlineMode(online)
        _userMessage.value = if (online) "Conectado a Red (Online)" else "Modo Terreno Intermitente (Offline)"
    }

    fun selectCuartel(cuartelId: String?) {
        _selectedCuartelId.value = cuartelId
    }

    fun syncData() {
        viewModelScope.launch {
            val result = repository.syncWithBackend()
            result.onSuccess { msg ->
                _userMessage.value = msg
            }.onFailure { err ->
                _userMessage.value = err.message ?: "Error de sincronización"
            }
        }
    }

    fun toggleActuator(cuartelId: String, actuatorType: String, currentState: Boolean) {
        viewModelScope.launch {
            repository.toggleActuator(cuartelId, actuatorType, currentState, _currentUserRole.value)
            _userMessage.value = "Actuador $actuatorType actualizado"
        }
    }

    fun acknowledgeAlert(alert: AlertEntity) {
        viewModelScope.launch {
            repository.acknowledgeAlert(alert, _currentUserRole.value)
            _userMessage.value = "Alerta atendida por ${_currentUserRole.value.displayName}"
        }
    }

    fun simulateCriticalAlert(type: String, cuartelId: String, cuartelName: String) {
        viewModelScope.launch {
            repository.triggerSimulatedCriticalAlert(type, cuartelId, cuartelName)
            _userMessage.value = "¡Alerta Crítica simulada e impulsada al sistema!"
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
