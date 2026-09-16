package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.CakeLoversDatabase
import com.example.data.repository.CakeLoversRepository
import com.example.ui.components.CakeLoversTopBar
import com.example.ui.navigation.Screen
import com.example.ui.screens.admin.AdminKitchenPortalScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.branches.BranchLocatorScreen
import com.example.ui.screens.branches.detectDeviceLocation
import com.example.ui.screens.cart.CartScreen
import com.example.ui.screens.catalog.CatalogScreen
import com.example.ui.screens.customstudio.CustomCakeStudioScreen
import com.example.ui.screens.offline.NoInternetScreen
import com.example.ui.screens.orders.OrderTrackerScreen
import com.example.ui.theme.BerryBurgundy
import com.example.ui.theme.CakeLoversTheme
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.GoldenCaramel
import com.example.ui.theme.TextGreySecondary
import com.example.ui.theme.TextWhitePrimary
import com.example.ui.viewmodel.CakeLoversViewModel
import com.example.ui.viewmodel.CakeLoversViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModelFactory = CakeLoversViewModelFactory(application)

        setContent {
            CakeLoversTheme {
                val appViewModel: CakeLoversViewModel = viewModel(factory = viewModelFactory)
                CakeLoversApp(viewModel = appViewModel)
            }
        }
    }
}

