package com.kavyaraj.tillflow.testing

import com.kavyaraj.tillflow.domain.model.Product
import com.kavyaraj.tillflow.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory stand-in for the Room-backed repository. Tests push data in with [emit]. */
class FakeProductRepository(initial: List<Product> = emptyList()) : ProductRepository {
    private val products = MutableStateFlow(initial)
    var seedCalls = 0
        private set

    fun emit(newProducts: List<Product>) {
        products.value = newProducts
    }

    override fun observeProducts(): Flow<List<Product>> = products

    override suspend fun seedIfEmpty() {
        seedCalls++
    }
}
