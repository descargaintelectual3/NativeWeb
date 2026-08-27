package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.AppLogger
import kotlinx.coroutines.launch

@Composable
fun SystemLogsCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var logs by remember { mutableStateOf(AppLogger.getLogs(context)) }
    val scrollState = rememberScrollState()

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ElegantCard),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantCardBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(MintSpeed.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ListAlt, contentDescription = null, tint = MintSpeed, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Registros del Sistema (Logs)", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 15.sp)
                        Text("Diagnóstico para Agentes AI", fontSize = 11.sp, color = TextGrayMuted)
                    }
                }
                
                Row {
                    IconButton(onClick = { 
                        AppLogger.clearLogs(context)
                        logs = AppLogger.getLogs(context)
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Limpiar", tint = TextGrayMuted)
                    }
                    IconButton(onClick = { logs = AppLogger.getLogs(context) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = LavenderPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = logs,
                    color = MintSpeed,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    modifier = Modifier.verticalScroll(scrollState)
                )
            }
            
            LaunchedEffect(logs) {
                scrollState.scrollTo(scrollState.maxValue)
            }
        }
    }
}
