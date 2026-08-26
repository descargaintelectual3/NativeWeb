package com.example.util

import android.content.Context
import android.util.Base64
import com.example.data.local.AppDatabase
import com.example.data.model.WebAppEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.regex.Pattern

object MultiUpdateEngines {

    fun convertGoogleDriveUrl(url: String): String {
        val trimmed = url.trim()
        val pattern = Pattern.compile("/d/([a-zA-Z0-9_-]+)")
        val matcher = pattern.matcher(trimmed)
        return if (matcher.find()) {
            "https://drive.google.com/uc?export=download&id=${matcher.group(1)}"
        } else if (trimmed.contains("id=")) {
            val idMatcher = Pattern.compile("id=([a-zA-Z0-9_-]+)").matcher(trimmed)
            if (idMatcher.find()) "https://drive.google.com/uc?export=download&id=${idMatcher.group(1)}" else trimmed
        } else {
            trimmed
        }
    }

    fun convertDropboxUrl(url: String): String {
        var trimmed = url.trim()
        if (trimmed.contains("dropbox.com")) {
            trimmed = trimmed.replace("dl=0", "dl=1")
            if (!trimmed.contains("dl=1")) trimmed = if (trimmed.contains("?")) "$trimmed&dl=1" else "$trimmed?dl=1"
        }
        return trimmed
    }

