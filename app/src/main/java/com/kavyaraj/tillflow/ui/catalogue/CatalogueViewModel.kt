package com.kavyaraj.tillflow.ui.catalogue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kavyaraj.tillflow.data.cart.CartStore
import com.kavyaraj.tillflow.domain.model.Product
import com.kavyaraj.tillflow.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Unidirectional data flow: the screen sends events in (onQueryChange, onCategorySelected,
 * onAddToCart), and gets one [CatalogueUiState] out. All filtering happens here, so it is
 * unit-testable without a device.
 */
@HiltViewModel
class CatalogueViewModel @Inject constructor(
    repository: ProductRepository,
    private val cartStore: CartStore,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CatalogueUiState> = combine(
        repository.observeProducts(),
        query,
        selectedCategory,
        cartStore.cart,
    ) { products, query, category, cart ->
        CatalogueUiState(
            isLoading = false,
            query = query,
            categories = products.map { it.category }.distinct().sorted(),
            selectedCategory = category,
            products = products.filterBy(query, category),
            cart = cart,
        )
    }.stateIn(
        scope = viewModelScope,
        // Keep the upstream alive for 5s after the UI stops collecting, so a screen
        // rotation doesn't restart the database query.
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CatalogueUiState(),
    )

    init {
        viewModelScope.launch { repository.seedIfEmpty() }
    }

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }

    /** Tapping the selected category again clears the filter. */
    fun onCategorySelected(category: String?) {
        selectedCategory.update { current -> if (current == category) null else category }
    }

    fun onAddToCart(product: Product) = cartStore.add(product)
}

private fun List<Product>.filterBy(query: String, category: String?): List<Product> {
    val trimmed = query.trim()
    return filter { product ->
        (category == null || product.category == category) &&
            (trimmed.isEmpty() || product.name.contains(trimmed, ignoreCase = true))
    }
}
