package com.example.ui.dialogs

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AmberEnergy
import com.example.ui.theme.CoralError
import com.example.ui.theme.DeepPurpleContainer
import com.example.ui.theme.DeepPurpleOnPrimary
import com.example.ui.theme.DeepPurpleTrack
import com.example.ui.theme.ElegantBackground
import com.example.ui.theme.ElegantCard
import com.example.ui.theme.ElegantCardBorder
import com.example.ui.theme.LavenderOnContainer
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.MintSpeed
import com.example.ui.theme.RoseAccent
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextGrayMuted
import com.example.ui.theme.TextWhite
import com.example.util.MultiUpdateEngines
import com.example.util.OtaNotificationHelper
import com.example.util.OtaUpdateManager
import com.example.util.RemoteConfigEngine
import com.example.util.RemoteSyncState
import com.example.util.UpdateStatus
import kotlinx.coroutines.launch
import java.io.File

data class UpdateMethodItem(
    val id: Int,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val category: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RemoteControlOtaDialog(
    onDismissRequest: () -> Unit,
    onSheetsSyncClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val updateState by OtaUpdateManager.updateState.collectAsStateWithLifecycle()
    val remoteSyncState by RemoteConfigEngine.syncState.collectAsStateWithLifecycle()

    val appInfo = remember { OtaUpdateManager.getAppVersionInfo(context) }

    // 15 Methods Definition
    val allMethods = remember {
        listOf(
            UpdateMethodItem(1, "1. Auto GitHub Releases", "API y detección automática", Icons.Default.Bolt, "Binario"),
            UpdateMethodItem(2, "2. Selector APK Local", "Almacenamiento y descargas", Icons.Default.Folder, "Local"),
            UpdateMethodItem(3, "3. URL Directa HTTP/HTTPS", "Descarga desde servidor VPS/Web", Icons.Default.Link, "Binario"),
            UpdateMethodItem(4, "4. Portal Web Releases", "Descarga manual en navegador", Icons.Default.Public, "Web"),
            UpdateMethodItem(5, "5. Google Sheets Sync", "Hot-Reload en vivo sin reinstalar", Icons.Default.TableChart, "OTA"),
            UpdateMethodItem(6, "6. Endpoint JSON Dinámico", "Sincronización CDN/REST", Icons.Default.CloudSync, "OTA"),
            UpdateMethodItem(7, "7. Git Raw Branch Artifact", "APK precompilado de rama main", Icons.Default.Code, "Binario"),
            UpdateMethodItem(8, "8. Google Drive Direct", "Conversor de enlace compartido", Icons.Default.CloudDownload, "Nube"),
            UpdateMethodItem(9, "9. Dropbox Direct Sync", "Descarga directa desde enlace Dropbox", Icons.Default.Download, "Nube"),
            UpdateMethodItem(10, "10. Servidor Wi-Fi / LAN P2P", "Sideload inalámbrico en red local", Icons.Default.Wifi, "Red"),
            UpdateMethodItem(11, "11. Escáner QR / Deep Link", "Actualización por enlace o cámara", Icons.Default.QrCode, "Directo"),
            UpdateMethodItem(12, "12. Base64 / Hex Patch", "Decodificador de binario en memoria", Icons.Default.DataObject, "Avanzado"),
            UpdateMethodItem(13, "13. Inyector CSS/JS Remoto", "Reglas y temas en tiempo real", Icons.Default.Palette, "OTA"),
            UpdateMethodItem(14, "14. Poller & Webhook Push", "Búsqueda periódica programada", Icons.Default.Schedule, "Automático"),
            UpdateMethodItem(15, "15. Backup, Restore & Migración", "Importar/Exportar catálogo completo", Icons.Default.Backup, "Datos")
        )
    }

    var selectedMethodIndex by remember { mutableIntStateOf(0) }

    // Dynamic inputs
    var manifestUrlInput by remember { mutableStateOf(OtaUpdateManager.getManifestUrl(context)) }
    var customDirectApkInput by remember {
        mutableStateOf(
            OtaUpdateManager.getCustomDirectApkUrl(context).ifBlank { OtaUpdateManager.PRESET_DEFAULT_DIRECT_APK }
        )
    }
    var remoteCatalogUrlInput by remember { mutableStateOf(RemoteConfigEngine.getRemoteSourceUrl(context)) }
    var gdriveUrlInput by remember { mutableStateOf("") }
    var dropboxUrlInput by remember { mutableStateOf("") }
    var lanIpInput by remember { mutableStateOf("http://192.168.1.100:8080/app-debug.apk") }
    var qrUrlInput by remember { mutableStateOf("") }
    var base64Input by remember { mutableStateOf("") }
    var backupJsonString by remember { mutableStateOf("") }

    var globalCssInput by remember { mutableStateOf(RemoteConfigEngine.getGlobalCustomCss(context)) }
    var globalJsInput by remember { mutableStateOf(RemoteConfigEngine.getGlobalCustomJs(context)) }

    var autoCheckUpdates by remember { mutableStateOf(OtaUpdateManager.isAutoCheckEnabled(context)) }
    var pushNotifications by remember { mutableStateOf(OtaUpdateManager.isPushNotificationsEnabled(context)) }
    var autoSyncCatalog by remember { mutableStateOf(RemoteConfigEngine.isAutoSyncEnabled(context)) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        pushNotifications = granted || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        OtaUpdateManager.setPushNotificationsEnabled(context, pushNotifications)
    }

    // Method 2 File Picker Launcher
    val apkFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val result = OtaUpdateManager.importAndInstallFromLocalUri(context, uri)
                result.fold(
                    onSuccess = { file ->
                        Toast.makeText(context, "APK importado con éxito. Abriendo instalador...", Toast.LENGTH_SHORT).show()
                        OtaUpdateManager.promptInstallApk(context, file)
                    },
                    onFailure = { err ->
                        Toast.makeText(context, "Error: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        if (updateState is UpdateStatus.Idle) {
            OtaUpdateManager.checkForUpdates(context)
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .widthIn(max = 680.dp)
                .heightIn(max = 820.dp)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(26.dp),
            color = ElegantCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantCardBorder),
            tonalElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                // Header with App Identity & Real Version Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(
                                    Brush.linearGradient(listOf(LavenderPrimary, DeepPurpleContainer))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RocketLaunch,
                                contentDescription = null,
                                tint = DeepPurpleOnPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Centro de Actualizaciones 15-in-1",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = TextWhite
                            )
                            Text(
                                text = "15 Canales Multicanal de Actualización",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGrayMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // CURRENT INSTALLED VERSION CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ElegantBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LavenderPrimary.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(MintSpeed)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "VERSIÓN ACTIVA EN DISPOSITIVO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = LavenderPrimary,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DeepPurpleContainer)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "v${appInfo.versionName}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextWhite
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            InfoBadge(label = "Build Code", value = "${appInfo.versionCode}", modifier = Modifier.weight(1f))
                            InfoBadge(label = "ID Paquete", value = "turbovx", modifier = Modifier.weight(1f))
                            InfoBadge(label = "Instalado", value = appInfo.firstInstallTime.take(10), modifier = Modifier.weight(1f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable 15-Method Selector Bar
                Text(
                    text = "SELECCIONA ENTRE LOS 15 MÉTODOS DE ACTUALIZACIÓN",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = TextGrayMuted,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                ScrollableTabRow(
                    selectedTabIndex = selectedMethodIndex,
                    containerColor = ElegantBackground,
                    contentColor = LavenderPrimary,
                    edgePadding = 8.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedMethodIndex]),
                            color = LavenderPrimary,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, ElegantCardBorder, RoundedCornerShape(12.dp))
                ) {
                    allMethods.forEachIndexed { index, item ->
                        Tab(
                            selected = selectedMethodIndex == index,
                            onClick = { selectedMethodIndex = index },
                            text = {
                                Text(
                                    text = item.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedMethodIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (selectedMethodIndex == index) LavenderPrimary else TextGrayMuted
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // DYNAMIC CONTENT FOR ALL 15 METHODS
                when (selectedMethodIndex) {
                    0 -> Method1AutoGithub(
                        updateState = updateState,
                        manifestUrl = manifestUrlInput,
                        onManifestUrlChange = {
                            manifestUrlInput = it
                            OtaUpdateManager.setManifestUrl(context, it)
                        },
                        onCheckUpdates = {
                            scope.launch { OtaUpdateManager.checkForUpdates(context, manifestUrlInput) }
                        },
                        onDownloadAndInstall = { downloadUrl, versionName ->
                            scope.launch {
                                val result = OtaUpdateManager.downloadAndPrepareInstall(context, downloadUrl, versionName)
                                result.onSuccess { apkFile ->
                                    OtaUpdateManager.promptInstallApk(context, apkFile)
                                }
                            }
                        }
                    )

                    1 -> Method2LocalApkPicker(
                        onPickFileClick = {
                            try {
                                apkFilePickerLauncher.launch("application/vnd.android.package-archive")
                            } catch (e: Exception) {
                                apkFilePickerLauncher.launch("*/*")
                            }
                        }
                    )

                    2 -> Method3DirectUrlDownloader(
                        updateState = updateState,
                        directUrl = customDirectApkInput,
                        onDirectUrlChange = {
                            customDirectApkInput = it
                            OtaUpdateManager.setCustomDirectApkUrl(context, it)
                        },
                        onDownloadAndInstall = { url ->
                            scope.launch {
                                val result = OtaUpdateManager.downloadAndPrepareInstall(context, url, "custom_url")
                                result.fold(
                                    onSuccess = { apkFile ->
                                        Toast.makeText(context, "Descarga lista. Abriendo instalador...", Toast.LENGTH_SHORT).show()
                                        OtaUpdateManager.promptInstallApk(context, apkFile)
                                    },
                                    onFailure = { err ->
                                        Toast.makeText(context, "Error: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        }
                    )

                    3 -> Method4WebReleases(
                        onOpenWebReleases = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(OtaUpdateManager.PRESET_GITHUB_RELEASES_WEB)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        },
                        onOpenRepo = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(OtaUpdateManager.GITHUB_REPO_WEB)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }
                    )

                    4 -> Method5LiveSheetsSync(
                        remoteSyncState = remoteSyncState,
                        catalogUrl = remoteCatalogUrlInput,
                        onCatalogUrlChange = {
                            remoteCatalogUrlInput = it
                            RemoteConfigEngine.setRemoteSourceUrl(context, it)
                        },
                        onPerformSync = {
                            scope.launch {
                                RemoteConfigEngine.performRemoteSync(context, this, remoteCatalogUrlInput)
                            }
                        },
                        onOpenSheetsSync = onSheetsSyncClick
                    )

                    5 -> Method6JsonEndpointSync(
                        remoteSyncState = remoteSyncState,
                        endpointUrl = remoteCatalogUrlInput,
                        onEndpointUrlChange = {
                            remoteCatalogUrlInput = it
                            RemoteConfigEngine.setRemoteSourceUrl(context, it)
                        },
                        onPerformSync = {
                            scope.launch {
                                RemoteConfigEngine.performRemoteSync(context, this, remoteCatalogUrlInput)
                            }
                        }
                    )

                    6 -> Method7GitRawBranchArtifact(
                        onDownloadAndInstall = {
                            scope.launch {
                                val result = OtaUpdateManager.downloadAndPrepareInstall(context, OtaUpdateManager.PRESET_RAW_MAIN_APK, "raw_main")
                                result.fold(
                                    onSuccess = { apkFile ->
                                        Toast.makeText(context, "APK descargado de rama principal.", Toast.LENGTH_SHORT).show()
                                        OtaUpdateManager.promptInstallApk(context, apkFile)
                                    },
                                    onFailure = { err ->
                                        Toast.makeText(context, "Error: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        }
                    )

                    7 -> Method8GoogleDriveConverter(
                        gdriveUrl = gdriveUrlInput,
                        onGdriveUrlChange = { gdriveUrlInput = it },
                        onDownloadAndInstall = { rawGdrive ->
                            val converted = MultiUpdateEngines.convertGoogleDriveUrl(rawGdrive)
                            scope.launch {
                                val result = OtaUpdateManager.downloadAndPrepareInstall(context, converted, "gdrive")
                                result.fold(
                                    onSuccess = { apkFile ->
                                        OtaUpdateManager.promptInstallApk(context, apkFile)
                                    },
                                    onFailure = { err ->
                                        Toast.makeText(context, "Error: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        }
                    )

                    8 -> Method9DropboxConverter(
                        dropboxUrl = dropboxUrlInput,
                        onDropboxUrlChange = { dropboxUrlInput = it },
                        onDownloadAndInstall = { rawDropbox ->
                            val converted = MultiUpdateEngines.convertDropboxUrl(rawDropbox)
                            scope.launch {
                                val result = OtaUpdateManager.downloadAndPrepareInstall(context, converted, "dropbox")
                                result.fold(
                                    onSuccess = { apkFile ->
                                        OtaUpdateManager.promptInstallApk(context, apkFile)
                                    },
                                    onFailure = { err ->
                                        Toast.makeText(context, "Error: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        }
                    )

                    9 -> Method10LanWifiServer(
                        lanUrl = lanIpInput,
                        onLanUrlChange = { lanIpInput = it },
                        onDownloadAndInstall = { url ->
                            scope.launch {
                                val result = OtaUpdateManager.downloadAndPrepareInstall(context, url, "lan_wifi")
                                result.fold(
                                    onSuccess = { apkFile ->
                                        OtaUpdateManager.promptInstallApk(context, apkFile)
                                    },
                                    onFailure = { err ->
                                        Toast.makeText(context, "Error conectando a LAN: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        }
                    )

                    10 -> Method11QrCodeDeepLink(
                        qrUrl = qrUrlInput,
                        onQrUrlChange = { qrUrlInput = it },
                        onProcessUrl = { url ->
                            if (url.endsWith(".apk") || url.contains("apk")) {
                                scope.launch {
                                    val result = OtaUpdateManager.downloadAndPrepareInstall(context, url, "qr_scan")
                                    result.onSuccess { OtaUpdateManager.promptInstallApk(context, it) }
                                }
                            } else {
                                scope.launch {
                                    RemoteConfigEngine.performRemoteSync(context, this, url)
                                }
                            }
                        }
                    )

                    11 -> Method12Base64Decoder(
                        base64String = base64Input,
                        onBase64Change = { base64Input = it },
                        onDecodeAndInstall = { payload ->
                            scope.launch {
                                val result = MultiUpdateEngines.decodeBase64ToApk(context, payload)
                                result.fold(
                                    onSuccess = { apkFile ->
                                        Toast.makeText(context, "Base64 decodificado con éxito.", Toast.LENGTH_SHORT).show()
                                        OtaUpdateManager.promptInstallApk(context, apkFile)
                                    },
                                    onFailure = { err ->
                                        Toast.makeText(context, "Error: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        }
                    )

                    12 -> Method13RemoteCssJsInjector(
                        globalCss = globalCssInput,
                        onGlobalCssChange = {
                            globalCssInput = it
                            RemoteConfigEngine.setGlobalCustomCss(context, it)
                        },
                        globalJs = globalJsInput,
                        onGlobalJsChange = {
                            globalJsInput = it
                            RemoteConfigEngine.setGlobalCustomJs(context, it)
                        }
                    )

                    13 -> Method14ScheduledPoller(
                        autoCheck = autoCheckUpdates,
                        onAutoCheckChange = {
                            autoCheckUpdates = it
                            OtaUpdateManager.setAutoCheckEnabled(context, it)
                        },
                        pushNotifications = pushNotifications,
                        onPushNotificationsChange = { enabled ->
                            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                pushNotifications = enabled
                                OtaUpdateManager.setPushNotificationsEnabled(context, enabled)
                            }
                        },
                        autoSyncCatalog = autoSyncCatalog,
                        onAutoSyncCatalogChange = {
                            autoSyncCatalog = it
                            RemoteConfigEngine.setAutoSyncEnabled(context, it)
                        }
                    )

                    14 -> Method15BackupRestoreMigration(
                        backupJson = backupJsonString,
                        onBackupJsonChange = { backupJsonString = it },
                        onExportBackup = {
                            scope.launch {
                                val json = MultiUpdateEngines.exportFullBackupJson(context, this)
                                backupJsonString = json
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("WebNative Backup", json))
                                Toast.makeText(context, "Copia de seguridad exportada y copiada al portapapeles.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onImportBackup = { json ->
                            scope.launch {
                                val result = MultiUpdateEngines.importFullBackupJson(context, json, this)
                                result.fold(
                                    onSuccess = { count ->
                                        Toast.makeText(context, "Se restauraron $count aplicaciones y configuraciones.", Toast.LENGTH_LONG).show()
                                    },
                                    onFailure = { err ->
                                        Toast.makeText(context, "Error: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Close Button
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepPurpleContainer)
                ) {
                    Text(
                        text = "Cerrar Centro de Actualizaciones",
                        fontWeight = FontWeight.Bold,
                        color = LavenderPrimary
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// MÉTODOS 1 AL 15 (DESGLOSE MODULAR)
// -------------------------------------------------------------

@Composable
private fun Method1AutoGithub(
    updateState: UpdateStatus,
    manifestUrl: String,
    onManifestUrlChange: (String) -> Unit,
    onCheckUpdates: () -> Unit,
    onDownloadAndInstall: (downloadUrl: String, versionName: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var probeResult by remember { mutableStateOf<OtaUpdateManager.UrlCheckResult?>(null) }
    var isProbing by remember { mutableStateOf(false) }

    var ghToken by remember { mutableStateOf(com.example.util.GitHubApiAutomation.getGitHubToken(context)) }
    var ghAutomationStatus by remember { mutableStateOf<com.example.util.GitHubApiAutomation.GitHubActionResult?>(null) }
    var isExecutingGhAction by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Método 1: Auto-Descarga GitHub Releases API", fontWeight = FontWeight.Bold, color = LavenderPrimary, fontSize = 14.sp)
        Text("Consulta la API de lanzamientos, busca binarios .apk y los descarga con indicador de progreso.", fontSize = 12.sp, color = TextGrayLight)

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = manifestUrl,
            onValueChange = onManifestUrlChange,
            label = { Text("Endpoint Releases GitHub API") },
            leadingIcon = { Icon(Icons.Default.Bolt, contentDescription = null, tint = LavenderPrimary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LavenderPrimary,
                unfocusedBorderColor = ElegantCardBorder,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Diagnostic probe button & status
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onCheckUpdates,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Comprobar", fontSize = 12.sp)
            }

            Button(
                onClick = {
                    scope.launch {
                        isProbing = true
                        probeResult = OtaUpdateManager.verifyRemoteUrlReal(manifestUrl)
                        isProbing = false
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DeepPurpleContainer)
            ) {
                if (isProbing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = LavenderPrimary, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Info, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Verificar Real", color = LavenderPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Auto-GitHub CI/CD and Release Publisher Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ElegantCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, ElegantCardBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = AmberEnergy, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Acceso GitHub Automático (Zero Clics)", fontWeight = FontWeight.Bold, color = AmberEnergy, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Conecta tu token para que la app dispare flujos de GitHub Actions y publique releases sin salir de la app.",
                    fontSize = 11.sp,
                    color = TextGrayLight
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = ghToken,
                    onValueChange = {
                        ghToken = it
                        com.example.util.GitHubApiAutomation.saveGitHubToken(context, it)
                    },
                    label = { Text("GitHub Token (PAT / GHP)", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberEnergy,
                        unfocusedBorderColor = ElegantCardBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                isExecutingGhAction = true
                                ghAutomationStatus = com.example.util.GitHubApiAutomation.verifyToken(ghToken)
                                isExecutingGhAction = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepPurpleContainer)
                    ) {
                        Text("Verificar Token", fontSize = 11.sp, color = LavenderPrimary)
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                isExecutingGhAction = true
                                ghAutomationStatus = com.example.util.GitHubApiAutomation.triggerWorkflowDispatch(context)
                                isExecutingGhAction = false
                            }
                        },
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberEnergy)
                    ) {
                        if (isExecutingGhAction) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = DeepPurpleOnPrimary, strokeWidth = 2.dp)
                        } else {
                            Text("Disparar CI/CD", fontSize = 11.sp, color = DeepPurpleOnPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (ghAutomationStatus != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val res = ghAutomationStatus!!
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (res.success) Color(0xFF0F382A) else Color(0xFF381414)
                        )
                    ) {
                        Text(
                            text = res.message,
                            fontSize = 11.sp,
                            color = if (res.success) MintSpeed else CoralError,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }

        if (probeResult != null) {
            Spacer(modifier = Modifier.height(8.dp))
            val probe = probeResult!!
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (probe.exists) Color(0xFF0F382A) else Color(0xFF381414)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (probe.exists) MintSpeed else CoralError)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (probe.exists) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (probe.exists) MintSpeed else CoralError,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (probe.exists) "Servidor Activo (HTTP ${probe.httpCode} OK)" else "Verificación Fallida (HTTP ${if (probe.httpCode > 0) probe.httpCode else "Error"})",
                            fontWeight = FontWeight.Bold,
                            color = if (probe.exists) MintSpeed else CoralError,
                            fontSize = 12.sp
                        )
                    }
                    if (probe.errorMessage != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(probe.errorMessage, fontSize = 11.sp, color = TextWhite)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Tipo MIME: ${probe.contentType} • Latencia: ${probe.responseTimeMs}ms • Tamaño: ${if (probe.contentLength > 0) "${probe.contentLength / (1024 * 1024)} MB" else "No reportado"}",
                        fontSize = 10.sp,
                        color = TextGrayLight
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (updateState) {
            is UpdateStatus.Checking -> {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = ElegantBackground)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = LavenderPrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Consultando API y verificando existencia de APK...", color = TextWhite, fontSize = 12.sp)
                    }
                }
            }
            is UpdateStatus.Downloading -> {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = DeepPurpleContainer)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Descargando actualización...", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 12.sp)
                            Text("${updateState.progressPercent}%", fontWeight = FontWeight.Black, color = MintSpeed, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { updateState.progressPercent / 100f },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = MintSpeed,
                            trackColor = ElegantBackground
                        )
                    }
                }
            }
            is UpdateStatus.ReadyToInstall -> {
                Button(
                    onClick = { OtaUpdateManager.promptInstallApk(context, updateState.apkFile) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MintSpeed)
                ) {
                    Icon(Icons.Default.InstallMobile, contentDescription = null, tint = DeepPurpleOnPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Abrir Instalador de Android", color = DeepPurpleOnPrimary, fontWeight = FontWeight.Bold)
                }
            }
            is UpdateStatus.UpdateAvailable -> {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = ElegantBackground)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Versión detectada: ${updateState.versionName}", fontWeight = FontWeight.Bold, color = LavenderPrimary, fontSize = 13.sp)
                        Text(updateState.changelog, fontSize = 11.sp, color = TextGrayLight, maxLines = 2)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onDownloadAndInstall(updateState.downloadUrl, updateState.versionName) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary)
                        ) {
                            Text("Descargar e Instalar Ahora", color = DeepPurpleOnPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            is UpdateStatus.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF381414)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CoralError.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = CoralError, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Aviso del Servidor", fontWeight = FontWeight.Bold, color = CoralError, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(updateState.message, color = TextWhite, fontSize = 11.sp)
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun Method2LocalApkPicker(onPickFileClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Método 2: Instalar APK desde Almacenamiento Local", fontWeight = FontWeight.Bold, color = LavenderPrimary, fontSize = 14.sp)
        Text("Selecciona un archivo .apk descargado previamente de WhatsApp, Telegram, Chrome o descargas de AI Studio.", fontSize = 12.sp, color = TextGrayLight)
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onPickFileClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary)
        ) {
            Icon(Icons.Default.FileOpen, contentDescription = null, tint = DeepPurpleOnPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Examinar Archivos .APK del Dispositivo", fontWeight = FontWeight.Bold, color = DeepPurpleOnPrimary)
        }
    }
}

@Composable
private fun Method3DirectUrlDownloader(
    updateState: UpdateStatus,
    directUrl: String,
    onDirectUrlChange: (String) -> Unit,
    onDownloadAndInstall: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var probeResult by remember { mutableStateOf<OtaUpdateManager.UrlCheckResult?>(null) }
    var isProbing by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Método 3: Descarga por URL Directa (HTTP/HTTPS)", fontWeight = FontWeight.Bold, color = LavenderPrimary, fontSize = 14.sp)
        Text("Descarga cualquier archivo APK alojado en servidores VPS, Apache, Nginx, S3 o Firebase.", fontSize = 12.sp, color = TextGrayLight)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = directUrl,
            onValueChange = onDirectUrlChange,
            label = { Text("URL Directa del Archivo .APK") },
            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = LavenderPrimary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    scope.launch {
                        isProbing = true
                        probeResult = OtaUpdateManager.verifyRemoteUrlReal(directUrl)
                        isProbing = false
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isProbing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = LavenderPrimary, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Verificar Enlace", fontSize = 12.sp)
                }
            }

            Button(
                onClick = { onDownloadAndInstall(directUrl) },
                enabled = directUrl.isNotBlank(),
                modifier = Modifier.weight(1.3f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, tint = DeepPurpleOnPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Descargar e Instalar", fontWeight = FontWeight.Bold, color = DeepPurpleOnPrimary, fontSize = 12.sp)
            }
        }

        if (probeResult != null) {
            Spacer(modifier = Modifier.height(8.dp))
            val probe = probeResult!!
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (probe.exists) Color(0xFF0F382A) else Color(0xFF381414)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (probe.exists) MintSpeed else CoralError)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (probe.exists) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (probe.exists) MintSpeed else CoralError,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (probe.exists) "APK Verificado en Servidor (HTTP ${probe.httpCode})" else "Enlace no accesible (HTTP ${if (probe.httpCode > 0) probe.httpCode else "Error"})",
                            fontWeight = FontWeight.Bold,
                            color = if (probe.exists) MintSpeed else CoralError,
                            fontSize = 12.sp
                        )
                    }
                    if (probe.errorMessage != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(probe.errorMessage, fontSize = 11.sp, color = TextWhite)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "MIME: ${probe.contentType} • Latencia: ${probe.responseTimeMs}ms • Tamaño: ${if (probe.contentLength > 0) "${probe.contentLength / (1024 * 1024)} MB" else "No reportado"}",
                        fontSize = 10.sp,
                        color = TextGrayLight
                    )
                }
            }
        }
    }
}

@Composable
private fun Method4WebReleases(onOpenWebReleases: () -> Unit, onOpenRepo: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Método 4: Portal Web de GitHub Releases", fontWeight = FontWeight.Bold, color = LavenderPrimary, fontSize = 14.sp)
        Text("Abre la página web oficial en tu navegador predeterminado para descargar cualquier versión binaria manualmente.", fontSize = 12.sp, color = TextGrayLight)
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onOpenWebReleases,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary)
        ) {
            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = DeepPurpleOnPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Abrir Releases en Navegador", fontWeight = FontWeight.Bold, color = DeepPurpleOnPrimary)
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onOpenRepo,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Code, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ver Código Fuente en GitHub")
        }
    }
}

@Composable
private fun Method5LiveSheetsSync(
    remoteSyncState: RemoteSyncState,
    catalogUrl: String,
    onCatalogUrlChange: (String) -> Unit,
    onPerformSync: () -> Unit,
    onOpenSheetsSync: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Método 5: Google Sheets Cloud Sync (OTA)", fontWeight = FontWeight.Bold, color = MintSpeed, fontSize = 14.sp)
        Text("Hot-Reload instantáneo de WebApps, iconos y categorías desde tu hoja de cálculo sin reinstalar la APK.", fontSize = 12.sp, color = TextGrayLight)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = catalogUrl,
            onValueChange = onCatalogUrlChange,
            label = { Text("URL de Hoja de Cálculo CSV") },
            leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null, tint = MintSpeed) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = onPerformSync,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MintSpeed)
        ) {
            Icon(Icons.Default.Sync, contentDescription = null, tint = DeepPurpleOnPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sincronizar Catálogo Ahora", fontWeight = FontWeight.Bold, color = DeepPurpleOnPrimary)
        }
    }
}

@Composable
private fun Method6JsonEndpointSync(
    remoteSyncState: RemoteSyncState,
    endpointUrl: String,
    onEndpointUrlChange: (String) -> Unit,
    onPerformSync: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Método 6: Endpoint JSON Dinámico (CDN/API)", fontWeight = FontWeight.Bold, color = LavenderPrimary, fontSize = 14.sp)
        Text("Carga catálogo en formato JSON desde GitHub Raw, jsDelivr, Firebase o cualquier REST API.", fontSize = 12.sp, color = TextGrayLight)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = endpointUrl,
            onValueChange = onEndpointUrlChange,
            label = { Text("URL del archivo JSON remoto") },
            leadingIcon = { Icon(Icons.Default.CloudSync, contentDescription = null, tint = LavenderPrimary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = onPerformSync,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary)
        ) {
            Icon(Icons.Default.Sync, contentDescription = null, tint = DeepPurpleOnPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Descargar e Importar Catálogo JSON", fontWeight = FontWeight.Bold, color = DeepPurpleOnPrimary)
        }
    }
}

@Composable
private fun Method7GitRawBranchArtifact(onDownloadAndInstall: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Método 7: Git Raw Branch Pre-Built Artifact", fontWeight = FontWeight.Bold, color = LavenderPrimary, fontSize = 14.sp)
        Text("Descarga directa del binario app-debug.apk compilado en la rama main/release del repositorio GitHub.", fontSize = 12.sp, color = TextGrayLight)
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onDownloadAndInstall,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary)
        ) {
            Icon(Icons.Default.Code, contentDescription = null, tint = DeepPurpleOnPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Descargar APK desde Rama Main", fontWeight = FontWeight.Bold, color = DeepPurpleOnPrimary)
        }
    }
}

@Composable
private fun Method8GoogleDriveConverter(
    gdriveUrl: String,
    onGdriveUrlChange: (String) -> Unit,
    onDownloadAndInstall: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Método 8: Conversor y Descargador Google Drive", fontWeight = FontWeight.Bold, color = LavenderPrimary, fontSize = 14.sp)
        Text("Pega cualquier enlace compartido de Google Drive. La app extraerá el ID de archivo y descargará el APK automáticamente.", fontSize = 12.sp, color = TextGrayLight)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = gdriveUrl,
            onValueChange = onGdriveUrlChange,
            label = { Text("Enlace de Google Drive (view?usp=sharing)") },
            placeholder = { Text("https://drive.google.com/file/d/...") },
            leadingIcon = { Icon(Icons.Default.CloudDownload, contentDescription = null, tint = LavenderPrimary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = { onDownloadAndInstall(gdriveUrl) },
            enabled = gdriveUrl.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary)
        ) {
            Icon(Icons.Default.Download, contentDescription = null, tint = DeepPurpleOnPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Convertir y Descargar APK desde Google Drive", fontWeight = FontWeight.Bold, color = DeepPurpleOnPrimary)
        }
    }
}

@Composable
private fun Method9DropboxConverter(
    dropboxUrl: String,
    onDropboxUrlChange: (String) -> Unit,
    onDownloadAndInstall: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Método 9: Descarga Directa Dropbox", fontWeight = FontWeight.Bold, color = LavenderPrimary, fontSize = 14.sp)
        Text("Pega cualquier enlace compartido de Dropbox (dl=0) y la app lo convertirá a descarga directa para sideloading.", fontSize = 12.sp, color = TextGrayLight)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = dropboxUrl,
            onValueChange = onDropboxUrlChange,
            label = { Text("Enlace compartido de Dropbox") },
            leadingIcon = { Icon(Icons.Default.Download, contentDescription = null, tint = LavenderPrimary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = { onDownloadAndInstall(dropboxUrl) },
            enabled = dropboxUrl.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary)
        ) {
            Text("Descargar e Instalar desde Dropbox", fontWeight = FontWeight.Bold, color = DeepPurpleOnPrimary)
        }
    }
}

@Composable
private fun Method10LanWifiServer(
    lanUrl: String,
    onLanUrlChange: (String) -> Unit,
    onDownloadAndInstall: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Método 10: Servidor LAN Wi-Fi / P2P Local", fontWeight = FontWeight.Bold, color = LavenderPrimary, fontSize = 14.sp)
        Text("Instalación instantánea por red local desde tu PC (por ejemplo ejecutando `python -m http.server 8080`).", fontSize = 12.sp, color = TextGrayLight)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = lanUrl,
            onValueChange = onLanUrlChange,
            label = { Text("IP y Puerto Local (http://192.168.x.x:8080/app.apk)") },
            leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null, tint = LavenderPrimary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = { onDownloadAndInstall(lanUrl) },
            enabled = lanUrl.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary)
        ) {
            Icon(Icons.Default.NetworkWifi, contentDescription = null, tint = DeepPurpleOnPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Descargar desde Red Local Wi-Fi", fontWeight = FontWeight.Bold, color = DeepPurpleOnPrimary)
        }
    }
}

@Composable
private fun Method11QrCodeDeepLink(
    qrUrl: String,
    onQrUrlChange: (String) -> Unit,
    onProcessUrl: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Método 11: Escáner QR / Deep Link", fontWeight = FontWeight.Bold, color = LavenderPrimary, fontSize = 14.sp)
        Text("Pega o procesa la URL obtenida de cualquier código QR para descargar el APK o sincronizar el catálogo.", fontSize = 12.sp, color = TextGrayLight)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = qrUrl,
            onValueChange = onQrUrlChange,
            label = { Text("URL o Carga útil del Código QR") },
            leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null, tint = LavenderPrimary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = { onProcessUrl(qrUrl) },
            enabled = qrUrl.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary)
        ) {
            Text("Ejecutar Enlace del QR", fontWeight = FontWeight.Bold, color = DeepPurpleOnPrimary)
        }
    }
}

@Composable
private fun Method12Base64Decoder(
    base64String: String,
    onBase64Change: (String) -> Unit,
    onDecodeAndInstall: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Método 12: Decodificador Base64 / Hex Patch", fontWeight = FontWeight.Bold, color = LavenderPrimary, fontSize = 14.sp)
        Text("Pega una cadena codificada en Base64 de un archivo APK o parche binario para reconstruirlo e instalarlo.", fontSize = 12.sp, color = TextGrayLight)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = base64String,
            onValueChange = onBase64Change,
            label = { Text("Texto Base64 de APK") },
            leadingIcon = { Icon(Icons.Default.DataObject, contentDescription = null, tint = LavenderPrimary) },
            maxLines = 4,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = { onDecodeAndInstall(base64String) },
            enabled = base64String.length > 50,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary)
        ) {
            Text("Decodificar Base64 e Instalar APK", fontWeight = FontWeight.Bold, color = DeepPurpleOnPrimary)
        }
    }
}

@Composable
private fun Method13RemoteCssJsInjector(
    globalCss: String,
    onGlobalCssChange: (String) -> Unit,
    globalJs: String,
    onGlobalJsChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Método 13: Inyector Global CSS/JS (Hot-Reload)", fontWeight = FontWeight.Bold, color = MintSpeed, fontSize = 14.sp)
        Text("Aplica hojas de estilo CSS oscuras, filtros anti-publicidad y scripts JavaScript en tiempo real a todas tus WebApps.", fontSize = 12.sp, color = TextGrayLight)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = globalCss,
            onValueChange = onGlobalCssChange,
            label = { Text("CSS Global Inyectado") },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = globalJs,
            onValueChange = onGlobalJsChange,
            label = { Text("JavaScript Global Inyectado") },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
        )
    }
}

@Composable
private fun Method14ScheduledPoller(
    autoCheck: Boolean,
    onAutoCheckChange: (Boolean) -> Unit,
    pushNotifications: Boolean,
    onPushNotificationsChange: (Boolean) -> Unit,
    autoSyncCatalog: Boolean,
    onAutoSyncCatalogChange: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Método 14: Poller Programado & Notificaciones Push", fontWeight = FontWeight.Bold, color = LavenderPrimary, fontSize = 14.sp)
        Text("Comprueba periódicamente nuevos lanzamientos en segundo plano cada 30 minutos y lanza alertas push.", fontSize = 12.sp, color = TextGrayLight)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Auto-comprobación periódica en background", fontSize = 12.sp, color = TextWhite)
            Switch(checked = autoCheck, onCheckedChange = onAutoCheckChange)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Notificaciones push al detectar nueva versión", fontSize = 12.sp, color = TextWhite)
            Switch(checked = pushNotifications, onCheckedChange = onPushNotificationsChange)
        }
    }
}

@Composable
private fun Method15BackupRestoreMigration(
    backupJson: String,
    onBackupJsonChange: (String) -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Método 15: Backup, Restauración & Migración Total", fontWeight = FontWeight.Bold, color = LavenderPrimary, fontSize = 14.sp)
        Text("Exporta o importa el paquete JSON completo con todas las WebApps, configuraciones, scripts y estilos.", fontSize = 12.sp, color = TextGrayLight)
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onExportBackup,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = DeepPurpleOnPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Exportar JSON", color = DeepPurpleOnPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { onImportBackup(backupJson) },
                enabled = backupJson.isNotBlank(),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MintSpeed)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = DeepPurpleOnPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Restaurar JSON", color = DeepPurpleOnPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = backupJson,
            onValueChange = onBackupJsonChange,
            label = { Text("Contenido JSON del Backup") },
            maxLines = 4,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
        )
    }
}

@Composable
private fun InfoBadge(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(DeepPurpleContainer.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column {
            Text(text = label, fontSize = 9.sp, color = TextGrayMuted, maxLines = 1)
            Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite, maxLines = 1)
        }
    }
}
