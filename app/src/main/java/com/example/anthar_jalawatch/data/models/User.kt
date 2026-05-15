package com.example.anthar_jalawatch.data.models

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val email: String = "",
    val fullName: String = "",
    val village: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val fcmToken: String = ""
)
