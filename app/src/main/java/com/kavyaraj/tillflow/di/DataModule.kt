package com.kavyaraj.tillflow.di

import android.content.Context
import androidx.room.Room
import com.kavyaraj.tillflow.data.local.OrderDao
import com.kavyaraj.tillflow.data.local.ProductDao
import com.kavyaraj.tillflow.data.local.TillFlowDatabase
import com.kavyaraj.tillflow.data.remote.DemoOrderApi
import com.kavyaraj.tillflow.data.remote.OrderApi
import com.kavyaraj.tillflow.data.repository.OfflineOrderRepository
import com.kavyaraj.tillflow.data.sync.WorkManagerSyncScheduler
import com.kavyaraj.tillflow.domain.sync.SyncScheduler
import com.kavyaraj.tillflow.data.repository.OfflineProductRepository
import com.kavyaraj.tillflow.domain.repository.OrderRepository
import com.kavyaraj.tillflow.domain.repository.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TillFlowDatabase =
        Room.databaseBuilder(context, TillFlowDatabase::class.java, "tillflow.db")
            // Development only: wipes and re-seeds the database when the version changes.
            // Replace with Migration objects before real users have data to lose.
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideProductDao(db: TillFlowDatabase): ProductDao = db.productDao()

    @Provides
    fun provideOrderDao(db: TillFlowDatabase): OrderDao = db.orderDao()
}

/** Tells Hilt which implementation to hand out for each repository interface. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(impl: OfflineProductRepository): ProductRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(impl: OfflineOrderRepository): OrderRepository
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    @Binds
    abstract fun bindSyncScheduler(impl: WorkManagerSyncScheduler): SyncScheduler

    /** Swap DemoOrderApi for a Retrofit-backed implementation to talk to a real server. */
    @Binds
    abstract fun bindOrderApi(impl: DemoOrderApi): OrderApi
}
