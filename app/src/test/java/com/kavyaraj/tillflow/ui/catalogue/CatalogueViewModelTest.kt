package com.kavyaraj.tillflow.ui.catalogue

import com.kavyaraj.tillflow.data.cart.CartStore
import com.kavyaraj.tillflow.domain.model.Product
import com.kavyaraj.tillflow.testing.FakeProductRepository
import com.kavyaraj.tillflow.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogueViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val flatWhite = Product("1", "Flat White", "Coffee", 340, 50)
    private val oatLatte = Product("2", "Oat Latte", "Coffee", 380, 3)
    private val croissant = Product("3", "Almond Croissant", "Bakery", 325, 12)
    private val all = listOf(flatWhite, oatLatte, croissant)

    private val repository = FakeProductRepository(all)
    private val cartStore = CartStore()
    private lateinit var viewModel: CatalogueViewModel

    // Created in @Before, not as a property: JUnit builds the class before the rule
    // swaps Dispatchers.Main, and the ViewModel's init block already uses Main.
    @Before
    fun setUp() {
        viewModel = CatalogueViewModel(repository, cartStore)
    }

    /** uiState uses WhileSubscribed, so a test must collect it for values to flow. */
    private fun TestScope.startCollecting() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
    }

    @Test
    fun `seeds the catalogue on creation`() {
        assertEquals(1, repository.seedCalls)
    }

    @Test
    fun `shows all products and sorted categories once loaded`() = runTest {
        startCollecting()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(all, state.products)
        assertEquals(listOf("Bakery", "Coffee"), state.categories)
    }

    @Test
    fun `search matches names case-insensitively and ignores surrounding spaces`() = runTest {
        startCollecting()

        viewModel.onQueryChange("  LATTE ")

        assertEquals(listOf(oatLatte), viewModel.uiState.value.products)
    }

    @Test
    fun `category filter combines with search`() = runTest {
        startCollecting()

        viewModel.onCategorySelected("Coffee")
        assertEquals(listOf(flatWhite, oatLatte), viewModel.uiState.value.products)

        viewModel.onQueryChange("flat")
        assertEquals(listOf(flatWhite), viewModel.uiState.value.products)
    }

    @Test
    fun `tapping the selected category again clears the filter`() = runTest {
        startCollecting()

        viewModel.onCategorySelected("Bakery")
        viewModel.onCategorySelected("Bakery")

        assertNull(viewModel.uiState.value.selectedCategory)
        assertEquals(all, viewModel.uiState.value.products)
    }

    @Test
    fun `updates when the database changes`() = runTest {
        startCollecting()

        repository.emit(listOf(croissant))

        assertEquals(listOf(croissant), viewModel.uiState.value.products)
        assertEquals(listOf("Bakery"), viewModel.uiState.value.categories)
    }

    @Test
    fun `tapping a product adds it to the shared cart`() = runTest {
        startCollecting()

        viewModel.onAddToCart(flatWhite)
        viewModel.onAddToCart(flatWhite)

        val cart = viewModel.uiState.value.cart
        assertEquals(2, cart.quantityOf(flatWhite.id))
        assertEquals(680L, cart.totalPence)
        // The cart screen reads the same store, so it sees this too.
        assertEquals(cart, cartStore.cart.value)
    }
}
