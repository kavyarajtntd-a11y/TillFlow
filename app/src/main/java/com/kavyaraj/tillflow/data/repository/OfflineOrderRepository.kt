package com.kavyaraj.tillflow.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.kavyaraj.tillflow.data.local.OrderDao
import com.kavyaraj.tillflow.data.local.OrderEntity
import com.kavyaraj.tillflow.data.local.OrderLineEntity
import com.kavyaraj.tillflow.data.local.toDomain
import com.kavyaraj.tillflow.domain.model.Cart
import com.kavyaraj.tillflow.domain.model.OrderDetail
import com.kavyaraj.tillflow.domain.model.OrderSummary
import com.kavyaraj.tillflow.domain.repository.OrderRepository
import com.kavyaraj.tillflow.domain.sync.SyncScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineOrderRepository @Inject constructor(
    private val dao: OrderDao,
    private val syncScheduler: SyncScheduler,
) : OrderRepository {

    override suspend fun placeOrder(cart: Cart): Long {
        require(!cart.isEmpty) { "Cannot place an empty order" }
        val order = OrderEntity(
            createdAtMillis = System.currentTimeMillis(),
            totalPence = cart.totalPence,
        )
        val lines = cart.lines.map { line ->
            OrderLineEntity(
                productId = line.product.id,
                productName = line.product.name,
                unitPricePence = line.product.pricePence,
                quantity = line.quantity,
            )
        }
        val orderId = dao.placeOrder(order, lines)
        // The sale is already safe in Room. Uploading happens in the background, whenever
        // the device is online; the till never waits for the network.
        syncScheduler.requestSync()
        return orderId
    }

    override fun pagedOrders(): Flow<PagingData<OrderSummary>> =
        Pager(
            // 30 rows fills about two screens; Paging loads the next page before the user
            // reaches the end. No placeholders, because the list has no fixed size to show.
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = { dao.pagingSource() },
        ).flow.map { page -> page.map { it.toDomain() } }

    override fun observeOrder(orderId: Long): Flow<OrderDetail?> =
        combine(dao.observeSummary(orderId), dao.observeLines(orderId)) { summary, lines ->
            summary?.let { OrderDetail(it.toDomain(), lines.map { line -> line.toDomain() }) }
        }

    override fun observeUnsyncedCount(): Flow<Int> = dao.observeUnsyncedCount()

    override suspend fun unsyncedOrders(): List<OrderDetail> =
        dao.unsyncedSummaries().map { summary ->
            OrderDetail(summary.toDomain(), dao.linesFor(summary.id).map { it.toDomain() })
        }

    override suspend fun markSynced(orderId: Long) = dao.markSynced(orderId)

    private companion object {
        const val PAGE_SIZE = 30
    }
}
