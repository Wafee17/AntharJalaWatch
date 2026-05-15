package com.example.anthar_jalawatch.data.models

import com.google.firebase.Timestamp

data class Alert(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val isRead: Boolean = false
)
