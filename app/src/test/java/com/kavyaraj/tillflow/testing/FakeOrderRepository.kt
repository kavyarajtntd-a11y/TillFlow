package com.kavyaraj.tillflow.testing

import androidx.paging.PagingData
import com.kavyaraj.tillflow.domain.model.Cart
import com.kavyaraj.tillflow.domain.model.OrderDetail
import com.kavyaraj.tillflow.domain.model.OrderSummary
import com.kavyaraj.tillflow.domain.repository.OrderRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Records the orders it receives. Set [shouldFail] to simulate a database error, or
 * [gate] to hold placeOrder open so a test can look at the "placing order" state.
 * Saved orders for the detail and sync features are added with [putDetail].
 */
class FakeOrderRepository : OrderRepository {
    val placedOrders = mutableListOf<Cart>()
    var shouldFail = false
    var gate: CompletableDeferred<Unit>? = null

    /** What pagedOrders() returns. */
    var summaries: List<OrderSummary> = emptyList()

    private val details = MutableStateFlow<Map<Long, OrderDetail>>(emptyMap())

    fun putDetail(detail: OrderDetail) = details.update { it + (detail.summary.id to detail) }

    fun isSynced(orderId: Long): Boolean = details.value.getValue(orderId).summary.isSynced

    override suspend fun placeOrder(cart: Cart): Long {
        gate?.await()
        if (shouldFail) error("Simulated database failure")
        placedOrders += cart
        return placedOrders.size.toLong()
    }

    override fun pagedOrders(): Flow<PagingData<OrderSummary>> = flowOf(PagingData.from(summaries))

    override fun observeOrder(orderId: Long): Flow<OrderDetail?> = details.map { it[orderId] }

    override fun observeUnsyncedCount(): Flow<Int> =
        details.map { orders -> orders.values.count { !it.summary.isSynced } }

    override suspend fun unsyncedOrders(): List<OrderDetail> =
        details.value.values.filter { !it.summary.isSynced }.sortedBy { it.summary.id }

    override suspend fun markSynced(orderId: Long) = details.update { orders ->
        val order = orders.getValue(orderId)
        orders + (orderId to order.copy(summary = order.summary.copy(isSynced = true)))
    }
}
