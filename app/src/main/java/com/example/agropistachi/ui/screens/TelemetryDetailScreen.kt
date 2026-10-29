package com.example.agropistachi.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelemetryDetailScreen(
    cuartelId: String,
    viewModel: AgroAlertaViewModel,
    onBack: () -> Unit
) {
    val cuartelesStatus by viewModel.cuartelesStatus.collectAsState()
    val status = cuartelesStatus.find { it.cuartel.id == cuartelId }
    val userRole by viewModel.currentUserRole.collectAsState()

    if (status == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Cuartel no encontrado")
        }
        return
    }

    val telemetry = status.telemetry
    val actuator = status.actuator

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(status.cuartel.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info Card
            item {
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Cultivo: ${status.cuartel.cropType}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Superficie: ${status.cuartel.areaHectares} Hectáreas · Agrícola FRUTIS (Buin)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Sensors Grid
            item {
                Text(
                    text = "Lecturas de Sensores en Terreno",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SensorMetricCard(
                        title = "Humedad Suelo",
                        value = "${telemetry?.soilHumidityPercent ?: 0.0}%",
                        icon = Icons.Default.WaterDrop,
                        color = Color(0xFF0288D1),
                        modifier = Modifier.weight(1f)
                    )
                    SensorMetricCard(
                        title = "Temperatura",
                        value = "${telemetry?.temperatureCelsius ?: 0.0} °C",
                        icon = Icons.Default.Thermostat,
                        color = if ((telemetry?.temperatureCelsius ?: 0.0) <= 0) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SensorMetricCard(
                        title = "Humedad Relativa",
                        value = "${telemetry?.relativeHumidityPercent ?: 0.0}%",
                        icon = Icons.Default.Opacity,
                        color = Color(0xFF1976D2),
                        modifier = Modifier.weight(1f)
                    )
                    SensorMetricCard(
                        title = "Radiación Solar",
                        value = "${telemetry?.solarRadiationWM2 ?: 0.0} W/m²",
                        icon = Icons.Default.LightMode,
                        color = Color(0xFFF57C00),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SensorMetricCard(
                        title = "Nivel Estanque",
                        value = "${telemetry?.tankLevelPercent ?: 0.0}%",
                        icon = Icons.Default.Water,
                        color = Color(0xFF00796B),
                        modifier = Modifier.weight(1f)
                    )
                    SensorMetricCard(
                        title = "Caudal de Riego",
                        value = "${telemetry?.flowRateLps ?: 0.0} L/s",
                        icon = Icons.Default.Speed,
                        color = Color(0xFF5D4037),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Actuators Control
            item {
                Text(
                    text = "Control Directo de Actuadores en Terreno",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                ActuatorControlCard(
                    title = "Electroválvulas de Riego",
                    description = "Apertura / cierre de válvulas del cuartel $cuartelId",
                    active = actuator?.electrovalvulaOpen == true,
                    onToggle = {
                        viewModel.toggleActuator(cuartelId, "electrovalvula", actuator?.electrovalvulaOpen == true)
                    }
                )
            }

            item {
                ActuatorControlCard(
                    title = "Bomba de Impulsión",
                    description = "Motor de impulsión de matriz de agua",
                    active = actuator?.bombaImpulsionActive == true,
                    onToggle = {
                        viewModel.toggleActuator(cuartelId, "bomba", actuator?.bombaImpulsionActive == true)
                    }
                )
            }

            item {
                ActuatorControlCard(
                    title = "Sistema Anti-Heladas",
                    description = "Aspersores de protección contra congelamiento",
                    active = actuator?.sistemaAntiHeladasActive == true,
                    isAlertStyle = true,
                    onToggle = {
                        viewModel.toggleActuator(cuartelId, "antihelada", actuator?.sistemaAntiHeladasActive == true)
                    }
                )
            }
        }
    }
}

@Composable
fun SensorMetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = color)
            }
        }
    }
}

@Composable
fun ActuatorControlCard(
    title: String,
    description: String,
    active: Boolean,
    isAlertStyle: Boolean = false,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (active && isAlertStyle) Color(0xFFFFF3E0) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(text = description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = if (active) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (active) "ESTADO: ACTIVADO" else "ESTADO: DESACTIVADO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (active) Color(0xFF2E7D32) else Color(0xFFC62828),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Switch(
                checked = active,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = if (isAlertStyle) Color(0xFFE65100) else Color(0xFF2E7D32)
                )
            )
        }
    }
}
