package com.kavyaraj.tillflow.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kavyaraj.tillflow.domain.model.OrderSummary
import com.kavyaraj.tillflow.domain.repository.OrderRepository
import com.kavyaraj.tillflow.domain.sync.SyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    repository: OrderRepository,
    private val syncScheduler: SyncScheduler,
) : ViewModel() {

    /**
     * cachedIn keeps the loaded pages in the ViewModel, so rotating the screen shows the
     * same list at the same scroll position instead of reloading from page one.
     */
    val orders: Flow<PagingData<OrderSummary>> =
        repository.pagedOrders().cachedIn(viewModelScope)

    val pendingSyncCount: StateFlow<Int> = repository.observeUnsyncedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = 0)

    fun onSyncNow() = syncScheduler.requestSync()
}
