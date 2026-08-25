package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.WebAppEntity
import com.example.data.repository.WebAppRepository
import com.example.util.FaviconHelper
import com.example.util.GoogleSheetsSyncEngine
import com.example.util.TurboPowerManager
import com.example.util.WebShortcutHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainNavTab {
    MY_APPS,
    EXPLORE_CATALOG,
    TURBO_STUDIO
}

enum class AppLayoutMode {
    GRID_2,
    GRID_3,
    LIST_DETAILED,
    HERO_CAROUSEL
}

data class WebAppUiState(
    val webApps: List<WebAppEntity> = emptyList(),
    val filteredWebApps: List<WebAppEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "Todas",
    val activeTab: MainNavTab = MainNavTab.MY_APPS,
    val layoutMode: AppLayoutMode = AppLayoutMode.GRID_2,
    val showStatsWidget: Boolean = true,
    val showCiverCloudBanner: Boolean = true,
    val showQuickActions: Boolean = true,
    val isHardwareBoostGlobal: Boolean = true,
    val isAdBlockGlobal: Boolean = true,
    val isBatterySaverBypassed: Boolean = false,
    val totalBlockedAds: Int = 0,
    val activeRunningApp: WebAppEntity? = null,
    val isCreateDialogOpen: Boolean = false,
    val isGoogleSheetsSyncOpen: Boolean = false,
    val editingApp: WebAppEntity? = null,
    val instantBrowseUrl: String? = null
)

class WebAppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WebAppRepository
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow("Todas")
    private val _activeTab = MutableStateFlow(MainNavTab.MY_APPS)
    private val _layoutMode = MutableStateFlow(AppLayoutMode.GRID_2)
    private val _showStatsWidget = MutableStateFlow(true)
    private val _showCiverCloudBanner = MutableStateFlow(true)
    private val _showQuickActions = MutableStateFlow(true)
    private val _isHardwareBoostGlobal = MutableStateFlow(true)
    private val _isAdBlockGlobal = MutableStateFlow(true)
    private val _totalBlockedAds = MutableStateFlow(0)
    private val _activeRunningApp = MutableStateFlow<WebAppEntity?>(null)
    private val _isCreateDialogOpen = MutableStateFlow(false)
    private val _isGoogleSheetsSyncOpen = MutableStateFlow(false)
    private val _editingApp = MutableStateFlow<WebAppEntity?>(null)
    private val _instantBrowseUrl = MutableStateFlow<String?>(null)
    private val _isBatterySaverBypassed = MutableStateFlow(false)

    val uiState: StateFlow<WebAppUiState>

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = WebAppRepository(database.webAppDao())
        _isBatterySaverBypassed.value = TurboPowerManager.isIgnoringBatteryOptimizations(application)

        // Seed default Civer Cloud and preloaded apps if empty
        viewModelScope.launch {
            repository.allWebApps.collect { currentApps ->
                if (currentApps.isEmpty()) {
                    repository.insertAllWebApps(AppDatabase.DEFAULT_PRELOADED_APPS)
                }
            }
        }

        uiState = combine(
            repository.allWebApps,
            _searchQuery,
            _selectedCategory,
            _activeTab,
            _layoutMode,
            _showStatsWidget,
            _showCiverCloudBanner,
            _showQuickActions,
            _isHardwareBoostGlobal,
            _isAdBlockGlobal,
            _totalBlockedAds,
            _activeRunningApp,
            _isCreateDialogOpen,
            _isGoogleSheetsSyncOpen,
            _editingApp,
            _instantBrowseUrl,
            _isBatterySaverBypassed
        ) { values ->
            @Suppress("UNCHECKED_CAST")
            val apps = values[0] as List<WebAppEntity>
            val query = values[1] as String
            val category = values[2] as String
            val tab = values[3] as MainNavTab
            val layout = values[4] as AppLayoutMode
            val statsW = values[5] as Boolean
            val civerW = values[6] as Boolean
            val quickW = values[7] as Boolean
            val hwBoost = values[8] as Boolean
            val adBlock = values[9] as Boolean
            val blockedAds = values[10] as Int
            val activeApp = values[11] as WebAppEntity?
            val createDialog = values[12] as Boolean
            val sheetsDialog = values[13] as Boolean
            val editing = values[14] as WebAppEntity?
            val instantUrl = values[15] as String?
            val batteryBypassed = values[16] as Boolean

            val filtered = apps.filter { app ->
                val matchesQuery = query.isBlank() ||
                        app.name.contains(query, ignoreCase = true) ||
                        app.url.contains(query, ignoreCase = true)
                val matchesCat = category == "Todas" || app.category.equals(category, ignoreCase = true)
                matchesQuery && matchesCat
            }

            WebAppUiState(
                webApps = apps,
                filteredWebApps = filtered,
                searchQuery = query,
                selectedCategory = category,
                activeTab = tab,
                layoutMode = layout,
                showStatsWidget = statsW,
                showCiverCloudBanner = civerW,
                showQuickActions = quickW,
                isHardwareBoostGlobal = hwBoost,
                isAdBlockGlobal = adBlock,
                totalBlockedAds = blockedAds,
                activeRunningApp = activeApp,
                isCreateDialogOpen = createDialog,
                isGoogleSheetsSyncOpen = sheetsDialog,
                editingApp = editing,
                instantBrowseUrl = instantUrl,
                isBatterySaverBypassed = batteryBypassed
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WebAppUiState()
        )
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setActiveTab(tab: MainNavTab) {
        _activeTab.value = tab
    }

    fun setLayoutMode(mode: AppLayoutMode) {
        _layoutMode.value = mode
    }

    fun toggleStatsWidget() {
        _showStatsWidget.value = !_showStatsWidget.value
    }

    fun toggleCiverBanner() {
        _showCiverCloudBanner.value = !_showCiverCloudBanner.value
    }

    fun toggleQuickActions() {
        _showQuickActions.value = !_showQuickActions.value
    }

    fun openCreateDialog(prefillUrl: String = "", prefillName: String = "") {
        _isCreateDialogOpen.value = true
    }

    fun closeCreateDialog() {
        _isCreateDialogOpen.value = false
    }

    fun openGoogleSheetsSyncDialog() {
        _isGoogleSheetsSyncOpen.value = true
    }

    fun closeGoogleSheetsSyncDialog() {
        _isGoogleSheetsSyncOpen.value = false
    }

    fun openEditDialog(app: WebAppEntity) {
        _editingApp.value = app
    }

    fun closeEditDialog() {
        _editingApp.value = null
    }

    fun createOrSaveApp(
        name: String,
        url: String,
        iconType: String = "EMOJI",
        iconValue: String = "⚡",
        accentColor: Long = 0xFF00F5D4,
        category: String = "Productividad",
        isFullscreen: Boolean = true,
        isHardwareBoostEnabled: Boolean = true,
        isAdBlockEnabled: Boolean = true,
        isBatterySaverBypassEnabled: Boolean = true,
        isDesktopMode: Boolean = false,
        customCss: String = "",
        customJs: String = "",
        isOledBlackMode: Boolean = false
    ) {
        val cleanUrl = FaviconHelper.cleanUrl(url)
        val finalName = if (name.isBlank()) FaviconHelper.extractDomainName(cleanUrl) else name.trim()

        viewModelScope.launch {
            val newApp = WebAppEntity(
                name = finalName,
                url = cleanUrl,
                iconType = iconType,
                iconValue = iconValue,
                accentColor = accentColor,
                category = category,
                isFullscreen = isFullscreen,
                isHardwareBoostEnabled = isHardwareBoostEnabled,
                isAdBlockEnabled = isAdBlockEnabled,
                isBatterySaverBypassEnabled = isBatterySaverBypassEnabled,
                isDesktopMode = isDesktopMode,
                customCss = customCss,
                customJs = customJs,
                isOledBlackMode = isOledBlackMode
            )
            repository.insertWebApp(newApp)
            _isCreateDialogOpen.value = false
        }
    }

    fun importAppsFromGoogleSheets(apps: List<WebAppEntity>) {
        viewModelScope.launch {
            repository.insertAllWebApps(apps)
        }
    }

    fun restoreCiverCloudSuite() {
        viewModelScope.launch {
            repository.insertAllWebApps(AppDatabase.DEFAULT_PRELOADED_APPS)
        }
    }

    fun pinAllApps(context: Context) {
        viewModelScope.launch {
            val currentApps = uiState.value.webApps
            for (app in currentApps) {
                WebShortcutHelper.pinWebAppShortcut(context, app) { success ->
                    if (success) {
                        viewModelScope.launch {
                            repository.updatePinnedStatus(app.id, true)
                        }
                    }
                }
            }
        }
    }

    fun updateExistingApp(app: WebAppEntity) {
        viewModelScope.launch {
            repository.updateWebApp(app)
            _editingApp.value = null
        }
    }

    fun deleteApp(app: WebAppEntity) {
        viewModelScope.launch {
            repository.deleteWebApp(app)
        }
    }

    fun launchAppInPlayer(app: WebAppEntity) {
        viewModelScope.launch {
            repository.recordAppOpen(app.id)
            _activeRunningApp.value = app
        }
    }

    fun launchInstantUrl(url: String) {
        val cleanUrl = FaviconHelper.cleanUrl(url)
        _instantBrowseUrl.value = cleanUrl
    }

    fun closePlayer() {
        _activeRunningApp.value = null
        _instantBrowseUrl.value = null
        TurboPowerManager.releaseWakeLock()
    }

    fun pinShortcut(context: Context, app: WebAppEntity) {
        viewModelScope.launch {
            WebShortcutHelper.pinWebAppShortcut(context, app) { success ->
                if (success) {
                    viewModelScope.launch {
                        repository.updatePinnedStatus(app.id, true)
                    }
                }
            }
        }
    }

    fun incrementBlockedAds() {
        _totalBlockedAds.value += 1
    }

    fun clearWebData(context: Context) {
        TurboPowerManager.clearAllWebData(context)
    }

    fun requestBatterySaverBypass(context: Context) {
        TurboPowerManager.requestIgnoreBatteryOptimization(context)
        _isBatterySaverBypassed.value = TurboPowerManager.isIgnoringBatteryOptimizations(context)
    }

    fun refreshBatteryOptimizationStatus(context: Context) {
        _isBatterySaverBypassed.value = TurboPowerManager.isIgnoringBatteryOptimizations(context)
    }
}
