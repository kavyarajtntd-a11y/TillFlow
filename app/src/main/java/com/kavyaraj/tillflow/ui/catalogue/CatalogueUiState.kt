package com.kavyaraj.tillflow.ui.catalogue

import com.kavyaraj.tillflow.domain.model.Cart
import com.kavyaraj.tillflow.domain.model.Product

/** Everything the catalogue screen needs to draw itself, in one immutable object. */
data class CatalogueUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val products: List<Product> = emptyList(),
    val cart: Cart = Cart(),
)
