package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.dialogs.GoogleSheetsSyncDialog
import com.example.ui.dialogs.CreateWebAppDialog
import com.example.ui.dialogs.EditWebAppDialog
import com.example.ui.screens.AppLibraryTab
import com.example.ui.screens.ExploreCatalogTab
import com.example.ui.screens.InAppBrowserScreen
import com.example.ui.screens.TurboStudioTab
import com.example.ui.theme.DeepPurpleContainer
import com.example.ui.theme.DeepPurpleOnPrimary
import com.example.ui.theme.ElegantBackground
import com.example.ui.theme.ElegantCard
import com.example.ui.theme.ElegantCardBorder
import com.example.ui.theme.ElegantNav
import com.example.ui.theme.LavenderOnContainer
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RoseAccent
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextGrayMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.MainNavTab
import com.example.ui.viewmodel.WebAppViewModel
import com.example.util.OtaUpdateManager
import com.example.util.OtaUpdateWorker
import java.io.File

class MainActivity : ComponentActivity() {

    private val viewModel: WebAppViewModel by viewModels()
    private var openOtaDialogOnLaunch by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Clear any sticky notification on launch if the user opened the app
        com.example.util.OtaNotificationHelper.dismissNotification(this)

        // Check if opened from update notification or a background download.
        handleUpdateIntent(intent)

        // Initialize Notification channel and persist the OTA worker when enabled.
        com.example.util.OtaNotificationHelper.createNotificationChannel(this)
        if (OtaUpdateManager.isAutoCheckEnabled(this)) {
            OtaUpdateWorker.schedule(this)
        }

