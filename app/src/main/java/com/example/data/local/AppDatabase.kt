package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.WebAppEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [WebAppEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun webAppDao(): WebAppDao

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

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
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
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialApps(database.webAppDao())
                    }
                }
            }

            suspend fun populateInitialApps(dao: WebAppDao) {
                for (app in DEFAULT_PRELOADED_APPS) {
                    dao.insertWebApp(app)
                }
            }
        }
    }
}
