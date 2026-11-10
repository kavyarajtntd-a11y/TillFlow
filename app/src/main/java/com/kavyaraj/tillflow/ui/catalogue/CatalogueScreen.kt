package com.kavyaraj.tillflow.ui.catalogue

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kavyaraj.tillflow.R
import com.kavyaraj.tillflow.domain.model.Cart
import com.kavyaraj.tillflow.domain.model.Product
import com.kavyaraj.tillflow.ui.theme.TillFlowTheme
import com.kavyaraj.tillflow.ui.util.formatPence

/** Stateful entry point: gets the ViewModel and hands plain state + callbacks to [CatalogueScreen]. */
@Composable
fun CatalogueRoute(
    onOpenCart: () -> Unit,
    onOpenOrders: () -> Unit,
    viewModel: CatalogueViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CatalogueScreen(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onCategorySelected = viewModel::onCategorySelected,
        onAddToCart = viewModel::onAddToCart,
        onOpenCart = onOpenCart,
        onOpenOrders = onOpenOrders,
    )
}

/** Stateless: takes state, emits events. That makes it previewable and easy to UI-test. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogueScreen(
    state: CatalogueUiState,
    onQueryChange: (String) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onAddToCart: (Product) -> Unit,
    onOpenCart: () -> Unit,
    onOpenOrders: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.catalogue_title)) },
                actions = {
                    IconButton(onClick = onOpenOrders) {
                        Icon(
                            Icons.AutoMirrored.Filled.List,
                            contentDescription = stringResource(R.string.order_history),
                        )
                    }
                },
            )
        },
        bottomBar = {
            if (!state.cart.isEmpty) CartBar(state.cart, onOpenCart)
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                placeholder = { Text(stringResource(R.string.search_products)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
            CategoryChips(
                categories = state.categories,
                selected = state.selectedCategory,
                onSelected = onCategorySelected,
            )
            when {
                state.isLoading -> CenteredBox { CircularProgressIndicator() }
                state.products.isEmpty() -> CenteredBox {
                    Text(
                        stringResource(R.string.no_products),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                else -> ProductList(state.products, state.cart, onAddToCart)
            }
        }
    }
}

@Composable
private fun CartBar(cart: Cart, onOpenCart: () -> Unit) {
    val label = LocalContext.current.resources.getQuantityString(
        R.plurals.view_cart,
        cart.itemCount,
        cart.itemCount,
        formatPence(cart.totalPence),
    )
    BottomAppBar {
        Button(
            onClick = onOpenCart,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null)
            Text(label, modifier = Modifier.padding(start = ButtonDefaults.IconSpacing))
        }
    }
}

@Composable
private fun CategoryChips(
    categories: List<String>,
    selected: String?,
    onSelected: (String?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelected(null) },
                label = { Text(stringResource(R.string.all_categories)) },
            )
        }
        items(categories, key = { it }) { category ->
            FilterChip(
                selected = category == selected,
                onClick = { onSelected(category) },
                label = { Text(category) },
            )
        }
    }
}

@Composable
private fun ProductList(
    products: List<Product>,
    cart: Cart,
    onAddToCart: (Product) -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize()) {
        // Stable keys let Compose keep each row's state when the list is filtered.
        items(products, key = { it.id }) { product ->
            ProductRow(
                product = product,
                quantityInCart = cart.quantityOf(product.id),
                canAdd = cart.canAdd(product),
                onClick = { onAddToCart(product) },
            )
            HorizontalDivider()
        }
    }
}

// Badge was experimental in some Material 3 versions; opting in keeps older ones compiling.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductRow(
    product: Product,
    quantityInCart: Int,
    canAdd: Boolean,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable(enabled = canAdd, onClick = onClick),
        headlineContent = { Text(product.name) },
        supportingContent = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(product.category)
                StockLabel(product)
            }
        },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (quantityInCart > 0) {
                    Badge { Text("$quantityInCart") }
                }
                Text(
                    formatPence(product.pricePence),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        },
    )
}

@Composable
private fun StockLabel(product: Product) {
    val (text, color) = when {
        product.isOutOfStock ->
            stringResource(R.string.out_of_stock) to MaterialTheme.colorScheme.error
        product.isLowStock ->
            stringResource(R.string.low_stock, product.stock) to MaterialTheme.colorScheme.tertiary
        else ->
            stringResource(R.string.in_stock, product.stock) to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(text, color = color)
}

@Composable
private fun CenteredBox(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}

@Preview(showBackground = true)
@Composable
private fun CatalogueScreenPreview() {
    val flatWhite = Product("1", "Flat White", "Coffee", 340, 50)
    TillFlowTheme(dynamicColor = false) {
        CatalogueScreen(
            state = CatalogueUiState(
                isLoading = false,
                categories = listOf("Bakery", "Coffee"),
                products = listOf(
                    flatWhite,
                    Product("2", "Cinnamon Bun", "Bakery", 350, 3),
                    Product("3", "Earl Grey", "Tea", 250, 0),
                ),
                cart = Cart().add(flatWhite).add(flatWhite),
            ),
            onQueryChange = {},
            onCategorySelected = {},
            onAddToCart = {},
            onOpenCart = {},
            onOpenOrders = {},
        )
    }
}
