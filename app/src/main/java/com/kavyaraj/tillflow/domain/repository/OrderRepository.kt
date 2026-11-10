package com.kavyaraj.tillflow.domain.repository

import androidx.paging.PagingData
import com.kavyaraj.tillflow.domain.model.Cart
import com.kavyaraj.tillflow.domain.model.OrderDetail
import com.kavyaraj.tillflow.domain.model.OrderSummary
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    /**
     * Saves the cart as a completed order and takes the sold items out of stock, all or
     * nothing. Returns the new order's number.
     */
    suspend fun placeOrder(cart: Cart): Long

    /**
     * All orders, newest first, loaded a page at a time as the user scrolls. PagingData
     * comes from paging-common, which is plain Kotlin, so domain/ still has no Android code.
     */
    fun pagedOrders(): Flow<PagingData<OrderSummary>>

    /** Emits the order and its lines, or null if no order has that id. */
    fun observeOrder(orderId: Long): Flow<OrderDetail?>

    /** How many orders haven't reached the server yet. */
    fun observeUnsyncedCount(): Flow<Int>

    /** Orders still waiting to upload, oldest first. */
    suspend fun unsyncedOrders(): List<OrderDetail>

    suspend fun markSynced(orderId: Long)
}
