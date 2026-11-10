package com.kavyaraj.tillflow.ui.orders

import androidx.lifecycle.SavedStateHandle
import com.kavyaraj.tillflow.domain.model.OrderDetail
import com.kavyaraj.tillflow.domain.model.OrderLineItem
import com.kavyaraj.tillflow.domain.model.OrderSummary
import com.kavyaraj.tillflow.testing.FakeOrderRepository
import com.kavyaraj.tillflow.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrderDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeOrderRepository()

    private val order = OrderDetail(
        summary = OrderSummary(7, 1_731_259_800_000, totalPence = 1030, itemCount = 3, isSynced = false),
        lines = listOf(OrderLineItem("Flat White", 340, 2), OrderLineItem("Cinnamon Bun", 350, 1)),
    )

    /** In the app, Navigation fills SavedStateHandle from the route. Tests fill it by hand. */
    private fun viewModelFor(orderId: Long) = OrderDetailViewModel(
        SavedStateHandle(mapOf(OrderDetailViewModel.ORDER_ID_ARG to orderId)),
        repository,
    )

    private fun TestScope.collect(viewModel: OrderDetailViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
    }

    @Test
    fun `loads the order named in the navigation argument`() = runTest {
        repository.putDetail(order)
        val viewModel = viewModelFor(7)
        collect(viewModel)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(order, state.order)
        assertEquals(1030L, state.order!!.lines.sumOf { it.subtotalPence })
    }

    @Test
    fun `reports a missing order as loaded but null`() = runTest {
        val viewModel = viewModelFor(99)
        collect(viewModel)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.order)
        assertEquals(99L, state.orderId)
    }
}
