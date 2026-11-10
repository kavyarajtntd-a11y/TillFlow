package com.kavyaraj.tillflow.domain.model

/**
 * A product the till can sell.
 *
 * Prices are stored as whole pence (Long), never Double: floating point can't represent
 * values like 0.10 exactly, so totals drift. This is standard practice for money.
 */
data class Product(
    val id: String,
    val name: String,
    val category: String,
    val pricePence: Long,
    val stock: Int,
) {
    val isLowStock: Boolean get() = stock in 1 until LOW_STOCK_THRESHOLD
    val isOutOfStock: Boolean get() = stock <= 0

    companion object {
        const val LOW_STOCK_THRESHOLD = 5
    }
}
