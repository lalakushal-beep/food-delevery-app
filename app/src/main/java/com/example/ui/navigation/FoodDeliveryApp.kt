package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.auth.attemptAutoSignIn
import com.example.data.model.FoodCategory
import com.example.ui.components.DishDetailBottomSheet
import com.example.ui.components.FloatingCartBar
import com.example.ui.screens.AdminOrdersScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.CheckoutScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MenuScreen
import com.example.ui.screens.OrderConfirmationScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.viewmodel.FoodDeliveryViewModel
import kotlinx.coroutines.launch

object Routes {
    const val HOME = "home"
    const val MENU = "menu"
    const val CART = "cart"
    const val CHECKOUT = "checkout"
    const val ORDER_CONFIRMATION = "order_confirmation"
    const val ORDERS = "orders"
    const val ADMIN_ORDERS = "admin_orders"
}

private data class TopLevelDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val topLevelDestinations = listOf(
    TopLevelDestination(
        route = Routes.HOME,
        label = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
    ),
    TopLevelDestination(
        route = Routes.MENU,
        label = "Menu",
        selectedIcon = Icons.Filled.RestaurantMenu,
        unselectedIcon = Icons.Outlined.RestaurantMenu,
    ),
    TopLevelDestination(
        route = Routes.CART,
        label = "Cart",
        selectedIcon = Icons.Filled.ShoppingCart,
        unselectedIcon = Icons.Outlined.ShoppingCart,
    ),
    TopLevelDestination(
        route = Routes.ORDERS,
        label = "Orders",
        selectedIcon = Icons.Filled.ReceiptLong,
        unselectedIcon = Icons.Outlined.ReceiptLong,
    ),
)

