package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Catalog : Screen(
        route = "catalog",
        title = "Browse Cakes",
        selectedIcon = Icons.Filled.Cake,
        unselectedIcon = Icons.Outlined.Cake
    )

    data object CustomStudio : Screen(
        route = "custom_studio",
        title = "Custom Cake",
        selectedIcon = Icons.Filled.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome
    )

    data object Branches : Screen(
        route = "branches",
        title = "Kitchens",
        selectedIcon = Icons.Filled.Place,
        unselectedIcon = Icons.Outlined.Place
    )

    data object Cart : Screen(
        route = "cart",
        title = "Cart",
        selectedIcon = Icons.Filled.ShoppingBag,
        unselectedIcon = Icons.Outlined.ShoppingBag
    )

    data object Orders : Screen(
        route = "orders",
        title = "Tracking",
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History
    )

    data object Login : Screen(
        route = "login",
        title = "Sign In",
        selectedIcon = Icons.Filled.AccountCircle,
        unselectedIcon = Icons.Outlined.AccountCircle
    )

    data object AdminPortal : Screen(
        route = "admin_portal",
        title = "Admin Ops",
        selectedIcon = Icons.Filled.AdminPanelSettings,
        unselectedIcon = Icons.Outlined.AdminPanelSettings
    )
}
