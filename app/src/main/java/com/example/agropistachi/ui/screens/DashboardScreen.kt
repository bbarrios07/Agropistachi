package com.example.agropistachi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDamage
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agropistachi.ui.AgroAlertaViewModel
import com.example.agropistachi.ui.CuartelStatus
import com.example.agropistachi.ui.components.MetricGaugeCard
import com.example.agropistachi.ui.components.RoleBadge

@Composable
fun DashboardScreen(
    viewModel: AgroAlertaViewModel,
    onSelectCuartel: (String) -> Unit
) {
    val cuartelesStatus by viewModel.cuartelesStatus.collectAsState()
    val userRole by viewModel.currentUserRole.collectAsState()
    val activeAlerts by viewModel.activeAlerts.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Executive / Field Banner
        item {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Agrícola FRUTIS",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Sector Buin · Telemetría en Terreno",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        RoleBadge(role = userRole)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SummaryMetricPill("4 Cuarteles", "Supervisados", Color(0xFF2E7D32))
                        SummaryMetricPill("${activeAlerts.size} Alertas", "Pendientes", if (activeAlerts.isNotEmpty()) Color(0xFFC62828) else Color(0xFF1565C0))
                        SummaryMetricPill("Helada -1.5°C", "Mínima Temp", Color(0xFF0288D1))
                    }
                }
            }
        }

        // Active critical alerts banner if any
        if (activeAlerts.isNotEmpty()) {
            item {
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF5350)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AcUnit,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "⚠️ Alertas Críticas Activas (${activeAlerts.size})",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB71C1C)
                            )
                            Text(
                                text = activeAlerts.firstOrNull()?.message ?: "Revisar sección de alertas",
                                fontSize = 12.sp,
                                color = Color(0xFFC62828)
                            )
                        }
                    }
                }
            }
        }

        // Section Title
        item {
            Text(
                text = "Estado de Cuarteles y Telemetría",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // List of Cuarteles Cards
        items(cuartelesStatus, key = { it.cuartel.id }) { item ->
            CuartelCard(
                status = item,
                onClick = { onSelectCuartel(item.cuartel.id) }
            )
        }
    }
}

@Composable
fun SummaryMetricPill(label: String, sublabel: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = color
            )
            Text(
                text = sublabel,
                fontSize = 10.sp,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun CuartelCard(
    status: CuartelStatus,
    onClick: () -> Unit
) {
    val telemetry = status.telemetry
    val actuator = status.actuator
    val temp = telemetry?.temperatureCelsius ?: 0.0
    val isFrostRisk = temp <= 0.0

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Name, Crop, Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = status.cuartel.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${status.cuartel.cropType} · ${status.cuartel.areaHectares} Has",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (status.activeAlertsCount > 0) {
                        Surface(
                            color = Color(0xFFD32F2F),
                            shape = CircleShape
                        ) {
                            Text(
                                text = "! ${status.activeAlertsCount}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Ver Detalle"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Grid of Gauges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricGaugeCard(
                    title = "Humedad Suelo",
                    valueString = "${telemetry?.soilHumidityPercent?.toInt() ?: 0}",
                    progress = ((telemetry?.soilHumidityPercent ?: 0.0) / 100.0).toFloat(),
                    unit = "%",
                    barColor = Color(0xFF0288D1),
                    modifier = Modifier.weight(1f)
                )

                MetricGaugeCard(
                    title = "Temperatura",
                    valueString = String.format("%.1f", temp),
                    progress = (((temp + 5) / 40.0)).toFloat(),
                    unit = "°C",
                    barColor = if (isFrostRisk) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )

                MetricGaugeCard(
                    title = "Estanque Agua",
                    valueString = "${telemetry?.tankLevelPercent?.toInt() ?: 0}",
                    progress = ((telemetry?.tankLevelPercent ?: 0.0) / 100.0).toFloat(),
                    unit = "%",
                    barColor = if ((telemetry?.tankLevelPercent ?: 0.0) < 20) Color(0xFFE65100) else Color(0xFF00796B),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Actuators summary chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ActuatorStatusChip("Válvula", actuator?.electrovalvulaOpen == true)
                    Spacer(modifier = Modifier.width(6.dp))
                    ActuatorStatusChip("Bomba", actuator?.bombaImpulsionActive == true)
                    Spacer(modifier = Modifier.width(6.dp))
                    ActuatorStatusChip("Anti-Helada", actuator?.sistemaAntiHeladasActive == true)
                }

                Text(
                    text = "Caudal: ${telemetry?.flowRateLps ?: 0.0} L/s",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ActuatorStatusChip(name: String, active: Boolean) {
    Surface(
        color = if (active) Color(0xFFE8F5E9) else Color(0xFFECEFF1),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (active) Color(0xFF2E7D32) else Color(0xFF78909C))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = name,
                fontSize = 10.sp,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                color = if (active) Color(0xFF1B5E20) else Color(0xFF455A64)
            )
        }
    }
}
