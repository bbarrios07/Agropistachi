package com.example.agropistachi.data.model

enum class UserRole(val displayName: String, val description: String) {
    SUPERVISOR("Supervisor Agrícola", "Monitoreo general y reacción inmediata ante alertas"),
    TECNICO("Técnico Mantenimiento", "Supervisión técnica de sensores y actuadores en terreno"),
    GERENCIA("Gerencia Explotación", "Visión ejecutiva del predio y métricas consolidadas"),
    ASTI_TEAM("Equipo Técnico ASTI", "Soporte, configuración IoT y telemetría remota")
}

data class Cuartel(
    val id: String,
    val name: String,
    val cropType: String,
    val areaHectares: Double,
    val isCritical: Boolean = false
)

enum class AlertType(val title: String) {
    HELADA("Alerta de Helada"),
    FUGA_OBSTRUCCION("Fuga / Obstrucción de Riego"),
    NIVEL_CRITICO_AGUA("Nivel Crítico de Estanque"),
    ANOMALIA_ELECTRICA("Anomalía Eléctrica en Bomba")
}

enum class AlertSeverity {
    CRITICAL, WARNING, INFO
}

enum class ActuatorStatus {
    ENCENDIDO, APAGADO, AUTOMATICO, ERROR
}
