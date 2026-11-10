package com.kavyaraj.tillflow.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kavyaraj.tillflow.domain.model.Product

/** Database row. Kept separate from [Product] so the schema can change without rippling into the UI. */
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    @ColumnInfo(name = "price_pence") val pricePence: Long,
    val stock: Int,
)

fun ProductEntity.toDomain() = Product(
    id = id,
    name = name,
    category = category,
    pricePence = pricePence,
    stock = stock,
)

fun Product.toEntity() = ProductEntity(
    id = id,
    name = name,
    category = category,
    pricePence = pricePence,
    stock = stock,
)
