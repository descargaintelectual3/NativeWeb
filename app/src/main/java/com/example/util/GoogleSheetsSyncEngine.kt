package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.WebAppEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object GoogleSheetsSyncEngine {

    val CSV_HEADER_15_COLUMNS = listOf(
        "id",
        "name",
        "url",
        "category",
        "icon_type",
        "icon_value",
        "accent_color",
        "fullscreen",
        "hardware_boost",
        "ad_block",
        "battery_bypass",
        "desktop_mode",
        "oled_black_mode",
        "cpu_priority",
        "custom_css"
    )

    fun generateDefaultTemplateCsv(): String {
        val sb = StringBuilder()
        sb.append(CSV_HEADER_15_COLUMNS.joinToString(",")).append("\n")

        val sampleRows = listOf(
            listOf("1", "Bene Cloud", "https://bene.civer.cloud/", "Cloud & Empresa", "EMOJI", "🌟", "#D0BCFF", "TRUE", "TRUE", "TRUE", "TRUE", "FALSE", "FALSE", "TURBO", "/* Bene Cloud Enterprise UI Tweaks */ header { backdrop-filter: blur(12px); }"),
            listOf("2", "Manager Cloud", "https://manager.civer.cloud/", "Cloud & Empresa", "EMOJI", "📊", "#A6EECA", "TRUE", "TRUE", "TRUE", "TRUE", "FALSE", "FALSE", "TURBO", "/* Manager Cloud Dashboard Styling */ .sidebar { font-family: sans-serif; }"),
            listOf("3", "ControlDroid Cloud", "https://controldroid.civer.cloud/", "Cloud & Empresa", "EMOJI", "🤖", "#FFD999", "TRUE", "TRUE", "TRUE", "TRUE", "FALSE", "TRUE", "TURBO", "/* Low-latency WebRTC Canvas */ canvas { image-rendering: pixelated; }"),
            listOf("4", "Civer Cloud Portal", "https://civer.pro/", "Cloud & Empresa", "EMOJI", "⚡", "#80D8FF", "TRUE", "TRUE", "TRUE", "TRUE", "FALSE", "FALSE", "TURBO", "/* Civer Pro SSO Hub */"),
            listOf("5", "Civer IDE Web", "https://civer.cloud/", "Desarrollo", "EMOJI", "💻", "#7D2AE8", "TRUE", "TRUE", "TRUE", "TRUE", "TRUE", "TRUE", "TURBO", "/* Monaco Dark Editor Theme */"),
            listOf("6", "GitHub Repositories", "https://github.com/PabloArboledai", "Desarrollo", "EMOJI", "🐙", "#EADDFF", "TRUE", "TRUE", "FALSE", "TRUE", "FALSE", "FALSE", "NORMAL", ""),
            listOf("7", "Telegram Web Bot Console", "https://web.telegram.org/a/", "Comunicación", "EMOJI", "✈️", "#2AABEE", "TRUE", "TRUE", "TRUE", "TRUE", "FALSE", "FALSE", "TURBO", ""),
            listOf("8", "YouTube Ultra Media", "https://m.youtube.com", "Entretenimiento", "EMOJI", "▶️", "#FF0000", "TRUE", "TRUE", "TRUE", "TRUE", "FALSE", "FALSE", "TURBO", "")
        )

        for (row in sampleRows) {
            sb.append(row.joinToString(",") { escapeCsv(it) }).append("\n")
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    suspend fun fetchCsvFromGoogleSheetUrl(sheetUrl: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            var finalUrl = sheetUrl.trim()
            if (finalUrl.contains("docs.google.com/spreadsheets/d/")) {
                if (!finalUrl.contains("export?format=csv") && !finalUrl.contains("pub?output=csv")) {
                    val sheetId = finalUrl.substringAfter("/d/").substringBefore("/")
                    val gid = if (finalUrl.contains("gid=")) finalUrl.substringAfter("gid=").substringBefore("&").substringBefore("#") else "0"
                    finalUrl = "https://docs.google.com/spreadsheets/d/$sheetId/export?format=csv&gid=$gid"
                }
            }

            val url = URL(finalUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 12000
            conn.readTimeout = 12000
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; WebNative-Sync)")

            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val content = reader.readText()
                reader.close()
                Result.success(content)
            } else {
                Result.failure(Exception("Error HTTP $responseCode al descargar Google Sheet"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun parseCsvToWebApps(csvContent: String): List<WebAppEntity> {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()

        val header = parseCsvLine(lines[0]).map { it.trim().lowercase() }
        val idIdx = header.indexOfFirst { it == "id" || it == "slug" }
        val nameIdx = header.indexOfFirst { it == "name" || it == "nombre" || it == "app_name" }
        val urlIdx = header.indexOfFirst { it == "url" || it == "sitio" || it == "link" }
        val catIdx = header.indexOfFirst { it == "category" || it == "categoria" }
        val iconTypeIdx = header.indexOfFirst { it == "icon_type" || it == "tipo_icono" }
        val iconValIdx = header.indexOfFirst { it == "icon_value" || it == "icono" || it == "logo" }
        val accentIdx = header.indexOfFirst { it == "accent_color" || it == "color" }
        val fsIdx = header.indexOfFirst { it == "fullscreen" || it == "pantalla_completa" }
        val hwIdx = header.indexOfFirst { it == "hardware_boost" || it == "gpu_boost" }
        val adIdx = header.indexOfFirst { it == "ad_block" || it == "bloqueador_anuncios" }
        val battIdx = header.indexOfFirst { it == "battery_bypass" || it == "bateria_opt" }
        val deskIdx = header.indexOfFirst { it == "desktop_mode" || it == "escritorio" }
        val oledIdx = header.indexOfFirst { it == "oled_black_mode" || it == "oled" }
        val cssIdx = header.indexOfFirst { it == "custom_css" || it == "css" }

        val result = mutableListOf<WebAppEntity>()

        for (i in 1 until lines.size) {
            val cols = parseCsvLine(lines[i])
            if (cols.isEmpty()) continue

            val rawUrl = if (urlIdx >= 0 && urlIdx < cols.size) cols[urlIdx].trim() else ""
            if (rawUrl.isBlank()) continue

            val cleanUrl = FaviconHelper.cleanUrl(rawUrl)
            val name = if (nameIdx >= 0 && nameIdx < cols.size && cols[nameIdx].isNotBlank()) {
                cols[nameIdx].trim()
            } else {
                FaviconHelper.extractDomainName(cleanUrl).replaceFirstChar { it.uppercase() }
            }

            val category = if (catIdx >= 0 && catIdx < cols.size && cols[catIdx].isNotBlank()) cols[catIdx].trim() else "Cloud & Empresa"
            val iconType = if (iconTypeIdx >= 0 && iconTypeIdx < cols.size && cols[iconTypeIdx].isNotBlank()) cols[iconTypeIdx].trim().uppercase() else "EMOJI"
            val iconVal = if (iconValIdx >= 0 && iconValIdx < cols.size && cols[iconValIdx].isNotBlank()) cols[iconValIdx].trim() else "⚡"
            val accentStr = if (accentIdx >= 0 && accentIdx < cols.size) cols[accentIdx].trim() else ""
            val accentColor = parseColor(accentStr)

            val isFs = if (fsIdx >= 0 && fsIdx < cols.size) cols[fsIdx].trim().equals("true", ignoreCase = true) else true
            val isHw = if (hwIdx >= 0 && hwIdx < cols.size) cols[hwIdx].trim().equals("true", ignoreCase = true) else true
            val isAd = if (adIdx >= 0 && adIdx < cols.size) cols[adIdx].trim().equals("true", ignoreCase = true) else true
            val isBatt = if (battIdx >= 0 && battIdx < cols.size) cols[battIdx].trim().equals("true", ignoreCase = true) else true
            val isDesk = if (deskIdx >= 0 && deskIdx < cols.size) cols[deskIdx].trim().equals("true", ignoreCase = true) else false
            val isOled = if (oledIdx >= 0 && oledIdx < cols.size) cols[oledIdx].trim().equals("true", ignoreCase = true) else false
            val customCss = if (cssIdx >= 0 && cssIdx < cols.size) cols[cssIdx].trim() else ""

            result.add(
                WebAppEntity(
                    name = name,
                    url = cleanUrl,
                    iconType = iconType,
                    iconValue = iconVal,
                    accentColor = accentColor,
                    category = category,
                    isFullscreen = isFs,
                    isHardwareBoostEnabled = isHw,
                    isAdBlockEnabled = isAd,
                    isBatterySaverBypassEnabled = isBatt,
                    isDesktopMode = isDesk,
                    customCss = customCss,
                    isOledBlackMode = isOled
                )
            )
        }
        return result
    }

    private fun parseColor(colorStr: String): Long {
        if (colorStr.isBlank()) return 0xFFD0BCFF
        return try {
            if (colorStr.startsWith("#")) {
                val hex = colorStr.removePrefix("#")
                if (hex.length == 6) {
                    ("FF$hex".toLong(16))
                } else if (hex.length == 8) {
                    hex.toLong(16)
                } else {
                    0xFFD0BCFF
                }
            } else if (colorStr.startsWith("0x", ignoreCase = true)) {
                colorStr.substring(2).toLong(16)
            } else {
                colorStr.toLongOrNull() ?: 0xFFD0BCFF
            }
        } catch (_: Exception) {
            0xFFD0BCFF
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = java.lang.StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    sb.append('\"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim())
                sb.setLength(0)
            } else {
                sb.append(c)
            }
            i++
        }
        tokens.add(sb.toString().trim())
        return tokens
    }

    private const val PREFS_NAME = "webnative_sheets_prefs"
    private const val KEY_SAVED_URL = "saved_sheets_url"

    fun getSavedSheetUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SAVED_URL, "") ?: ""
    }

    fun saveSheetUrl(context: Context, url: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SAVED_URL, url).apply()
    }

    suspend fun syncFromUrl(
        context: Context,
        url: String,
        scope: kotlinx.coroutines.CoroutineScope
    ): Result<Int> = withContext(Dispatchers.IO) {
        val fetchResult = fetchCsvFromGoogleSheetUrl(url)
        fetchResult.fold(
            onSuccess = { csv ->
                val apps = parseCsvToWebApps(csv)
                if (apps.isEmpty()) {
                    Result.failure(Exception("No se encontraron filas válidas en la hoja de cálculo"))
                } else {
                    saveSheetUrl(context, url)
                    val db = com.example.data.local.AppDatabase.getDatabase(context, scope)
                    db.webAppDao().deleteAllWebApps()
                    db.webAppDao().insertAllWebApps(apps)
                    Result.success(apps.size)
                }
            },
            onFailure = { err ->
                Result.failure(err)
            }
        )
    }

    fun exportAndShareTemplate(context: Context) {
        try {
            val csvContent = generateDefaultTemplateCsv()
            val fileName = "WebNative_GoogleSheets_Template.csv"
            val file = File(context.cacheDir, fileName)
            file.writeText(csvContent)

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Plantilla Google Sheets - WebNative Apps")
                putExtra(Intent.EXTRA_TEXT, "Plantilla oficial de Google Sheets para auto-importar sitios y Web Apps en WebNative con 15 columnas de metadata.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Compartir o Abrir Plantilla Google Sheets"))
        } catch (e: Exception) {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Plantilla Google Sheets - WebNative Apps")
                putExtra(Intent.EXTRA_TEXT, generateDefaultTemplateCsv())
            }
            context.startActivity(Intent.createChooser(sendIntent, "Compartir Plantilla"))
        }
    }
}
