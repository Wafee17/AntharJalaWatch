package com.example.anthar_jalawatch.data.models

import com.google.firebase.Timestamp

enum class BorewellStatus {
    OK, WARNING, CRITICAL
}

data class Borewell(
    val id: String = "",
    val userId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val depth: Double = 0.0,
    val yield: Double = 0.0,
    val yearOfDigging: Int = 0,
    val timestamp: Timestamp = Timestamp.now(),
    val status: String = BorewellStatus.OK.name
)
