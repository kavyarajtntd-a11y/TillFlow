package com.kavyaraj.tillflow.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Runs [OrderSyncer] in the background. WorkManager only starts it when the device has a
 * network connection, and keeps it scheduled across app restarts and reboots.
 *
 * @AssistedInject: WorkManager supplies context and params at runtime; Hilt supplies the rest.
 */
@HiltWorker
class OrderSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncer: OrderSyncer,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val result = syncer.syncPending()
        return when {
            !result.hasFailures -> Result.success()
            // Retry uses the exponential backoff set in WorkManagerSyncScheduler.
            runAttemptCount < MAX_ATTEMPTS -> Result.retry()
            // Give up for now; the periodic sync will pick the orders up later.
            else -> Result.failure()
        }
    }

    private companion object {
        const val MAX_ATTEMPTS = 5
    }
}
