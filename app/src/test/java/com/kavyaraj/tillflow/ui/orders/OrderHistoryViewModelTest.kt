package com.kavyaraj.tillflow.ui.orders

import androidx.paging.testing.asSnapshot
import com.kavyaraj.tillflow.domain.model.OrderDetail
import com.kavyaraj.tillflow.domain.model.OrderSummary
import com.kavyaraj.tillflow.testing.FakeOrderRepository
import com.kavyaraj.tillflow.testing.FakeSyncScheduler
import com.kavyaraj.tillflow.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrderHistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeOrderRepository()
    private val syncScheduler = FakeSyncScheduler()

    private fun summary(id: Long, synced: Boolean = false) =
        OrderSummary(id, createdAtMillis = id * 1_000, totalPence = 340, itemCount = 1, isSynced = synced)

    @Test
    fun `exposes the repository's orders as paged data`() = runTest {
        val summaries = (3L downTo 1L).map { summary(it) }
        repository.summaries = summaries
        val viewModel = OrderHistoryViewModel(repository, syncScheduler)

        // asSnapshot (from paging-testing) collects the PagingData into a plain list.
        assertEquals(summaries, viewModel.orders.asSnapshot())
    }

    @Test
    fun `counts orders waiting to sync, live`() = runTest {
        repository.putDetail(OrderDetail(summary(1), emptyList()))
        repository.putDetail(OrderDetail(summary(2), emptyList()))
        val viewModel = OrderHistoryViewModel(repository, syncScheduler)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.pendingSyncCount.collect {}
        }
        assertEquals(2, viewModel.pendingSyncCount.value)

        repository.markSynced(1)

        assertEquals(1, viewModel.pendingSyncCount.value)
    }

    @Test
    fun `sync now asks the scheduler for a sync`() {
        val viewModel = OrderHistoryViewModel(repository, syncScheduler)

        viewModel.onSyncNow()

        assertEquals(1, syncScheduler.requests)
    }
}
