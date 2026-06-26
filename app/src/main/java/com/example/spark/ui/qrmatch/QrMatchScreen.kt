package com.example.spark.ui.qrmatch

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.spark.data.repository.AuthRepository
import com.example.spark.data.repository.SparkRepository
import com.example.spark.theme.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrMatchScreen(
    onBack: () -> Unit,
    onMatchFound: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val currentUserId = AuthRepository.currentUser?.uid ?: return

    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isScanning by remember { mutableStateOf(false) }
    var matchStatus by remember { mutableStateOf("Kodunu paylaş veya arkadaşının kodunu tara!") }

    // Pulse animation for waiting state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Generate QR Code for my ID
    LaunchedEffect(currentUserId) {
        val size = 512
        try {
            val bitMatrix = MultiFormatWriter().encode(
                "spark:$currentUserId",
                BarcodeFormat.QR_CODE, size, size
            )
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE)
                }
            }
            qrBitmap = bitmap
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Create QR session in Firestore
        SparkRepository.createQrSession(currentUserId)
        matchStatus = "Bekleniyor... Arkadaşın kodunu taratsın!"
    }

    // Listen to our session — real-time matching
    DisposableEffect(currentUserId) {
        val listener = SparkRepository.listenToQrSession(currentUserId) { matchedWithId ->
            matchStatus = "Eşleşme bulundu! 🎉"
            onMatchFound(matchedWithId)
        }
        onDispose {
            listener.remove()
        }
    }

    // QR Scanner
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            val rawContent = result.contents
            val scannedHostId = if (rawContent.startsWith("spark:")) {
                rawContent.removePrefix("spark:")
            } else {
                rawContent
            }
            isScanning = true
            matchStatus = "Bağlanılıyor..."
            coroutineScope.launch {
                SparkRepository.joinQrSession(scannedHostId, currentUserId)
                onMatchFound(scannedHostId)
                isScanning = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark)
            .statusBarsPadding()
    ) {
        // Top bar
        Surface(
            color = SurfaceCard,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = TextPrimary)
                }
                Text(
                    text = "QR ile Eşleş",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Status indicator
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
                        text = matchStatus,
                        style = MaterialTheme.typography.labelMedium,
                        color = AccentGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Benim Kodum",
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // QR Code with pulse animation
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(pulseScale)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap!!.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    CircularProgressIndicator(color = PurplePrimary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Share QR Button
            OutlinedButton(
                onClick = {
                    qrBitmap?.let { bitmap ->
                        try {
                            val cachePath = File(context.cacheDir, "qr_codes")
                            cachePath.mkdirs()
                            val file = File(cachePath, "spark_qr.png")
                            FileOutputStream(file).use { out ->
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                            }
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/png"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                putExtra(Intent.EXTRA_TEXT, "Spark'ta benimle eşleş! 🔥 QR kodumu tara ve hemen sohbete başla!")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "QR Kodunu Paylaş"))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentGold),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true)
            ) {
                Icon(Icons.Filled.Share, contentDescription = "Paylaş", tint = AccentGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("QR Kodunu Paylaş", color = AccentGold, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "veya",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Scan Button
            Button(
                onClick = {
                    val options = ScanOptions()
                    options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                    options.setPrompt("Spark QR Kodunu Tara")
                    options.setCameraId(0)
                    options.setBeepEnabled(false)
                    options.setBarcodeImageEnabled(true)
                    options.setOrientationLocked(false)
                    scanLauncher.launch(options)
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.horizontalGradient(listOf(PurplePrimary, PinkPrimary))),
                    contentAlignment = Alignment.Center
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.QrCodeScanner, contentDescription = "Tara", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("QR Kodu Tara", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
