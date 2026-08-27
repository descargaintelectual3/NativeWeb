package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DevelopmentActivityEntity
import com.example.data.model.WebAppEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [WebAppEntity::class, DevelopmentActivityEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun webAppDao(): WebAppDao
    abstract fun developmentActivityDao(): DevelopmentActivityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val DEFAULT_PRELOADED_APPS = listOf(
            WebAppEntity(
                name = "Bene Cloud",
                url = "https://bene.civer.cloud/",
                iconType = "EMOJI",
                iconValue = "🌟",
                accentColor = 0xFFD0BCFF,
                category = "Cloud & Empresa",
                isFullscreen = true,
                isHardwareBoostEnabled = true,
                isAdBlockEnabled = true,
                isBatterySaverBypassEnabled = true,
                customCss = "/* Bene Cloud Enterprise UI Tweaks */ header { backdrop-filter: blur(12px); }"
            ),
            WebAppEntity(
                name = "Manager Cloud",
                url = "https://manager.civer.cloud/",
                iconType = "EMOJI",
                iconValue = "📊",
                accentColor = 0xFFA6EECA,
                category = "Cloud & Empresa",
                isFullscreen = true,
                isHardwareBoostEnabled = true,
                isAdBlockEnabled = true,
                isBatterySaverBypassEnabled = true,
                customCss = "/* Manager Cloud Dashboard Styling */ .sidebar { font-family: sans-serif; }"
            ),
            WebAppEntity(
                name = "ControlDroid Cloud",
                url = "https://controldroid.civer.cloud/",
                iconType = "EMOJI",
                iconValue = "🤖",
                accentColor = 0xFFFFD999,
                category = "Cloud & Empresa",
                isFullscreen = true,
                isHardwareBoostEnabled = true,
                isAdBlockEnabled = true,
                isBatterySaverBypassEnabled = true,
                isDesktopMode = true,
                customCss = "/* Low-latency WebRTC Canvas */ canvas { image-rendering: pixelated; }"
            ),
            WebAppEntity(
                name = "Civer Cloud Portal",
                url = "https://civer.pro/",
                iconType = "EMOJI",
                iconValue = "⚡",
                accentColor = 0xFF80D8FF,
                category = "Cloud & Empresa",
                isFullscreen = true,
                isHardwareBoostEnabled = true,
                isAdBlockEnabled = true,
                isBatterySaverBypassEnabled = true
            ),
            WebAppEntity(
                name = "Civer IDE Web",
                url = "https://civer.cloud/",
                iconType = "EMOJI",
                iconValue = "💻",
                accentColor = 0xFF7D2AE8,
                category = "Desarrollo",
                isFullscreen = true,
                isHardwareBoostEnabled = true,
                isAdBlockEnabled = true,
                isBatterySaverBypassEnabled = true,
                isDesktopMode = true
            ),
            WebAppEntity(
                name = "GitHub Repositories",
                url = "https://github.com/PabloArboledai",
                iconType = "EMOJI",
                iconValue = "🐙",
                accentColor = 0xFFEADDFF,
                category = "Desarrollo",
                isFullscreen = true,
                isHardwareBoostEnabled = true,
                isAdBlockEnabled = false,
                isBatterySaverBypassEnabled = true
            ),
            WebAppEntity(
                name = "Telegram Web Bot Console",
                url = "https://web.telegram.org/a/",
                iconType = "EMOJI",
                iconValue = "✈️",
                accentColor = 0xFF2AABEE,
                category = "Comunicación",
                isFullscreen = true,
                isHardwareBoostEnabled = true,
                isAdBlockEnabled = true,
                isBatterySaverBypassEnabled = true
            ),
            WebAppEntity(
                name = "YouTube Ultra Media",
                url = "https://m.youtube.com",
                iconType = "EMOJI",
                iconValue = "▶️",
                accentColor = 0xFFFF0000,
                category = "Entretenimiento",
                isFullscreen = true,
                isHardwareBoostEnabled = true,
                isAdBlockEnabled = true,
                isBatterySaverBypassEnabled = true
            )
        )

        fun getDatabase(context: Context, scope: CoroutineScope? = null): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "webnative_database"
                )
                .addCallback(DatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope?
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val targetScope = scope ?: CoroutineScope(Dispatchers.IO)
                INSTANCE?.let { database ->
                    targetScope.launch(Dispatchers.IO) {
                        populateInitialApps(database.webAppDao())
                        populateInitialActivities(database.developmentActivityDao())
                    }
                }
            }

            suspend fun populateInitialApps(dao: WebAppDao) {
                for (app in DEFAULT_PRELOADED_APPS) {
                    dao.insertWebApp(app)
                }
            }

            suspend fun populateInitialActivities(dao: DevelopmentActivityDao) {
                val baseline = listOf(
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
                dao.insertAll(baseline)
            }
        }
    }
}
