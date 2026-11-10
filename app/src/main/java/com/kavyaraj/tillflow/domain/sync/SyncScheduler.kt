package com.kavyaraj.tillflow.domain.sync

/**
 * Asks for pending orders to be uploaded as soon as the device is online. Callers don't
 * know or care that WorkManager is behind it, so tests can swap in a fake.
 */
interface SyncScheduler {
    fun requestSync()
}
