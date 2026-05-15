package com.example.anthar_jalawatch.data.models

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

data class BorewellClusterItem(
    val borewellId: String,
    val itemPosition: LatLng,
    val itemTitle: String,
    val itemSnippet: String,
    val itemZIndex: Float?,
    val yieldValue: Double,
    val statusLevel: String
) : ClusterItem {
    override fun getPosition(): LatLng = itemPosition
    override fun getTitle(): String? = itemTitle
    override fun getSnippet(): String? = itemSnippet
    override fun getZIndex(): Float? = itemZIndex
}
