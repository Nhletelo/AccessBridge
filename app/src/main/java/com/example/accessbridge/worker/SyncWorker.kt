package com.example.accessbridge.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.accessbridge.di.AppModule

/**
 * Background WorkManager CoroutineWorker for triggering resource synchronization.
 */
class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val repository = AppModule.provideResourceRepository(applicationContext)
            val syncResult = repository.syncResources()
            if (syncResult.isSuccess) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
