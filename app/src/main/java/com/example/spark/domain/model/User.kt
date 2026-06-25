package com.example.spark.domain.model

data class User(
    val id: String = "",
    val username: String = "",
    val displayName: String = "",
    val bio: String = "",
    val avatarEmoji: String = "😊",
    val age: Int = 0,
    val interests: List<String> = emptyList(),
    val isOnline: Boolean = false,
    val friendCount: Int = 0,
    val postCount: Int = 0,
    val level: Int = 1,
    val auraScore: Int = 500, // Başlangıç Aura Puanı
)
