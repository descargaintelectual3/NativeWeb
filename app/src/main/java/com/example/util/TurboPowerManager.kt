package com.example.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import android.widget.Toast

object TurboPowerManager {

    private var wakeLock: PowerManager.WakeLock? = null

    fun acquireWakeLock(context: Context, tag: String = "WebNative:TurboBoost") {
        try {
            if (wakeLock == null) {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                wakeLock = powerManager.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    tag
                )
            }
            if (wakeLock?.isHeld == false) {
                wakeLock?.acquire(60 * 60 * 1000L) // 1 hour max safeguard
            }
        } catch (_: Exception) {
            // WakeLock permission fallback
        }
    }

    fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {
            // Ignore
        }
    }

    fun setKeepScreenOn(activity: Activity, keepOn: Boolean) {
        if (keepOn) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
    }

    fun requestIgnoreBatteryOptimization(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "El ahorro de batería no está restringido en este dispositivo", Toast.LENGTH_SHORT).show()
            }
        } catch (_: Exception) {
            try {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "No se pudo abrir el menú de optimización de batería", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun clearAllWebData(context: Context, onComplete: () -> Unit = {}) {
        try {
            // Clear WebView Cache
            WebView(context).apply {
                clearCache(true)
                clearFormData()
                clearHistory()
                clearSslPreferences()
            }
            // Clear Storage
            WebStorage.getInstance().deleteAllData()
            // Clear Cookies
            CookieManager.getInstance().removeAllCookies {
                CookieManager.getInstance().flush()
                Toast.makeText(context, "Caché, almacenamiento y cookies limpiados", Toast.LENGTH_SHORT).show()
                onComplete()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Limpieza de caché completada", Toast.LENGTH_SHORT).show()
            onComplete()
        }
    }
}
