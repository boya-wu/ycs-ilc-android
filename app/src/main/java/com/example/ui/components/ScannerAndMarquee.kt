package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- Digital Clock ---
@Composable
fun LiveClock(modifier: Modifier = Modifier) {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            val sdfTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val sdfDate = SimpleDateFormat("yyyy-MM-dd (E)", Locale.CHINESE)
            currentTime = sdfTime.format(Date())
            currentDate = sdfDate.format(Date())
            delay(1000)
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = currentTime,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color(0xFF0891B2), // Cyan-600
            modifier = Modifier.testTag("live_clock_time")
        )
        Text(
            text = currentDate,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            color = Color(0xFF64748B) // Slate-500
        )
    }
}

// --- Status Badge and Pulsing LED ---
@Composable
fun LedIndicator(
    color: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val sizeScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "size"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(16.dp)
    ) {
        // Glowing Background Halo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        color = color.copy(alpha = 0.3f),
                        radius = (size.minDimension / 1.6f) * sizeScale
                    )
                }
        )
        // Solid Center LED
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
    }
}

@Composable
fun ConnectionStatusBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Color(0xFFF1F5F9), RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        LedIndicator(color = Color(0xFF16A34A))
        Text(
            text = "ONLINE",
            color = Color(0xFF16A34A),
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

// --- Alarm Scrolling Marquee ---
@Composable
fun NoticeMarqueeBanner(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFFEF2F2), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Warning Icon with Alert LED pulse
        LedIndicator(color = Color(0xFFDC2626))
        
        Text(
            text = "⚠️ 警報通知:",
            color = Color(0xFFDC2626),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            fontFamily = FontFamily.SansSerif
        )

        Text(
            text = text,
            color = Color(0xFF991B1B),
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .weight(1f)
                .basicMarquee(iterations = Int.MAX_VALUE)
        )
    }
}

// --- Camera Viewfinder Simulator ---
@Composable
fun SimulatedScanner(
    title: String,
    instruction: String,
    defaultOutputCode: String,
    onCodeScanned: (String) -> Unit,
    onCancel: () -> Unit
) {
    var scanCompleted by remember { mutableStateOf(false) }
    var scaleFactor by remember { mutableStateOf(0f) }
    var manualValue by remember { mutableStateOf("") }
    var showManualDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val animatedYOffset by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    LaunchedEffect(Unit) {
        // Auto-scan simulator after 1.8 seconds of camera calibration
        delay(1800)
        scanCompleted = true
        onCodeScanned(defaultOutputCode)
    }

    if (showManualDialog) {
        AlertDialog(
            onDismissRequest = { showManualDialog = false },
            title = { Text("手動輸入代碼 (Manual Entry)") },
            text = {
                OutlinedTextField(
                    value = manualValue,
                    onValueChange = { manualValue = it },
                    placeholder = { Text("例如: $defaultOutputCode") },
                    modifier = Modifier.fillMaxWidth().testTag("manual_input_field"),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showManualDialog = false
                        onCodeScanned(manualValue.ifBlank { defaultOutputCode })
                    },
                    modifier = Modifier.testTag("manual_confirm_button")
                ) {
                    Text("確認")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
    ) {
        // Viewfinder Camera Frame Simulation
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Scanner Header
            Column(
                modifier = Modifier.padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = instruction,
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp
                )
            }

            // Viewfinder Bracket Area
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .drawBehind {
                        val strokeWidth = 5.dp.toPx()
                        val cornerLen = 30.dp.toPx()
                        val w = size.width
                        val h = size.height
                        
                        // Brackets (Corner markers)
                        // Top Left
                        drawLine(Color.White, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(cornerLen, 0f), strokeWidth = strokeWidth)
                        drawLine(Color.White, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(0f, cornerLen), strokeWidth = strokeWidth)
                        // Top Right
                        drawLine(Color.White, start = androidx.compose.ui.geometry.Offset(w, 0f), end = androidx.compose.ui.geometry.Offset(w - cornerLen, 0f), strokeWidth = strokeWidth)
                        drawLine(Color.White, start = androidx.compose.ui.geometry.Offset(w, 0f), end = androidx.compose.ui.geometry.Offset(w, cornerLen), strokeWidth = strokeWidth)
                        // Bottom Left
                        drawLine(Color.White, start = androidx.compose.ui.geometry.Offset(0f, h), end = androidx.compose.ui.geometry.Offset(cornerLen, h), strokeWidth = strokeWidth)
                        drawLine(Color.White, start = androidx.compose.ui.geometry.Offset(0f, h), end = androidx.compose.ui.geometry.Offset(0f, h - cornerLen), strokeWidth = strokeWidth)
                        // Bottom Right
                        drawLine(Color.White, start = androidx.compose.ui.geometry.Offset(w, h), end = androidx.compose.ui.geometry.Offset(w - cornerLen, h), strokeWidth = strokeWidth)
                        drawLine(Color.White, start = androidx.compose.ui.geometry.Offset(w, h), end = androidx.compose.ui.geometry.Offset(w, h - cornerLen), strokeWidth = strokeWidth)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (!scanCompleted) {
                    // Scanning animated lasers
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.01f)
                            .align(Alignment.TopCenter)
                            .offset(y = 260.dp * animatedYOffset)
                            .background(Color(0xFF16A34A))
                    )
                    
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "scanning",
                        tint = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(120.dp)
                    )
                } else {
                    // Flash success glow
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF16A34A).copy(alpha = 0.3f))
                    )
                    Text(
                        text = "條碼讀取成功!",
                        color = Color(0xFF86EFAC),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            // Footer of viewfinder
            Column(
                modifier = Modifier.padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { /* Simulated toggle flash */ },
                        modifier = Modifier.background(Color.White.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.FlashOn, contentDescription = "Flash", tint = Color.Yellow)
                    }
                    Button(
                        onClick = { showManualDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier.testTag("manual_entry_toggle")
                    ) {
                        Text("手動輸入 (Manual)", color = Color.White)
                    }
                }

                TextButton(onClick = onCancel, modifier = Modifier.testTag("cancel_scanner_button")) {
                    Text("返回 (Go Back)", color = Color(0xFFFCA5A5), fontSize = 15.sp)
                }
            }
        }
    }
}
