package com.example.anthar_jalawatch.util

import com.example.anthar_jalawatch.data.models.BorewellStatus
import com.example.anthar_jalawatch.data.models.WaterAlert
import com.example.anthar_jalawatch.data.models.WaterCluster

object AlertGenerator {

    private const val DROP_ALERT_THRESHOLD_FT = 10.0

    fun generateAlerts(clusters: List<WaterCluster>): List<WaterAlert> {
        val alerts = mutableListOf<WaterAlert>()

        clusters.forEach { cluster ->
            if (cluster.seasonalDepthDropFt >= DROP_ALERT_THRESHOLD_FT) {
                alerts += WaterAlert(
                    id = "drop_${cluster.clusterKey}",
                    title = "Water table drop detected",
                    message = "The water table near ${formatClusterArea(cluster.center)} has dropped by " +
                        "${cluster.seasonalDepthDropFt.toInt()} ft this summer. Consider recharge activities.",
                    severity = BorewellStatus.CRITICAL
                )
            }
            when (cluster.stressLevel) {
                BorewellStatus.CRITICAL -> alerts += WaterAlert(
                    id = "critical_${cluster.clusterKey}",
                    title = "Critical water stress",
                    message = "Area near ${formatClusterArea(cluster.center)} shows critical stress " +
                        "(avg yield ${"%.1f".format(cluster.averageYieldInHr)} in/hr, " +
                        "${cluster.readingCount} wells logged).",
                    severity = BorewellStatus.CRITICAL
                )
                BorewellStatus.WARNING -> alerts += WaterAlert(
                    id = "warning_${cluster.clusterKey}",
                    title = "Moderate water stress",
                    message = "Area near ${formatClusterArea(cluster.center)} is under moderate stress. " +
                        "Monitor usage and plan recharge.",
                    severity = BorewellStatus.WARNING
                )
                BorewellStatus.OK -> Unit
            }
        }

        return alerts.distinctBy { it.id }
    }

    private fun formatClusterArea(center: com.google.android.gms.maps.model.LatLng): String =
        "${String.format("%.3f", center.latitude)}, ${String.format("%.3f", center.longitude)}"
}
