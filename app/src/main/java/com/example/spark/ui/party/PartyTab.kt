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
import androidx.compose.material.icons.filled.Add
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
import com.example.spark.domain.model.Party
import com.example.spark.theme.*

@Composable
fun PartyTab(
    onRoomClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("Tümü", "Sohbet", "Müzik", "Oyun", "Kültür", "Spor", "Seyahat")
    var selectedCategory by remember { mutableStateOf("Tümü") }
    var allParties by remember { mutableStateOf<List<Party>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val result = SparkRepository.getActiveParties()
        if (result.isSuccess) {
            val fetched = result.getOrNull() ?: emptyList()
            if (fetched.isEmpty()) {
                allParties = emptyList()
            } else {
                allParties = fetched
            }
        }
        isLoading = false
    }

    val filteredParties = if (selectedCategory == "Tümü") allParties
        else allParties.filter { it.category == selectedCategory }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "\uD83C\uDF89 Parti Sohbeti",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            val context = androidx.compose.ui.platform.LocalContext.current
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(PurplePrimary, PinkPrimary)))
                    .clickable { android.widget.Toast.makeText(context, "Oda oluşturma yakında!", android.widget.Toast.LENGTH_SHORT).show() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Oda Oluştur",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Category chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                Surface(
                    color = if (isSelected) PurplePrimary else SurfaceCard,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.clickable { selectedCategory = category }
                ) {
                    Text(
                        text = category,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) Color.White else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PinkPrimary)
            }
        } else {
            // Party list
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredParties, key = { it.id }) { party ->
                    PartyCard(party = party, onClick = { onRoomClick(party.id) })
                }
            }
        }
    }
}

@Composable
fun PartyCard(party: Party, onClick: () -> Unit) {
    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = party.hostAvatar, fontSize = 28.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = party.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        if (party.isLive) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = AccentRed,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "CANLI",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text(
                        text = party.hostName,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${party.participantCount}/${party.maxParticipants}",
                        style = MaterialTheme.typography.labelLarge,
                        color = PurpleLight
                    )
                    Text(
                        text = "kişi",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
            }
            if (party.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    party.tags.forEach { tag ->
                        Surface(
                            color = SurfaceElevated,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = tag,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
