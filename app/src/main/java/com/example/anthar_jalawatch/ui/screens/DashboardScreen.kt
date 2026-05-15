package com.example.anthar_jalawatch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.anthar_jalawatch.R
import com.example.anthar_jalawatch.data.models.Borewell
import com.example.anthar_jalawatch.data.models.BorewellClusterItem
import com.example.anthar_jalawatch.ui.components.WaterStressHeatmap
import com.example.anthar_jalawatch.ui.theme.colorError
import com.example.anthar_jalawatch.ui.theme.colorSuccess
import com.example.anthar_jalawatch.ui.theme.colorWarning
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: BorewellViewModel,
    onNavigateToLog: () -> Unit,
    onNavigateToRecharge: () -> Unit,
    onNavigateToAlerts: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadBorewells()
    }

    val defaultLocation = LatLng(14.6196, 74.8354)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    LaunchedEffect(uiState.borewells) {
        if (uiState.borewells.isNotEmpty()) {
            val first = uiState.borewells.first()
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(first.latitude, first.longitude),
                13f
            )
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Map") },
                    selected = true,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = "Log") },
                    label = { Text("Log") },
                    selected = false,
                    onClick = onNavigateToLog
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.WaterDrop, contentDescription = "Recharge") },
                    label = { Text("Recharge") },
                    selected = false,
                    onClick = onNavigateToRecharge
                )
                NavigationBarItem(
                    icon = {
                        BadgedBox(
                            badge = {
                                if (uiState.alerts.isNotEmpty()) {
                                    Badge { Text("${uiState.alerts.size}") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Alerts")
                        }
                    },
                    label = { Text("Alerts") },
                    selected = false,
                    onClick = onNavigateToAlerts
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToLog,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log Borewell")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            if (uiState.isLoading && uiState.borewells.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.58f)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(isMyLocationEnabled = false),
                        uiSettings = MapUiSettings(zoomControlsEnabled = true)
                    ) {
                        if (uiState.borewells.isNotEmpty()) {
                            WaterStressHeatmap(borewells = uiState.borewells)
                        }

                        val clusterItems = uiState.borewells.map { toClusterItem(it) }
                        if (clusterItems.isNotEmpty()) {
                            Clustering(
                                items = clusterItems,
                                onClusterClick = { false },
                                onClusterItemClick = { false }
                            )
                        }
                    }

                    HeatmapLegend(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.42f)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Village Overview",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.privacy_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            StatCard(title = "Avg Yield", value = uiState.stats.avgYield)
                        }
                        item {
                            StatCard(
                                title = "Critical Wells",
                                value = uiState.stats.criticalCount.toString(),
                                isWarning = uiState.stats.criticalCount > 0
                            )
                        }
                        item {
                            StatCard(title = "Total Logged", value = uiState.stats.totalLogged.toString())
                        }
                        item {
                            StatCard(title = "Avg Depth", value = uiState.stats.avgDepth)
                        }
                    }

                    if (uiState.clusters.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Cluster averages (~100 m areas)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        uiState.clusters.take(3).forEach { cluster ->
                            Text(
                                text = "• ${cluster.readingCount} wells — avg depth ${"%.0f".format(cluster.averageDepthFt)} ft, " +
                                    "yield ${"%.1f".format(cluster.averageYieldInHr)} in/hr",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun toClusterItem(borewell: Borewell): BorewellClusterItem =
    BorewellClusterItem(
        borewellId = borewell.id,
        itemPosition = LatLng(borewell.latitude, borewell.longitude),
        itemTitle = "Depth: ${borewell.depth.toInt()} ft",
        itemSnippet = "Yield: ${borewell.yield} in/hr · ${borewell.status}",
        itemZIndex = 0f,
        yieldValue = borewell.yield,
        statusLevel = borewell.status
    )

@Composable
private fun HeatmapLegend(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text("Stress", style = MaterialTheme.typography.labelSmall)
            LegendRow(color = colorSuccess, label = "Healthy")
            LegendRow(color = colorWarning, label = "Moderate")
            LegendRow(color = colorError, label = "Critical")
        }
    }
}

@Composable
private fun LegendRow(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun StatCard(title: String, value: String, isWarning: Boolean = false) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .height(96.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = if (isWarning) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
