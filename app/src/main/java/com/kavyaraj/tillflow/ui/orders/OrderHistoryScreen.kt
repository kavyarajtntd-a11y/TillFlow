package com.kavyaraj.tillflow.ui.orders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.kavyaraj.tillflow.R
import com.kavyaraj.tillflow.domain.model.OrderSummary
import com.kavyaraj.tillflow.ui.util.formatOrderTime
import com.kavyaraj.tillflow.ui.util.formatPence

@Composable
fun OrderHistoryRoute(
    onBack: () -> Unit,
    onOpenOrder: (Long) -> Unit,
    viewModel: OrderHistoryViewModel = hiltViewModel(),
) {
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsStateWithLifecycle()
    OrderHistoryScreen(
        orders = viewModel.orders.collectAsLazyPagingItems(),
        pendingSyncCount = pendingSyncCount,
        onBack = onBack,
        onOpenOrder = onOpenOrder,
        onSyncNow = viewModel::onSyncNow,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    orders: LazyPagingItems<OrderSummary>,
    pendingSyncCount: Int,
    onBack: () -> Unit,
    onOpenOrder: (Long) -> Unit,
    onSyncNow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.order_history))
                        Text(
                            pendingSyncText(pendingSyncCount),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSyncNow, enabled = pendingSyncCount > 0) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.sync_now))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            // "refresh" is the first page. Later pages have their own "append" state.
            when (orders.loadState.refresh) {
                is LoadState.Loading -> if (orders.itemCount == 0) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                } else {
                    OrderList(orders, onOpenOrder)
                }
                is LoadState.Error -> ErrorMessage(
                    onRetry = orders::retry,
                    modifier = Modifier.align(Alignment.Center),
                )
                is LoadState.NotLoading -> if (orders.itemCount == 0) {
                    Text(
                        stringResource(R.string.orders_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                    )
                } else {
                    OrderList(orders, onOpenOrder)
                }
            }
        }
    }
}

@Composable
private fun OrderList(orders: LazyPagingItems<OrderSummary>, onOpenOrder: (Long) -> Unit) {
    LazyColumn(Modifier.fillMaxSize()) {
        items(count = orders.itemCount, key = orders.itemKey { it.id }) { index ->
            // Reading orders[index] is what tells Paging the user has scrolled this far.
            val order = orders[index] ?: return@items
            OrderRow(order, onClick = { onOpenOrder(order.id) })
            HorizontalDivider()
        }
        if (orders.loadState.append is LoadState.Loading) {
            item {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun OrderRow(order: OrderSummary, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(stringResource(R.string.order_number, order.id)) },
        supportingContent = {
            Column {
                Text(formatOrderTime(order.createdAtMillis))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(itemCountText(order.itemCount))
                    SyncLabel(order.isSynced)
                }
            }
        },
        trailingContent = {
            Text(formatPence(order.totalPence), style = MaterialTheme.typography.titleMedium)
        },
    )
}

@Composable
private fun ErrorMessage(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.orders_load_failed), style = MaterialTheme.typography.bodyLarge)
        Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
    }
}
