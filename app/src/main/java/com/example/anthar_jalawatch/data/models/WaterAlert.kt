package com.example.anthar_jalawatch.data.models

data class WaterAlert(
    val id: String,
    val title: String,
    val message: String,
    val severity: BorewellStatus
)
