package com.example.accessbridge.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Helper utility to schedule or trigger background resource synchronization using WorkManager.
 */
object SyncWorkManager {
    private const val PERIODIC_SYNC_WORK_NAME = "AccessBridgePeriodicSync"
    private const val ONE_TIME_SYNC_WORK_NAME = "AccessBridgeOneTimeSync"

    /**
     * Schedules periodic resource sync with network constraints (NetworkType.CONNECTED).
     */
    fun schedulePeriodicSync(
        context: Context,
        repeatIntervalMinutes: Long = 15
    ) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatIntervalMinutes, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    /**
     * Triggers immediate one-time sync with network constraints (NetworkType.CONNECTED).
     */
    fun triggerOneTimeSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            ONE_TIME_SYNC_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }

    /**
     * Cancels scheduled periodic synchronization.
     */
    fun cancelSync(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_SYNC_WORK_NAME)
    }
}
