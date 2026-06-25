package com.example.spark

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.spark.ui.auth.LoginScreen
import com.example.spark.ui.auth.RegisterScreen
import com.example.spark.ui.main.MainScreen
import com.example.spark.ui.soulgame.SoulChatScreen
import com.example.spark.ui.voicegame.VoiceGameScreen
import com.example.spark.ui.party.PartyRoomScreen

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(Login)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Login> {
                LoginScreen(
                    onLoginClick = {
                        backStack.clear()
                        backStack.add(Main)
                    },
                    onRegisterClick = { backStack.add(Register) }
                )
            }

            entry<Register> {
                RegisterScreen(
                    onRegisterClick = {
                        backStack.clear()
                        backStack.add(Main)
                    },
                    onLoginClick = { backStack.removeLastOrNull() }
                )
            }

            entry<Main> {
                MainScreen(
                    onItemClick = { navKey -> backStack.add(navKey) },
                    onLogout = { 
                        backStack.clear()
                        backStack.add(Login)
                    }
                )
            }

            entry<SoulChat> { key ->
                SoulChatScreen(
                    matchedUserId = key.matchedUserId,
                    onBack = { backStack.removeLastOrNull() },
                    onAddFriend = { backStack.removeLastOrNull() }
                )
            }

            entry<VoiceGame> { key ->
                VoiceGameScreen(
                    matchedUserId = key.matchedUserId,
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<PartyRoom> { key ->
                PartyRoomScreen(
                    partyId = key.partyId,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<SoulGame> {
                com.example.spark.ui.soulgame.SoulGameTab(
                    onStartGame = { matchedId -> backStack.add(SoulChat(matchedId)) }
                )
            }

            entry<CreatePost> {
                com.example.spark.ui.feed.CreatePostScreen(
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<EditProfile> {
                com.example.spark.ui.profile.EditProfileScreen(
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<QrMatch> {
                com.example.spark.ui.qrmatch.QrMatchScreen(
                    onBack = { backStack.removeLastOrNull() },
                    onMatchFound = { matchedUserId ->
                        backStack.removeLastOrNull() // remove qr screen
                        backStack.add(SoulChat(matchedUserId)) // go to chat
                    }
                )
            }
        },
    )
}
