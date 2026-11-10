package com.kavyaraj.tillflow.ui.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kavyaraj.tillflow.domain.model.OrderDetail
import com.kavyaraj.tillflow.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class OrderDetailUiState(
    val orderId: Long,
    val isLoading: Boolean = true,
    /** Null once loaded means the order doesn't exist. */
    val order: OrderDetail? = null,
)

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: OrderRepository,
) : ViewModel() {

    /**
     * Navigation puts the route's {orderId} argument into SavedStateHandle. Reading it here,
     * rather than passing it in from the screen, means it also survives process death.
     */
    private val orderId: Long = checkNotNull(savedStateHandle[ORDER_ID_ARG]) {
        "OrderDetailViewModel needs an '$ORDER_ID_ARG' navigation argument"
    }

    val uiState: StateFlow<OrderDetailUiState> = repository.observeOrder(orderId)
        .map { OrderDetailUiState(orderId = orderId, isLoading = false, order = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = OrderDetailUiState(orderId = orderId),
        )

    companion object {
        const val ORDER_ID_ARG = "orderId"
    }
}
