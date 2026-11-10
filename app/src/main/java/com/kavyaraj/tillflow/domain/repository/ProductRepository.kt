package com.kavyaraj.tillflow.domain.repository

import com.kavyaraj.tillflow.domain.model.Product
import kotlinx.coroutines.flow.Flow

/**
 * What the UI layer knows about products. It depends on this interface, not on Room,
 * so the ViewModel can be unit-tested with a fake and the data source can change later
 * (for example, adding a network sync) without touching the UI.
 */
interface ProductRepository {
    /** Emits the full product list now, and again every time it changes. */
    fun observeProducts(): Flow<List<Product>>

    /** Loads the demo catalogue on first launch. Does nothing if products already exist. */
    suspend fun seedIfEmpty()
}
