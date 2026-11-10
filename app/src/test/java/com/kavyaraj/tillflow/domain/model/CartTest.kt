package com.kavyaraj.tillflow.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class CartTest {

    private val flatWhite = Product("1", "Flat White", "Coffee", 340, 50)
    private val bun = Product("2", "Cinnamon Bun", "Bakery", 350, 2)
    private val earlGrey = Product("3", "Earl Grey", "Tea", 250, 0)

    @Test
    fun `adding the same product twice increases its quantity instead of adding a line`() {
        val cart = Cart().add(flatWhite).add(flatWhite)

        assertEquals(1, cart.lines.size)
        assertEquals(2, cart.quantityOf(flatWhite.id))
    }

    @Test
    fun `totals are exact in pence`() {
        val cart = Cart().add(flatWhite).add(flatWhite).add(bun)

        assertEquals(3, cart.itemCount)
        assertEquals(340L * 2 + 350L, cart.totalPence)
    }

    @Test
    fun `cannot add more than is in stock`() {
        val full = Cart().add(bun).add(bun)

        assertFalse(full.canAdd(bun))
        assertSame(full, full.add(bun))
        assertEquals(2, full.quantityOf(bun.id))
    }

    @Test
    fun `out-of-stock products cannot be added at all`() {
        assertTrue(Cart().add(earlGrey).isEmpty)
    }

    @Test
    fun `removing the last unit removes the line`() {
        val cart = Cart().add(flatWhite).add(bun).removeOne(bun.id)

        assertEquals(listOf(flatWhite.id), cart.lines.map { it.product.id })
    }

    @Test
    fun `adding again picks up the latest price`() {
        val repriced = flatWhite.copy(pricePence = 360)
        val cart = Cart().add(flatWhite).add(repriced)

        assertEquals(720L, cart.totalPence)
    }
}
