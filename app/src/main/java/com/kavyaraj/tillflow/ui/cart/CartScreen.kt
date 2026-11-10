package com.kavyaraj.tillflow.ui.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kavyaraj.tillflow.R
import com.kavyaraj.tillflow.domain.model.Cart
import com.kavyaraj.tillflow.domain.model.CartLine
import com.kavyaraj.tillflow.domain.model.Product
import com.kavyaraj.tillflow.ui.theme.TillFlowTheme
import com.kavyaraj.tillflow.ui.util.formatPence

@Composable
fun CartRoute(
    onBack: () -> Unit,
    viewModel: CartViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CartScreen(
        state = state,
        onBack = onBack,
        onIncrease = viewModel::onIncrease,
        onDecrease = viewModel::onDecrease,
        onCharge = viewModel::onCharge,
        onOrderAcknowledged = {
            viewModel.onOrderAcknowledged()
            onBack()
        },
        onErrorShown = viewModel::onErrorShown,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    state: CartUiState,
    onBack: () -> Unit,
    onIncrease: (Product) -> Unit,
    onDecrease: (String) -> Unit,
    onCharge: () -> Unit,
    onOrderAcknowledged: () -> Unit,
    onErrorShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = stringResource(R.string.order_failed)

    // A failure is part of the state; the screen shows it once, then tells the ViewModel.
    LaunchedEffect(state.orderFailed) {
        if (state.orderFailed) {
            snackbarHostState.showSnackbar(errorMessage)
            onErrorShown()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.cart_title)) },
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
        bottomBar = {
            if (!state.cart.isEmpty) {
                ChargeBar(state.cart.totalPence, state.isPlacingOrder, onCharge)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (state.cart.isEmpty) {
            Box(
                Modifier.padding(padding).fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.cart_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(Modifier.padding(padding).fillMaxSize()) {
                items(state.cart.lines, key = { it.product.id }) { line ->
                    CartLineRow(
                        line = line,
                        canIncrease = state.cart.canAdd(line.product),
                        onIncrease = { onIncrease(line.product) },
                        onDecrease = { onDecrease(line.product.id) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    state.placedOrderId?.let { orderId ->
        AlertDialog(
            onDismissRequest = onOrderAcknowledged,
            title = { Text(stringResource(R.string.order_placed_title, orderId)) },
            text = { Text(stringResource(R.string.order_placed_body)) },
            confirmButton = {
                TextButton(onClick = onOrderAcknowledged) { Text(stringResource(R.string.done)) }
            },
        )
    }
}

@Composable
private fun CartLineRow(
    line: CartLine,
    canIncrease: Boolean,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
) {
    val name = line.product.name
    ListItem(
        headlineContent = { Text(name) },
        supportingContent = {
            Text(stringResource(R.string.each_price, formatPence(line.product.pricePence)))
        },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val decreaseLabel = stringResource(R.string.decrease_quantity, name)
                OutlinedIconButton(
                    onClick = onDecrease,
                    modifier = Modifier.semantics { contentDescription = decreaseLabel },
                ) {
                    // material-icons-core has no minus icon, so a text glyph stands in.
                    Text("−", style = MaterialTheme.typography.titleLarge)
                }
                Text(
                    "${line.quantity}",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(min = 24.dp),
                )
                OutlinedIconButton(onClick = onIncrease, enabled = canIncrease) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.increase_quantity, name),
                    )
                }
                Text(
                    formatPence(line.subtotalPence),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.End,
                    modifier = Modifier.widthIn(min = 64.dp),
                )
            }
        },
    )
}

@Composable
private fun ChargeBar(totalPence: Long, isPlacingOrder: Boolean, onCharge: () -> Unit) {
    BottomAppBar {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.total), style = MaterialTheme.typography.labelMedium)
                Text(formatPence(totalPence), style = MaterialTheme.typography.titleLarge)
            }
            Button(onClick = onCharge, enabled = !isPlacingOrder) {
                if (isPlacingOrder) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.charge, formatPence(totalPence)))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CartScreenPreview() {
    val flatWhite = Product("1", "Flat White", "Coffee", 340, 50)
    val bun = Product("2", "Cinnamon Bun", "Bakery", 350, 2)
    TillFlowTheme(dynamicColor = false) {
        CartScreen(
            state = CartUiState(cart = Cart().add(flatWhite).add(flatWhite).add(bun)),
            onBack = {},
            onIncrease = {},
            onDecrease = {},
            onCharge = {},
            onOrderAcknowledged = {},
            onErrorShown = {},
        )
    }
}
