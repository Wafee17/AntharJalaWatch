package com.example.anthar_jalawatch.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.anthar_jalawatch.data.models.Borewell
import com.example.anthar_jalawatch.util.WaterClusterCalculator
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng
import kotlinx.coroutines.awaitCancellation

@Composable
fun WaterStressHeatmap(borewells: List<Borewell>) {
    val weightedPoints = remember(borewells) {
        borewells.map { borewell ->
            WeightedLatLng(
                LatLng(borewell.latitude, borewell.longitude),
                WaterClusterCalculator.heatmapIntensity(borewell)
            )
        }
    }

    MapEffect(weightedPoints) { map ->
        var overlay: TileOverlay? = null
        if (weightedPoints.isNotEmpty()) {
            val colors = intArrayOf(
                android.graphics.Color.rgb(34, 197, 94),
                android.graphics.Color.rgb(234, 179, 8),
                android.graphics.Color.rgb(239, 68, 68)
            )
            val startPoints = floatArrayOf(0.2f, 0.55f, 1.0f)
            val gradient = Gradient(colors, startPoints)

            val provider = HeatmapTileProvider.Builder()
                .weightedData(weightedPoints)
                .gradient(gradient)
                .radius(50)
                .opacity(0.7)
                .build()

            overlay = map.addTileOverlay(TileOverlayOptions().tileProvider(provider))
        }
        try {
            awaitCancellation()
        } finally {
            overlay?.remove()
        }
    }
}
