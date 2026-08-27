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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import com.example.util.AppearanceSettingsManager
import com.example.util.ChatCopyInjector
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

    val defaultUrl = remember { AppearanceSettingsManager.getDefaultHomeUrl(context) }
    val targetUrl = app?.url ?: instantUrl ?: defaultUrl
    val appName = app?.name ?: "WebNative Pro"

    var currentProgress by remember { mutableFloatStateOf(0f) }
    var currentTitle by remember { mutableStateOf(appName) }
    var isDesktopMode by remember { mutableStateOf(app?.isDesktopMode ?: false) }
    var isAdBlockEnabled by remember { mutableStateOf(app?.isAdBlockEnabled ?: true) }
    var isOledBlackMode by remember { mutableStateOf(app?.isOledBlackMode ?: false) }
    var isKeepAwakeActive by remember { mutableStateOf(true) }
    var blockedAdsCount by remember { mutableIntStateOf(0) }
    var dockMode by remember { mutableStateOf(DockDisplayMode.MINI_PILL) }
    var showSitePermissionsDialog by remember { mutableStateOf(false) }

    // Dock positioning configuration
    var dockPosition by remember { mutableStateOf(AppearanceSettingsManager.getDockPosition(context)) }

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

    // Keep screen on while web is active
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

    // Auto-inject copy buttons when webview completes loading if enabled
    LaunchedEffect(currentProgress) {
        if (currentProgress >= 1f && webViewInstance != null && AppearanceSettingsManager.isAutoInjectCopyEnabled(context)) {
            webViewInstance?.let { wv ->
                ChatCopyInjector.injectCopyButtons(wv)
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

        // Fullscreen Turbo WebView
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

        // Alignment logic based on dockPosition preference
        val dockBoxAlignment = when (dockPosition) {
            AppearanceSettingsManager.DOCK_POS_BOTTOM_RIGHT -> Alignment.BottomEnd
            AppearanceSettingsManager.DOCK_POS_TOP_RIGHT -> Alignment.TopEnd
            else -> Alignment.BottomCenter
        }

        // -------------------------------------------------------------
        // 1. PURE IMMERSION TRIGGER
        // -------------------------------------------------------------
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
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(LavenderPrimary.copy(alpha = 0.85f))
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

        // -------------------------------------------------------------
        // 2. MINI PILL MODE: Compact floating control pill
        // -------------------------------------------------------------
        if (dockMode == DockDisplayMode.MINI_PILL) {
            Box(
                modifier = Modifier
                    .align(dockBoxAlignment)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(32.dp))
                        .border(1.dp, LavenderPrimary.copy(alpha = 0.5f), RoundedCornerShape(32.dp)),
                    color = ElegantSurface.copy(alpha = 0.94f),
                    tonalElevation = 10.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { webViewInstance?.goBack() },
                            enabled = webViewInstance?.canGoBack() == true,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Atrás",
                                tint = if (webViewInstance?.canGoBack() == true) TextWhite else TextGrayLight.copy(alpha = 0.3f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // App/Title label & Click to Open Tools
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(LavenderPrimary.copy(alpha = 0.18f))
                                .clickable { dockMode = DockDisplayMode.EXPANDED }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentTitle.take(12) + if (currentTitle.length > 12) "…" else "",
                                color = LavenderPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ExpandLess, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(15.dp))
                        }

                        // Quick 1-Click Copy Injector Button
                        IconButton(
                            onClick = {
                                webViewInstance?.let { wv ->
                                    ChatCopyInjector.injectCopyButtons(wv) { count ->
                                        Toast.makeText(context, "📋 $count botones de copia inyectados en el chat", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Inyectar Copia en Chat", tint = AmberEnergy, modifier = Modifier.size(18.dp))
                        }

                        // Quick Site Permissions
                        IconButton(
                            onClick = { showSitePermissionsDialog = true },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.Security, contentDescription = "Permisos", tint = MintSpeed, modifier = Modifier.size(18.dp))
                        }

                        // Close button
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Salir", tint = CoralError, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // 3. EXPANDED FULL CONTROLS DOCK: Clean, modern modular panel
        // -------------------------------------------------------------
        AnimatedVisibility(
            visible = dockMode == DockDisplayMode.EXPANDED,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .border(1.dp, LavenderPrimary.copy(alpha = 0.5f), RoundedCornerShape(26.dp)),
                color = ElegantSurface.copy(alpha = 0.98f),
                tonalElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header: WebApp Title & Quick Utilities
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MintSpeed, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentTitle,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Copy Full Page Text
                            IconButton(
                                onClick = {
                                    webViewInstance?.let { wv ->
                                        ChatCopyInjector.extractFullText(wv) { text ->
                                            if (text.isNotBlank()) {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = ClipData.newPlainText("Texto Completo", text)
                                                clipboard.setPrimaryClip(clip)
                                                Toast.makeText(context, "Texto completo copiado al portapapeles", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "No se encontró texto para copiar", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.TextFields, contentDescription = "Copiar Texto de Página", tint = TextWhite, modifier = Modifier.size(17.dp))
                            }

                            // Share URL
                            IconButton(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, webViewInstance?.url ?: targetUrl)
                                        putExtra(Intent.EXTRA_SUBJECT, currentTitle)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Compartir Sitio"))
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Compartir", tint = TextGrayLight, modifier = Modifier.size(17.dp))
                            }

                            // Pin Shortcut to Home
                            if (app != null) {
                                IconButton(
                                    onClick = {
                                        WebShortcutHelper.pinWebAppShortcut(context, app)
                                        Toast.makeText(context, "Acceso directo fijado en inicio", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.PushPin, contentDescription = "Fijar a inicio", tint = LavenderPrimary, modifier = Modifier.size(17.dp))
                                }
                            }

                            // Minimize Button
                            IconButton(
                                onClick = { dockMode = DockDisplayMode.MINI_PILL },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Minimizar", tint = TextGrayLight, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 1: Turbo Action Grid (Mobile/Desktop, Ads, OLED, Awake, Copy Injector)
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
                            icon = Icons.Default.Chat,
                            label = "Copiar Chat",
                            isActive = true,
                            activeColor = Color(0xFFA855F7),
                            onClick = {
                                webViewInstance?.let { wv ->
                                    ChatCopyInjector.injectCopyButtons(wv) { count ->
                                        Toast.makeText(context, "✅ $count botones de copia agregados a los mensajes del chat", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 2: Zoom, Fast Scroll & Dock Position Controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DeepPurpleContainer.copy(alpha = 0.6f))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Zoom Controls
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Zoom:", fontSize = 11.sp, color = TextGrayLight, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { webViewInstance?.zoomOut() },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(Icons.Default.ZoomOut, contentDescription = "Menos Zoom", tint = TextWhite, modifier = Modifier.size(15.dp))
                            }
                            IconButton(
                                onClick = { webViewInstance?.zoomIn() },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(Icons.Default.ZoomIn, contentDescription = "Más Zoom", tint = TextWhite, modifier = Modifier.size(15.dp))
                            }
                        }

                        // Fast Scroll Actions
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    webViewInstance?.let { ChatCopyInjector.scrollTo(it, toTop = true) }
                                },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(Icons.Default.VerticalAlignTop, contentDescription = "Subir al inicio", tint = MintSpeed, modifier = Modifier.size(15.dp))
                            }
                            IconButton(
                                onClick = {
                                    webViewInstance?.let { ChatCopyInjector.scrollTo(it, toTop = false) }
                                },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(Icons.Default.VerticalAlignBottom, contentDescription = "Bajar al final", tint = AmberEnergy, modifier = Modifier.size(15.dp))
                            }
                        }

                        // Dock Position Switcher
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(LavenderPrimary.copy(alpha = 0.15f))
                                .clickable {
                                    val nextPos = when (dockPosition) {
                                        AppearanceSettingsManager.DOCK_POS_BOTTOM_CENTER -> AppearanceSettingsManager.DOCK_POS_BOTTOM_RIGHT
                                        AppearanceSettingsManager.DOCK_POS_BOTTOM_RIGHT -> AppearanceSettingsManager.DOCK_POS_TOP_RIGHT
                                        else -> AppearanceSettingsManager.DOCK_POS_BOTTOM_CENTER
                                    }
                                    dockPosition = nextPos
                                    AppearanceSettingsManager.setDockPosition(context, nextPos)
                                    val label = when (nextPos) {
                                        AppearanceSettingsManager.DOCK_POS_BOTTOM_RIGHT -> "Abajo Derecha"
                                        AppearanceSettingsManager.DOCK_POS_TOP_RIGHT -> "Arriba Derecha"
                                        else -> "Abajo Centro"
                                    }
                                    Toast.makeText(context, "Posición del dock: $label", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ubicación Dock", fontSize = 10.sp, color = LavenderPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bottom Navigation Row: Back, Forward, Reload & Immersion
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Navigation Arrows
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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
                            IconButton(
                                onClick = { showSitePermissionsDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Security, contentDescription = "Permisos", tint = MintSpeed, modifier = Modifier.size(18.dp))
                            }
                        }

                        // Immersion Button
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
                                Text("Inmersión Total", color = MintSpeed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
            .background(if (isActive) activeColor.copy(alpha = 0.18f) else DeepPurpleContainer)
            .border(
                1.dp,
                if (isActive) activeColor.copy(alpha = 0.6f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 7.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) activeColor else TextGrayLight,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) activeColor else TextGrayLight,
            maxLines = 1
        )
    }
}
