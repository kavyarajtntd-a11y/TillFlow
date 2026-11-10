package com.kavyaraj.tillflow.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kavyaraj.tillflow.domain.sync.SyncScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerSyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : SyncScheduler {

    private val workManager get() = WorkManager.getInstance(context)

    private val onlineOnly = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /**
     * Uploads pending orders as soon as the device is online.
     *
     * Unique work with APPEND_OR_REPLACE: if a sync is already running when a new sale
     * comes in, one more run is queued after it, so the new order isn't missed, and
     * ten quick sales don't queue ten separate uploads.
     */
    override fun requestSync() {
        val request = OneTimeWorkRequestBuilder<OrderSyncWorker>()
            .setConstraints(onlineOnly)
            // 10s, 20s, 40s, ... between attempts, so a flaky server isn't hammered.
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .build()
        workManager.enqueueUniqueWork(ONE_TIME_SYNC, ExistingWorkPolicy.APPEND_OR_REPLACE, request)
    }

    /** Safety net: every 15 minutes (WorkManager's minimum), upload anything still pending. */
    fun schedulePeriodicSync() {
        val request = PeriodicWorkRequestBuilder<OrderSyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(onlineOnly)
            .build()
        // KEEP: don't reset the timer every time the app starts.
        workManager.enqueueUniquePeriodicWork(PERIODIC_SYNC, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    private companion object {
        const val ONE_TIME_SYNC = "order-sync"
        const val PERIODIC_SYNC = "order-sync-periodic"
    }
}
