package com.kavyaraj.tillflow.data.local

import androidx.room.ColumnInfo
import com.kavyaraj.tillflow.domain.model.OrderLineItem
import com.kavyaraj.tillflow.domain.model.OrderSummary

/** Not a table: the shape of one row returned by the order-summary query in [OrderDao]. */
data class OrderSummaryRow(
    val id: Long,
    @ColumnInfo(name = "created_at_millis") val createdAtMillis: Long,
    @ColumnInfo(name = "total_pence") val totalPence: Long,
    @ColumnInfo(name = "item_count") val itemCount: Int,
    @ColumnInfo(name = "is_synced") val isSynced: Boolean,
)

fun OrderSummaryRow.toDomain() = OrderSummary(
    id = id,
    createdAtMillis = createdAtMillis,
    totalPence = totalPence,
    itemCount = itemCount,
    isSynced = isSynced,
)

fun OrderLineEntity.toDomain() = OrderLineItem(
    productName = productName,
    unitPricePence = unitPricePence,
    quantity = quantity,
)