@Composable
fun CakeLoversApp(
    viewModel: CakeLoversViewModel
) {
    val isOnline by viewModel.isNetworkAvailable.collectAsStateWithLifecycle()

    if (!isOnline) {
        NoInternetScreen(
            onRetry = {
                val nowOnline = viewModel.checkNetworkConnection()
                if (nowOnline) {
                    viewModel.syncWithCloud()
                }
            }
        )
        return
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Catalog.route

    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val totalCartQuantity = cartItems.sumOf { it.quantity }
    val snackbarHostState = remember { SnackbarHostState() }
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val currentUserName = userProfile?.name?.split(" ")?.firstOrNull() ?: ""
    val context = LocalContext.current

    // Request Location Permission on App Install / Launch (for nearest kitchen branch proximity)
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val granted = fineGranted || coarseGranted
        viewModel.setLocationPermission(granted)
        if (granted) {
            detectDeviceLocation(context, viewModel)
        }
    }

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            viewModel.setLocationPermission(true)
            detectDeviceLocation(context, viewModel)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val bottomNavScreens = listOf(
        Screen.Catalog,
        Screen.CustomStudio,
        Screen.Branches,
        Screen.Cart,
        Screen.Orders
    )

    val currentScreenTitle = when {
        isAdmin -> "Kitchen Operations (Ops)"
        currentRoute == Screen.Catalog.route -> "Cake Lovers"
        currentRoute == Screen.CustomStudio.route -> "Custom Cake"
        currentRoute == Screen.Branches.route -> "Artisan Kitchens"
        currentRoute == Screen.Cart.route -> "Celebration Cart"
        currentRoute == Screen.Orders.route -> "Order Lifecycle"
        currentRoute == Screen.Login.route -> "Sign In"
        currentRoute == Screen.AdminPortal.route -> "Kitchen Operations (Ops)"
        else -> "Cake Lovers"
    }

    val currentSubtitle = when {
        isAdmin -> "Live Queue & Kitchen Management"
        currentRoute == Screen.Catalog.route -> "Indo-Western Confectionery"
        currentRoute == Screen.CustomStudio.route -> "Custom Cake Designer & Live Piping"
        currentRoute == Screen.Branches.route -> "Express Baking Branches"
        currentRoute == Screen.Cart.route -> "Free Celebration Kit Included"
        currentRoute == Screen.Orders.route -> "5-Stage Live Progression"
        currentRoute == Screen.Login.route -> "Browse cakes freely or sign in"
        currentRoute == Screen.AdminPortal.route -> "Live Queue & Kitchen Management"
        else -> "Artisanal Confectionery Studio"
    }

    val isAdminRoute = currentRoute == Screen.AdminPortal.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (currentRoute != Screen.Catalog.route || isAdmin) {
                CakeLoversTopBar(
                    title = currentScreenTitle,
                    subtitle = currentSubtitle,
                    showBack = !isAdminRoute && !isAdmin,
                    cartItemCount = totalCartQuantity,
                    isAdminActive = isAdminRoute || isAdmin,
                    isAdminAuthorized = isAdmin,
                    isLoggedIn = isLoggedIn,
                    userName = currentUserName,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onLoginClick = {
                        if (isLoggedIn) {
                            if (currentRoute != Screen.Catalog.route) {
                                navController.navigate(Screen.Catalog.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        } else {
                            if (currentRoute != Screen.Login.route) {
                                navController.navigate(Screen.Login.route) {
                                    launchSingleTop = true
                                }
                            }
                        }
                    },
                    onCartClick = {
                        if (currentRoute != Screen.Cart.route) {
                            navController.navigate(Screen.Cart.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    onAdminToggleClick = {
                        if (isAdminRoute) {
                            navController.popBackStack()
                        } else {
                            navController.navigate(Screen.AdminPortal.route)
                        }
                    },
                    onLogoutClick = {
                        viewModel.logout()
                    }
                )
            }
        },
        bottomBar = {
            if (!isAdmin && !isAdminRoute) {
                NavigationBar(
                    containerColor = DarkCardSurface,
                    contentColor = TextWhitePrimary,
                    modifier = Modifier.testTag("main_bottom_navigation")
                ) {
                    bottomNavScreens.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                if (screen == Screen.Cart && totalCartQuantity > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = BerryBurgundy,
                                                contentColor = Color.White
                                            ) {
                                                Text(totalCartQuantity.toString())
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                            contentDescription = screen.title
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                        contentDescription = screen.title
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BerryBurgundy,
                                selectedTextColor = GoldenCaramel,
                                unselectedIconColor = TextGreySecondary,
                                unselectedTextColor = TextGreySecondary,
                                indicatorColor = DarkCardElevated
                            ),
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Catalog.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Catalog.route) {
                CatalogScreen(
                    viewModel = viewModel,
                    onNavigateToCustomStudio = {
                        navController.navigate(Screen.CustomStudio.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    snackbarHostState = snackbarHostState,
                    onNavigateToLogin = {
                        if (currentRoute != Screen.Login.route) {
                            navController.navigate(Screen.Login.route) {
                                launchSingleTop = true
                            }
                        }
                    },
                    onNavigateToCart = {
                        if (currentRoute != Screen.Cart.route) {
                            navController.navigate(Screen.Cart.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    onNavigateToAdmin = {
                        navController.navigate(Screen.AdminPortal.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.CustomStudio.route) {
                CustomCakeStudioScreen(
                    viewModel = viewModel,
                    onNavigateToCart = {
                        navController.navigate(Screen.Cart.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(Screen.Branches.route) {
                BranchLocatorScreen(
                    viewModel = viewModel
                )
            }

            composable(Screen.Cart.route) {
                CartScreen(
                    viewModel = viewModel,
                    onNavigateToMenu = {
                        navController.navigate(Screen.Catalog.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onOrderPlaced = { orderId ->
                        navController.navigate(Screen.Orders.route) {
                            popUpTo(Screen.Catalog.route)
                            launchSingleTop = true
                        }
                    },
                    onOrderPlacedAsGuest = { orderId ->
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Catalog.route)
                            launchSingleTop = true
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route)
                    }
                )
            }

            composable(Screen.Orders.route) {
                OrderTrackerScreen(
                    viewModel = viewModel
                )
            }

            composable(Screen.Login.route) {
                if (isLoggedIn) {
                    LaunchedEffect(isAdmin) {
                        val destination = if (isAdmin) Screen.AdminPortal.route else Screen.Catalog.route
                        val popped = navController.popBackStack(destination, inclusive = false)
                        if (!popped) {
                            navController.navigate(destination) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                    }
                } else {
                    LoginScreen(
                        viewModel = viewModel,
                        snackbarHostState = snackbarHostState,
                        onNavigateToCatalog = {
                            val destination = if (isAdmin) Screen.AdminPortal.route else Screen.Catalog.route
                            val popped = navController.popBackStack(destination, inclusive = false)
                            if (!popped) {
                                navController.navigate(destination) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            }
                        },
                        onNavigateToOrders = {
                            navController.navigate(Screen.Orders.route) {
                                popUpTo(Screen.Catalog.route)
                                launchSingleTop = true
                            }
                        },
                        onNavigateToAdmin = {
                            navController.navigate(Screen.AdminPortal.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            composable(Screen.AdminPortal.route) {
                AdminKitchenPortalScreen(
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState,
                    onSignOut = {
                        viewModel.logout()
                        navController.navigate(Screen.Catalog.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
