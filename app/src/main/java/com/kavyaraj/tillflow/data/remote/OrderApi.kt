package com.kavyaraj.tillflow.data.remote

import com.kavyaraj.tillflow.domain.model.OrderDetail
import java.io.IOException

/**
 * The server the till uploads sales to. A real version would be a Retrofit interface
 * sending a JSON DTO; nothing outside data/ would need to change.
 */
interface OrderApi {
    /**
     * Uploads one order. Must be idempotent: the server keys orders by id, so if a
     * response is lost and the upload is retried, it isn't recorded twice.
     *
     * @throws IOException if the network or server fails; the caller retries later.
     */
    suspend fun upload(order: OrderDetail)
}
