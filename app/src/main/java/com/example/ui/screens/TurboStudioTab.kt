package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberEnergy
import com.example.ui.theme.DeepPurpleContainer
import com.example.ui.theme.DeepPurpleOnPrimary
import com.example.ui.theme.DeepPurpleTrack
import com.example.ui.theme.ElegantCard
import com.example.ui.theme.ElegantCardBorder
import com.example.ui.theme.LavenderOnContainer
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.MintSpeed
import com.example.ui.theme.RoseAccent
import com.example.ui.theme.RoseDarkText
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextGrayMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.WebAppUiState

import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VpnKey
import com.example.ui.components.GitHubControlPanelCard
import com.example.ui.components.DeveloperDiagnosticsCard
import com.example.ui.components.BuildMonitorView
import com.example.ui.components.CommandTerminalCard
import com.example.ui.components.DevelopmentActivityLogCard
import com.example.ui.components.TestRunnerCard
import com.example.ui.components.SystemLogsCard

import com.example.util.AppearanceSettingsManager
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import com.example.ui.dialogs.DevicePermissionsDialog
import com.example.ui.dialogs.RemoteControlOtaDialog
import com.example.util.OtaUpdateManager
import com.example.util.RemoteConfigEngine
import com.example.util.SitePermissionManager

@Composable
fun TurboStudioTab(
    state: WebAppUiState,
    onRequestBatteryBypass: () -> Unit,
    onClearAllCache: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hwAcceleration by remember { mutableStateOf(true) }
    var domStorage by remember { mutableStateOf(true) }
    var highRefreshRateRequested by remember { mutableStateOf(true) }
    var showOtaDialog by remember { mutableStateOf(false) }
    var showDevicePermissionsDialog by remember { mutableStateOf(false) }

    val appInfo = remember { OtaUpdateManager.getAppVersionInfo(context) }

    if (showOtaDialog) {
        RemoteControlOtaDialog(
            onDismissRequest = { showOtaDialog = false },
            onSheetsSyncClick = { }
        )
    }

    if (showDevicePermissionsDialog) {
        DevicePermissionsDialog(
            onDismiss = { showDevicePermissionsDialog = false }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Hero Performance Nexus Card (From Elegant Dark Design Spec)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = DeepPurpleContainer),
                border = androidx.compose.foundation.BorderStroke(1.dp, LavenderPrimary.copy(alpha = 0.25f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    DeepPurpleContainer,
                                    Color(0xFF2C1659),
                                    Color(0xFF1E103D)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = "Performance Nexus",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = LavenderOnContainer
                                )
                                Text(
                                    text = "Optimization Layer V2.4",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LavenderPrimary.copy(alpha = 0.8f),
                                    fontSize = 12.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(LavenderPrimary)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "ULTRA",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DeepPurpleOnPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Gauges from design
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Gauge 1: CPU Priority
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "CPU PRIORITY",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = LavenderPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "98%",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(5.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(CircleShape)
                                        .background(DeepPurpleTrack)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.98f)
                                            .height(8.dp)
                                            .clip(CircleShape)
                                            .background(LavenderPrimary)
                                    )
                                }
                            }

                            // Gauge 2: RAM Override
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "RAM OVERRIDE",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = LavenderPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "4.2GB ALLOC",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(5.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(CircleShape)
                                        .background(DeepPurpleTrack)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.75f)
                                            .height(8.dp)
                                            .clip(CircleShape)
                                            .background(LavenderPrimary)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Dual Action Buttons from design HTML
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onRequestBatteryBypass() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LavenderPrimary,
                                    contentColor = DeepPurpleOnPrimary
                                )
                            ) {
                                Text("BOOST CORE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = { onClearAllCache() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.1f),
                                    contentColor = TextWhite
                                )
                            ) {
                                Text("RESOURCE LOG", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Section: Energy Bypass Card (From Elegant Dark Design Spec)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(RoseAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = RoseDarkText,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Energy Bypass",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                            Text(
                                text = "Power saving disabled for render",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGrayMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = state.isBatterySaverBypassed,
                        onCheckedChange = { onRequestBatteryBypass() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DeepPurpleOnPrimary,
                            checkedTrackColor = LavenderPrimary,
                            uncheckedThumbColor = TextGrayMuted,
                            uncheckedTrackColor = ElegantCardBorder
                        ),
                        modifier = Modifier.testTag("energy_bypass_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // Section: Remote Control & OTA Updates Nexus
        item {
            Text(
                text = "Control Remoto & Actualizaciones OTA",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LavenderPrimary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeepPurpleContainer),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LavenderPrimary.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(LavenderPrimary, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = DeepPurpleOnPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Centro de Actualizaciones (15 Métodos)",
                                    fontWeight = FontWeight.Bold,
                                    color = LavenderOnContainer,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(DeepPurpleTrack)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "v${appInfo.versionName}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MintSpeed
                                    )
                                }
                            }
                            Text(
                                "Releases API, Local APK, URL directa, GDrive, Dropbox, LAN Wi-Fi, QR, Base64, Hot-Reload, Sheets y Backup",
                                fontSize = 11.sp,
                                color = TextGrayLight
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showOtaDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_open_ota_center"),
                            colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = DeepPurpleOnPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Abrir Centro OTA", color = DeepPurpleOnPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { showOtaDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LavenderPrimary.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = LavenderPrimary)
                        ) {
                            Icon(Icons.Default.SystemUpdate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Buscar APK", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Section: Developer Diagnostics (ADB & Connect)
        item {
            DeveloperDiagnosticsCard()
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Section: Shell Command Terminal & ADB Hardware Acceleration Engine
        item {
            CommandTerminalCard()
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Section: GitHub CI/CD Control Center
        item {
            GitHubControlPanelCard()
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Section: CI/CD Build Monitor & Artifacts Live Pipeline
        item {
            BuildMonitorView()
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Section: Automated Test Runner & Health Check (Roborazzi / JUnit)
        item {
            TestRunnerCard()
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Section: Development Activity Log & Revert Capabilities
        item {
            DevelopmentActivityLogCard()
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Section: System Logs Observability
        item {
            SystemLogsCard()
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Section: Appearance, Default Location & Floating Tools Configuration
        item {
            var defaultHomeUrl by remember { mutableStateOf(AppearanceSettingsManager.getDefaultHomeUrl(context)) }
            var dockPosition by remember { mutableStateOf(AppearanceSettingsManager.getDockPosition(context)) }
            var autoInjectCopy by remember { mutableStateOf(AppearanceSettingsManager.isAutoInjectCopyEnabled(context)) }

            Text(
                text = "Configuración de Apariencia & Ubicación",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LavenderPrimary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ElegantCard),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantCardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
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
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = LavenderPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Apariencia & Dock de Herramientas",
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Ubicación de inicio, posición del dock y botones de copia rápida",
                                fontSize = 11.sp,
                                color = TextGrayMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Default Location / URL
                    Text(
                        text = "Ubicación / URL de Inicio por Defecto",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LavenderOnContainer
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = defaultHomeUrl,
                        onValueChange = {
                            defaultHomeUrl = it
                            AppearanceSettingsManager.setDefaultHomeUrl(context, it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("https://google.com", color = TextGrayMuted, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Place, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(18.dp))
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LavenderPrimary,
                            unfocusedBorderColor = ElegantCardBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dock Position Selection
                    Text(
                        text = "Posición por Defecto del Botón Flotante Tools",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LavenderOnContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple(AppearanceSettingsManager.DOCK_POS_BOTTOM_CENTER, "Inferior Centro", "Abajo"),
                            Triple(AppearanceSettingsManager.DOCK_POS_BOTTOM_RIGHT, "Inferior Derecha", "Esquina"),
                            Triple(AppearanceSettingsManager.DOCK_POS_TOP_RIGHT, "Superior Derecha", "Arriba")
                        ).forEach { (posKey, label, sub) ->
                            val isSelected = dockPosition == posKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) LavenderPrimary.copy(alpha = 0.25f) else DeepPurpleContainer)
                                    .border(
                                        1.dp,
                                        if (isSelected) LavenderPrimary else ElegantCardBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        dockPosition = posKey
                                        AppearanceSettingsManager.setDockPosition(context, posKey)
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) LavenderPrimary else TextGrayLight
                                    )
                                    Text(
                                        text = sub,
                                        fontSize = 9.sp,
                                        color = if (isSelected) MintSpeed else TextGrayMuted
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Auto Copy Buttons Injection Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Auto-Inyectar Botones 'Copiar' en Chats Web & IDE",
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                fontSize = 13.sp
                            )
                            Text(
                                "Añade un botón de 1 toque a cada mensaje de IA, prompt y código",
                                fontSize = 11.sp,
                                color = TextGrayMuted
                            )
                        }
                        Switch(
                            checked = autoInjectCopy,
                            onCheckedChange = {
                                autoInjectCopy = it
                                AppearanceSettingsManager.setAutoInjectCopyEnabled(context, it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DeepPurpleOnPrimary,
                                checkedTrackColor = LavenderPrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
        item {
            Text(
                text = "Permisos del Dispositivo & Sitios Web",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LavenderPrimary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ElegantCard),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantCardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
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
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = LavenderPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Centro de Permisos y Hardware",
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Micrófono, Cámara, GPS, Selector de Archivos y Reglas por Sitio",
                                fontSize = 11.sp,
                                color = TextGrayMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val micOk = SitePermissionManager.isSystemMicGranted(context)
                        val camOk = SitePermissionManager.isSystemCameraGranted(context)
                        val locOk = SitePermissionManager.isSystemLocationGranted(context)
                        val filesOk = SitePermissionManager.isSystemStorageGranted(context)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (micOk) MintSpeed.copy(alpha = 0.15f) else AmberEnergy.copy(alpha = 0.15f))
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (micOk) "🎙️ Mic: Activo" else "🎙️ Mic: Pendiente",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (micOk) MintSpeed else AmberEnergy
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (camOk) MintSpeed.copy(alpha = 0.15f) else AmberEnergy.copy(alpha = 0.15f))
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (camOk) "📷 Cam: Activo" else "📷 Cam: Pendiente",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (camOk) MintSpeed else AmberEnergy
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (filesOk) MintSpeed.copy(alpha = 0.15f) else AmberEnergy.copy(alpha = 0.15f))
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (filesOk) "📎 Adjuntos: OK" else "📎 Adjuntos: Espera",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (filesOk) MintSpeed else AmberEnergy
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showDevicePermissionsDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = DeepPurpleOnPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gestionar Permisos y Hardware del Dispositivo",
                            color = DeepPurpleOnPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Section 2: Engine Switches Card
        item {
            Text(
                text = "Motor de Renderizado & WebEngine",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LavenderPrimary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ElegantCard),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantCardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // HW Accel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Aceleración GPU por Capas", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 14.sp)
                            Text("Renderiza gráficos y animaciones CSS a través de Vulkan/OpenGL", fontSize = 11.sp, color = TextGrayMuted)
                        }
                        Switch(
                            checked = hwAcceleration,
                            onCheckedChange = { hwAcceleration = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = DeepPurpleOnPrimary, checkedTrackColor = LavenderPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // DOM Storage & IndexedDB
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Almacenamiento DOM & Carga Instantánea", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 14.sp)
                            Text("Guarda estados y assets localmente para abrir a velocidad relámpago", fontSize = 11.sp, color = TextGrayMuted)
                        }
                        Switch(
                            checked = domStorage,
                            onCheckedChange = { domStorage = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = DeepPurpleOnPrimary, checkedTrackColor = LavenderPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // High Refresh Rate
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Forzar Tasa de Refresco 120Hz / 90Hz", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 14.sp)
                            Text("Fluidez ultra suave en desplazamientos y animaciones", fontSize = 11.sp, color = TextGrayMuted)
                        }
                        Switch(
                            checked = highRefreshRateRequested,
                            onCheckedChange = { highRefreshRateRequested = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = DeepPurpleOnPrimary, checkedTrackColor = LavenderPrimary)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Section 3: Clean Cache & Data
        item {
            Text(
                text = "Mantenimiento & Almacenamiento",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LavenderPrimary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ElegantCard),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantCardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(DeepPurpleContainer, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CleaningServices, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Limpiar Memoria Caché y Cookies", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 14.sp)
                            Text("Libera memoria RAM y espacio en disco si algún sitio va lento", fontSize = 11.sp, color = TextGrayMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = onClearAllCache,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("clear_cache_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LavenderPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LavenderPrimary.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Limpiar Todo el Almacenamiento Temporal", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Section 4: How shortcuts work guide
        item {
            Text(
                text = "Guía: Accesos Directos e Instalación en Pantalla",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LavenderPrimary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ElegantCard),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantCardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PushPin, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("¿Cómo funcionan los accesos directos?", fontWeight = FontWeight.Bold, color = TextWhite)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Toca 'Fijar' en cualquier app o en el menú de tres puntos.\n" +
                                "2. El sistema creará un icono independiente en tu pantalla de inicio con el logo y color que elegiste.\n" +
                                "3. Al pulsarlo, se abrirá directamente a pantalla completa como una app nativa, sin barras de navegación ni esperas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGrayLight,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
