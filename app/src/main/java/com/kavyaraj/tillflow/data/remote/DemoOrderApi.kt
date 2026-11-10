package com.kavyaraj.tillflow.data.remote

import android.util.Log
import com.kavyaraj.tillflow.domain.model.OrderDetail
import kotlinx.coroutines.delay
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Stand-in for a real backend so the sync flow can be demonstrated end to end.
 * Each upload takes about a second and fails 20% of the time, which exercises the
 * retry and backoff logic. Received orders are kept in memory, keyed by id.
 */
@Singleton
class DemoOrderApi @Inject constructor() : OrderApi {

    private val received = ConcurrentHashMap.newKeySet<Long>()

    override suspend fun upload(order: OrderDetail) {
        delay(LATENCY_MILLIS)
        if (Random.nextInt(100) < FAILURE_PERCENT) {
            throw IOException("Simulated network failure for order #${order.summary.id}")
        }
        val isNew = received.add(order.summary.id)
        Log.d(TAG, "Order #${order.summary.id} ${if (isNew) "received" else "already received, ignored"}")
    }

    private companion object {
        const val TAG = "DemoOrderApi"
        const val LATENCY_MILLIS = 800L
        const val FAILURE_PERCENT = 20
    }
}
