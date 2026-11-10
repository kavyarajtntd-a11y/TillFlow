package com.kavyaraj.tillflow

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.kavyaraj.tillflow.data.sync.WorkManagerSyncScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Entry point for Hilt. Also gives WorkManager a Hilt-aware factory, so workers can have
 * dependencies injected. (The manifest turns off WorkManager's default start-up for this.)
 */
@HiltAndroidApp
class TillFlowApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var syncScheduler: WorkManagerSyncScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        syncScheduler.schedulePeriodicSync()
        // Pick up anything left unsynced from last time the app ran.
        syncScheduler.requestSync()
    }
}
