package com.kavyaraj.tillflow.domain.model

data class CartLine(
    val product: Product,
    val quantity: Int,
) {
    val subtotalPence: Long get() = product.pricePence * quantity
}

/**
 * The basket being rung up. Immutable: every change returns a new Cart, which makes
 * it safe to share through a StateFlow and trivial to unit-test.
 */
data class Cart(val lines: List<CartLine> = emptyList()) {

    val itemCount: Int get() = lines.sumOf { it.quantity }
    val totalPence: Long get() = lines.sumOf { it.subtotalPence }
    val isEmpty: Boolean get() = lines.isEmpty()

    fun quantityOf(productId: String): Int =
        lines.firstOrNull { it.product.id == productId }?.quantity ?: 0

    /** True if one more of [product] can be sold without exceeding its stock. */
    fun canAdd(product: Product): Boolean = quantityOf(product.id) < product.stock

    /** Adds one unit. Ignored if it would sell more than is in stock. */
    fun add(product: Product): Cart {
        if (!canAdd(product)) return this
        val existing = lines.indexOfFirst { it.product.id == product.id }
        return if (existing == -1) {
            copy(lines = lines + CartLine(product, quantity = 1))
        } else {
            copy(
                lines = lines.mapIndexed { index, line ->
                    // Take the latest product too, so the price and stock stay current.
                    if (index == existing) line.copy(product = product, quantity = line.quantity + 1) else line
                },
            )
        }
    }

    /** Removes one unit; the line disappears when its quantity reaches zero. */
    fun removeOne(productId: String): Cart = copy(
        lines = lines.mapNotNull { line ->
            when {
                line.product.id != productId -> line
                line.quantity > 1 -> line.copy(quantity = line.quantity - 1)
                else -> null
            }
        },
    )
}
