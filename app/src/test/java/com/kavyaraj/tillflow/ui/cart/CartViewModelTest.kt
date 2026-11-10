package com.kavyaraj.tillflow.ui.cart

import com.kavyaraj.tillflow.data.cart.CartStore
import com.kavyaraj.tillflow.domain.model.Product
import com.kavyaraj.tillflow.testing.FakeOrderRepository
import com.kavyaraj.tillflow.testing.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val flatWhite = Product("1", "Flat White", "Coffee", 340, 50)
    private val bun = Product("2", "Cinnamon Bun", "Bakery", 350, 2)

    private val cartStore = CartStore()
    private val orders = FakeOrderRepository()
    private lateinit var viewModel: CartViewModel

    @Before
    fun setUp() {
        viewModel = CartViewModel(cartStore, orders)
        cartStore.add(flatWhite)
        cartStore.add(bun)
    }

    private fun TestScope.startCollecting() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
    }

    @Test
    fun `plus and minus change quantities`() = runTest {
        startCollecting()

        viewModel.onIncrease(flatWhite)
        viewModel.onDecrease(bun.id)

        val cart = viewModel.uiState.value.cart
        assertEquals(2, cart.quantityOf(flatWhite.id))
        assertEquals(0, cart.quantityOf(bun.id))
    }

    @Test
    fun `charging saves the order, clears the cart and reports the order number`() = runTest {
        startCollecting()

        viewModel.onCharge()

        assertEquals(1, orders.placedOrders.size)
        assertEquals(690L, orders.placedOrders.single().totalPence)
        val state = viewModel.uiState.value
        assertTrue(state.cart.isEmpty)
        assertEquals(1L, state.placedOrderId)
    }

    @Test
    fun `a double tap only charges once`() = runTest {
        startCollecting()
        val gate = CompletableDeferred<Unit>()
        orders.gate = gate

        viewModel.onCharge()
        assertTrue(viewModel.uiState.value.isPlacingOrder)
        viewModel.onCharge()
        gate.complete(Unit)

        assertEquals(1, orders.placedOrders.size)
        assertFalse(viewModel.uiState.value.isPlacingOrder)
    }

    @Test
    fun `a failed order keeps the cart so staff can retry`() = runTest {
        startCollecting()
        orders.shouldFail = true

        viewModel.onCharge()

        val state = viewModel.uiState.value
        assertTrue(state.orderFailed)
        assertEquals(2, state.cart.itemCount)

        viewModel.onErrorShown()
        assertFalse(viewModel.uiState.value.orderFailed)
    }

    @Test
    fun `acknowledging the confirmation resets checkout`() = runTest {
        startCollecting()

        viewModel.onCharge()
        viewModel.onOrderAcknowledged()

        assertNull(viewModel.uiState.value.placedOrderId)
    }
}
