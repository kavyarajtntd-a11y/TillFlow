package com.kavyaraj.tillflow.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    /** Room re-runs this query and emits a new list whenever the products table changes. */
    @Query("SELECT * FROM products ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<ProductEntity>>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun count(): Int

    @Upsert
    suspend fun upsertAll(products: List<ProductEntity>)
}
