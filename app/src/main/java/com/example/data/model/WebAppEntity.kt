package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "web_apps")
data class WebAppEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val url: String,
    val iconType: String = "EMOJI", // "EMOJI", "URL", "VECTOR", "COLOR"
    val iconValue: String = "⚡",
    val accentColor: Long = 0xFF00F5D4,
    val category: String = "Productividad", // Productividad, Entretenimiento, IA & Herramientas, Redes, Juegos & Cloud, Dev
    val isFullscreen: Boolean = true,
    val isHardwareBoostEnabled: Boolean = true,
    val isBatterySaverBypassEnabled: Boolean = true,
    val isAdBlockEnabled: Boolean = true,
    val isDesktopMode: Boolean = false,
    val userAgent: String = "",
    val customCss: String = "",
    val customJs: String = "",
    val isPinnedShortcut: Boolean = false,
    val openCount: Int = 0,
    val lastOpened: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val isOledBlackMode: Boolean = false,
    val autoClearCacheOnExit: Boolean = false
)
