package com.example.util

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.model.WebAppEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

sealed class RemoteSyncState {
    object Idle : RemoteSyncState()
    object Syncing : RemoteSyncState()
    data class Success(val appsCount: Int, val source: String, val timestamp: Long) : RemoteSyncState()
    data class Failed(val error: String) : RemoteSyncState()
}

object RemoteConfigEngine {
    private const val PREFS_NAME = "webnative_remote_config"
    private const val KEY_AUTO_SYNC = "auto_sync_enabled"
    private const val KEY_SYNC_INTERVAL_MINS = "sync_interval_mins"
    private const val KEY_LAST_SYNC_TIME = "last_sync_timestamp"
    private const val KEY_GLOBAL_CUSTOM_CSS = "global_remote_css"
    private const val KEY_GLOBAL_CUSTOM_JS = "global_remote_js"
    private const val KEY_REMOTE_SOURCE_URL = "remote_source_endpoint"

    const val DEFAULT_SAMPLE_CATALOG_URL =
        "https://raw.githubusercontent.com/descargaintelectual3/NativeWeb/main/apps_catalog.json"

    private val _syncState = MutableStateFlow<RemoteSyncState>(RemoteSyncState.Idle)
    val syncState: StateFlow<RemoteSyncState> = _syncState.asStateFlow()

    fun isAutoSyncEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUTO_SYNC, false)
    }

    fun setAutoSyncEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTO_SYNC, enabled).apply()
    }

    fun getSyncInterval(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_SYNC_INTERVAL_MINS, 15)
    }

    fun setSyncInterval(context: Context, minutes: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_SYNC_INTERVAL_MINS, minutes).apply()
    }

    fun getLastSyncTime(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_LAST_SYNC_TIME, 0L)
    }

    fun getGlobalCustomCss(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_GLOBAL_CUSTOM_CSS, "") ?: ""
    }

    fun setGlobalCustomCss(context: Context, css: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_GLOBAL_CUSTOM_CSS, css).apply()
    }

    fun getGlobalCustomJs(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_GLOBAL_CUSTOM_JS, "") ?: ""
    }

    fun setGlobalCustomJs(context: Context, js: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_GLOBAL_CUSTOM_JS, js).apply()
    }

    fun getRemoteSourceUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_REMOTE_SOURCE_URL, "")
        return if (!saved.isNullOrBlank()) {
            saved
        } else {
            val sheetUrl = GoogleSheetsSyncEngine.getSavedSheetUrl(context)
            if (sheetUrl.isNotBlank()) sheetUrl else DEFAULT_SAMPLE_CATALOG_URL
        }
    }

    fun setRemoteSourceUrl(context: Context, url: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_REMOTE_SOURCE_URL, url.trim()).apply()
    }

    suspend fun performRemoteSync(
        context: Context,
        scope: CoroutineScope,
        sourceUrlOverride: String? = null
    ): RemoteSyncState = withContext(Dispatchers.IO) {
        _syncState.value = RemoteSyncState.Syncing

        val rawUrl = (sourceUrlOverride ?: getRemoteSourceUrl(context)).trim()

        if (rawUrl.isBlank()) {
            val failed = RemoteSyncState.Failed("Introduce una URL de Google Sheets (CSV) o endpoint JSON para sincronizar")
            _syncState.value = failed
            return@withContext failed
        }

        try {
            val result = if (rawUrl.contains("docs.google.com") || rawUrl.endsWith(".csv") || rawUrl.contains("output=csv")) {
                GoogleSheetsSyncEngine.syncFromUrl(context, rawUrl, scope)
            } else {
                syncFromJsonEndpoint(context, rawUrl, scope)
            }

            result.fold(
                onSuccess = { count ->
                    val now = System.currentTimeMillis()
                    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    prefs.edit().putLong(KEY_LAST_SYNC_TIME, now).apply()

                    val success = RemoteSyncState.Success(count, rawUrl, now)
                    _syncState.value = success
                    success
                },
                onFailure = { err ->
                    val failed = RemoteSyncState.Failed(err.localizedMessage ?: "Error al sincronizar datos")
                    _syncState.value = failed
                    failed
                }
            )
        } catch (e: Exception) {
            val failed = RemoteSyncState.Failed(e.localizedMessage ?: "Fallo de conexión en sincronización remota")
            _syncState.value = failed
            failed
        }
    }

    private suspend fun syncFromJsonEndpoint(
        context: Context,
        jsonUrl: String,
        scope: CoroutineScope
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val url = URL(jsonUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.setRequestProperty("Accept", "application/json, text/plain, */*")
            conn.setRequestProperty("User-Agent", "WebNative-Android-App")

            val responseCode = conn.responseCode
            if (responseCode !in 200..299) {
                return@withContext Result.failure(Exception("Servidor respondió con código HTTP $responseCode"))
            }

            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val database = AppDatabase.getDatabase(context, scope)

            var importedCount = 0

            val appsArray = if (body.trim().startsWith("[")) {
                JSONArray(body)
            } else {
                val obj = JSONObject(body)
                when {
                    obj.has("apps") -> obj.getJSONArray("apps")
                    obj.has("catalog") -> obj.getJSONArray("catalog")
                    obj.has("items") -> obj.getJSONArray("items")
                    else -> JSONArray()
                }
            }

            for (i in 0 until appsArray.length()) {
                val item = appsArray.optJSONObject(i) ?: continue
                val name = item.optString("name", item.optString("title", "App Remota"))
                val targetUrl = item.optString("url", item.optString("targetUrl", item.optString("link", "")))

                if (targetUrl.isBlank()) continue

                val category = item.optString("category", "Cloud & Empresa")
                val isDesktopMode = item.optBoolean("isDesktopMode", item.optBoolean("desktop", false))
                val userAgent = item.optString("userAgent", "")
                val iconType = item.optString("iconType", "EMOJI")
                val iconValue = item.optString("iconValue", item.optString("icon", "⚡"))

                val entity = WebAppEntity(
                    name = name,
                    url = targetUrl,
                    category = category,
                    iconType = iconType,
                    iconValue = iconValue,
                    isDesktopMode = isDesktopMode,
                    userAgent = userAgent
                )
                database.webAppDao().insertWebApp(entity)
                importedCount++
            }

            Result.success(importedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
