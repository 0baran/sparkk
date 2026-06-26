package com.example.spark.ui.main

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.example.spark.*
import com.example.spark.data.repository.AuthRepository
import com.example.spark.data.repository.SparkRepository
import com.example.spark.domain.model.Party
import com.example.spark.domain.model.User
import com.example.spark.theme.*
import com.example.spark.ui.feed.FeedTab
import com.example.spark.ui.party.PartyTab
import com.example.spark.ui.profile.ProfileTab
import com.example.spark.ui.soulgame.SoulGameTab
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class BottomTab(val label: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    HOME("Ana Sayfa", Icons.Outlined.Home, Icons.Filled.Home),
    SOUL("Vibe Check", Icons.Outlined.Favorite, Icons.Filled.Favorite),
    PARTY("Lounge", Icons.Outlined.Celebration, Icons.Filled.Celebration),
    FEED("Akış", Icons.Outlined.DynamicFeed, Icons.Filled.DynamicFeed),
    PROFILE("Profil", Icons.Outlined.Person, Icons.Filled.Person),
}

@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(BottomTab.HOME) }

    Scaffold(
        containerColor = SurfaceDark,
        bottomBar = {
            LitmacBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                BottomTab.HOME -> HomeTab(onItemClick = onItemClick)
                BottomTab.SOUL -> SoulGameTab(onStartGame = { matchedId -> onItemClick(SoulChat(matchedId)) })
                BottomTab.PARTY -> PartyTab(onRoomClick = { onItemClick(PartyRoom(it)) })
                BottomTab.FEED -> FeedTab(onCreatePost = { onItemClick(CreatePost) })
                BottomTab.PROFILE -> ProfileTab(onEditProfile = { onItemClick(EditProfile) }, onLogout = onLogout)
            }
        }
    }
}

@Composable
fun LitmacBottomBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit
) {
    Surface(
        color = SurfaceCard,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BottomTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                val iconColor by animateColorAsState(
                    if (isSelected) PinkPrimary else TextTertiary,
                    label = "iconColor"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (isSelected) tab.selectedIcon else tab.icon,
                        contentDescription = tab.label,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = iconColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun HomeTab(onItemClick: (NavKey) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            val userProfile by produceState<User?>(initialValue = null) {
                val userId = AuthRepository.currentUser?.uid
                if (userId != null) {
                    value = SparkRepository.getUserProfile(userId).getOrNull()
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Merhaba, ${userProfile?.displayName ?: "Kullanıcı"}! 👋",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Bugün kiminle tanışmak istersin?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(PurplePrimary, PinkPrimary))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userProfile?.avatarEmoji ?: "😊",
                        fontSize = 24.sp
                    )
                }
            }
        }

        // Feature Cards
        item {
            Text(
                text = "Keşfet",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            var isSearchingVoice by remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()
            val context = LocalContext.current

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureCard(
                    title = "Vibe Check",
                    subtitle = "Anonim sohbet",
                    emoji = "💜",
                    gradient = listOf(PurplePrimary, PurpleLight),
                    modifier = Modifier.weight(1f),
                    onClick = { onItemClick(SoulGame) }
                )
                FeatureCard(
                    title = if (isSearchingVoice) "Aranıyor..." else "Frekans",
                    subtitle = "Sesini duyur",
                    emoji = "🎤",
                    gradient = listOf(PinkPrimary, PinkLight),
                    modifier = Modifier.weight(1f),
                    onClick = { 
                        if (isSearchingVoice) return@FeatureCard
                        isSearchingVoice = true
                        coroutineScope.launch {
                            delay(1000)
                            val currentUserId = AuthRepository.currentUser?.uid ?: return@launch
                            val match = SparkRepository.findRandomMatch(currentUserId).getOrNull()
                            if (match != null) {
                                onItemClick(VoiceGame(match.id))
                            } else {
                                Toast.makeText(context, "Şu an aktif kimse yok.", Toast.LENGTH_SHORT).show()
                            }
                            isSearchingVoice = false
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // QR ile Bağlan Card
            FeatureCard(
                title = "QR ile Bağlan",
                subtitle = "Yan yana eşleş",
                emoji = "📸",
                gradient = listOf(AccentGold, AccentGreen),
                modifier = Modifier.fillMaxWidth().height(100.dp),
                onClick = { onItemClick(QrMatch) }
            )
        }

        // Online Users
        item {
            Text(
                text = "Çevrimiçi Kullanıcılar 🟢",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            var onlineUsers by remember { mutableStateOf<List<User>>(emptyList()) }
            LaunchedEffect(Unit) {
                onlineUsers = SparkRepository.getOnlineUsers().getOrNull()?.filter { it.id != AuthRepository.currentUser?.uid } ?: emptyList()
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(onlineUsers) { user ->
                    OnlineUserCard(user = user)
                }
            }
        }

        // Active Parties
        item {
            Text(
                text = "Aktif Partiler 🎉",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            var activeParties by remember { mutableStateOf<List<Party>>(emptyList()) }
            LaunchedEffect(Unit) {
                activeParties = SparkRepository.getActiveParties().getOrNull()?.take(3) ?: emptyList()
            }
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                activeParties.forEach { party ->
                    Surface(
                        color = SurfaceCard,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = party.hostAvatar, fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = party.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${party.participantCount}/${party.maxParticipants} kişi",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Brush.horizontalGradient(listOf(PurplePrimary, PinkPrimary)))
                                    .clickable { onItemClick(PartyRoom(party.id)) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Katıl",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    subtitle: String,
    emoji: String,
    gradient: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .height(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(gradient))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = emoji, fontSize = 32.sp)
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun OnlineUserCard(user: User) {
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { Toast.makeText(context, "${user.displayName} profili yakında!", Toast.LENGTH_SHORT).show() }
            .padding(4.dp)
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(SurfaceCard),
                contentAlignment = Alignment.Center
            ) {
                Text(text = user.avatarEmoji, fontSize = 28.sp)
            }
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(OnlineGreen)
                    .align(Alignment.BottomEnd)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = user.displayName.split(" ").first(),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            maxLines = 1
        )
    }
}
