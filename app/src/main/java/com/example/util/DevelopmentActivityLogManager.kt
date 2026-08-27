package com.example.util

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.model.ActivityCategory
import com.example.data.model.ActivityLogEntry
import com.example.data.model.DevelopmentActivityEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DevelopmentActivityLogManager {

    private fun getDao(context: Context) = AppDatabase.getDatabase(context).developmentActivityDao()

    /**
     * Observes real-time activity log stream directly from Room SQLite database.
     */
    fun observeActivities(context: Context): Flow<List<ActivityLogEntry>> {
        return getDao(context).getAllActivities().map { entities ->
            entities.map { entity -> entityToEntry(entity) }
        }
    }

    /**
     * Gets all activities as a List (suspend).
     */
    suspend fun getActivitiesSuspend(context: Context): List<ActivityLogEntry> = withContext(Dispatchers.IO) {
        try {
            val dao = getDao(context)
            val list = dao.getAllActivitiesList()
            if (list.isEmpty()) {
                // Ensure initial baseline is loaded
                val baseline = getInitialBaselineEntities()
                dao.insertAll(baseline)
                return@withContext baseline.map { entityToEntry(it) }
            }
            list.map { entityToEntry(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Synchronous / helper fallback for legacy callers.
     */
    fun getActivities(context: Context): List<ActivityLogEntry> {
        return try {
            val list = mutableListOf<ActivityLogEntry>()
            // Using standard background query or baseline
            val dao = getDao(context)
            kotlinx.coroutines.runBlocking(Dispatchers.IO) {
                val dbList = dao.getAllActivitiesList()
                if (dbList.isNotEmpty()) {
                    list.addAll(dbList.map { entityToEntry(it) })
                } else {
                    val baseline = getInitialBaselineEntities()
                    dao.insertAll(baseline)
                    list.addAll(baseline.map { entityToEntry(it) })
                }
            }
            list
        } catch (e: Exception) {
            getInitialBaselineEntities().map { entityToEntry(it) }
        }
    }

    /**
     * Inserts an activity log into Room Database.
     */
    fun logActivity(
        context: Context,
        title: String,
        description: String,
        category: ActivityCategory,
        agentTag: String = "AgentDev",
        affectedFiles: List<String> = emptyList(),
        rollbackInstruction: String = "Revisar cambios en Git y recompilar con compile_applet",
        executionDetails: String = ""
    ) {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val entity = DevelopmentActivityEntity(
            activityId = "act-${System.currentTimeMillis()}",
            timestamp = System.currentTimeMillis(),
            dateString = dateStr,
            title = title,
            description = description,
            category = category.name,
            agentTag = agentTag,
            affectedFiles = affectedFiles.joinToString(", "),
            rollbackInstruction = rollbackInstruction,
            executionDetails = executionDetails
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                getDao(context).insert(entity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        AppLogger.log(context, "Nueva actividad registrada en Room DB: $title [$category]", tag = "ACTIVITY_LOG")
    }

    suspend fun clearActivities(context: Context) = withContext(Dispatchers.IO) {
        getDao(context).clearAll()
    }

    private fun entityToEntry(entity: DevelopmentActivityEntity): ActivityLogEntry {
        val cat = try {
            ActivityCategory.valueOf(entity.category)
        } catch (e: Exception) {
            ActivityCategory.ALL
        }

        val files = if (entity.affectedFiles.isBlank()) {
            emptyList()
        } else {
            entity.affectedFiles.split(",").map { it.trim() }.filter { it.isNotBlank() }
        }

        return ActivityLogEntry(
            id = entity.activityId.ifBlank { "act-${entity.id}" },
            timestamp = entity.timestamp,
            dateString = entity.dateString,
            title = entity.title,
            description = entity.description,
            category = cat,
            agentTag = entity.agentTag,
            affectedFiles = files,
            rollbackInstruction = entity.rollbackInstruction
        )
    }

    private fun getInitialBaselineEntities(): List<DevelopmentActivityEntity> {
        return listOf(
            DevelopmentActivityEntity(
                activityId = "act-001",
                timestamp = 1724700000000L,
                dateString = "2026-08-25 14:30",
                title = "Estabilización de Compilador & Compose BOM",
                description = "Se resolvió el bug de empaquetado invisible fijando compatibilidad JVM 11 y plugins Compose.",
                category = "CORE_ENGINE",
                agentTag = "CoreArchitect",
                affectedFiles = "app/build.gradle.kts, libs.versions.toml",
                rollbackInstruction = "Verificar que jvmTarget = 11 y compose compiler version estén alineados con Kotlin 2.0.21."
            ),
            DevelopmentActivityEntity(
                activityId = "act-002",
                timestamp = 1724720000000L,
                dateString = "2026-08-26 10:15",
                title = "Implementación del Motor de Actualización OTA",
                description = "Soporte para descarga autónoma e instalación nativa via FileProvider de paquetes APK generados por GitHub.",
                category = "CICD",
                agentTag = "OTASpecialist",
                affectedFiles = "OtaUpdateManager.kt, AndroidManifest.xml, file_paths.xml",
                rollbackInstruction = "Comprobar que FileProvider authority coincida con applicationId.fileprovider."
            ),
            DevelopmentActivityEntity(
                activityId = "act-003",
                timestamp = 1724740000000L,
                dateString = "2026-08-26 16:30",
                title = "Diagnóstico ADB & SystemProperties Reflexión",
                description = "Lectura de sys.usb.config y persist.sys.adb.config en tiempo real para verificar el estado de depuración USB.",
                category = "ADB_HARDWARE",
                agentTag = "DiagnosticsAgent",
                affectedFiles = "DeveloperDiagnosticsCard.kt, ADBManager.kt",
                rollbackInstruction = "Usar fallback a 'getprop' en caso de que SystemProperties lance SecurityException."
            ),
            DevelopmentActivityEntity(
                activityId = "act-004",
                timestamp = 1724760000000L,
                dateString = "2026-08-26 22:45",
                title = "Protocolo AGENTS.md & Sistema de Observabilidad",
                description = "Creación de AGENTS.md para memoria de agentes y AppLogger en disco para trazabilidad en tiempo real.",
                category = "CICD",
                agentTag = "ProtocolAgent",
                affectedFiles = "AGENTS.md, AppLogger.kt, SystemLogsCard.kt",
                rollbackInstruction = "Conservar AGENTS.md en la raíz del proyecto para inyección automática en el contexto."
            )
        )
    }

    fun generateAuditReport(context: Context): String {
        val list = getActivities(context)
        val sb = StringBuilder()
        sb.append("=== INFORME DE AUDITORÍA Y REVERSIÓN WEB NATIVE PRO (ROOM DB) ===\n")
        sb.append("Fecha de generación: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n")
        sb.append("Total de Hitos/Cambios Registrados en SQLite: ${list.size}\n\n")

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
