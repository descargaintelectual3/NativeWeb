package com.example.util

import android.content.Context
import android.net.Uri
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
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

object MultiUpdateEngines {

    /**
     * Converts a Google Drive share URL (e.g., https://drive.google.com/file/d/XYZ/view?usp=sharing)
     * into a direct download URL.
     */
    fun convertGoogleDriveUrl(url: String): String {
        val trimmed = url.trim()
        val pattern = Pattern.compile("/d/([a-zA-Z0-9_-]+)")
        val matcher = pattern.matcher(trimmed)
        return if (matcher.find()) {
            val fileId = matcher.group(1)
            "https://drive.google.com/uc?export=download&id=$fileId"
        } else if (trimmed.contains("id=")) {
            val idPattern = Pattern.compile("id=([a-zA-Z0-9_-]+)")
            val idMatcher = idPattern.matcher(trimmed)
            if (idMatcher.find()) {
                val fileId = idMatcher.group(1)
                "https://drive.google.com/uc?export=download&id=$fileId"
            } else {
                trimmed
            }
        } else {
            trimmed
        }
    }

    /**
     * Converts a Dropbox share link to direct download.
     */
    fun convertDropboxUrl(url: String): String {
        var trimmed = url.trim()
        if (trimmed.contains("dropbox.com")) {
            trimmed = trimmed.replace("dl=0", "dl=1")
            if (!trimmed.contains("dl=1")) {
                trimmed = if (trimmed.contains("?")) "$trimmed&dl=1" else "$trimmed?dl=1"
            }
        }
        return trimmed
    }

    /**
     * Decodes a Base64 string into an APK file and saves it in cache for installation.
     */
    suspend fun decodeBase64ToApk(context: Context, base64Content: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val cleanBase64 = base64Content
                .replace("data:application/vnd.android.package-archive;base64,", "")
                .replace("data:application/octet-stream;base64,", "")
                .replace("\\s".toRegex(), "")

            if (cleanBase64.length < 100) {
                return@withContext Result.failure(Exception("El texto Base64 introducido es demasiado corto o inválido."))
            }

            val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            if (decodedBytes.size < 10 * 1024) {
                return@withContext Result.failure(Exception("Los datos decodificados son menores a 10 KB, no parece ser un archivo APK."))
            }

            val targetFile = File(context.cacheDir, "WebNative_base64_patch.apk")
            if (targetFile.exists()) targetFile.delete()

            FileOutputStream(targetFile).use { output ->
                output.write(decodedBytes)
                output.flush()
            }

            Result.success(targetFile)
        } catch (e: Exception) {
            Result.failure(Exception("Error al decodificar Base64: ${e.localizedMessage}"))
        }
    }

    /**
     * Exports full configuration & WebApps catalog as a clean JSON backup string.
     */
    suspend fun exportFullBackupJson(context: Context, scope: CoroutineScope): String = withContext(Dispatchers.IO) {
        val database = AppDatabase.getDatabase(context, scope)
        val apps = database.webAppDao().getAllWebAppsList()

        val root = JSONObject()
        root.put("app", "WebNative Pro")
        root.put("version", OtaUpdateManager.getCurrentAppVersion(context))
        root.put("exportedAt", System.currentTimeMillis())

        val appsArray = JSONArray()
        for (app in apps) {
            val item = JSONObject()
            item.put("name", app.name)
            item.put("url", app.url)
            item.put("category", app.category)
            item.put("iconType", app.iconType)
            item.put("iconValue", app.iconValue)
            item.put("accentColor", app.accentColor)
            item.put("isDesktopMode", app.isDesktopMode)
            item.put("isFullscreen", app.isFullscreen)
            item.put("isHardwareBoostEnabled", app.isHardwareBoostEnabled)
            item.put("isBatterySaverBypassEnabled", app.isBatterySaverBypassEnabled)
            item.put("isAdBlockEnabled", app.isAdBlockEnabled)
            item.put("isOledBlackMode", app.isOledBlackMode)
            item.put("autoClearCacheOnExit", app.autoClearCacheOnExit)
            item.put("userAgent", app.userAgent)
            item.put("customCss", app.customCss)
            item.put("customJs", app.customJs)
            appsArray.put(item)
        }
        root.put("webApps", appsArray)

        val settings = JSONObject()
        settings.put("globalCss", RemoteConfigEngine.getGlobalCustomCss(context))
        settings.put("globalJs", RemoteConfigEngine.getGlobalCustomJs(context))
        settings.put("remoteSourceUrl", RemoteConfigEngine.getRemoteSourceUrl(context))
        root.put("settings", settings)

        root.toString(2)
    }

    /**
     * Imports configuration & WebApps catalog from JSON backup string.
     */
    suspend fun importFullBackupJson(context: Context, jsonString: String, scope: CoroutineScope): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString.trim())
            val database = AppDatabase.getDatabase(context, scope)
            var count = 0

            if (root.has("webApps")) {
                val appsArray = root.getJSONArray("webApps")
                for (i in 0 until appsArray.length()) {
                    val item = appsArray.getJSONObject(i)
                    val name = item.optString("name", "App Importada")
                    val url = item.optString("url", "")
                    if (url.isBlank()) continue

                    val entity = WebAppEntity(
                        name = name,
                        url = url,
                        category = item.optString("category", "Productividad"),
                        iconType = item.optString("iconType", "EMOJI"),
                        iconValue = item.optString("iconValue", "🌐"),
                        accentColor = item.optLong("accentColor", 0xFF00F5D4),
                        isDesktopMode = item.optBoolean("isDesktopMode", false),
                        isFullscreen = item.optBoolean("isFullscreen", true),
                        isHardwareBoostEnabled = item.optBoolean("isHardwareBoostEnabled", true),
                        isBatterySaverBypassEnabled = item.optBoolean("isBatterySaverBypassEnabled", true),
                        isAdBlockEnabled = item.optBoolean("isAdBlockEnabled", true),
                        isOledBlackMode = item.optBoolean("isOledBlackMode", false),
                        autoClearCacheOnExit = item.optBoolean("autoClearCacheOnExit", false),
                        userAgent = item.optString("userAgent", ""),
                        customCss = item.optString("customCss", ""),
                        customJs = item.optString("customJs", "")
                    )
                    database.webAppDao().insertWebApp(entity)
                    count++
                }
            }

            if (root.has("settings")) {
                val settings = root.getJSONObject("settings")
                val globalCss = settings.optString("globalCss", "")
                val globalJs = settings.optString("globalJs", "")
                val remoteSourceUrl = settings.optString("remoteSourceUrl", "")

                if (globalCss.isNotBlank()) RemoteConfigEngine.setGlobalCustomCss(context, globalCss)
                if (globalJs.isNotBlank()) RemoteConfigEngine.setGlobalCustomJs(context, globalJs)
                if (remoteSourceUrl.isNotBlank()) RemoteConfigEngine.setRemoteSourceUrl(context, remoteSourceUrl)
            }

            Result.success(count)
        } catch (e: Exception) {
            Result.failure(Exception("Error al restaurar copia de seguridad: ${e.localizedMessage}"))
        }
    }
}
