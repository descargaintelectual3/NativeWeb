package com.example.ui.dialogs

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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
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
fun SitePermissionsDialog(
    currentUrl: String,
    onDismiss: () -> Unit,
    onReloadRequested: () -> Unit
) {
    val context = LocalContext.current
    val domain = remember(currentUrl) { SitePermissionManager.extractDomain(currentUrl) }
    val initialRule = remember(domain) { SitePermissionManager.getSiteRule(context, domain) }

    var micAllowed by remember { mutableStateOf(initialRule.micAllowed) }
    var cameraAllowed by remember { mutableStateOf(initialRule.cameraAllowed) }
    var locationAllowed by remember { mutableStateOf(initialRule.locationAllowed) }
    var filesAllowed by remember { mutableStateOf(initialRule.filesAllowed) }
    var adBlockEnabled by remember { mutableStateOf(initialRule.adBlockEnabled) }
    var autoplayAudio by remember { mutableStateOf(initialRule.autoPlayAudio) }

    val isSysMic = remember { SitePermissionManager.isSystemMicGranted(context) }
    val isSysCam = remember { SitePermissionManager.isSystemCameraGranted(context) }
    val isSysLoc = remember { SitePermissionManager.isSystemLocationGranted(context) }
    val isSysStorage = remember { SitePermissionManager.isSystemStorageGranted(context) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
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
                                text = "Permisos del Sitio",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = domain,
                                fontSize = 12.sp,
                                color = LavenderPrimary,
                                fontWeight = FontWeight.Medium
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

                // System Warning Banner if any hardware permission is not granted in Android OS
                if (!isSysMic || !isSysCam || !isSysLoc || !isSysStorage) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = AmberEnergy.copy(alpha = 0.15f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AmberEnergy.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚠️", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Algunos permisos están pendientes a nivel de Android. Actívalos en el Centro de Control de la app principal.",
                                fontSize = 11.sp,
                                color = TextWhite,
                                lineHeight = 14.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                Text(
                    text = "CONTROL DE HARDWARE Y FUNCIONES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGrayMuted,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Toggles List
                PermissionToggleRow(
                    icon = Icons.Default.Mic,
                    title = "Micrófono y Reconocimiento de Voz",
                    subtitle = "Permite dictado, WebRTC y entrada de voz en vivo.",
                    enabled = micAllowed,
                    onToggle = { micAllowed = it }
                )

                PermissionToggleRow(
                    icon = Icons.Default.CameraAlt,
                    title = "Cámara y Video WebRTC",
                    subtitle = "Acceso a captura de cámara y videollamadas web.",
                    enabled = cameraAllowed,
                    onToggle = { cameraAllowed = it }
                )

                PermissionToggleRow(
                    icon = Icons.Default.AttachFile,
                    title = "Subida de Archivos y Adjuntos",
                    subtitle = "Permite que botones de adjuntar abran el selector de archivos.",
                    enabled = filesAllowed,
                    onToggle = { filesAllowed = it }
                )

                PermissionToggleRow(
                    icon = Icons.Default.LocationOn,
                    title = "Ubicación Geográfica (GPS)",
                    subtitle = "Permite al sitio acceder a tu posición precisa.",
                    enabled = locationAllowed,
                    onToggle = { locationAllowed = it }
                )

                PermissionToggleRow(
                    icon = Icons.Default.VolumeUp,
                    title = "Reproducción Automática de Audio",
                    subtitle = "Permite sonidos y síntesis de voz sin requerir clics previos.",
                    enabled = autoplayAudio,
                    onToggle = { autoplayAudio = it }
                )

                PermissionToggleRow(
                    icon = Icons.Default.Block,
                    title = "Bloqueador de Anuncios y Trackers",
                    subtitle = "Elimina banners y rastreadores invasivos para este sitio.",
                    enabled = adBlockEnabled,
                    onToggle = { adBlockEnabled = it }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            // Quick enable all
                            micAllowed = true
                            cameraAllowed = true
                            locationAllowed = true
                            filesAllowed = true
                            autoplayAudio = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Permitir Todo", fontSize = 12.sp, color = LavenderPrimary)
                    }

                    Button(
                        onClick = {
                            val newRule = SitePermissionRule(
                                domain = domain,
                                micAllowed = micAllowed,
                                cameraAllowed = cameraAllowed,
                                locationAllowed = locationAllowed,
                                filesAllowed = filesAllowed,
                                adBlockEnabled = adBlockEnabled,
                                autoPlayAudio = autoplayAudio
                            )
                            SitePermissionManager.saveSiteRule(context, newRule)
                            onReloadRequested()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Guardar y Aplicar", fontSize = 12.sp, color = DeepPurpleOnPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (enabled) LavenderPrimary.copy(alpha = 0.2f) else DeepPurpleContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) LavenderPrimary else TextGrayMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite
                    )
                    Text(
                        text = subtitle,
                        fontSize = 10.sp,
                        color = TextGrayLight,
                        lineHeight = 13.sp
                    )
                }
            }

            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
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
