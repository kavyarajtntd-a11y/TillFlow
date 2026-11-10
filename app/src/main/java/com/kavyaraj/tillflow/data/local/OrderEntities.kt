package com.kavyaraj.tillflow.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "created_at_millis") val createdAtMillis: Long,
    @ColumnInfo(name = "total_pence") val totalPence: Long,
    // Every order starts unsynced; the sync worker in step 4 uploads these and flips the flag.
    @ColumnInfo(name = "is_synced") val isSynced: Boolean = false,
)

/**
 * One product on an order. The name and price are copied in at the time of sale,
 * so changing a product's price later never rewrites past receipts.
 */
@Entity(
    tableName = "order_lines",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["order_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("order_id")],
)
data class OrderLineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "order_id") val orderId: Long = 0,
    @ColumnInfo(name = "product_id") val productId: String,
    @ColumnInfo(name = "product_name") val productName: String,
    @ColumnInfo(name = "unit_price_pence") val unitPricePence: Long,
    val quantity: Int,
)
