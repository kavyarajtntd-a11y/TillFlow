package com.kavyaraj.tillflow.data.sync

import com.kavyaraj.tillflow.domain.model.OrderDetail
import com.kavyaraj.tillflow.domain.model.OrderLineItem
import com.kavyaraj.tillflow.domain.model.OrderSummary
import com.kavyaraj.tillflow.testing.FakeOrderApi
import com.kavyaraj.tillflow.testing.FakeOrderRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderSyncerTest {

    private val orders = FakeOrderRepository()
    private val api = FakeOrderApi()
    private val syncer = OrderSyncer(orders, api)

    private fun order(id: Long, synced: Boolean = false) = OrderDetail(
        summary = OrderSummary(id, createdAtMillis = id, totalPence = 340, itemCount = 1, isSynced = synced),
        lines = listOf(OrderLineItem("Flat White", 340, 1)),
    )

    @Test
    fun `uploads pending orders oldest first and marks them synced`() = runTest {
        orders.putDetail(order(2))
        orders.putDetail(order(1))

        val result = syncer.syncPending()

        assertEquals(SyncResult(synced = 2, failed = 0), result)
        assertEquals(listOf(1L, 2L), api.uploadedIds)
        assertTrue(orders.isSynced(1) && orders.isSynced(2))
    }

    @Test
    fun `skips orders that are already synced`() = runTest {
        orders.putDetail(order(1, synced = true))
        orders.putDetail(order(2))

        syncer.syncPending()

        assertEquals(listOf(2L), api.uploadedIds)
    }

    @Test
    fun `one failed upload doesn't block the others, and stays pending`() = runTest {
        (1L..3L).forEach { orders.putDetail(order(it)) }
        api.failingIds += 2L

        val result = syncer.syncPending()

        assertEquals(SyncResult(synced = 2, failed = 1), result)
        assertTrue(result.hasFailures)
        assertFalse(orders.isSynced(2))
        assertEquals(1, orders.unsyncedOrders().size)
    }

    @Test
    fun `a retry picks up what failed last time`() = runTest {
        orders.putDetail(order(1))
        api.failingIds += 1L
        syncer.syncPending()

        api.failingIds.clear() // The network is back.
        val retry = syncer.syncPending()

        assertEquals(SyncResult(synced = 1, failed = 0), retry)
        assertTrue(orders.isSynced(1))
    }
}
