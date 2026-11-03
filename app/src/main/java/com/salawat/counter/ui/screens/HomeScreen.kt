package com.salawat.counter.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salawat.counter.ui.theme.*
import com.salawat.counter.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onToggleListening: (Boolean) -> Unit
) {
    val todayRecord by viewModel.todayRecord.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()

    val count = todayRecord?.count ?: 0
    val target = todayRecord?.target ?: 100
    val progress = if (target > 0) (count.toFloat() / target).coerceIn(0f, 1f) else 0f

    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkGreen,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "صلوات‌شمار هوشمند",
                        color = Gold,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkGreen
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = count.toString(),
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = Gold
            )

            Text(
                text = "صلوات امروز",
                color = White.copy(alpha = 0.8f),
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = Gold,
                trackColor = White.copy(alpha = 0.2f)
            )

            Text(
                text = "$count از $target",
                color = White.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "مجموع کل: $totalCount",
                color = White.copy(alpha = 0.6f),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { onToggleListening(!isListening) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isListening) Color(0xFFB71C1C) else Gold
                ),
                modifier = Modifier.size(120.dp),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = null,
                    tint = if (isListening) White else DarkGreen,
                    modifier = Modifier.size(48.dp)
                )
            }

            Text(
                text = if (isListening) "تشخیص فعال است" else "شروع تشخیص صوتی",
                color = White,
                modifier = Modifier.padding(top = 12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = { viewModel.incrementManual() },
                border = BorderStroke(2.dp, Gold),
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "+ یک صلوات",
                    color = Gold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { showResetDialog = true }) {
                Text("صفر کردن شمارنده", color = White.copy(alpha = 0.7f))
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("تأیید صفر کردن") },
            text = { Text("آیا مطمئن هستید که می‌خواهید شمارنده امروز را صفر کنید؟") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetCounter()
                    showResetDialog = false
                }) {
                    Text("بله", color = Gold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("خیر")
                }
            }
        )
    }
}
