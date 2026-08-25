package com.example.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.WebAppEntity
import com.example.ui.components.TurboWebView
import com.example.ui.dialogs.SitePermissionsDialog
import com.example.ui.theme.AmberEnergy
import com.example.ui.theme.CoralError
import com.example.ui.theme.DeepPurpleContainer
import com.example.ui.theme.DeepPurpleOnPrimary
import com.example.ui.theme.ElegantCard
import com.example.ui.theme.ElegantCardBorder
import com.example.ui.theme.ElegantSurface
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.MintSpeed
import com.example.ui.theme.RoseAccent
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextWhite
import com.example.util.SitePermissionManager
import com.example.util.TurboPowerManager
import com.example.util.WebShortcutHelper

enum class DockDisplayMode {
    EXPANDED,
    MINI_PILL,
    PURE_IMMERSION
}

@Composable
fun InAppBrowserScreen(
    app: WebAppEntity?,
    instantUrl: String?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val targetUrl = app?.url ?: instantUrl ?: "https://google.com"
    val appName = app?.name ?: "WebNative App"

    var currentProgress by remember { mutableFloatStateOf(0f) }
    var currentTitle by remember { mutableStateOf(appName) }
    var isDesktopMode by remember { mutableStateOf(app?.isDesktopMode ?: false) }
    var isAdBlockEnabled by remember { mutableStateOf(app?.isAdBlockEnabled ?: true) }
    var isOledBlackMode by remember { mutableStateOf(app?.isOledBlackMode ?: false) }
    var isKeepAwakeActive by remember { mutableStateOf(true) }
    var blockedAdsCount by remember { mutableIntStateOf(0) }
    var dockMode by remember { mutableStateOf(DockDisplayMode.MINI_PILL) }
    var showSitePermissionsDialog by remember { mutableStateOf(false) }

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var customVideoView by remember { mutableStateOf<View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }

    // File Chooser Attachment Bridge
    var filePathCallbackRef by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    val fileChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uris = if (result.resultCode == Activity.RESULT_OK) {
            val intentData = result.data
            if (intentData?.clipData != null) {
                val clipData = intentData.clipData!!
                val uriList = ArrayList<Uri>()
                for (i in 0 until clipData.itemCount) {
                    uriList.add(clipData.getItemAt(i).uri)
                }
                uriList.toTypedArray()
            } else if (intentData?.data != null) {
                arrayOf(intentData.data!!)
            } else {
                null
            }
        } else {
            null
        }
        filePathCallbackRef?.onReceiveValue(uris)
        filePathCallbackRef = null
    }

    // System Permissions Launcher for WebRTC / Mic / Cam
    var pendingPermissionRequest by remember { mutableStateOf<PermissionRequest?>(null) }
    val systemPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        pendingPermissionRequest?.let { req ->
            req.grant(req.resources)
            pendingPermissionRequest = null
        }
    }

    // High performance screen keep-on
    DisposableEffect(Unit) {
        if (activity != null) {
            TurboPowerManager.setKeepScreenOn(activity, true)
            TurboPowerManager.acquireWakeLock(context, "WebNative:Player:$appName")
        }
        onDispose {
            if (activity != null) {
                TurboPowerManager.setKeepScreenOn(activity, false)
                TurboPowerManager.releaseWakeLock()
            }
        }
    }

    // Handle back navigation
    BackHandler {
        if (showSitePermissionsDialog) {
            showSitePermissionsDialog = false
        } else if (customVideoView != null) {
            customViewCallback?.onCustomViewHidden()
            customVideoView = null
        } else if (dockMode == DockDisplayMode.EXPANDED) {
            dockMode = DockDisplayMode.MINI_PILL
        } else if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            onClose()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isOledBlackMode) Color.Black else ElegantSurface)
    ) {
        val globalCss = remember { com.example.util.RemoteConfigEngine.getGlobalCustomCss(context) }
        val globalJs = remember { com.example.util.RemoteConfigEngine.getGlobalCustomJs(context) }
        val combinedCss = remember(app?.customCss, globalCss) {
            listOfNotNull(app?.customCss?.takeIf { it.isNotBlank() }, globalCss.takeIf { it.isNotBlank() }).joinToString("\n")
        }
        val combinedJs = remember(app?.customJs, globalJs) {
            listOfNotNull(app?.customJs?.takeIf { it.isNotBlank() }, globalJs.takeIf { it.isNotBlank() }).joinToString("\n")
        }

        // Fullscreen Turbo WebView with soft keyboard IME resilience
        TurboWebView(
            url = targetUrl,
            modifier = Modifier.fillMaxSize(),
            isDesktopMode = isDesktopMode,
            isAdBlockEnabled = isAdBlockEnabled,
            isHardwareBoostEnabled = app?.isHardwareBoostEnabled ?: true,
            isOledBlackMode = isOledBlackMode,
            customCss = combinedCss,
            customJs = combinedJs,
            onProgressChange = { currentProgress = it },
            onTitleChange = { currentTitle = it },
            onBlockedAdCountChange = { blockedAdsCount = it },
            onWebViewCreated = { webViewInstance = it },
            onCustomViewShow = { view, callback ->
                customVideoView = view
                customViewCallback = callback
            },
            onCustomViewHide = {
                customVideoView = null
                customViewCallback = null
            },
            onFileChooser = { callback, params ->
                filePathCallbackRef?.onReceiveValue(null)
                filePathCallbackRef = callback
                val intent = params?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "*/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
                try {
                    fileChooserLauncher.launch(intent)
                    true
                } catch (e: Exception) {
                    filePathCallbackRef?.onReceiveValue(null)
                    filePathCallbackRef = null
                    false
                }
            },
            onPermissionPromptRequested = { request ->
                pendingPermissionRequest = request
                systemPermissionLauncher.launch(SitePermissionManager.REQUIRED_SYSTEM_PERMISSIONS)
            }
        )

        // Loading Progress Indicator
        if (currentProgress < 1f) {
            LinearProgressIndicator(
                progress = { currentProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.TopCenter)
                    .statusBarsPadding(),
                color = LavenderPrimary,
                trackColor = Color.Transparent
            )
        }

        // Custom Fullscreen Video Container Overlay
        if (customVideoView != null) {
            AndroidView(
                factory = { _ ->
                    FrameLayout(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        addView(customVideoView)
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
        }

        // -------------------------------------------------------------
        // IMMERSIVE DOCK CONTROL SYSTEM (Show / Hide / Mini Pill / Full)
        // -------------------------------------------------------------

        // 1. PURE IMMERSION TRIGGER: Subtle corner floating button
        if (dockMode == DockDisplayMode.PURE_IMMERSION) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(end = 12.dp, bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(LavenderPrimary.copy(alpha = 0.5f))
                        .border(1.dp, LavenderPrimary, CircleShape)
                        .clickable { dockMode = DockDisplayMode.EXPANDED }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Mostrar Controles",
                        tint = DeepPurpleOnPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // 2. MINI PILL MODE: Compact floating pill at bottom center
        if (dockMode == DockDisplayMode.MINI_PILL) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(bottom = 12.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .border(1.dp, LavenderPrimary.copy(alpha = 0.5f), RoundedCornerShape(30.dp)),
                    color = ElegantSurface.copy(alpha = 0.90f),
                    tonalElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = { webViewInstance?.goBack() },
                            enabled = webViewInstance?.canGoBack() == true,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Atrás",
                                tint = if (webViewInstance?.canGoBack() == true) TextWhite else TextGrayLight.copy(alpha = 0.3f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // App/Title label & Click to Expand
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(LavenderPrimary.copy(alpha = 0.15f))
                                .clickable { dockMode = DockDisplayMode.EXPANDED }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentTitle.take(14) + if (currentTitle.length > 14) "…" else "",
                                color = LavenderPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ExpandLess, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(14.dp))
                        }

                        // Quick Site Permissions (Mic/Cam/Files)
                        IconButton(
                            onClick = { showSitePermissionsDialog = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Security, contentDescription = "Permisos del Sitio", tint = MintSpeed, modifier = Modifier.size(18.dp))
                        }

                        // Quick Pure Fullscreen Immersion Button
                        IconButton(
                            onClick = { dockMode = DockDisplayMode.PURE_IMMERSION },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Fullscreen, contentDescription = "Inmersión Total", tint = TextWhite, modifier = Modifier.size(18.dp))
                        }

                        // Close button
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Salir", tint = CoralError, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // 3. EXPANDED FULL CONTROLS DOCK: Complete command center
        if (dockMode == DockDisplayMode.EXPANDED) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(start = 14.dp, end = 14.dp, bottom = 12.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .border(1.dp, LavenderPrimary.copy(alpha = 0.6f), RoundedCornerShape(26.dp)),
                    color = ElegantSurface.copy(alpha = 0.96f),
                    tonalElevation = 12.dp
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Title Bar & Quick Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = currentTitle,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Site Permissions Config Button
                                IconButton(
                                    onClick = { showSitePermissionsDialog = true },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = "Permisos de Sitio", tint = MintSpeed, modifier = Modifier.size(16.dp))
                                }

                                // Copy URL
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("URL", webViewInstance?.url ?: targetUrl)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "URL copiada al portapapeles", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copiar Link", tint = TextGrayLight, modifier = Modifier.size(16.dp))
                                }

                                // Share
                                IconButton(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, webViewInstance?.url ?: targetUrl)
                                            putExtra(Intent.EXTRA_SUBJECT, currentTitle)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Compartir Sitio"))
                                    },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Compartir", tint = TextGrayLight, modifier = Modifier.size(16.dp))
                                }

                                // Pin Shortcut
                                if (app != null) {
                                    IconButton(
                                        onClick = {
                                            WebShortcutHelper.pinWebAppShortcut(context, app)
                                            Toast.makeText(context, "Acceso directo fijado en inicio", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Default.PushPin, contentDescription = "Fijar a inicio", tint = LavenderPrimary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Turbo Speed Action Grid (Desktop, AdBlock, OLED, Turbo, Zoom)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SpeedDockActionItem(
                                icon = Icons.Default.Computer,
                                label = if (isDesktopMode) "Escritorio" else "Móvil",
                                isActive = isDesktopMode,
                                activeColor = LavenderPrimary,
                                onClick = {
                                    isDesktopMode = !isDesktopMode
                                    webViewInstance?.reload()
                                }
                            )

                            SpeedDockActionItem(
                                icon = Icons.Default.Security,
                                label = "$blockedAdsCount Ads",
                                isActive = isAdBlockEnabled,
                                activeColor = MintSpeed,
                                onClick = {
                                    isAdBlockEnabled = !isAdBlockEnabled
                                    webViewInstance?.reload()
                                }
                            )

                            SpeedDockActionItem(
                                icon = Icons.Default.DarkMode,
                                label = "OLED",
                                isActive = isOledBlackMode,
                                activeColor = AmberEnergy,
                                onClick = {
                                    isOledBlackMode = !isOledBlackMode
                                    webViewInstance?.reload()
                                }
                            )

                            SpeedDockActionItem(
                                icon = Icons.Default.Bolt,
                                label = "Awake",
                                isActive = isKeepAwakeActive,
                                activeColor = RoseAccent,
                                onClick = {
                                    isKeepAwakeActive = !isKeepAwakeActive
                                    if (activity != null) {
                                        TurboPowerManager.setKeepScreenOn(activity, isKeepAwakeActive)
                                    }
                                }
                            )

                            SpeedDockActionItem(
                                icon = Icons.Default.ZoomIn,
                                label = "Zoom +",
                                isActive = false,
                                activeColor = TextWhite,
                                onClick = {
                                    webViewInstance?.zoomIn()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Bottom Action Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Back, Forward, Reload
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(
                                    onClick = { webViewInstance?.goBack() },
                                    enabled = webViewInstance?.canGoBack() == true,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Atrás",
                                        tint = if (webViewInstance?.canGoBack() == true) TextWhite else TextGrayLight.copy(alpha = 0.3f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { webViewInstance?.goForward() },
                                    enabled = webViewInstance?.canGoForward() == true,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Adelante",
                                        tint = if (webViewInstance?.canGoForward() == true) TextWhite else TextGrayLight.copy(alpha = 0.3f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { webViewInstance?.reload() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Recargar", tint = LavenderPrimary, modifier = Modifier.size(20.dp))
                                }
                            }

                            // Immersion Mode Switchers
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MintSpeed.copy(alpha = 0.2f))
                                        .border(1.dp, MintSpeed, RoundedCornerShape(12.dp))
                                        .clickable { dockMode = DockDisplayMode.PURE_IMMERSION }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = MintSpeed, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Ocultar Todo", color = MintSpeed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(LavenderPrimary.copy(alpha = 0.2f))
                                        .clickable { dockMode = DockDisplayMode.MINI_PILL }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Minimizar", color = LavenderPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Site Permissions Customizer Dialog
        if (showSitePermissionsDialog) {
            SitePermissionsDialog(
                currentUrl = webViewInstance?.url ?: targetUrl,
                onDismiss = { showSitePermissionsDialog = false },
                onReloadRequested = {
                    webViewInstance?.reload()
                }
            )
        }
    }
}

@Composable
fun SpeedDockActionItem(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) activeColor.copy(alpha = 0.2f) else DeepPurpleContainer)
            .border(
                1.dp,
                if (isActive) activeColor.copy(alpha = 0.7f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) activeColor else TextGrayLight,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) activeColor else TextGrayLight
        )
    }
}
