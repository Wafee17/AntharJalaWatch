package com.example.anthar_jalawatch.util

import com.example.anthar_jalawatch.data.models.Borewell
import com.example.anthar_jalawatch.data.models.BorewellStatus
import com.example.anthar_jalawatch.data.models.WaterCluster
import com.google.android.gms.maps.model.LatLng
import java.util.concurrent.TimeUnit

object WaterClusterCalculator {

    private const val CLUSTER_DECIMALS = 3

    fun clusterKey(latitude: Double, longitude: Double): String {
        val lat = String.format("%.3f", latitude).toDouble()
        val lng = String.format("%.3f", longitude).toDouble()
        return "$lat,$lng"
    }

    fun computeClusters(borewells: List<Borewell>): List<WaterCluster> {
        if (borewells.isEmpty()) return emptyList()

        val grouped = borewells.groupBy { clusterKey(it.latitude, it.longitude) }

        return grouped.map { (key, readings) ->
            val avgDepth = readings.map { it.depth }.average()
            val avgYield = readings.map { it.yield }.average()
            val center = readings.first().let { LatLng(it.latitude, it.longitude) }
            val drop = computeSeasonalDepthDrop(readings)

            WaterCluster(
                clusterKey = key,
                center = center,
                averageDepthFt = avgDepth,
                averageYieldInHr = avgYield,
                readingCount = readings.size,
                stressLevel = stressFromYield(avgYield),
                seasonalDepthDropFt = drop
            )
        }
    }

    fun computeSeasonalDepthDrop(readings: List<Borewell>): Double {
        if (readings.size < 2) return 0.0

        val nowMs = System.currentTimeMillis()
        val thirtyDaysMs = TimeUnit.DAYS.toMillis(30)
        val ninetyDaysMs = TimeUnit.DAYS.toMillis(90)

        val recent = readings.filter {
            val age = nowMs - it.timestamp.toDate().time
            age in 0..thirtyDaysMs
        }
        val previous = readings.filter {
            val age = nowMs - it.timestamp.toDate().time
            age in (thirtyDaysMs + 1)..ninetyDaysMs
        }

        if (recent.isEmpty() || previous.isEmpty()) return 0.0

        val recentAvg = recent.map { it.depth }.average()
        val previousAvg = previous.map { it.depth }.average()
        return (recentAvg - previousAvg).coerceAtLeast(0.0)
    }

    fun stressFromYield(avgYield: Double): BorewellStatus = when {
        avgYield < 2.0 -> BorewellStatus.CRITICAL
        avgYield < 3.5 -> BorewellStatus.WARNING
        else -> BorewellStatus.OK
    }

    fun heatmapIntensity(borewell: Borewell): Double {
        val depthFactor = (borewell.depth / 500.0).coerceIn(0.1, 1.0)
        val yieldFactor = when {
            borewell.yield < 2.0 -> 1.0
            borewell.yield < 3.5 -> 0.6
            else -> 0.3
        }
        return (depthFactor * 0.6 + yieldFactor * 0.4).coerceIn(0.2, 1.0)
    }
}
