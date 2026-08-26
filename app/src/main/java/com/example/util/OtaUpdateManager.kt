package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AppVersionInfo(
    val versionName: String,
    val versionCode: Long,
    val packageName: String,
    val firstInstallTime: String,
    val lastUpdateTime: String,
    val isSystemOrDebug: Boolean
)

sealed class UpdateStatus {
    object Idle : UpdateStatus()
    data class Checking(val sourceUrl: String = "") : UpdateStatus()
    data class UpdateAvailable(
        val versionName: String,
        val versionCode: Int,
        val changelog: String,
        val downloadUrl: String,
        val publishedAt: String,
        val isIgnored: Boolean = false,
        val isDirectApkAvailable: Boolean = true,
        val mirrorUrls: List<String> = emptyList()
    ) : UpdateStatus()
    object UpToDate : UpdateStatus()
    data class Downloading(
        val progressPercent: Int,
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val downloadSpeedKbps: Long = 0,
        val sourceLabel: String = ""
    ) : UpdateStatus()
    data class ReadyToInstall(val apkFile: File, val versionName: String) : UpdateStatus()
    data class Error(
        val message: String,
        val fallbackUrl: String? = null,
        val suggestedDirectUrl: String? = null
    ) : UpdateStatus()
}

object OtaUpdateManager {
    private const val PREFS_NAME = "webnative_ota_prefs"
    private const val KEY_UPDATE_URL = "ota_update_manifest_url"
    private const val KEY_CUSTOM_DIRECT_APK = "ota_custom_direct_apk_url"
    private const val KEY_AUTO_CHECK = "ota_auto_check_enabled"
    private const val KEY_PUSH_NOTIFICATIONS = "ota_push_notifications_enabled"
    private const val KEY_LAST_CHECK_TIME = "ota_last_check_timestamp"
    private const val KEY_IGNORED_VERSION = "ota_ignored_version"
    private const val KEY_LAST_NOTIFIED_VERSION = "ota_last_notified_version"
    private const val KEY_LAST_NOTIFIED_TIME = "ota_last_notified_time"

    // Presets & GitHub Repositories
    const val GITHUB_REPO_WEB = "https://github.com/descargaintelectual3/NativeWeb"
    const val PRESET_GITHUB_RELEASES_WEB = "https://github.com/descargaintelectual3/NativeWeb/releases"
    const val PRESET_GITHUB_PACKAGE_JSON = "https://raw.githubusercontent.com/descargaintelectual3/NativeWeb/main/package.json"
    const val PRESET_GITHUB_VERSION_MANIFEST = "https://raw.githubusercontent.com/descargaintelectual3/NativeWeb/main/version_manifest.json"
    const val PRESET_GITHUB_RELEASES_API = "https://api.github.com/repos/descargaintelectual3/NativeWeb/releases/latest"
    const val PRESET_DEFAULT_DIRECT_APK = "https://github.com/descargaintelectual3/NativeWeb/releases/latest/download/app-debug.apk"
    const val PRESET_DEFAULT_RELEASE_APK = "https://github.com/descargaintelectual3/NativeWeb/releases/latest/download/app-release.apk"
    const val PRESET_RAW_MAIN_APK = "https://raw.githubusercontent.com/descargaintelectual3/NativeWeb/main/app/build/outputs/apk/debug/app-debug.apk"

    private val _updateState = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val updateState: StateFlow<UpdateStatus> = _updateState.asStateFlow()

    fun resetState() {
        _updateState.value = UpdateStatus.Idle
    }

