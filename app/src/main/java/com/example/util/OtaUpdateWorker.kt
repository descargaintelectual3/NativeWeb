package com.example.util

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * Comprueba releases aunque la interfaz principal no esté abierta.
 * La descarga se realiza automáticamente cuando el usuario activó la comprobación automática.
 */
class OtaUpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        if (!OtaUpdateManager.isAutoCheckEnabled(applicationContext)) {
            return Result.success()
        }

        return try {
            val status = OtaUpdateManager.checkForUpdates(
                context = applicationContext,
                notifyIfAvailable = true
            )

            if (status is UpdateStatus.UpdateAvailable && !status.isIgnored) {
                val download = OtaUpdateManager.downloadAndPrepareInstall(
                    context = applicationContext,
                    targetUrl = status.downloadUrl,
                    versionName = status.versionName
                )

                download.onSuccess { apkFile ->
                    val openedInstaller = OtaUpdateManager.promptInstallApk(applicationContext, apkFile)
                    if (!openedInstaller) {
                        OtaNotificationHelper.showReadyToInstallNotification(
                            context = applicationContext,
                            versionName = status.versionName,
                            apkPath = apkFile.absolutePath
                        )
                    }
                }
            }

            if (status is UpdateStatus.Error && runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.success()
            }
        } catch (_: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.success()
        }
    }

    companion object {
        private const val WORK_NAME = "webnative_ota_periodic_check"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<OtaUpdateWorker>(
                30,
                TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    15,
                    TimeUnit.MINUTES
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}