@Composable
fun FoodDeliveryApp(
    viewModel: FoodDeliveryViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Routes.HOME
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val credentialManager = remember(context) { CredentialManager.create(context) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {},
            onUnauthenticated = {},
            scope = coroutineScope,
        )
    }

    val showTopLevelNav = currentRoute in setOf(
        Routes.HOME,
        Routes.MENU,
        Routes.CART,
        Routes.ORDERS,
    )
    val showFloatingCart = uiState.totalCartCount > 0 &&
        (currentRoute == Routes.HOME || currentRoute == Routes.MENU)

    fun navigateToTopLevel(route: String) {
        if (currentRoute == route) return
        navController.navigate(route) {
            popUpTo(Routes.HOME) {
                inclusive = false
            }
            launchSingleTop = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (showTopLevelNav && !useNavigationRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp,
                    ) {
                        topLevelDestinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTopLevel(dest.route) },
                                icon = {
                                    NavTabIcon(
                                        dest = dest,
                                        selected = selected,
                                        cartCount = uiState.totalCartCount,
                                    )
                                },
                                label = { Text(dest.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                ),
                                modifier = Modifier.testTag("nav_tab_${dest.route}"),
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (showTopLevelNav && useNavigationRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight(),
                    ) {
                        topLevelDestinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTopLevel(dest.route) },
                                icon = {
                                    NavTabIcon(
                                        dest = dest,
                                        selected = selected,
                                        cartCount = uiState.totalCartCount,
                                    )
                                },
                                label = { Text(dest.label) },
                                modifier = Modifier.testTag("rail_tab_${dest.route}"),
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    NavHost(
                        navController = navController,
                        startDestination = Routes.HOME,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    ) {
                        composable(Routes.HOME) {
                            HomeScreen(
                                uiState = uiState,
                                onSearchChange = viewModel::updateSearchQuery,
                                onVegOnlyChange = viewModel::toggleVegOnly,
                                onCategorySelected = { category ->
                                    viewModel.selectCategory(category)
                                    navigateToTopLevel(Routes.MENU)
                                },
                                onSeeAllMenu = {
                                    viewModel.selectCategory(FoodCategory.ALL)
                                    navigateToTopLevel(Routes.MENU)
                                },
                                onDishClick = viewModel::openDishDetail,
                                onAddToCart = { viewModel.addToCart(it) },
                                onIncrementInCart = viewModel::incrementMenuItem,
                                onDecrementFromCart = viewModel::decrementMenuItem,
                                onOrderHeroBanner = {
                                    viewModel.selectCategory(FoodCategory.THALI)
                                    navigateToTopLevel(Routes.MENU)
                                },
                                onOpenAdminPanel = { navController.navigate(Routes.ADMIN_ORDERS) },
                            )
                        }

                        composable(Routes.MENU) {
                            MenuScreen(
                                uiState = uiState,
                                onCategorySelected = viewModel::selectCategory,
                                onSearchChange = viewModel::updateSearchQuery,
                                onVegOnlyChange = viewModel::toggleVegOnly,
                                onSortOptionSelected = viewModel::selectSortOption,
                                onResetFilters = viewModel::resetFilters,
                                onDishClick = viewModel::openDishDetail,
                                onAddToCart = { viewModel.addToCart(it) },
                                onIncrementInCart = viewModel::incrementMenuItem,
                                onDecrementFromCart = viewModel::decrementMenuItem,
                            )
                        }

                        composable(Routes.CART) {
                            CartScreen(
                                uiState = uiState,
                                onIncrementItem = viewModel::incrementCartItem,
                                onDecrementItem = viewModel::decrementCartItem,
                                onRemoveItem = viewModel::removeCartItem,
                                onKitchenNoteChange = viewModel::updateKitchenNote,
                                onCouponInputChange = viewModel::updateCouponInput,
                                onApplyCoupon = viewModel::applyCoupon,
                                onRemoveCoupon = viewModel::removeCoupon,
                                onBrowseMenu = { navigateToTopLevel(Routes.MENU) },
                                onProceedToCheckout = { navController.navigate(Routes.CHECKOUT) },
                            )
                        }

                        composable(Routes.CHECKOUT) {
                            CheckoutScreen(
                                uiState = uiState,
                                onBack = { navController.popBackStack() },
                                onSelectSavedAddress = viewModel::selectSavedAddress,
                                onRecipientNameChange = viewModel::updateRecipientName,
                                onPhoneNumberChange = viewModel::updatePhoneNumber,
                                onAddressLineChange = viewModel::updateAddressLine,
                                onLandmarkChange = viewModel::updateLandmark,
                                onDeliveryInstructionSelect = viewModel::selectDeliveryInstruction,
                                onPaymentMethodSelect = viewModel::selectPaymentMethod,
                                onPlaceOrder = {
                                    viewModel.placeOrder {
                                        navController.navigate(Routes.ORDER_CONFIRMATION) {
                                            popUpTo(Routes.HOME) { inclusive = false }
                                            launchSingleTop = true
                                        }
                                    }
                                },
                            )
                        }

                        composable(Routes.ORDER_CONFIRMATION) {
                            OrderConfirmationScreen(
                                order = uiState.lastConfirmedOrder,
                                onTrackOrders = {
                                    navController.navigate(Routes.ORDERS) {
                                        popUpTo(Routes.HOME) { inclusive = false }
                                        launchSingleTop = true
                                    }
                                },
                                onBackToHome = {
                                    navController.navigate(Routes.HOME) {
                                        popUpTo(Routes.HOME) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                },
                            )
                        }

                        composable(Routes.ORDERS) {
                            OrdersScreen(
                                uiState = uiState,
                                onAdvanceOrderStatus = viewModel::advanceOrderStatus,
                                onReorder = { order ->
                                    viewModel.reorder(order) {
                                        navigateToTopLevel(Routes.CART)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Items added back to your cart!")
                                        }
                                    }
                                },
                                onBrowseMenu = { navigateToTopLevel(Routes.MENU) },
                                onOpenAdminOrders = { navController.navigate(Routes.ADMIN_ORDERS) },
                            )
                        }

                        composable(Routes.ADMIN_ORDERS) {
                            AdminOrdersScreen(
                                uiState = uiState,
                                onBack = { navController.popBackStack() },
                                onUpdateOrderStatus = viewModel::updateOrderStatus,
                            )
                        }
                    }

                    // Persistent Floating Cart Summary Bar on Home & Menu
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showFloatingCart,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut(),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                        ) {
                            FloatingCartBar(
                                itemCount = uiState.totalCartCount,
                                itemTotalInr = uiState.billSummary.itemTotalInr,
                                onViewCart = { navigateToTopLevel(Routes.CART) },
                            )
                        }
                    }
                }
            }
        }

        // Dish Detail Modal Bottom Sheet
        uiState.selectedDishForDetail?.let { dish ->
            DishDetailBottomSheet(
                dish = dish,
                onDismiss = viewModel::dismissDishDetail,
                onAddToCart = { selectedDish, choice ->
                    viewModel.addToCart(selectedDish, choice)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("${selectedDish.name} added to cart")
                    }
                },
            )
        }
    }
}

@Composable
private fun NavTabIcon(
    dest: TopLevelDestination,
    selected: Boolean,
    cartCount: Int,
) {
    val icon = if (selected) dest.selectedIcon else dest.unselectedIcon
    if (dest.route == Routes.CART && cartCount > 0) {
        BadgedBox(
            badge = {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Text("$cartCount")
                }
            }
        ) {
            Icon(imageVector = icon, contentDescription = dest.label)
        }
    } else {
        Icon(imageVector = icon, contentDescription = dest.label)
    }
}
