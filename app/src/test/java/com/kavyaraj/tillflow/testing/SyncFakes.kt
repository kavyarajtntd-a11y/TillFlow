package com.kavyaraj.tillflow.testing

import com.kavyaraj.tillflow.data.remote.OrderApi
import com.kavyaraj.tillflow.domain.model.OrderDetail
import com.kavyaraj.tillflow.domain.sync.SyncScheduler
import java.io.IOException

class FakeSyncScheduler : SyncScheduler {
    var requests = 0
        private set

    override fun requestSync() {
        requests++
    }
}

/** Accepts uploads, except for the order ids listed in [failingIds]. */
class FakeOrderApi : OrderApi {
    val uploadedIds = mutableListOf<Long>()
    val failingIds = mutableSetOf<Long>()

    override suspend fun upload(order: OrderDetail) {
        val id = order.summary.id
        if (id in failingIds) throw IOException("Simulated failure for order #$id")
        uploadedIds += id
    }
}
