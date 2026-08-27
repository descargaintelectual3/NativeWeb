package com.example.util

import android.content.Context
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

data class CommandResult(
    val command: String,
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
    val executionTimeMs: Long
) {
    val isSuccess: Boolean get() = exitCode == 0
}

data class HardwareAccelerationInfo(
    val renderer: String,
    val isHardwareAccelerated: Boolean,
    val glVersion: String,
    val isSkiaEnabled: Boolean,
    val properties: Map<String, String>
)

object ADBManager {

    /**
     * Executes an arbitrary shell command using the native runtime process.
     */
    suspend fun execShellCommand(command: String): CommandResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        var process: Process? = null
        var stdout = ""
        var stderr = ""
        var exitCode = -1

        try {
            process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))

            val stdoutReader = BufferedReader(InputStreamReader(process.inputStream))
            val stderrReader = BufferedReader(InputStreamReader(process.errorStream))

            val stdoutBuilder = StringBuilder()
            var line: String?
            while (stdoutReader.readLine().also { line = it } != null) {
                stdoutBuilder.append(line).append("\n")
            }
            stdout = stdoutBuilder.toString().trimEnd()

            val stderrBuilder = StringBuilder()
            while (stderrReader.readLine().also { line = it } != null) {
                stderrBuilder.append(line).append("\n")
            }
            stderr = stderrBuilder.toString().trimEnd()

            exitCode = process.waitFor()
        } catch (e: Exception) {
            stderr = "Execution error: ${e.localizedMessage}"
            exitCode = -1
        } finally {
            process?.destroy()
        }

        val elapsed = System.currentTimeMillis() - startTime
        CommandResult(
            command = command,
            exitCode = exitCode,
            stdout = stdout,
            stderr = stderr,
            executionTimeMs = elapsed
        )
    }

    /**
     * Reads a system property via reflection from android.os.SystemProperties,
     * falling back to 'getprop' command line if reflection fails.
     */
    fun getSystemProperty(key: String, defaultValue: String = ""): String {
        return try {
            val clazz = Class.forName("android.os.SystemProperties")
            val method = clazz.getMethod("get", String::class.java, String::class.java)
            val result = method.invoke(null, key, defaultValue) as? String
            if (result.isNullOrBlank()) defaultValue else result
        } catch (e: Exception) {
            try {
                val p = Runtime.getRuntime().exec("getprop $key")
                val reader = BufferedReader(InputStreamReader(p.inputStream))
                val out = reader.readLine()?.trim() ?: defaultValue
                p.destroy()
                if (out.isBlank()) defaultValue else out
            } catch (ex: Exception) {
                defaultValue
            }
        }
    }

    /**
     * Gathers quick system properties for verification and diagnostics.
     */
    fun getQuickSystemDiagnostics(): Map<String, String> {
        val keys = listOf(
            "sys.usb.config" to "Configuración USB Activa",
            "persist.sys.adb.config" to "Persistencia ADB",
            "debug.hwui.renderer" to "Renderizador HWUI",
            "ro.build.version.release" to "Versión Android",
            "ro.build.version.sdk" to "Nivel SDK API",
            "ro.product.manufacturer" to "Fabricante",
            "ro.product.model" to "Modelo de Dispositivo",
            "ro.board.platform" to "Plataforma / Chipset",
            "ro.hardware" to "Hardware SoC",
            "ro.boot.flash.locked" to "Bootloader Bloqueado"
        )

        return keys.associate { (key, label) ->
            val value = getSystemProperty(key, "[No definido]")
            label to value
        }
    }

    /**
     * Inspects current hardware acceleration settings and renderers.
     */
    suspend fun getHardwareAccelerationInfo(): HardwareAccelerationInfo = withContext(Dispatchers.IO) {
        val currentRenderer = getSystemProperty("debug.hwui.renderer", "opengl (default)")
        val forceGpu = getSystemProperty("debug.hwui.force_gpu", "0")
        val glEsVersion = getSystemProperty("ro.opengles.version", "desconocido")
        val renderThread = getSystemProperty("debug.hwui.render_thread", "1")

        val properties = mapOf(
            "debug.hwui.renderer" to currentRenderer,
            "debug.hwui.force_gpu" to forceGpu,
            "ro.opengles.version" to glEsVersion,
            "debug.hwui.render_thread" to renderThread,
            "Hardware Level" to if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) "Vulkan/Skia Ready" else "OpenGL ES"
        )

        val isSkia = currentRenderer.contains("skia", ignoreCase = true)

        HardwareAccelerationInfo(
            renderer = currentRenderer,
            isHardwareAccelerated = true,
            glVersion = glEsVersion,
            isSkiaEnabled = isSkia,
            properties = properties
        )
    }

    /**
     * Toggles/sets hardware acceleration rendering engine (e.g. skiagl, skiavk, opengl).
     */
    suspend fun setHardwareAccelerationRenderer(context: Context, renderer: String): CommandResult {
        AppLogger.log(context, "Intentando configurar renderizador HWUI: $renderer", tag = "ADB/HW")
        val cmd = "setprop debug.hwui.renderer $renderer"
        val result = execShellCommand(cmd)
        AppLogger.log(context, "Resultado cambio HWUI ($renderer): exitCode=${result.exitCode}", tag = "ADB/HW")
        return result
    }

    /**
     * Resets hardware acceleration to default OpenGL/Skia engine.
     */
    suspend fun resetHardwareAcceleration(context: Context): CommandResult {
        AppLogger.log(context, "Restableciendo renderizador HWUI por defecto", tag = "ADB/HW")
        val cmd = "setprop debug.hwui.renderer \"\""
        return execShellCommand(cmd)
    }
}
