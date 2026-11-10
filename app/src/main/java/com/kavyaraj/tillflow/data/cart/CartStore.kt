package com.kavyaraj.tillflow.data.cart

import com.kavyaraj.tillflow.domain.model.Cart
import com.kavyaraj.tillflow.domain.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds the current cart in memory. It's a @Singleton so the catalogue and cart screens,
 * which have separate ViewModels, see the same basket.
 */
@Singleton
class CartStore @Inject constructor() {
    private val _cart = MutableStateFlow(Cart())
    val cart: StateFlow<Cart> = _cart.asStateFlow()

    fun add(product: Product) = _cart.update { it.add(product) }

    fun removeOne(productId: String) = _cart.update { it.removeOne(productId) }

    fun clear() {
        _cart.value = Cart()
    }
}
