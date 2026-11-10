package com.kavyaraj.tillflow.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

// Version 2 added the orders tables. exportSchema is off while the schema is still changing;
// switch it on (with real migrations) before a release that users would upgrade from.
@Database(
    entities = [ProductEntity::class, OrderEntity::class, OrderLineEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class TillFlowDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
}
