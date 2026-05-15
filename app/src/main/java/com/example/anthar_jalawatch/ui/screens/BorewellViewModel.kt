package com.example.anthar_jalawatch.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anthar_jalawatch.data.models.Borewell
import com.example.anthar_jalawatch.data.models.BorewellStatus
import com.example.anthar_jalawatch.data.models.WaterAlert
import com.example.anthar_jalawatch.data.models.WaterCluster
import com.example.anthar_jalawatch.data.repository.BorewellRepository
import com.example.anthar_jalawatch.util.AlertGenerator
import com.example.anthar_jalawatch.util.WaterClusterCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VillageStats(
    val avgYield: String = "—",
    val criticalCount: Int = 0,
    val totalLogged: Int = 0,
    val avgDepth: String = "—"
)

data class BorewellUiState(
    val isLoading: Boolean = false,
    val borewells: List<Borewell> = emptyList(),
    val clusters: List<WaterCluster> = emptyList(),
    val alerts: List<WaterAlert> = emptyList(),
    val stats: VillageStats = VillageStats(),
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false
)

class BorewellViewModel : ViewModel() {
    private val repository = BorewellRepository()

    private val _uiState = MutableStateFlow(BorewellUiState())
    val uiState: StateFlow<BorewellUiState> = _uiState.asStateFlow()

    init {
        loadBorewells()
    }

    fun loadBorewells() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.getBorewells()
            result.fold(
                onSuccess = { borewells -> updateFromBorewells(borewells) },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Failed to load readings"
                        )
                    }
                }
            )
        }
    }

    fun saveReading(
        depth: Double,
        yield: Double,
        yearOfDigging: Int,
        latitude: Double,
        longitude: Double
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, saveSuccess = false) }
            val result = repository.addBorewellReading(
                depth = depth,
                yield = yield,
                yearOfDigging = yearOfDigging,
                latitude = latitude,
                longitude = longitude
            )
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(saveSuccess = true) }
                    loadBorewells()
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Failed to save reading"
                        )
                    }
                }
            )
        }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun updateFromBorewells(borewells: List<Borewell>) {
        val clusters = WaterClusterCalculator.computeClusters(borewells)
        val alerts = AlertGenerator.generateAlerts(clusters)
        val criticalCount = borewells.count { it.status == BorewellStatus.CRITICAL.name }
        val avgYield = if (borewells.isNotEmpty()) {
            borewells.map { it.yield }.average()
        } else 0.0
        val avgDepth = if (borewells.isNotEmpty()) {
            borewells.map { it.depth }.average()
        } else 0.0

        _uiState.update {
            it.copy(
                isLoading = false,
                borewells = borewells,
                clusters = clusters,
                alerts = alerts,
                stats = VillageStats(
                    avgYield = if (borewells.isEmpty()) "—" else "${"%.1f".format(avgYield)} in/hr",
                    criticalCount = criticalCount,
                    totalLogged = borewells.size,
                    avgDepth = if (borewells.isEmpty()) "—" else "${"%.0f".format(avgDepth)} ft"
                ),
                errorMessage = null
            )
        }
    }
}
