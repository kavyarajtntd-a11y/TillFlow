package com.kavyaraj.tillflow.domain.model

/** One row in the order history list. */
data class OrderSummary(
    val id: Long,
    val createdAtMillis: Long,
    val totalPence: Long,
    val itemCount: Int,
    val isSynced: Boolean,
)

/** A product line on a saved order, as it was at the time of sale. */
data class OrderLineItem(
    val productName: String,
    val unitPricePence: Long,
    val quantity: Int,
) {
    val subtotalPence: Long get() = unitPricePence * quantity
}

data class OrderDetail(
    val summary: OrderSummary,
    val lines: List<OrderLineItem>,
)
