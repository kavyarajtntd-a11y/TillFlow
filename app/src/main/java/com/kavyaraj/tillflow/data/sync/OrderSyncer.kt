package com.kavyaraj.tillflow.data.sync

import com.kavyaraj.tillflow.data.remote.OrderApi
import com.kavyaraj.tillflow.domain.repository.OrderRepository
import kotlinx.coroutines.CancellationException
import java.io.IOException
import javax.inject.Inject

data class SyncResult(val synced: Int, val failed: Int) {
    val hasFailures: Boolean get() = failed > 0
}

/**
 * Uploads every pending order, oldest first. Kept separate from the Worker so the logic
 * is plain Kotlin and can be unit-tested without WorkManager or a device.
 */
class OrderSyncer @Inject constructor(
    private val orders: OrderRepository,
    private val api: OrderApi,
) {
    suspend fun syncPending(): SyncResult {
        var synced = 0
        var failed = 0
        for (order in orders.unsyncedOrders()) {
            try {
                api.upload(order)
                // Marked only after the server confirms. If the app dies between the two
                // steps, the order uploads again next time; the server ignores the duplicate.
                orders.markSynced(order.summary.id)
                synced++
            } catch (e: CancellationException) {
                throw e // WorkManager stopped us (e.g. network lost). Let it reschedule.
            } catch (e: IOException) {
                failed++ // Keep going: one bad upload shouldn't block the rest.
            }
        }
        return SyncResult(synced, failed)
    }
}