    suspend fun decodeBase64ToApk(context: Context, base64Content: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val cleanBase64 = base64Content
                .replace("data:application/vnd.android.package-archive;base64,", "")
                .replace("data:application/octet-stream;base64,", "")
                .replace("\\s".toRegex(), "")
            if (cleanBase64.length < 100) return@withContext Result.failure(Exception("El texto Base64 introducido es demasiado corto o inválido."))
            val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            if (decodedBytes.size < 10 * 1024) return@withContext Result.failure(Exception("Los datos decodificados son menores a 10 KB, no parece ser un archivo APK."))
            val targetFile = File(context.cacheDir, "WebNative_base64_patch.apk")
            if (targetFile.exists()) targetFile.delete()
            FileOutputStream(targetFile).use { output -> output.write(decodedBytes); output.flush() }
            Result.success(targetFile)
        } catch (e: Exception) {
            Result.failure(Exception("Error al decodificar Base64: ${e.localizedMessage}"))
        }
    }

    /** Exports all application data, OTA preferences and zero-click GitHub settings. Keep the file private. */
    suspend fun exportFullBackupJson(context: Context, scope: CoroutineScope): String = withContext(Dispatchers.IO) {
        val database = AppDatabase.getDatabase(context, scope)
        val root = JSONObject().apply {
            put("app", "WebNative Pro")
            put("schemaVersion", 2)
            put("version", OtaUpdateManager.getCurrentAppVersion(context))
            put("exportedAt", System.currentTimeMillis())
            put("containsSensitiveSettings", true)
        }

        val appsArray = JSONArray()
        database.webAppDao().getAllWebAppsList().forEach { app ->
            appsArray.put(JSONObject().apply {
                put("name", app.name)
                put("url", app.url)
                put("category", app.category)
                put("iconType", app.iconType)
                put("iconValue", app.iconValue)
                put("accentColor", app.accentColor)
                put("isDesktopMode", app.isDesktopMode)
                put("isFullscreen", app.isFullscreen)
                put("isHardwareBoostEnabled", app.isHardwareBoostEnabled)
                put("isBatterySaverBypassEnabled", app.isBatterySaverBypassEnabled)
                put("isAdBlockEnabled", app.isAdBlockEnabled)
                put("isOledBlackMode", app.isOledBlackMode)
                put("autoClearCacheOnExit", app.autoClearCacheOnExit)
                put("userAgent", app.userAgent)
                put("customCss", app.customCss)
                put("customJs", app.customJs)
                put("isPinnedShortcut", app.isPinnedShortcut)
                put("openCount", app.openCount)
                put("lastOpened", app.lastOpened)
                put("createdAt", app.createdAt)
            })
        }
        root.put("webApps", appsArray)

        root.put("settings", JSONObject().apply {
            put("globalCss", RemoteConfigEngine.getGlobalCustomCss(context))
            put("globalJs", RemoteConfigEngine.getGlobalCustomJs(context))
            put("remoteSourceUrl", RemoteConfigEngine.getRemoteSourceUrl(context))
            put("autoSyncEnabled", RemoteConfigEngine.isAutoSyncEnabled(context))
            put("syncIntervalMinutes", RemoteConfigEngine.getSyncInterval(context))
            put("sheetsUrl", GoogleSheetsSyncEngine.getSavedSheetUrl(context))
            put("otaManifestUrl", OtaUpdateManager.getManifestUrl(context))
            put("otaCustomDirectApkUrl", OtaUpdateManager.getCustomDirectApkUrl(context))
            put("otaAutoCheckEnabled", OtaUpdateManager.isAutoCheckEnabled(context))
            put("otaPushNotificationsEnabled", OtaUpdateManager.isPushNotificationsEnabled(context))
            put("otaIgnoredVersion", OtaUpdateManager.getIgnoredVersion(context))
            put("githubOwner", GitHubApiAutomation.getRepoOwner(context))
            put("githubRepo", GitHubApiAutomation.getRepoName(context))
            put("githubToken", GitHubApiAutomation.getGitHubToken(context))
        })
        root.toString(2)
    }

    /** Imports a backup and merges by URL so repeated imports do not duplicate catalog entries. */
    suspend fun importFullBackupJson(context: Context, jsonString: String, scope: CoroutineScope): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString.trim())
            val database = AppDatabase.getDatabase(context, scope)
            var count = 0
            val appsArray = root.optJSONArray("webApps") ?: JSONArray()
            for (i in 0 until appsArray.length()) {
                val item = appsArray.optJSONObject(i) ?: continue
                val url = item.optString("url", "").trim()
                if (url.isBlank()) continue
                val existing = database.webAppDao().getWebAppByUrl(url)
                val entity = WebAppEntity(
                    id = existing?.id ?: 0L,
                    name = item.optString("name", existing?.name ?: "App Importada"),
                    url = url,
                    category = item.optString("category", existing?.category ?: "Productividad"),
                    iconType = item.optString("iconType", existing?.iconType ?: "EMOJI"),
                    iconValue = item.optString("iconValue", existing?.iconValue ?: "🌐"),
                    accentColor = item.optLong("accentColor", existing?.accentColor ?: 0xFF00F5D4),
                    isDesktopMode = item.optBoolean("isDesktopMode", existing?.isDesktopMode ?: false),
                    isFullscreen = item.optBoolean("isFullscreen", existing?.isFullscreen ?: true),
                    isHardwareBoostEnabled = item.optBoolean("isHardwareBoostEnabled", existing?.isHardwareBoostEnabled ?: true),
                    isBatterySaverBypassEnabled = item.optBoolean("isBatterySaverBypassEnabled", existing?.isBatterySaverBypassEnabled ?: true),
                    isAdBlockEnabled = item.optBoolean("isAdBlockEnabled", existing?.isAdBlockEnabled ?: true),
                    isOledBlackMode = item.optBoolean("isOledBlackMode", existing?.isOledBlackMode ?: false),
                    autoClearCacheOnExit = item.optBoolean("autoClearCacheOnExit", existing?.autoClearCacheOnExit ?: false),
                    userAgent = item.optString("userAgent", existing?.userAgent ?: ""),
                    customCss = item.optString("customCss", existing?.customCss ?: ""),
                    customJs = item.optString("customJs", existing?.customJs ?: ""),
                    isPinnedShortcut = item.optBoolean("isPinnedShortcut", existing?.isPinnedShortcut ?: false),
                    openCount = item.optInt("openCount", existing?.openCount ?: 0),
                    lastOpened = item.optLong("lastOpened", existing?.lastOpened ?: System.currentTimeMillis()),
                    createdAt = item.optLong("createdAt", existing?.createdAt ?: System.currentTimeMillis())
                )
                database.webAppDao().insertWebApp(entity)
                count++
            }

            root.optJSONObject("settings")?.let { settings ->
                RemoteConfigEngine.setGlobalCustomCss(context, settings.optString("globalCss", ""))
                RemoteConfigEngine.setGlobalCustomJs(context, settings.optString("globalJs", ""))
                RemoteConfigEngine.setRemoteSourceUrl(context, settings.optString("remoteSourceUrl", ""))
                RemoteConfigEngine.setAutoSyncEnabled(context, settings.optBoolean("autoSyncEnabled", false))
                RemoteConfigEngine.setSyncInterval(context, settings.optInt("syncIntervalMinutes", 15).coerceIn(1, 1440))
                GoogleSheetsSyncEngine.saveSheetUrl(context, settings.optString("sheetsUrl", ""))
                OtaUpdateManager.setManifestUrl(context, settings.optString("otaManifestUrl", OtaUpdateManager.PRESET_GITHUB_RELEASES_API))
                OtaUpdateManager.setCustomDirectApkUrl(context, settings.optString("otaCustomDirectApkUrl", ""))
                OtaUpdateManager.setAutoCheckEnabled(context, settings.optBoolean("otaAutoCheckEnabled", false))
                OtaUpdateManager.setPushNotificationsEnabled(context, settings.optBoolean("otaPushNotificationsEnabled", false))
                OtaUpdateManager.setIgnoredVersion(context, settings.optString("otaIgnoredVersion", ""))
                GitHubApiAutomation.saveRepoInfo(context, settings.optString("githubOwner", GitHubApiAutomation.DEFAULT_OWNER), settings.optString("githubRepo", GitHubApiAutomation.DEFAULT_REPO))
                GitHubApiAutomation.saveGitHubToken(context, settings.optString("githubToken", ""))
            }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(Exception("Error al restaurar copia de seguridad: ${e.localizedMessage}"))
        }
    }
}
