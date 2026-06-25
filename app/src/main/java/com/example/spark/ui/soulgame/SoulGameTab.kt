package com.example.spark.ui.soulgame

import com.example.spark.data.repository.SparkRepository
import com.example.spark.data.repository.AuthRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spark.theme.*

@Composable
fun SoulGameTab(
    onStartGame: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSearching by remember { mutableStateOf(false) }
    var searchMessage by remember { mutableStateOf<String?>(null) }
    var onlineCount by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        onlineCount = SparkRepository.getOnlineUsers().getOrNull()?.filter { it.id != AuthRepository.currentUser?.uid }?.size ?: 0
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "s1"
    )
    val scale2 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.5f,
        animationSpec = infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "s2"
    )
    val scale3 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.7f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "s3"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "\uD83D\uDC9C Ruh Eşini Bul",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Anonim olarak biriyle eşleş ve sohbet et",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Pulsing circles
        Box(contentAlignment = Alignment.Center) {
            // Outer ring
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(scale3)
                    .clip(CircleShape)
                    .background(PurplePrimary.copy(alpha = 0.1f))
            )
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(scale2)
                    .clip(CircleShape)
                    .background(PurplePrimary.copy(alpha = 0.15f))
            )
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(scale1)
                    .clip(CircleShape)
                    .background(PurplePrimary.copy(alpha = 0.2f))
            )
            // Center circle
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(PurplePrimary, PinkPrimary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "\uD83D\uDC9C", fontSize = 56.sp)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Online count
        Surface(
            color = SurfaceCard,
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(OnlineGreen)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (onlineCount > 0) "$onlineCount kişi eşleşmeye uygun" else "Şu an kimse yok",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Start button
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(PurplePrimary, PinkPrimary)
                    )
                )
                .clickable(enabled = !isSearching) { 
                    isSearching = true
                    searchMessage = "Eşleşme aranıyor..."
                    coroutineScope.launch {
                        delay(1000) // Small delay for UX
                        val currentUserId = AuthRepository.currentUser?.uid ?: return@launch
                        val match = SparkRepository.findRandomMatch(currentUserId).getOrNull()
                        if (match != null) {
                            onStartGame(match.id)
                        } else {
                            searchMessage = "Aktif kullanıcı bulunamadı."
                        }
                        isSearching = false
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (isSearching) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = "Başlat",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (searchMessage != null && !isSearching) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = searchMessage!!,
                color = AccentRed,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
