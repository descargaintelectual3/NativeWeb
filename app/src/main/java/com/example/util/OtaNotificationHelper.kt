package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

object OtaNotificationHelper {
    private const val CHANNEL_ID = "civer_ota_updates_channel"
    private const val CHANNEL_NAME = "Actualizaciones Civer Cloud OTA"
    const val NOTIFICATION_ID = 9001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Notificaciones automáticas de nuevas versiones y releases"
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun dismissNotification(context: Context) {
        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancel(NOTIFICATION_ID)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showUpdateNotification(
        context: Context,
        versionName: String,
        changelog: String,
        downloadUrl: String
    ) {
        // Respect user's ignore settings and notification preferences
        if (!OtaUpdateManager.isPushNotificationsEnabled(context)) {
            return
        }

        val ignored = OtaUpdateManager.getIgnoredVersion(context)
        if (ignored.isNotBlank() && ignored.equals(versionName, ignoreCase = true)) {
            return
        }

        // Avoid re-alerting if already notified for this exact version in the last 24h
        val lastNotifiedVersion = OtaUpdateManager.getLastNotifiedVersion(context)
        val lastNotifiedTime = OtaUpdateManager.getLastNotifiedTime(context)
        val isRecent = System.currentTimeMillis() - lastNotifiedTime < 24 * 60 * 60 * 1000L
        if (lastNotifiedVersion.equals(versionName, ignoreCase = true) && isRecent) {
            return
        }

        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("EXTRA_OPEN_OTA_DIALOG", true)
            putExtra("EXTRA_AUTO_UPDATE_VERSION", versionName)
            putExtra("EXTRA_AUTO_UPDATE_URL", downloadUrl)
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            intent,
            pendingIntentFlags
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("🚀 Nueva Actualización $versionName")
            .setContentText("Toca para ver los detalles y actualizar.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Se ha detectado $versionName.\n$changelog\n\nToca para abrir el panel de actualización."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(NOTIFICATION_ID, builder.build())
            OtaUpdateManager.recordNotificationSent(context, versionName)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
