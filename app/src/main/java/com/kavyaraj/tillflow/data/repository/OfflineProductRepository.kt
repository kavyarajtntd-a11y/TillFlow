package com.kavyaraj.tillflow.data.repository

import com.kavyaraj.tillflow.data.local.ProductDao
import com.kavyaraj.tillflow.data.local.sampleProducts
import com.kavyaraj.tillflow.data.local.toDomain
import com.kavyaraj.tillflow.data.local.toEntity
import com.kavyaraj.tillflow.domain.model.Product
import com.kavyaraj.tillflow.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Offline-first: the local database is the single source of truth and the UI only ever
 * reads from it. A later step adds a sync worker that writes server changes into Room,
 * and the UI updates automatically because it is already observing the table.
 */
class OfflineProductRepository @Inject constructor(
    private val dao: ProductDao,
) : ProductRepository {

    override fun observeProducts(): Flow<List<Product>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun seedIfEmpty() {
        if (dao.count() == 0) {
            dao.upsertAll(sampleProducts.map { it.toEntity() })
        }
    }
}