    fun getAppVersionInfo(context: Context): AppVersionInfo {
        return try {
            val pInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            val vName = pInfo.versionName ?: "5.0.0"
            val vCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode.toLong()
            }
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val installDate = sdf.format(Date(pInfo.firstInstallTime))
            val updateDate = sdf.format(Date(pInfo.lastUpdateTime))

            AppVersionInfo(
                versionName = vName,
                versionCode = vCode,
                packageName = context.packageName,
                firstInstallTime = installDate,
                lastUpdateTime = updateDate,
                isSystemOrDebug = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
            )
        } catch (e: Exception) {
            AppVersionInfo(
                versionName = "5.0.0",
                versionCode = 500,
                packageName = context.packageName,
                firstInstallTime = "Desconocida",
                lastUpdateTime = "Desconocida",
                isSystemOrDebug = true
            )
        }
    }

    fun getCurrentAppVersion(context: Context): String {
        return getAppVersionInfo(context).versionName
    }

    fun getManifestUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_UPDATE_URL, "")
        return if (!saved.isNullOrBlank()) saved else PRESET_GITHUB_RELEASES_API
    }

    fun setManifestUrl(context: Context, url: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_UPDATE_URL, url.trim()).apply()
    }

    fun getCustomDirectApkUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CUSTOM_DIRECT_APK, "") ?: ""
    }

    fun setCustomDirectApkUrl(context: Context, url: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CUSTOM_DIRECT_APK, url.trim()).apply()
    }

    fun getLastNotifiedVersion(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LAST_NOTIFIED_VERSION, "") ?: ""
    }

    fun getLastNotifiedTime(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_LAST_NOTIFIED_TIME, 0L)
    }

    fun recordNotificationSent(context: Context, version: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_LAST_NOTIFIED_VERSION, version)
            .putLong(KEY_LAST_NOTIFIED_TIME, System.currentTimeMillis())
            .apply()
    }

    fun isAutoCheckEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUTO_CHECK, false)
    }

    fun setAutoCheckEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTO_CHECK, enabled).apply()
        if (enabled) {
            OtaNotificationHelper.createNotificationChannel(context)
            OtaUpdateWorker.schedule(context)
        } else {
            OtaUpdateWorker.cancel(context)
        }
    }

    fun isPushNotificationsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_PUSH_NOTIFICATIONS, false)
    }

    fun setPushNotificationsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_PUSH_NOTIFICATIONS, enabled).apply()
        if (enabled) {
            OtaNotificationHelper.createNotificationChannel(context)
            if (isAutoCheckEnabled(context)) OtaUpdateWorker.schedule(context)
        } else {
            OtaNotificationHelper.dismissNotification(context)
        }
    }

    fun getIgnoredVersion(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_IGNORED_VERSION, "") ?: ""
    }

    fun setIgnoredVersion(context: Context, version: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_IGNORED_VERSION, version).apply()
        OtaNotificationHelper.dismissNotification(context)
    }

    fun clearIgnoredVersion(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_IGNORED_VERSION).apply()
    }

    fun startContinuousUpdatePoller(context: Context, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                if (isAutoCheckEnabled(context) && isPushNotificationsEnabled(context)) {
                    checkForUpdates(context, notifyIfAvailable = true)
                }
                delay(30 * 60 * 1000L) // 30 mins
            }
        }
    }

    data class UrlCheckResult(
        val url: String,
        val exists: Boolean,
        val httpCode: Int,
        val isApk: Boolean,
        val contentLength: Long,
        val contentType: String,
        val responseTimeMs: Long,
        val errorMessage: String? = null
    )

    /**
     * Performs a REAL HTTP verification (HEAD/GET) to check if a remote APK or URL actually exists.
     */
    suspend fun verifyRemoteUrlReal(targetUrl: String): UrlCheckResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        var currentUrl = targetUrl.trim()
        var redirects = 0
        val maxRedirects = 5

        try {
            while (redirects < maxRedirects) {
                val url = URL(currentUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 6000
                conn.readTimeout = 6000
                conn.instanceFollowRedirects = false
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
                conn.setRequestProperty("Accept", "*/*")

                val code = conn.responseCode
                val elapsed = System.currentTimeMillis() - startTime

                // Handle Redirects (e.g. GitHub release downloads redirect to AWS S3)
                if (code in listOf(301, 302, 303, 307, 308)) {
                    val location = conn.getHeaderField("Location")
                    if (!location.isNullOrBlank()) {
                        currentUrl = location
                        redirects++
                        continue
                    }
                }

                val cType = conn.contentType ?: "unknown"
                val cLength = conn.contentLength.toLong()
                val isApkType = cType.contains("vnd.android.package-archive", ignoreCase = true) ||
                        cType.contains("octet-stream", ignoreCase = true) ||
                        currentUrl.contains(".apk", ignoreCase = true)

                val exists = code in 200..299 && (cLength > 50 * 1024 || isApkType)

                return@withContext UrlCheckResult(
                    url = targetUrl,
                    exists = exists,
                    httpCode = code,
                    isApk = isApkType,
                    contentLength = if (cLength > 0) cLength else 0L,
                    contentType = cType,
                    responseTimeMs = elapsed,
                    errorMessage = if (!exists) {
                        if (code == 404) "El archivo o release NO existe en el servidor (Error HTTP 404 Not Found)."
                        else if (code == 403) "Acceso denegado por el servidor o límite de API alcanzado (Error HTTP 403)."
                        else "El servidor respondió con código HTTP $code y contenido no binario."
                    } else null
                )
            }
            UrlCheckResult(targetUrl, false, -1, false, 0, "none", System.currentTimeMillis() - startTime, "Demasiadas redirecciones")
        } catch (e: Exception) {
            UrlCheckResult(
                url = targetUrl,
                exists = false,
                httpCode = 0,
                isApk = false,
                contentLength = 0,
                contentType = "error",
                responseTimeMs = System.currentTimeMillis() - startTime,
                errorMessage = "Error de red/conexión: ${e.localizedMessage ?: "Servidor inalcanzable"}"
            )
        }
    }

    // =========================================================================
    // MÉTODO 1 & VERIFICACIÓN REAL DE ACTUALIZACIONES
    // =========================================================================
    suspend fun checkForUpdates(
        context: Context,
        customUrl: String? = null,
        notifyIfAvailable: Boolean = false
    ): UpdateStatus = withContext(Dispatchers.IO) {
        val rawUrl = (customUrl ?: getManifestUrl(context)).trim()
        _updateState.value = UpdateStatus.Checking(rawUrl)

        val status = queryEndpoint(context, rawUrl)

        if (status is UpdateStatus.UpdateAvailable) {
            val ignored = getIgnoredVersion(context)
            if (ignored.isNotBlank() && ignored.equals(status.versionName, ignoreCase = true)) {
                _updateState.value = status.copy(isIgnored = true)
                return@withContext status
            } else if (notifyIfAvailable && isPushNotificationsEnabled(context)) {
                OtaNotificationHelper.showUpdateNotification(
                    context,
                    versionName = status.versionName,
                    changelog = status.changelog,
                    downloadUrl = status.downloadUrl
                )
            }
        }

        _updateState.value = status
        return@withContext status
    }

    private fun queryEndpoint(context: Context, urlString: String): UpdateStatus {
        try {
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("Accept", "application/json, text/plain, */*")
            conn.setRequestProperty("User-Agent", "WebNative-Android-App")

            val responseCode = conn.responseCode
            if (responseCode !in 200..299) {
                val msg = when (responseCode) {
                    404 -> "No se encontraron Releases o APKs publicados en este repositorio GitHub (HTTP 404 Not Found). Las releases deben crearse en GitHub con el APK adjunto, o usar un método directo (Local, Drive, Sheets OTA)."
                    403 -> "Límite de peticiones de GitHub API alcanzado (HTTP 403 Forbidden). Usa descarga por URL directa o APK local."
                    else -> "El servidor de actualizaciones respondió con código HTTP $responseCode"
                }
                return UpdateStatus.Error(
                    message = msg,
                    fallbackUrl = PRESET_GITHUB_RELEASES_WEB,
                    suggestedDirectUrl = null
                )
            }

            val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(jsonStr)

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putLong(KEY_LAST_CHECK_TIME, System.currentTimeMillis()).apply()

            val tagName = json.optString("tag_name",
                json.optString("versionName",
                    json.optString("version", "")
                )
            )

            if (tagName.isBlank()) {
                return UpdateStatus.Error(
                    message = "El endpoint no contiene información válida de versión.",
                    fallbackUrl = PRESET_GITHUB_RELEASES_WEB,
                    suggestedDirectUrl = null
                )
            }

            val remoteVersionCodeFromManifest = json.optInt("versionCode", 0)
            val formattedVersion = if (tagName.startsWith("v", ignoreCase = true)) tagName else "v$tagName"
            val remoteVersionCode = if (remoteVersionCodeFromManifest > 0) {
                remoteVersionCodeFromManifest
            } else {
                versionNameToCode(formattedVersion)
            }
            val installedVersion = getAppVersionInfo(context)
            if (remoteVersionCode > 0 && remoteVersionCode <= installedVersion.versionCode) {
                return UpdateStatus.UpToDate
            }

            val changelog = json.optString("body",
                json.optString("changelog",
                    json.optString("description", "Actualización disponible en servidor.")
                )
            )
            val publishedAt = json.optString("published_at", json.optString("releaseDate", "Disponible"))

            val mirrors = mutableListOf<String>()
            var primaryDownloadUrl = ""

            // 1. Search GitHub release assets
            if (json.has("assets")) {
                val assets = json.getJSONArray("assets")
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val assetName = asset.optString("name", "")
                    val assetUrl = asset.optString("browser_download_url", "")
                    if (assetName.endsWith(".apk", ignoreCase = true) && assetUrl.isNotBlank()) {
                        mirrors.add(assetUrl)
                        if (primaryDownloadUrl.isEmpty()) {
                            primaryDownloadUrl = assetUrl
                        }
                    }
                }
            }

            // 2. Search explicit downloadUrl
            val explicitUrl = json.optString("downloadUrl", json.optString("apk_url", ""))
            if (explicitUrl.isNotBlank()) {
                mirrors.add(0, explicitUrl)
                if (primaryDownloadUrl.isEmpty()) primaryDownloadUrl = explicitUrl
            }

            // 3. User custom direct APK URL
            val customUserApk = getCustomDirectApkUrl(context)
            if (customUserApk.isNotBlank()) {
                mirrors.add(0, customUserApk)
                if (primaryDownloadUrl.isEmpty()) primaryDownloadUrl = customUserApk
            }

            if (primaryDownloadUrl.isEmpty()) {
                return UpdateStatus.Error(
                    message = "La Release '$tagName' existe pero NO tiene ningún archivo .APK subido a GitHub aún. Debes subir el APK a la Release o usar el Método 2 (APK Local) / Método 5 (Google Sheets OTA).",
                    fallbackUrl = PRESET_GITHUB_RELEASES_WEB,
                    suggestedDirectUrl = null
                )
            }

            return UpdateStatus.UpdateAvailable(
                versionName = formattedVersion,
                versionCode = remoteVersionCode,
                changelog = changelog,
                downloadUrl = primaryDownloadUrl,
                publishedAt = publishedAt,
                isDirectApkAvailable = true,
                mirrorUrls = mirrors.distinct()
            )
        } catch (e: Exception) {
            return UpdateStatus.Error(
                message = "Error de conexión: ${e.localizedMessage ?: "No se pudo alcanzar el servidor"}",
                fallbackUrl = PRESET_GITHUB_RELEASES_WEB,
                suggestedDirectUrl = null
            )
        }
    }

    private fun versionNameToCode(version: String): Int {
        val match = Regex("v?(\\d+)\\.(\\d+)(?:\\.(\\d+))?").find(version) ?: return 0
        val major = match.groupValues[1].toIntOrNull() ?: return 0
        val minor = match.groupValues[2].toIntOrNull() ?: 0
        val patch = match.groupValues.getOrNull(3)?.toIntOrNull() ?: 0
        return major * 100 + minor * 10 + patch
    }

    // =========================================================================
    // DESCARGADOR MULTI-FUENTE CON SOPORTE DE REDIRECCIONES Y PROGRESO
    // =========================================================================
    suspend fun downloadAndPrepareInstall(
        context: Context,
        targetUrl: String,
        versionName: String = "latest"
    ): Result<File> = withContext(Dispatchers.IO) {
        val cleanVersion = versionName.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
        val outputFile = File(context.cacheDir, "WebNative_update_${cleanVersion}.apk")
        if (outputFile.exists()) outputFile.delete()

        val candidateUrls = mutableListOf<String>()
        if (targetUrl.isNotBlank()) candidateUrls.add(targetUrl.trim())
        val customApk = getCustomDirectApkUrl(context)
        if (customApk.isNotBlank() && !candidateUrls.contains(customApk)) candidateUrls.add(customApk)

        if (candidateUrls.isEmpty()) {
            val msg = "No se proporcionó ninguna URL de descarga válida."
            _updateState.value = UpdateStatus.Error(msg, PRESET_GITHUB_RELEASES_WEB, null)
            return@withContext Result.failure(Exception(msg))
        }

        var lastError: Exception? = null

        for (urlStr in candidateUrls) {
            try {
                // First: quick real verification of URL existence
                val probe = verifyRemoteUrlReal(urlStr)
                if (!probe.exists) {
                    lastError = Exception(probe.errorMessage ?: "El archivo no existe en el servidor remoto (HTTP ${probe.httpCode})")
                    continue
                }

                _updateState.value = UpdateStatus.Downloading(0, 0, probe.contentLength, 0, urlStr)
                val success = downloadStreamToFile(urlStr, outputFile) { percent, read, total, speed ->
                    _updateState.value = UpdateStatus.Downloading(percent, read, total, speed, urlStr)
                }

                if (success && outputFile.exists() && outputFile.length() > 50 * 1024) {
                    _updateState.value = UpdateStatus.ReadyToInstall(outputFile, versionName)
                    return@withContext Result.success(outputFile)
                }
            } catch (e: Exception) {
                lastError = e
            }
        }

        val specificReason = lastError?.localizedMessage ?: "El archivo APK no existe en el servidor o retornó error 404"
        val errMessage = "No se pudo descargar el archivo APK: $specificReason.\n\nSube el APK a la Release de GitHub, compártelo por Google Drive/Dropbox, o instálalo directamente con el 'Método 2: Selector APK Local'."
        val err = UpdateStatus.Error(
            message = errMessage,
            fallbackUrl = PRESET_GITHUB_RELEASES_WEB,
            suggestedDirectUrl = null
        )
        _updateState.value = err
        return@withContext Result.failure(Exception(errMessage))
    }

    private fun downloadStreamToFile(
        urlString: String,
        outputFile: File,
        onProgress: (percent: Int, readBytes: Long, totalBytes: Long, speedKbps: Long) -> Unit
    ): Boolean {
        var currentUrl = urlString
        var redirects = 0
        val maxRedirects = 5

        while (redirects < maxRedirects) {
            val url = URL(currentUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 15000
            conn.readTimeout = 30000
            conn.instanceFollowRedirects = false
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:109.0) Gecko/109.0 Firefox/109.0")
            conn.setRequestProperty("Accept", "*/*")

            val status = conn.responseCode
            if (status == HttpURLConnection.HTTP_MOVED_TEMP ||
                status == HttpURLConnection.HTTP_MOVED_PERM ||
                status == HttpURLConnection.HTTP_SEE_OTHER ||
                status == 307 || status == 308) {
                val newUrl = conn.getHeaderField("Location")
                if (!newUrl.isNullOrBlank()) {
                    currentUrl = newUrl
                    redirects++
                    continue
                }
            }

            if (status !in 200..299) {
                return false
            }

            val totalBytes = conn.contentLength.toLong()
            val startTime = System.currentTimeMillis()

            conn.inputStream.use { input ->
                FileOutputStream(outputFile).use { output ->
                    val buffer = ByteArray(16384)
                    var bytesRead: Int
                    var totalRead = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead

                        val elapsedSecs = (System.currentTimeMillis() - startTime) / 1000.0
                        val speedKbps = if (elapsedSecs > 0) ((totalRead / 1024) / elapsedSecs).toLong() else 0L

                        val percent = if (totalBytes > 0) {
                            ((totalRead * 100) / totalBytes).toInt().coerceIn(0, 100)
                        } else {
                            50
                        }
                        onProgress(percent, totalRead, totalBytes, speedKbps)
                    }
                    output.flush()
                }
            }

            return outputFile.length() > 50 * 1024
        }
        return false
    }

    // =========================================================================
    // MÉTODO 2: INSTALACIÓN DESDE ARCHIVO LOCAL (Content URI / Storage Picker)
    // =========================================================================
    suspend fun importAndInstallFromLocalUri(context: Context, localUri: Uri): Result<File> = withContext(Dispatchers.IO) {
        try {
            _updateState.value = UpdateStatus.Downloading(10, 0, 0, 0, "Copiando archivo local...")
            val inputStream = context.contentResolver.openInputStream(localUri)
                ?: return@withContext Result.failure(Exception("No se pudo leer el archivo seleccionado"))

            val targetFile = File(context.cacheDir, "WebNative_local_install.apk")
            if (targetFile.exists()) targetFile.delete()

            inputStream.use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(16384)
                    var bytesRead: Int
                    var totalCopied = 0L
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalCopied += bytesRead
                    }
                    output.flush()
                }
            }

            if (targetFile.length() < 10 * 1024) {
                return@withContext Result.failure(Exception("El archivo seleccionado está vacío o no es un APK válido."))
            }

            _updateState.value = UpdateStatus.ReadyToInstall(targetFile, "Local")
            Result.success(targetFile)
        } catch (e: Exception) {
            val err = "Error al procesar archivo local: ${e.localizedMessage}"
            _updateState.value = UpdateStatus.Error(err)
            Result.failure(Exception(err))
        }
    }

    // =========================================================================
    // LANZADOR NATIVO DE INSTALACIÓN (FileProvider / Android Package Manager)
    // =========================================================================
    fun promptInstallApk(context: Context, apkFile: File): Boolean {
        return try {
            if (!apkFile.exists() || apkFile.length() < 1000) return false
            val cacheRoot = context.cacheDir.canonicalFile
            val candidate = apkFile.canonicalFile
            if (!candidate.path.startsWith(cacheRoot.path + File.separator)) return false

            // Check Unknown Sources Permission for Android 8+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(settingsIntent)
                    return false
                }
            }

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(installIntent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
