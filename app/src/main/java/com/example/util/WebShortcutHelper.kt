package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.widget.Toast
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.StandaloneAppActivity
import com.example.data.model.WebAppEntity

object WebShortcutHelper {

    const val ACTION_LAUNCH_WEB_APP = "com.example.ACTION_LAUNCH_WEB_APP"
    const val EXTRA_WEB_APP_ID = "EXTRA_WEB_APP_ID"
    const val EXTRA_WEB_URL = "EXTRA_WEB_URL"
    const val EXTRA_WEB_NAME = "EXTRA_WEB_NAME"

    fun pinWebAppShortcut(context: Context, app: WebAppEntity, onComplete: ((Boolean) -> Unit)? = null): Boolean {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            Toast.makeText(context, "Tu lanzador no soporta fijar accesos directos automáticos", Toast.LENGTH_LONG).show()
            onComplete?.invoke(false)
            return false
        }

        val launchIntent = Intent(context, StandaloneAppActivity::class.java).apply {
            action = ACTION_LAUNCH_WEB_APP
            putExtra(EXTRA_WEB_APP_ID, app.id)
            putExtra(EXTRA_WEB_URL, app.url)
            putExtra(EXTRA_WEB_NAME, app.name)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val iconBitmap = generateShortcutIconBitmap(context, app)
        val iconCompat = IconCompat.createWithBitmap(iconBitmap)

        val shortcutInfo = ShortcutInfoCompat.Builder(context, "web_app_${app.id}")
            .setShortLabel(app.name)
            .setLongLabel("${app.name} (WebNative)")
            .setIcon(iconCompat)
            .setIntent(launchIntent)
            .setAlwaysBadged()
            .build()

        val pinned = ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
        if (pinned) {
            Toast.makeText(
                context,
                "¡Acceso directo a '${app.name}' fijado a la pantalla de inicio!",
                Toast.LENGTH_SHORT
            ).show()
        }
        onComplete?.invoke(pinned)
        return pinned
    }

    private fun generateShortcutIconBitmap(context: Context, app: WebAppEntity): Bitmap {
        val size = 144
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = (app.accentColor.toInt() and 0x00FFFFFF) or (0xFF shl 24)
        }

        // Draw rounded squircle / background
        val rect = RectF(6f, 6f, (size - 6).toFloat(), (size - 6).toFloat())
        canvas.drawRoundRect(rect, 32f, 32f, paint)

        // Draw inner dark overlay
        val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.argb(40, 0, 0, 0)
        }
        val innerRect = RectF(12f, 12f, (size - 12).toFloat(), (size - 12).toFloat())
        canvas.drawRoundRect(innerRect, 26f, 26f, innerPaint)

        // Draw text / emoji
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 58f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val displayIcon = when {
            app.iconType == "EMOJI" && app.iconValue.isNotEmpty() -> app.iconValue
            app.name.isNotEmpty() -> app.name.take(2).uppercase()
            else -> "⚡"
        }

        val yPos = ((size / 2) - ((textPaint.descent() + textPaint.ascent()) / 2))
        canvas.drawText(displayIcon, (size / 2).toFloat(), yPos, textPaint)

        return bitmap
    }
}
