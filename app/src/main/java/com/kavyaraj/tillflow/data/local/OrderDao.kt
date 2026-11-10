package com.kavyaraj.tillflow.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

// An abstract class rather than an interface, so placeOrder can have a body that Room
// wraps in a single database transaction.
@Dao
abstract class OrderDao {

    @Insert
    abstract suspend fun insertOrder(order: OrderEntity): Long

    @Insert
    abstract suspend fun insertLines(lines: List<OrderLineEntity>)

    @Query("UPDATE products SET stock = MAX(stock - :quantity, 0) WHERE id = :productId")
    abstract suspend fun decrementStock(productId: String, quantity: Int)

    /**
     * Saves the order, its lines and the stock changes together. If any step fails
     * (say, the app is killed halfway), the whole thing rolls back: no half-saved orders,
     * no stock taken for a sale that never happened.
     */
    @Transaction
    open suspend fun placeOrder(order: OrderEntity, lines: List<OrderLineEntity>): Long {
        val orderId = insertOrder(order)
        insertLines(lines.map { it.copy(orderId = orderId) })
        lines.forEach { decrementStock(it.productId, it.quantity) }
        return orderId
    }

    /**
     * Newest orders first. Room generates a PagingSource that reads one page per query
     * and invalidates itself when the orders or order_lines tables change, so a new sale
     * appears at the top of the history without any manual refresh.
     * The id is a tie-breaker, so two orders in the same millisecond keep a stable order.
     */
    @Query(
        """
        SELECT o.id, o.created_at_millis, o.total_pence, o.is_synced,
               COALESCE((SELECT SUM(l.quantity) FROM order_lines l WHERE l.order_id = o.id), 0)
                   AS item_count
        FROM orders o
        ORDER BY o.created_at_millis DESC, o.id DESC
        """,
    )
    abstract fun pagingSource(): PagingSource<Int, OrderSummaryRow>

    @Query(
        """
        SELECT o.id, o.created_at_millis, o.total_pence, o.is_synced,
               COALESCE((SELECT SUM(l.quantity) FROM order_lines l WHERE l.order_id = o.id), 0)
                   AS item_count
        FROM orders o
        WHERE o.id = :orderId
        """,
    )
    abstract fun observeSummary(orderId: Long): Flow<OrderSummaryRow?>

    @Query("SELECT * FROM order_lines WHERE order_id = :orderId ORDER BY id")
    abstract fun observeLines(orderId: Long): Flow<List<OrderLineEntity>>

    // --- Sync ---

    @Query("SELECT COUNT(*) FROM orders WHERE is_synced = 0")
    abstract fun observeUnsyncedCount(): Flow<Int>

    @Query(
        """
        SELECT o.id, o.created_at_millis, o.total_pence, o.is_synced,
               COALESCE((SELECT SUM(l.quantity) FROM order_lines l WHERE l.order_id = o.id), 0)
                   AS item_count
        FROM orders o
        WHERE o.is_synced = 0
        ORDER BY o.id
        """,
    )
    abstract suspend fun unsyncedSummaries(): List<OrderSummaryRow>

    @Query("SELECT * FROM order_lines WHERE order_id = :orderId ORDER BY id")
    abstract suspend fun linesFor(orderId: Long): List<OrderLineEntity>

    @Query("UPDATE orders SET is_synced = 1 WHERE id = :orderId")
    abstract suspend fun markSynced(orderId: Long)
}
