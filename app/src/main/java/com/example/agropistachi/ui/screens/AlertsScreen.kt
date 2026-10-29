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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDamage
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.agropistachi.data.local.AlertEntity
import com.example.agropistachi.ui.AgroAlertaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AlertsScreen(viewModel: AgroAlertaViewModel) {
    val activeAlerts by viewModel.activeAlerts.collectAsState()
    val allAlerts by viewModel.allAlerts.collectAsState()
    val userRole by viewModel.currentUserRole.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Simulation Panel (for academic MVP demonstration and on-device test)
        item {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Simulador de Eventos Críticos de Terreno",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    Text(
                        text = "Genera alertas en vivo para probar notificaciones push y respuesta en terreno:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.simulateCriticalAlert("HELADA", "C1", "Cuartel 1 - Paltos Hass")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("❄️ Helada", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.simulateCriticalAlert("FUGA", "C4", "Cuartel 4 - Cítricos Lemon")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("💧 Fuga", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.simulateCriticalAlert("ESTANQUE", "C2", "Cuartel 2 - Cerezos Lapins")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🛢️ Estanque", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Active Alerts Section
        item {
            Text(
                text = "Alertas Activas Pendientes de Atención (${activeAlerts.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (activeAlerts.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "No hay alertas críticas pendientes. Todo el predio opera bajo norma.",
                            color = Color(0xFF1B5E20),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            items(activeAlerts, key = { it.id }) { alert ->
                AlertItemCard(
                    alert = alert,
                    isActive = true,
                    onAcknowledge = { viewModel.acknowledgeAlert(alert) }
                )
            }
        }

        // All/Resolved Alerts Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Historial General de Alertas y Notificaciones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        val resolvedAlerts = allAlerts.filter { it.isAcknowledged }
        if (resolvedAlerts.isEmpty()) {
            item {
                Text(
                    text = "Aún no hay registro de alertas atendidas.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(resolvedAlerts, key = { it.id }) { alert ->
                AlertItemCard(
                    alert = alert,
                    isActive = false,
                    onAcknowledge = {}
                )
            }
        }
    }
}

@Composable
fun AlertItemCard(
    alert: AlertEntity,
    isActive: Boolean,
    onAcknowledge: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    val formattedTime = dateFormat.format(Date(alert.timestamp))

    val (cardColor, icon, iconColor) = when (alert.type) {
        "HELADA" -> Triple(Color(0xFFE1F5FE), Icons.Default.AcUnit, Color(0xFF0288D1))
        "FUGA", "FUGA_OBSTRUCCION" -> Triple(Color(0xFFFFEBEE), Icons.Default.WaterDamage, Color(0xFFD32F2F))
        else -> Triple(Color(0xFFFFF3E0), Icons.Default.Warning, Color(0xFFE65100))
    }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) cardColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(iconColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = alert.cuartelName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = formattedTime,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = if (isActive) iconColor else Color(0xFF78909C),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (isActive) "ACTIVA" else "ATENDIDA",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = alert.message,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!alert.resolvedBy.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Atendida por: ${alert.resolvedBy}",
                    fontSize = 11.sp,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Medium
                )
            }

            if (isActive) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onAcknowledge,
                    colors = ButtonDefaults.buttonColors(containerColor = iconColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Registrar Acción en Terreno")
                }
            }
        }
    }
}
