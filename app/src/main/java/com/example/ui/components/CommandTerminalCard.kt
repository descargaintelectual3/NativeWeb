package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.ADBManager
import com.example.util.AppLogger
import com.example.util.HardwareAccelerationInfo
import kotlinx.coroutines.launch

@Composable
fun CommandTerminalCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    var currentCommand by remember { mutableStateOf("getprop sys.usb.config") }
    var terminalOutput by remember {
        mutableStateOf(
            "=== WebNative Pro ADB / Shell Terminal ===\n" +
            "Directorio activo: ${context.filesDir.absolutePath}\n" +
            "Escribe un comando o selecciona un acceso rápido.\n"
        )
    }
    var isExecuting by remember { mutableStateOf(false) }
    var hwInfo by remember { mutableStateOf<HardwareAccelerationInfo?>(null) }
    var showQuickProperties by remember { mutableStateOf(false) }
    var systemProperties by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    val scrollState = rememberScrollState()
    val chipScrollState = rememberScrollState()

    // Fetch initial hardware acceleration status
    LaunchedEffect(Unit) {
        hwInfo = ADBManager.getHardwareAccelerationInfo()
    }

    val quickCommands = listOf(
        "getprop sys.usb.config",
        "getprop persist.sys.adb.config",
        "getprop debug.hwui.renderer",
        "dumpsys battery",
        "pm list features | grep opengl",
        "getprop ro.product.model",
        "cat /proc/meminfo | head -n 8",
        "uname -a"
    )

    fun runCommand(cmd: String) {
        if (cmd.isBlank() || isExecuting) return
        isExecuting = true
        AppLogger.log(context, "Ejecutando comando terminal: $cmd", tag = "TERMINAL")

        coroutineScope.launch {
            terminalOutput += "\n$ $cmd\n"
            val result = ADBManager.execShellCommand(cmd)
            
            if (result.stdout.isNotBlank()) {
                terminalOutput += result.stdout + "\n"
            }
            if (result.stderr.isNotBlank()) {
                terminalOutput += "[STDERR] ${result.stderr}\n"
            }
            terminalOutput += "[Retorno: ${result.exitCode} en ${result.executionTimeMs}ms]\n"

            // refresh HW status if relevant
            if (cmd.contains("debug.hwui") || cmd.contains("setprop")) {
                hwInfo = ADBManager.getHardwareAccelerationInfo()
            }
            isExecuting = false
        }
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(LavenderPrimary.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Terminal, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Terminal Shell & Motor ADB", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 15.sp)
                        Text("Aceleración por Hardware y Diagnóstico", fontSize = 11.sp, color = TextGrayMuted)
                    }
                }

                Row {
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(terminalOutput))
                        AppLogger.log(context, "Copiado volcado del terminal al portapapeles", tag = "TERMINAL")
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copiar salida", tint = TextGrayMuted, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = {
                        terminalOutput = "=== Terminal Limpio ===\n"
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Limpiar consola", tint = TextGrayMuted, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Hardware Acceleration Toggle Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.25f))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = MintSpeed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Aceleración HWUI:",
                                color = TextWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = hwInfo?.renderer ?: "Cargando...",
                            color = MintSpeed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset Engine Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    ADBManager.setHardwareAccelerationRenderer(context, "skiagl")
                                    hwInfo = ADBManager.getHardwareAccelerationInfo()
                                    terminalOutput += "\n[HWUI] Configurado renderizador -> skiagl (Skia OpenGL)\n"
                                }
                            },
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary.copy(alpha = 0.25f)),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("SkiaGL", fontSize = 11.sp, color = LavenderPrimary, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    ADBManager.setHardwareAccelerationRenderer(context, "skiavk")
                                    hwInfo = ADBManager.getHardwareAccelerationInfo()
                                    terminalOutput += "\n[HWUI] Configurado renderizador -> skiavk (Skia Vulkan)\n"
                                }
                            },
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MintSpeed.copy(alpha = 0.25f)),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("Vulkan", fontSize = 11.sp, color = MintSpeed, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    ADBManager.resetHardwareAcceleration(context)
                                    hwInfo = ADBManager.getHardwareAccelerationInfo()
                                    terminalOutput += "\n[HWUI] Restablecido renderizador por defecto de Android\n"
                                }
                            },
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AmberEnergy.copy(alpha = 0.25f)),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("Default", fontSize = 11.sp, color = AmberEnergy, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Command Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(chipScrollState),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickCommands.forEach { cmd ->
                    Surface(
                        onClick = {
                            currentCommand = cmd
                            runCommand(cmd)
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Text(
                            text = cmd,
                            color = TextGrayLight,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Terminal Screen Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0D0E15))
                    .border(1.dp, Color(0xFF2A2D3D), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = terminalOutput,
                    color = MintSpeed,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.verticalScroll(scrollState)
                )

                LaunchedEffect(terminalOutput) {
                    scrollState.scrollTo(scrollState.maxValue)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Command Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = currentCommand,
                    onValueChange = { currentCommand = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ej: getprop sys.usb.config", fontSize = 12.sp, color = TextGrayMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LavenderPrimary,
                        unfocusedBorderColor = ElegantCardBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                )

                Button(
                    onClick = { runCommand(currentCommand) },
                    enabled = !isExecuting && currentCommand.isNotBlank(),
                    modifier = Modifier.height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary)
                ) {
                    if (isExecuting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = DeepPurpleOnPrimary, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Ejecutar", tint = DeepPurpleOnPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("RUN", fontWeight = FontWeight.Bold, color = DeepPurpleOnPrimary, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Diagnostic Properties accordion toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        showQuickProperties = !showQuickProperties
                        if (showQuickProperties && systemProperties.isEmpty()) {
                            systemProperties = ADBManager.getQuickSystemDiagnostics()
                        }
                    }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ver Diagnóstico de Propiedades del Sistema",
                    fontSize = 11.sp,
                    color = LavenderPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = if (showQuickProperties) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = LavenderPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = showQuickProperties) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.2f))
                        .padding(10.dp)
                ) {
                    systemProperties.forEach { (label, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = label, color = TextGrayMuted, fontSize = 10.sp)
                            Text(
                                text = value,
                                color = if (value.contains("adb", ignoreCase = true) || value == "1") MintSpeed else TextWhite,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