        setContent {
            MyApplicationTheme(darkTheme = true) {
                MainScreen(
                    viewModel = viewModel,
                    initialShowOtaDialog = openOtaDialogOnLaunch
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleUpdateIntent(intent)
        com.example.util.OtaNotificationHelper.dismissNotification(this)
    }

    private fun handleUpdateIntent(updateIntent: android.content.Intent?) {
        if (updateIntent?.getBooleanExtra("EXTRA_OPEN_OTA_DIALOG", false) == true ||
            updateIntent?.hasExtra("EXTRA_AUTO_UPDATE_VERSION") == true) {
            openOtaDialogOnLaunch = true
        }

        updateIntent?.getStringExtra("EXTRA_PENDING_APK_PATH")?.let { path ->
            val apkFile = File(path)
            if (apkFile.exists()) {
                OtaUpdateManager.promptInstallApk(this, apkFile)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshBatteryOptimizationStatus(this)
        com.example.util.OtaNotificationHelper.dismissNotification(this)
    }
}

@Composable
fun MainScreen(
    viewModel: WebAppViewModel,
    initialShowOtaDialog: Boolean = false
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showOtaUpdateDialog by androidx.compose.runtime.remember(initialShowOtaDialog) {
        mutableStateOf(initialShowOtaDialog)
    }

    // Dismiss any pending notification when main screen is rendered
    LaunchedEffect(Unit) {
        com.example.util.OtaNotificationHelper.dismissNotification(context)

        // Start poller only if auto check is actively enabled
        if (com.example.util.OtaUpdateManager.isAutoCheckEnabled(context)) {
            com.example.util.OtaUpdateManager.startContinuousUpdatePoller(context, this)
        }

        if (com.example.util.RemoteConfigEngine.isAutoSyncEnabled(context)) {
            com.example.util.RemoteConfigEngine.performRemoteSync(context, this)
        }
    }

    // If an app is running in the full-screen player
    if (state.activeRunningApp != null || state.instantBrowseUrl != null) {
        InAppBrowserScreen(
            app = state.activeRunningApp,
            instantUrl = state.instantBrowseUrl,
            onClose = { viewModel.closePlayer() }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ElegantBackground,
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ElegantCardBorder, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                containerColor = ElegantNav,
                tonalElevation = 8.dp
            ) {
                // Tab 1: Mis Apps
                NavigationBarItem(
                    selected = state.activeTab == MainNavTab.MY_APPS,
                    onClick = { viewModel.setActiveTab(MainNavTab.MY_APPS) },
                    icon = {
                        Icon(
                            if (state.activeTab == MainNavTab.MY_APPS) Icons.Filled.Apps else Icons.Outlined.Apps,
                            contentDescription = "Mis Apps"
                        )
                    },
                    label = {
                        Text(
                            "Mis Apps",
                            fontWeight = if (state.activeTab == MainNavTab.MY_APPS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LavenderPrimary,
                        selectedTextColor = LavenderPrimary,
                        unselectedIconColor = TextGrayMuted,
                        unselectedTextColor = TextGrayMuted,
                        indicatorColor = DeepPurpleContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_my_apps")
                )

                // Tab 2: Explorar
                NavigationBarItem(
                    selected = state.activeTab == MainNavTab.EXPLORE_CATALOG,
                    onClick = { viewModel.setActiveTab(MainNavTab.EXPLORE_CATALOG) },
                    icon = {
                        Icon(
                            if (state.activeTab == MainNavTab.EXPLORE_CATALOG) Icons.Filled.Explore else Icons.Outlined.Explore,
                            contentDescription = "Explorar"
                        )
                    },
                    label = {
                        Text(
                            "Explorar",
                            fontWeight = if (state.activeTab == MainNavTab.EXPLORE_CATALOG) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LavenderPrimary,
                        selectedTextColor = LavenderPrimary,
                        unselectedIconColor = TextGrayMuted,
                        unselectedTextColor = TextGrayMuted,
                        indicatorColor = DeepPurpleContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_explore")
                )

                // Tab 3: Turbo Studio
                NavigationBarItem(
                    selected = state.activeTab == MainNavTab.TURBO_STUDIO,
                    onClick = { viewModel.setActiveTab(MainNavTab.TURBO_STUDIO) },
                    icon = {
                        Icon(
                            if (state.activeTab == MainNavTab.TURBO_STUDIO) Icons.Filled.Speed else Icons.Outlined.Speed,
                            contentDescription = "Turbo Studio"
                        )
                    },
                    label = {
                        Text(
                            "Turbo Studio",
                            fontWeight = if (state.activeTab == MainNavTab.TURBO_STUDIO) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LavenderPrimary,
                        selectedTextColor = LavenderPrimary,
                        unselectedIconColor = TextGrayMuted,
                        unselectedTextColor = TextGrayMuted,
                        indicatorColor = DeepPurpleContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_turbo")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            // App Top Bar in Elegant Dark styling (Responsive & Clean)
            val appInfo = androidx.compose.runtime.remember { com.example.util.OtaUpdateManager.getAppVersionInfo(context) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LavenderPrimary)
                            .border(1.dp, LavenderPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "W",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = DeepPurpleOnPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "WebNative",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = TextWhite,
                                letterSpacing = (-0.5).sp,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DeepPurpleContainer)
                                    .clickable { showOtaUpdateDialog = true }
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "v${appInfo.versionName}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = LavenderPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                        Text(
                            text = "Web-to-Native Engine",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGrayMuted,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Action buttons row (Updates & Add)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Update Center Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DeepPurpleContainer)
                            .border(1.dp, LavenderPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .clickable { showOtaUpdateDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = "Actualizaciones",
                            tint = LavenderPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Quick Add button (Never broken or wrapped)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(LavenderPrimary)
                            .clickable { viewModel.openCreateDialog() }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Nueva App",
                                tint = DeepPurpleOnPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Nueva App",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepPurpleOnPrimary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            // Tab Content with Animated Transition
            AnimatedContent(
                targetState = state.activeTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "MainTabsAnimation",
                modifier = Modifier.weight(1f)
            ) { targetTab ->
                when (targetTab) {
                    MainNavTab.MY_APPS -> {
                        AppLibraryTab(
                            state = state,
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            onCategoryChange = { viewModel.setSelectedCategory(it) },
                            onLayoutModeChange = { viewModel.setLayoutMode(it) },
                            onLaunchApp = { viewModel.launchAppInPlayer(it) },
                            onPinShortcut = { viewModel.pinShortcut(context, it) },
                            onEditApp = { viewModel.openEditDialog(it) },
                            onDeleteApp = { viewModel.deleteApp(it) },
                            onCreateAppClick = { viewModel.openCreateDialog() },
                            onOpenSheetsSync = { viewModel.openGoogleSheetsSyncDialog() },
                            onRestoreCiverCloud = { viewModel.restoreCiverCloudSuite() }
                        )
                    }
                    MainNavTab.EXPLORE_CATALOG -> {
                        ExploreCatalogTab(
                            onInstallApp = { name, url, icon, accent, cat, hw, ad ->
                                viewModel.createOrSaveApp(
                                    name = name,
                                    url = url,
                                    iconType = "EMOJI",
                                    iconValue = icon,
                                    accentColor = accent,
                                    category = cat,
                                    isFullscreen = true,
                                    isHardwareBoostEnabled = hw,
                                    isAdBlockEnabled = ad,
                                    isBatterySaverBypassEnabled = true
                                )
                            },
                            onLaunchDirect = { url ->
                                viewModel.launchInstantUrl(url)
                            }
                        )
                    }
                    MainNavTab.TURBO_STUDIO -> {
                        TurboStudioTab(
                            state = state,
                            onRequestBatteryBypass = { viewModel.requestBatterySaverBypass(context) },
                            onClearAllCache = { viewModel.clearWebData(context) }
                        )
                    }
                }
            }
        }
    }

    // Create App Dialog
    if (state.isCreateDialogOpen) {
        CreateWebAppDialog(
            onDismiss = { viewModel.closeCreateDialog() },
            onConfirm = { name, url, iconType, iconValue, accentColor, category, isFullscreen, isHardwareBoost, isAdBlock, isBatteryBypass, isDesktop, customCss, customJs, isOled ->
                viewModel.createOrSaveApp(
                    name = name,
                    url = url,
                    iconType = iconType,
                    iconValue = iconValue,
                    accentColor = accentColor,
                    category = category,
                    isFullscreen = isFullscreen,
                    isHardwareBoostEnabled = isHardwareBoost,
                    isAdBlockEnabled = isAdBlock,
                    isBatterySaverBypassEnabled = isBatteryBypass,
                    isDesktopMode = isDesktop,
                    customCss = customCss,
                    customJs = customJs,
                    isOledBlackMode = isOled
                )
            }
        )
    }

    // Google Sheets Sync Dialog
    if (state.isGoogleSheetsSyncOpen) {
        GoogleSheetsSyncDialog(
            onDismiss = { viewModel.closeGoogleSheetsSyncDialog() },
            onImportApps = { apps ->
                viewModel.importAppsFromGoogleSheets(apps)
            },
            onPinAllApps = {
                viewModel.pinAllApps(context)
            }
        )
    }

    // Edit App Dialog
    state.editingApp?.let { editingApp ->
        EditWebAppDialog(
            app = editingApp,
            onDismiss = { viewModel.closeEditDialog() },
            onSave = { updated -> viewModel.updateExistingApp(updated) },
            onDelete = { toDelete ->
                viewModel.deleteApp(toDelete)
                viewModel.closeEditDialog()
            }
        )
    }

    // OTA Update Dialog (from notification tap or deep-link or top bar)
    if (showOtaUpdateDialog) {
        com.example.ui.dialogs.RemoteControlOtaDialog(
            onDismissRequest = {
                showOtaUpdateDialog = false
                com.example.util.OtaNotificationHelper.dismissNotification(context)
            },
            onSheetsSyncClick = {
                showOtaUpdateDialog = false
                viewModel.openGoogleSheetsSyncDialog()
            }
        )
    }
}
