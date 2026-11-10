package com.kavyaraj.tillflow.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kavyaraj.tillflow.data.cart.CartStore
import com.kavyaraj.tillflow.domain.model.Cart
import com.kavyaraj.tillflow.domain.model.Product
import com.kavyaraj.tillflow.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartUiState(
    val cart: Cart = Cart(),
    val isPlacingOrder: Boolean = false,
    /** Set once an order is saved; the screen shows a confirmation, then calls onOrderAcknowledged. */
    val placedOrderId: Long? = null,
    val orderFailed: Boolean = false,
)

/** The parts of the state that belong to checkout, as opposed to the cart contents. */
private data class CheckoutStatus(
    val isPlacingOrder: Boolean = false,
    val placedOrderId: Long? = null,
    val failed: Boolean = false,
)

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartStore: CartStore,
    private val orderRepository: OrderRepository,
) : ViewModel() {

    private val checkout = MutableStateFlow(CheckoutStatus())

    val uiState: StateFlow<CartUiState> = combine(cartStore.cart, checkout) { cart, status ->
        CartUiState(
            cart = cart,
            isPlacingOrder = status.isPlacingOrder,
            placedOrderId = status.placedOrderId,
            orderFailed = status.failed,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CartUiState())

    fun onIncrease(product: Product) = cartStore.add(product)

    fun onDecrease(productId: String) = cartStore.removeOne(productId)

    fun onCharge() {
        val cart = cartStore.cart.value
        // Guard against a double tap charging the customer twice.
        if (cart.isEmpty || checkout.value.isPlacingOrder) return

        checkout.value = CheckoutStatus(isPlacingOrder = true)
        viewModelScope.launch {
            checkout.value = try {
                val orderId = orderRepository.placeOrder(cart)
                cartStore.clear()
                CheckoutStatus(placedOrderId = orderId)
            } catch (e: CancellationException) {
                throw e // Never swallow cancellation, or the coroutine can't be stopped.
            } catch (e: Exception) {
                CheckoutStatus(failed = true) // The cart is kept so staff can retry.
            }
        }
    }

    fun onOrderAcknowledged() {
        checkout.value = CheckoutStatus()
    }

    fun onErrorShown() {
        checkout.update { it.copy(failed = false) }
    }
}
