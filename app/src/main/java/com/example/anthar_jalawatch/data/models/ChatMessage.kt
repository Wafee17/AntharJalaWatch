package com.example.anthar_jalawatch.data.models

enum class ChatRole {
    USER, AI
}

data class ChatMessage(
    val id: String = "",
    val role: ChatRole = ChatRole.USER,
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
