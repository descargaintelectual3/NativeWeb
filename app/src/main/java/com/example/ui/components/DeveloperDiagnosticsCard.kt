package com.example.ui.components

import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun DeveloperDiagnosticsCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    
    var isAdbEnabled by remember { mutableStateOf(false) }
    var isDevModeEnabled by remember { mutableStateOf(false) }
    var sysUsbConfig by remember { mutableStateOf("Leyendo...") }
    var persistAdbConfig by remember { mutableStateOf("Leyendo...") }
    var showHelp by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isAdbEnabled = Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        isDevModeEnabled = Settings.Global.getInt(context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1
        sysUsbConfig = getSystemProperty("sys.usb.config")
        persistAdbConfig = getSystemProperty("persist.sys.adb.config")
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ElegantCard),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantCardBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(LavenderPrimary.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeveloperMode,
                        contentDescription = null,
                        tint = LavenderPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Diagnóstico AI Studio / ADB",
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Estado de conexión y depuración USB",
                        fontSize = 11.sp,
                        color = TextGrayMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status Indicators
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusChip(
                    title = "Modo Desarrollador",
                    isActive = isDevModeEnabled,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(
                    title = "Depuración (ADB)",
                    isActive = isAdbEnabled,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Raw System Properties
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.2f))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "Propiedades Internas (Raw)",
                        color = TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PropertyRow("sys.usb.config", sysUsbConfig)
                    PropertyRow("persist.sys.adb.config", persistAdbConfig)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Help Guide Toggle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DeepPurpleContainer)
                    .clickable { showHelp = !showHelp }
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = null,
                        tint = LavenderPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "¿Cómo conectar con Google AI Studio?",
                        color = LavenderPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (showHelp) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = LavenderPrimary
                    )
                }
            }

            AnimatedVisibility(visible = showHelp) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = "Para instalar la app directo desde AI Studio usando WebUSB:",
                        color = TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    HelpStep("1", "Asegúrate que los dos cuadros de arriba digan 'Activado'.")
                    HelpStep("2", "Conecta este celular a una Computadora (PC/Mac) con cable USB.")
                    HelpStep("3", "Abre aistudio.google.com en Google Chrome desde tu Computadora.")
                    HelpStep("4", "Usa la opción 'Install via USB' en la pantalla de la computadora.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(AmberEnergy.copy(alpha = 0.15f))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = AmberEnergy, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Por seguridad nativa de Android, un celular no puede conectarse a sí mismo por WebUSB. ¡Siempre se requiere una PC como puente físico, o usa el Panel CI/CD (abajo) para compilar sin cables!",
                                color = AmberEnergy,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(title: String, isActive: Boolean, modifier: Modifier = Modifier) {
    val bgColor = if (isActive) MintSpeed.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.1f)
    val fgColor = if (isActive) MintSpeed else Color(0xFFFF5252)
    val icon = if (isActive) Icons.Default.CheckCircle else Icons.Default.Cancel

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, fgColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = fgColor, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            color = TextWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = if (isActive) "Activado" else "Desactivado",
            color = fgColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun HelpStep(step: String, text: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(LavenderPrimary),
            contentAlignment = Alignment.Center
        ) {
            Text(text = step, color = DeepPurpleOnPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, color = TextGrayLight, fontSize = 11.sp, lineHeight = 16.sp)
    }
}

@Composable
private fun PropertyRow(key: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = key, color = TextGrayMuted, fontSize = 11.sp)
        Text(
            text = value,
            color = if (value.contains("adb", ignoreCase = true)) MintSpeed else TextWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun getSystemProperty(key: String): String {
    return try {
        val clazz = Class.forName("android.os.SystemProperties")
        val method = clazz.getMethod("get", String::class.java)
        val result = method.invoke(null, key) as? String
        if (result.isNullOrBlank()) "[Vacío/No set]" else result
    } catch (e: Exception) {
        "[Acceso Denegado]"
    }
}
