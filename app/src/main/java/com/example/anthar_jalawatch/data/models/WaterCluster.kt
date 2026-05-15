package com.example.anthar_jalawatch.data.models

import com.google.android.gms.maps.model.LatLng

data class WaterCluster(
    val clusterKey: String,
    val center: LatLng,
    val averageDepthFt: Double,
    val averageYieldInHr: Double,
    val readingCount: Int,
    val stressLevel: BorewellStatus,
    val seasonalDepthDropFt: Double
)
