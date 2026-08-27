package com.example.util

import android.content.Context
import com.example.data.model.ActivityCategory
import com.example.data.model.ActivityLogEntry
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DevelopmentActivityLogManager {
    private const val PREFS_NAME = "webnative_dev_activity_prefs"
    private const val KEY_ACTIVITIES_JSON = "historical_activities_json"

    private fun getInitialBaselineEntries(): List<ActivityLogEntry> {
        return listOf(
            ActivityLogEntry(
                id = "act-001",
                timestamp = 1724700000000L,
                dateString = "2026-08-25 14:30",
                title = "Estabilización de Compilador & Compose BOM",
                description = "Se resolvió el bug de empaquetado invisible fijando compatibilidad JVM 11 y plugins Compose.",
                category = ActivityCategory.CORE_ENGINE,
                agentTag = "CoreArchitect",
                affectedFiles = listOf("app/build.gradle.kts", "libs.versions.toml"),
                rollbackInstruction = "Verificar que jvmTarget = 11 y compose compiler version estén alineados con Kotlin 2.0.21."
            ),
            ActivityLogEntry(
                id = "act-002",
                timestamp = 1724720000000L,
                dateString = "2026-08-26 10:15",
                title = "Implementación del Motor de Actualización OTA",
                description = "Soporte para descarga autónoma e instalación nativa via FileProvider de paquetes APK generados por GitHub.",
                category = ActivityCategory.CICD,
                agentTag = "OTASpecialist",
                affectedFiles = listOf("OtaUpdateManager.kt", "AndroidManifest.xml", "file_paths.xml"),
                rollbackInstruction = "Comprobar que FileProvider authority coincida con applicationId.fileprovider."
            ),
            ActivityLogEntry(
                id = "act-003",
                timestamp = 1724740000000L,
                dateString = "2026-08-26 16:30",
                title = "Diagnóstico ADB & SystemProperties Reflexión",
                description = "Lectura de sys.usb.config y persist.sys.adb.config en tiempo real para verificar el estado de depuración USB.",
                category = ActivityCategory.ADB_HARDWARE,
                agentTag = "DiagnosticsAgent",
                affectedFiles = listOf("DeveloperDiagnosticsCard.kt", "ADBManager.kt"),
                rollbackInstruction = "Usar fallback a 'getprop' en caso de que SystemProperties lance SecurityException."
            ),
            ActivityLogEntry(
                id = "act-004",
                timestamp = 1724760000000L,
                dateString = "2026-08-26 22:45",
                title = "Protocolo AGENTS.md & Sistema de Observabilidad",
                description = "Creación de AGENTS.md para memoria de agentes y AppLogger en disco para trazabilidad en tiempo real.",
                category = ActivityCategory.CICD,
                agentTag = "ProtocolAgent",
                affectedFiles = listOf("AGENTS.md", "AppLogger.kt", "SystemLogsCard.kt"),
                rollbackInstruction = "Conservar AGENTS.md en la raíz del proyecto para inyección automática en el contexto."
            )
        )
    }

    fun getActivities(context: Context): List<ActivityLogEntry> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_ACTIVITIES_JSON, null)

        if (jsonStr.isNullOrBlank()) {
            val baseline = getInitialBaselineEntries()
            saveActivities(context, baseline)
            return baseline
        }

        return try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<ActivityLogEntry>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val filesArray = obj.optJSONArray("affectedFiles")
                val filesList = mutableListOf<String>()
                if (filesArray != null) {
                    for (j in 0 until filesArray.length()) {
                        filesList.add(filesArray.getString(j))
                    }
                }

                val categoryStr = obj.optString("category", "ALL")
                val cat = try {
                    ActivityCategory.valueOf(categoryStr)
                } catch (e: Exception) {
                    ActivityCategory.ALL
                }

                list.add(
                    ActivityLogEntry(
                        id = obj.optString("id", "act-$i"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        dateString = obj.optString("dateString", ""),
                        title = obj.optString("title", ""),
                        description = obj.optString("description", ""),
                        category = cat,
                        agentTag = obj.optString("agentTag", "Agent"),
                        affectedFiles = filesList,
                        rollbackInstruction = obj.optString("rollbackInstruction", "")
                    )
                )
            }
            list.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            getInitialBaselineEntries()
        }
    }

    fun logActivity(
        context: Context,
        title: String,
        description: String,
        category: ActivityCategory,
        agentTag: String = "AgentDev",
        affectedFiles: List<String> = emptyList(),
        rollbackInstruction: String = "Revisar cambios en Git y recompilar con compile_applet"
    ) {
        val currentList = getActivities(context).toMutableList()
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val newEntry = ActivityLogEntry(
            id = "act-${System.currentTimeMillis()}",
            timestamp = System.currentTimeMillis(),
            dateString = dateStr,
            title = title,
            description = description,
            category = category,
            agentTag = agentTag,
            affectedFiles = affectedFiles,
            rollbackInstruction = rollbackInstruction
        )

        currentList.add(0, newEntry)
        saveActivities(context, currentList)
        AppLogger.log(context, "Nueva actividad registrada: $title [$category]", tag = "ACTIVITY_LOG")
    }

    private fun saveActivities(context: Context, list: List<ActivityLogEntry>) {
        try {
            val jsonArray = JSONArray()
            list.take(30).forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("timestamp", item.timestamp)
                    put("dateString", item.dateString)
                    put("title", item.title)
                    put("description", item.description)
                    put("category", item.category.name)
                    put("agentTag", item.agentTag)
                    put("affectedFiles", JSONArray(item.affectedFiles))
                    put("rollbackInstruction", item.rollbackInstruction)
                }
                jsonArray.put(obj)
            }
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_ACTIVITIES_JSON, jsonArray.toString())
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun generateAuditReport(context: Context): String {
        val list = getActivities(context)
        val sb = StringBuilder()
        sb.append("=== INFORME DE AUDITORÍA Y REVERSIÓN WEB NATIVE PRO ===\n")
        sb.append("Fecha de generación: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n")
        sb.append("Total de Hitos/Cambios Registrados: ${list.size}\n\n")

        list.forEachIndexed { index, entry ->
            sb.append("${index + 1}. [${entry.dateString}] ${entry.title} (${entry.category.name})\n")
            sb.append("   - Agente: @${entry.agentTag}\n")
            sb.append("   - Descripción: ${entry.description}\n")
            if (entry.affectedFiles.isNotEmpty()) {
                sb.append("   - Archivos: ${entry.affectedFiles.joinToString(", ")}\n")
            }
            sb.append("   - Guía de Reversión: ${entry.rollbackInstruction}\n\n")
        }
        return sb.toString()
    }
}
