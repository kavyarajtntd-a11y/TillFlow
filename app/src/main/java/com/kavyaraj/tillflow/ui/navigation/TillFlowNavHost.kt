package com.kavyaraj.tillflow.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kavyaraj.tillflow.ui.cart.CartRoute
import com.kavyaraj.tillflow.ui.catalogue.CatalogueRoute
import com.kavyaraj.tillflow.ui.orders.OrderDetailRoute
import com.kavyaraj.tillflow.ui.orders.OrderDetailViewModel
import com.kavyaraj.tillflow.ui.orders.OrderHistoryRoute

private object Routes {
    const val CATALOGUE = "catalogue"
    const val CART = "cart"
    const val ORDERS = "orders"
    const val ORDER_DETAIL = "orders/{${OrderDetailViewModel.ORDER_ID_ARG}}"

    fun orderDetail(orderId: Long) = "orders/$orderId"
}

/**
 * The app's screens and how to move between them. Screens never see the NavController;
 * they get plain callbacks (onOpenCart, onBack), so they stay reusable and testable.
 */
@Composable
fun TillFlowNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.CATALOGUE) {
        composable(Routes.CATALOGUE) {
            CatalogueRoute(
                onOpenCart = { navController.navigate(Routes.CART) },
                onOpenOrders = { navController.navigate(Routes.ORDERS) },
            )
        }
        composable(Routes.CART) {
            CartRoute(onBack = { navController.popBackStack() })
        }
        composable(Routes.ORDERS) {
            OrderHistoryRoute(
                onBack = { navController.popBackStack() },
                onOpenOrder = { orderId -> navController.navigate(Routes.orderDetail(orderId)) },
            )
        }
        composable(
            route = Routes.ORDER_DETAIL,
            arguments = listOf(
                navArgument(OrderDetailViewModel.ORDER_ID_ARG) { type = NavType.LongType },
            ),
        ) {
            OrderDetailRoute(onBack = { navController.popBackStack() })
        }
    }
}
