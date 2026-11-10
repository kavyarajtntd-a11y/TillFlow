package com.kavyaraj.tillflow.ui.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kavyaraj.tillflow.R
import com.kavyaraj.tillflow.domain.model.OrderDetail
import com.kavyaraj.tillflow.domain.model.OrderLineItem
import com.kavyaraj.tillflow.domain.model.OrderSummary
import com.kavyaraj.tillflow.ui.theme.TillFlowTheme
import com.kavyaraj.tillflow.ui.util.formatOrderTime
import com.kavyaraj.tillflow.ui.util.formatPence

@Composable
fun OrderDetailRoute(
    onBack: () -> Unit,
    viewModel: OrderDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    OrderDetailScreen(state = state, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    state: OrderDetailUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.order_number, state.orderId)) },
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
            val order = state.order
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                order == null -> Text(
                    stringResource(R.string.order_not_found),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                )
                else -> OrderContent(order)
            }
        }
    }
}

@Composable
private fun OrderContent(order: OrderDetail) {
    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Column(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    formatOrderTime(order.summary.createdAtMillis),
                    style = MaterialTheme.typography.titleMedium,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(itemCountText(order.summary.itemCount))
                    SyncLabel(order.summary.isSynced)
                }
            }
            HorizontalDivider()
        }
        items(order.lines) { line ->
            ListItem(
                headlineContent = { Text(line.productName) },
                supportingContent = {
                    Text(
                        stringResource(
                            R.string.quantity_times_price,
                            line.quantity,
                            formatPence(line.unitPricePence),
                        ),
                    )
                },
                trailingContent = { Text(formatPence(line.subtotalPence)) },
            )
        }
        item {
            HorizontalDivider()
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(stringResource(R.string.total), style = MaterialTheme.typography.titleLarge)
                Text(formatPence(order.summary.totalPence), style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OrderDetailScreenPreview() {
    TillFlowTheme(dynamicColor = false) {
        OrderDetailScreen(
            state = OrderDetailUiState(
                orderId = 12,
                isLoading = false,
                order = OrderDetail(
                    summary = OrderSummary(12, 1_731_259_800_000, 1030, 3, isSynced = false),
                    lines = listOf(
                        OrderLineItem("Flat White", 340, 2),
                        OrderLineItem("Cinnamon Bun", 350, 1),
                    ),
                ),
            ),
            onBack = {},
        )
    }
}
