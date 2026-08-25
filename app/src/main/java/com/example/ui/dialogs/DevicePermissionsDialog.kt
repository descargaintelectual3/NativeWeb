package com.example.ui.dialogs

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AmberEnergy
import com.example.ui.theme.CoralError
import com.example.ui.theme.DeepPurpleContainer
import com.example.ui.theme.DeepPurpleOnPrimary
import com.example.ui.theme.ElegantCard
import com.example.ui.theme.ElegantCardBorder
import com.example.ui.theme.ElegantSurface
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.MintSpeed
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextGrayMuted
import com.example.ui.theme.TextWhite
import com.example.util.SitePermissionManager
import com.example.util.SitePermissionRule

@Composable
fun DevicePermissionsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var isSysMic by remember { mutableStateOf(SitePermissionManager.isSystemMicGranted(context)) }
    var isSysCam by remember { mutableStateOf(SitePermissionManager.isSystemCameraGranted(context)) }
    var isSysLoc by remember { mutableStateOf(SitePermissionManager.isSystemLocationGranted(context)) }
    var isSysStorage by remember { mutableStateOf(SitePermissionManager.isSystemStorageGranted(context)) }

    var globalMic by remember { mutableStateOf(SitePermissionManager.isGlobalMicEnabled(context)) }
    var globalCam by remember { mutableStateOf(SitePermissionManager.isGlobalCamEnabled(context)) }
    var globalLoc by remember { mutableStateOf(SitePermissionManager.isGlobalLocEnabled(context)) }
    var globalFiles by remember { mutableStateOf(SitePermissionManager.isGlobalFilesEnabled(context)) }
    var globalAutoplay by remember { mutableStateOf(SitePermissionManager.isGlobalAutoplayEnabled(context)) }

    var customRules by remember { mutableStateOf(SitePermissionManager.getAllSiteRules(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        isSysMic = SitePermissionManager.isSystemMicGranted(context)
        isSysCam = SitePermissionManager.isSystemCameraGranted(context)
        isSysLoc = SitePermissionManager.isSystemLocationGranted(context)
        isSysStorage = SitePermissionManager.isSystemStorageGranted(context)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, ElegantCardBorder, RoundedCornerShape(24.dp)),
            color = ElegantSurface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(LavenderPrimary.copy(alpha = 0.15f)),
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
                        Column {
                            Text(
                                text = "Permisos & Dispositivo",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = "Micrófono, Cámara, Archivos & WebRTC",
                                fontSize = 11.sp,
                                color = TextGrayLight
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = TextGrayLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 1: Android System Status
                Text(
                    text = "ESTADO EN EL SISTEMA ANDROID",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGrayMuted,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SystemStatusPill(
                        label = "Micrófono",
                        granted = isSysMic,
                        modifier = Modifier.weight(1f)
                    )
                    SystemStatusPill(
                        label = "Cámara",
                        granted = isSysCam,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SystemStatusPill(
                        label = "GPS / Ubicación",
                        granted = isSysLoc,
                        modifier = Modifier.weight(1f)
                    )
                    SystemStatusPill(
                        label = "Archivos / Fotos",
                        granted = isSysStorage,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        permissionLauncher.launch(SitePermissionManager.REQUIRED_SYSTEM_PERMISSIONS)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = DeepPurpleOnPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Solicitar Todos los Permisos de Android",
                        color = DeepPurpleOnPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section 2: Global Web Defaults
                Text(
                    text = "PERMISOS POR DEFECTO PARA SITIOS WEB",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGrayMuted,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                GlobalDefaultToggleRow(
                    icon = Icons.Default.Mic,
                    title = "Permitir Micrófono siempre",
                    enabled = globalMic,
                    onToggle = {
                        globalMic = it
                        SitePermissionManager.setGlobalMicEnabled(context, it)
                    }
                )

                GlobalDefaultToggleRow(
                    icon = Icons.Default.CameraAlt,
                    title = "Permitir Cámara y Video WebRTC",
                    enabled = globalCam,
                    onToggle = {
                        globalCam = it
                        SitePermissionManager.setGlobalCamEnabled(context, it)
                    }
                )

                GlobalDefaultToggleRow(
                    icon = Icons.Default.AttachFile,
                    title = "Permitir Adjuntos y Selector de Archivos",
                    enabled = globalFiles,
                    onToggle = {
                        globalFiles = it
                        SitePermissionManager.setGlobalFilesEnabled(context, it)
                    }
                )

                GlobalDefaultToggleRow(
                    icon = Icons.Default.LocationOn,
                    title = "Permitir Ubicación Geográfica (GPS)",
                    enabled = globalLoc,
                    onToggle = {
                        globalLoc = it
                        SitePermissionManager.setGlobalLocEnabled(context, it)
                    }
                )

                GlobalDefaultToggleRow(
                    icon = Icons.Default.VolumeUp,
                    title = "Permitir Autoplay de Audio y Sonido",
                    enabled = globalAutoplay,
                    onToggle = {
                        globalAutoplay = it
                        SitePermissionManager.setGlobalAutoplayEnabled(context, it)
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Section 3: Configured Site Rules
                if (customRules.isNotEmpty()) {
                    Text(
                        text = "REGLAS PERSONALIZADAS POR SITIO (${customRules.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextGrayMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    customRules.values.forEach { rule ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = ElegantCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantCardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(rule.domain, fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 13.sp)
                                    Text(
                                        text = "Mic: ${if (rule.micAllowed) "✅" else "❌"}  Cam: ${if (rule.cameraAllowed) "✅" else "❌"}  Adjuntos: ${if (rule.filesAllowed) "✅" else "❌"}",
                                        fontSize = 10.sp,
                                        color = TextGrayLight
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        SitePermissionManager.resetSiteRule(context, rule.domain)
                                        customRules = SitePermissionManager.getAllSiteRules(context)
                                    },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Borrar regla",
                                        tint = CoralError,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = LavenderPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Abrir Ajustes de Sistema de la Aplicación",
                        fontSize = 12.sp,
                        color = LavenderPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun SystemStatusPill(
    label: String,
    granted: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (granted) MintSpeed.copy(alpha = 0.15f) else CoralError.copy(alpha = 0.15f))
            .border(
                1.dp,
                if (granted) MintSpeed.copy(alpha = 0.4f) else CoralError.copy(alpha = 0.4f),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(label, fontSize = 11.sp, color = TextWhite, fontWeight = FontWeight.Medium)
            Text(
                text = if (granted) "Activo" else "Falta",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (granted) MintSpeed else CoralError
            )
        }
    }
}

@Composable
private fun GlobalDefaultToggleRow(
    icon: ImageVector,
    title: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) LavenderPrimary else TextGrayMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(title, fontSize = 12.sp, color = TextWhite, fontWeight = FontWeight.Medium)
            }

            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                modifier = Modifier.size(38.dp, 24.dp),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = DeepPurpleOnPrimary,
                    checkedTrackColor = LavenderPrimary,
                    uncheckedThumbColor = TextGrayMuted,
                    uncheckedTrackColor = DeepPurpleContainer
                )
            )
        }
    }
}
