package com.example.spark.ui.voicegame

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.VolumeUp
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

import com.example.spark.domain.model.User
import com.example.spark.data.repository.SparkRepository
import kotlinx.coroutines.delay

@Composable
fun VoiceGameScreen(
    matchedUserId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMuted by remember { mutableStateOf(false) }
    var isConnected by remember { mutableStateOf(false) }
    var matchSeconds by remember { mutableStateOf(0) }
    var matchedUser by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(matchedUserId) {
        matchedUser = SparkRepository.getUserProfile(matchedUserId).getOrNull()
        isConnected = true
        while(true) {
            delay(1000)
            matchSeconds++
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "voice")
    val wave1 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "w1"
    )
    val wave2 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.4f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "w2"
    )
    val wave3 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.6f,
        animationSpec = infiniteRepeatable(tween(1600), RepeatMode.Reverse),
        label = "w3"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PurpleDark, SurfaceDark, SurfaceDark)
                )
            )
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "🎙️ Sesli Sohbet",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.weight(0.3f))

        // Connection status
        Surface(
            color = AccentGreen.copy(alpha = 0.15f),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(AccentGreen)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isConnected) "Bağlantı kuruldu" else "Bağlanıyor...",
                    style = MaterialTheme.typography.labelMedium,
                    color = AccentGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Voice waves and avatar
        Box(contentAlignment = Alignment.Center) {
            if (!isMuted) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(wave3)
                        .clip(CircleShape)
                        .background(PinkPrimary.copy(alpha = 0.08f))
                )
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(wave2)
                        .clip(CircleShape)
                        .background(PinkPrimary.copy(alpha = 0.12f))
                )
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(wave1)
                        .clip(CircleShape)
                        .background(PinkPrimary.copy(alpha = 0.18f))
                )
            }
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(PinkPrimary, PurplePrimary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = matchedUser?.avatarEmoji ?: "🎭", fontSize = 60.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = matchedUser?.displayName ?: "Bağlanıyor...",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "⏱ ${String.format("%02d:%02d", matchSeconds / 60, matchSeconds % 60)}",
            style = MaterialTheme.typography.bodyMedium,
            color = AccentGold,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.weight(0.4f))

        // Controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(bottom = 48.dp)
        ) {
            // Mute button
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(if (isMuted) AccentRed.copy(alpha = 0.2f) else SurfaceCard)
                    .clickable { isMuted = !isMuted },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Mikrofon",
                    tint = if (isMuted) AccentRed else TextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            // End call button
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(AccentRed)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CallEnd,
                    contentDescription = "Bitir",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Speaker button
            val context = androidx.compose.ui.platform.LocalContext.current
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(SurfaceCard)
                    .clickable { android.widget.Toast.makeText(context, "Hoparlör geçişi yakında!", android.widget.Toast.LENGTH_SHORT).show() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Hoparlör",
                    tint = TextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}
