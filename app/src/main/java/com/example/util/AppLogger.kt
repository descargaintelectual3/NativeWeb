package com.example.util

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppLogger {
    private const val LOG_FILE_NAME = "agent_system_logs.txt"

    fun log(context: Context, message: String, tag: String = "CI/CD") {
        try {
            val file = File(context.filesDir, LOG_FILE_NAME)
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val logEntry = "[$timestamp] [$tag] $message\n"
            file.appendText(logEntry)
            
            // Keep file size manageable (max ~500 lines roughly)
            val lines = file.readLines()
            if (lines.size > 500) {
                file.writeText(lines.takeLast(300).joinToString("\n") + "\n")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getLogs(context: Context): String {
        return try {
            val file = File(context.filesDir, LOG_FILE_NAME)
            if (file.exists()) {
                val text = file.readText()
                if (text.isBlank()) "No hay registros." else text
            } else {
                "No hay registros. El sistema está limpio."
            }
        } catch (e: Exception) {
            "Error al leer registros: ${e.message}"
        }
    }

    fun clearLogs(context: Context) {
        try {
            File(context.filesDir, LOG_FILE_NAME).writeText("")
        } catch (e: Exception) {}
    }
}
