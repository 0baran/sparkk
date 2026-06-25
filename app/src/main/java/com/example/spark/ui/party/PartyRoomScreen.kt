package com.example.spark.ui.party

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spark.data.repository.SparkRepository
import com.example.spark.domain.model.Message
import com.example.spark.domain.model.Party
import com.example.spark.domain.model.User
import com.example.spark.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyRoomScreen(
    partyId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var party by remember { mutableStateOf<Party?>(null) }
    var participants by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(partyId) {
        isLoading = true
        val partyResult = SparkRepository.getParty(partyId).getOrNull()
        val participantsResult = SparkRepository.getPartyParticipants(partyId).getOrNull() ?: emptyList()
        
        party = partyResult
        participants = participantsResult
        isLoading = false
    }

    var messages by remember {
        mutableStateOf<List<Message>>(emptyList())
    }

    LaunchedEffect(participants) {
        if (participants.isNotEmpty() && messages.isEmpty()) {
            messages = emptyList()
        }
    }

    var inputText by remember { mutableStateOf("") }

    if (isLoading) {
        Box(modifier = modifier.fillMaxSize().background(SurfaceDark), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PurplePrimary)
        }
        return
    }

    val currentParty = party
    if (currentParty == null) {
        Box(modifier = modifier.fillMaxSize().background(SurfaceDark), contentAlignment = Alignment.Center) {
            Text("Parti bulunamadı.", color = TextPrimary)
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceDark)
            .statusBarsPadding()
    ) {
        // Top bar
        Surface(color = SurfaceCard, shadowElevation = 4.dp) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = TextPrimary)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentParty.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${currentParty.participantCount} katılımcı",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                    if (currentParty.isLive) {
                        Surface(color = AccentRed, shape = RoundedCornerShape(4.dp)) {
                            Text(
                                text = "CANLI",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Participant avatars
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(participants) { user ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = user.avatarEmoji, fontSize = 20.sp)
                            }
                            Text(
                                text = user.displayName.split(" ").first(),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                if (message.isFromMe) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = 260.dp)
                                .clip(RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp))
                                .background(Brush.horizontalGradient(listOf(PurplePrimary, PinkPrimary)))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(text = message.content, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(
                                text = message.senderName,
                                style = MaterialTheme.typography.labelSmall,
                                color = PurpleLight,
                                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 260.dp)
                                    .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
                                    .background(SurfaceCard)
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(text = message.content, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }

        // Input
        Surface(color = SurfaceCard) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Mesajını yaz...", color = TextTertiary) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurplePrimary,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = PurpleLight,
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputText.isNotBlank()) Brush.linearGradient(listOf(PurplePrimary, PinkPrimary))
                            else Brush.linearGradient(listOf(SurfaceElevated, SurfaceElevated))
                        )
                        .clickable {
                            if (inputText.isNotBlank()) {
                                messages = messages + Message(
                                    id = "pm${messages.size + 1}",
                                    senderId = "me",
                                    senderName = "Ben",
                                    content = inputText,
                                    isFromMe = true
                                )
                                inputText = ""
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Gönder",
                        tint = if (inputText.isNotBlank()) Color.White else TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
