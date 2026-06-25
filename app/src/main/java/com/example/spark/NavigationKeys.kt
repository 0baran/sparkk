package com.example.spark

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Login : NavKey
@Serializable data object Register : NavKey
@Serializable data object Main : NavKey
@Serializable data object SoulGame : NavKey
@Serializable data class SoulChat(val matchedUserId: String) : NavKey
@Serializable data class VoiceGame(val matchedUserId: String) : NavKey
@Serializable data object PartyList : NavKey
@Serializable data class PartyRoom(val partyId: String) : NavKey
@Serializable data object Feed : NavKey
@Serializable data object CreatePost : NavKey
@Serializable data object Profile : NavKey
@Serializable data object EditProfile : NavKey
@Serializable data object QrMatch : NavKey
