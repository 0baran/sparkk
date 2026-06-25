package com.example.spark.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.spark.theme.SurfaceDark
import com.example.spark.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yeni Gönderi") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = TextPrimary)                  }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        },
        containerColor = SurfaceDark
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(SurfaceDark), contentAlignment = Alignment.Center) {
            Text("Yeni Gönderi Ekranı", color = Color.White)
        }
    }
}
