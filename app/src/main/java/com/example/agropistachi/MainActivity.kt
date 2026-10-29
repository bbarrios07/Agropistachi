package com.example.agropistachi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.example.agropistachi.ui.AgroAlertaViewModel
import com.example.agropistachi.ui.components.ConnectionPill
import com.example.agropistachi.ui.screens.AlertsScreen
import com.example.agropistachi.ui.screens.DashboardScreen
import com.example.agropistachi.ui.screens.EventHistoryScreen
import com.example.agropistachi.ui.screens.SettingsScreen
import com.example.agropistachi.ui.screens.TelemetryDetailScreen
import com.example.agropistachi.ui.theme.AgropistachiTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AgroAlertaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AgropistachiTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: AgroAlertaViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val selectedCuartelId by viewModel.selectedCuartelId.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val activeAlerts by viewModel.activeAlerts.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    if (selectedCuartelId != null) {
        TelemetryDetailScreen(
            cuartelId = selectedCuartelId!!,
            viewModel = viewModel,
            onBack = { viewModel.selectCuartel(null) }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "AgroAlerta · ASTI",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        ConnectionPill(
                            isOnline = isOnline,
                            isSyncing = isSyncing,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF1B5E20),
                        titleContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(imageVector = Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Dashboard") }
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            if (activeAlerts.isNotEmpty()) {
                                BadgedBox(
                                    badge = { Badge { Text("${activeAlerts.size}") } }
                                ) {
                                    Icon(imageVector = Icons.Default.Notifications, contentDescription = "Alertas")
                                }
                            } else {
                                Icon(imageVector = Icons.Default.Notifications, contentDescription = "Alertas")
                            }
                        },
                        label = { Text("Alertas") }
                    )

                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(imageVector = Icons.Default.History, contentDescription = "Historial") },
                        label = { Text("Historial") }
                    )

                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Ajustes") },
                        label = { Text("Ajustes") }
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> DashboardScreen(
                        viewModel = viewModel,
                        onSelectCuartel = { id -> viewModel.selectCuartel(id) }
                    )
                    1 -> AlertsScreen(viewModel = viewModel)
                    2 -> EventHistoryScreen(viewModel = viewModel)
                    3 -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
