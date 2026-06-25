package com.example.spark.domain.model

data class Party(
    val id: String = "",
    val name: String = "",
    val hostName: String = "",
    val hostAvatar: String = "🎤",
    val category: String = "",
    val description: String = "",
    val participantCount: Int = 0,
    val maxParticipants: Int = 10,
    val isLive: Boolean = false,
    val tags: List<String> = emptyList(),
)
