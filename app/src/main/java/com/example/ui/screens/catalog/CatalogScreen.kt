package com.example.ui.screens.catalog

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.ui.screens.admin.AdminKitchenPortalScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.BranchEntity
import com.example.data.model.CakeCategory
import com.example.data.model.CakeItemEntity
import com.example.data.model.FulfillmentType
import com.example.data.model.UserProfileEntity
import com.example.ui.components.CakeThumbnailImage
import com.example.ui.components.GlutenFreeBadge
import com.example.ui.components.LeadTimeBadge
import com.example.ui.components.NutFreeBadge
import com.example.ui.components.PureVegIcon
import com.example.ui.components.VegEgglessBadge
import com.example.ui.theme.BerryBurgundy
import com.example.ui.theme.DarkAmber
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkCanvasBackground
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.DarkSearchField
import com.example.ui.theme.GoldenCaramel
import com.example.ui.theme.HoneyButter
import com.example.ui.theme.PistachioGreen
import com.example.ui.theme.RoseContainer
import com.example.ui.theme.TextGreySecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhitePrimary
import com.example.ui.theme.WarmRose
import com.example.ui.viewmodel.CakeLoversViewModel
import com.example.ui.viewmodel.SortOption
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CatalogScreen(
    viewModel: CakeLoversViewModel,
    onNavigateToCustomStudio: () -> Unit,
    snackbarHostState: SnackbarHostState,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    if (isAdmin) {
        AdminKitchenPortalScreen(
            viewModel = viewModel,
            snackbarHostState = snackbarHostState,
            onSignOut = {
                viewModel.logout()
            },
            modifier = modifier.fillMaxSize()
        )
        return
    }

    val cakes by viewModel.filteredCakes.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isEgglessOnly by viewModel.isEgglessOnly.collectAsStateWithLifecycle()
    val isGlutenFreeOnly by viewModel.isGlutenFreeOnly.collectAsStateWithLifecycle()
    val isNutFreeOnly by viewModel.isNutFreeOnly.collectAsStateWithLifecycle()
    val sortBy by viewModel.sortBy.collectAsStateWithLifecycle()
    val selectedCakeDetail by viewModel.selectedCakeDetail.collectAsStateWithLifecycle()
    val userLocationName by viewModel.userLocationName.collectAsStateWithLifecycle()
    val selectedBranch by viewModel.selectedBranch.collectAsStateWithLifecycle()
    val hasLocationPermission by viewModel.hasLocationPermission.collectAsStateWithLifecycle()
    val isDetectingLocation by viewModel.isDetectingLocation.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val totalCartQuantity = cartItems.sumOf { it.quantity }
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val fulfillmentType by viewModel.fulfillmentType.collectAsStateWithLifecycle()
    val allBranches by viewModel.allBranches.collectAsStateWithLifecycle()
    val userLat by viewModel.userLatitude.collectAsStateWithLifecycle()
    val userLon by viewModel.userLongitude.collectAsStateWithLifecycle()
    var branchSearchQuery by remember { mutableStateOf("") }

    val sortedBranches = remember(allBranches, userLat, userLon, branchSearchQuery) {
        val filtered = if (branchSearchQuery.isBlank()) {
            allBranches
        } else {
            allBranches.filter {
                it.name.contains(branchSearchQuery, ignoreCase = true) ||
                it.address.contains(branchSearchQuery, ignoreCase = true)
            }
        }
        filtered.sortedBy { viewModel.calculateDistanceKm(it.latitude, it.longitude) }
    }

    val context = LocalContext.current
    var showSortMenu by remember { mutableStateOf(false) }
    var showLocationPickerSheet by remember { mutableStateOf(false) }
    var showUserAccountDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val locationSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    val prefs = remember { context.getSharedPreferences("cake_lovers_prefs", Context.MODE_PRIVATE) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val granted = fine || coarse
        viewModel.setLocationPermission(granted)
        if (granted) {
            viewModel.detectDeviceLocation(context) { locName ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("📍 Delivering to: $locName")
                }
            }
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("📍 Location permission skipped. Tap GPS anytime to enable live tracking.")
            }
        }
    }

    LaunchedEffect(Unit) {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (fine || coarse) {
            viewModel.setLocationPermission(true)
            viewModel.detectDeviceLocation(context)
        } else {
            // First time install: ask for location permission automatically
            val hasAskedFirstTime = prefs.getBoolean("has_asked_first_time_location", false)
            if (!hasAskedFirstTime) {
                prefs.edit().putBoolean("has_asked_first_time_location", true).apply()
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("catalog_screen"),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        // 0. Top Location Bar & Fulfillment Selector (Pick from Shop or Delivery)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("top_location_delivery_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                border = BorderStroke(1.dp, DarkBorderSubtle)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Top Row: Location Icon + Location / Branch Name + Action Icons (Admin, Cart, Profile)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Location Row clickable
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showLocationPickerSheet = true },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(BerryBurgundy.copy(alpha = 0.22f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Current Location",
                                    tint = BerryBurgundy,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (fulfillmentType == FulfillmentType.PICKUP) "PICK UP FROM" else "DELIVERING TO",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = BerryBurgundy,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 10.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Change Location",
                                        tint = BerryBurgundy,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = if (fulfillmentType == FulfillmentType.PICKUP) {
                                        selectedBranch?.name ?: "Indiranagar Flagship Studio"
                                    } else {
                                        userLocationName
                                    },
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhitePrimary
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // GPS Refresh or Enable Button
                        if (!hasLocationPermission) {
                            Button(
                                onClick = {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BerryBurgundy),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(30.dp)
                                    .testTag("enable_location_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("GPS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    viewModel.detectDeviceLocation(context) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("GPS updated: $it")
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("refresh_gps_button")
                            ) {
                                if (isDetectingLocation) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = BerryBurgundy
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = "Refresh GPS",
                                        tint = BerryBurgundy,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Cloud Sync Button
                        IconButton(
                            onClick = {
                                viewModel.syncWithCloud {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("☁️ Pushed cakes, orders & branches to Firebase Firestore! Check Data tab in Console.")
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("top_cloud_sync_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = "Sync to Firebase",
                                tint = BerryBurgundy,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Optional Kitchen Ops chip for verified Admin
                        if (isAdmin) {
                            FilterChip(
                                selected = true,
                                onClick = onNavigateToAdmin,
                                label = {
                                    Text("Ops", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BerryBurgundy,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .padding(start = 2.dp, end = 2.dp)
                                    .height(28.dp)
                                    .testTag("discover_admin_chip")
                            )
                        }

                        // Cart Button with Badge
                        IconButton(
                            onClick = onNavigateToCart,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("discover_cart_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (totalCartQuantity > 0) {
                                        Badge(
                                            containerColor = BerryBurgundy,
                                            contentColor = Color.White
                                        ) {
                                            Text(totalCartQuantity.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = "Shopping Bag",
                                    tint = TextWhitePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(2.dp))

                        // User Profile Avatar
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(DarkCardSurface)
                                .border(1.5.dp, BerryBurgundy, CircleShape)
                                .clickable {
                                    if (isLoggedIn) {
                                        showUserAccountDialog = true
                                    } else {
                                        onNavigateToLogin()
                                    }
                                }
                                .testTag("discover_user_avatar"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoggedIn && userProfile != null && userProfile!!.name.isNotBlank()) {
                                Text(
                                    text = userProfile!!.name.take(1).uppercase(),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "User Profile",
                                    tint = WarmRose,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Two Options: Delivery and Pick from Shop Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkCanvasBackground)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Delivery Option
                        val isDeliverySelected = fulfillmentType == FulfillmentType.DELIVERY
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDeliverySelected) BerryBurgundy else Color.Transparent)
                                .clickable {
                                    viewModel.setFulfillmentType(FulfillmentType.DELIVERY)
                                }
                                .padding(vertical = 9.dp)
                                .testTag("fulfillment_delivery_option"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeliveryDining,
                                    contentDescription = null,
                                    tint = if (isDeliverySelected) Color.White else TextGreySecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Delivery",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isDeliverySelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isDeliverySelected) Color.White else TextGreySecondary
                                    )
                                )
                            }
                        }

                        // Pick from Shop Option
                        val isPickupSelected = fulfillmentType == FulfillmentType.PICKUP
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isPickupSelected) BerryBurgundy else Color.Transparent)
                                .clickable {
                                    viewModel.setFulfillmentType(FulfillmentType.PICKUP)
                                }
                                .padding(vertical = 9.dp)
                                .testTag("fulfillment_pickup_option"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = if (isPickupSelected) Color.White else TextGreySecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Pick from Shop",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isPickupSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isPickupSelected) Color.White else TextGreySecondary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 1. Permission Reminder Banner (shown if location permission was not granted)
        if (!hasLocationPermission) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("location_permission_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                    border = BorderStroke(1.dp, BerryBurgundy.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(BerryBurgundy.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = BerryBurgundy,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable Location for Express Delivery",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhitePrimary
                                )
                            )
                            Text(
                                text = "Find nearest kitchen & get accurate delivery time",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextGreySecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BerryBurgundy),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("banner_allow_location_button")
                        ) {
                            Text("Allow", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (fulfillmentType == FulfillmentType.PICKUP) {
            // ==========================================
            // PICK FROM SHOP: SHOW BRANCHES DETAILS ONLY
            // ==========================================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Bakery Branches for Pickup",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = TextWhitePrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Select an artisan studio to collect your fresh bakery order",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextGreySecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkCardElevated)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${sortedBranches.size} Kitchens",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = GoldenCaramel,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            // Branch Search Field
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    OutlinedTextField(
                        value = branchSearchQuery,
                        onValueChange = { branchSearchQuery = it },
                        placeholder = {
                            Text(
                                text = "Search branches by name, area, road...",
                                color = TextGreySecondary,
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search branches",
                                tint = TextGreySecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (branchSearchQuery.isNotBlank()) {
                                IconButton(onClick = { branchSearchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear search",
                                        tint = TextGreySecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("catalog_branch_search_input"),
                        shape = RoundedCornerShape(26.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkBorderSubtle,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = GoldenCaramel,
                            focusedTextColor = TextWhitePrimary,
                            unfocusedTextColor = TextWhitePrimary,
                            unfocusedContainerColor = DarkSearchField,
                            focusedContainerColor = DarkSearchField
                        ),
                        singleLine = true
                    )
                }
            }

            if (sortedBranches.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🏬", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No bakery branches found",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhitePrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try searching for a different locality or street",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextGreySecondary)
                            )
                        }
                    }
                }
            } else {
                items(sortedBranches, key = { it.id }) { branch ->
                    val isSelected = selectedBranch?.id == branch.id
                    val distanceKm = viewModel.calculateDistanceKm(branch.latitude, branch.longitude)

                    CatalogBranchDetailCard(
                        branch = branch,
                        distanceKm = distanceKm,
                        isSelected = isSelected,
                        onSelect = {
                            viewModel.selectBranch(branch)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("📍 Selected ${branch.name} for pickup")
                            }
                        },
                        onCall = {
                            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${branch.phone.replace(" ", "")}")
                            }
                            context.startActivity(dialIntent)
                        },
                        onDirections = {
                            val mapUri = Uri.parse("geo:${branch.latitude},${branch.longitude}?q=${Uri.encode(branch.name)}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
                            context.startActivity(mapIntent)
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        } else {
            // ==========================================
            // DELIVERY: SHOW CAKE CATALOG
            // ==========================================
            // 2. Pill Search Bar (matching screenshot "Search shop")
            item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = {
                        Text(
                            text = "Search shop, cakes, flavors...",
                            color = TextGreySecondary,
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextGreySecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = TextGreySecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Sort options",
                                    tint = TextGreySecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("catalog_search_input"),
                    shape = RoundedCornerShape(26.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBorderSubtle,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = GoldenCaramel,
                        focusedTextColor = TextWhitePrimary,
                        unfocusedTextColor = TextWhitePrimary,
                        unfocusedContainerColor = DarkSearchField,
                        focusedContainerColor = DarkSearchField
                    ),
                    singleLine = true
                )

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false },
                    modifier = Modifier.background(DarkCardElevated)
                ) {
                    SortOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.label,
                                    fontWeight = if (sortBy == option) FontWeight.Bold else FontWeight.Normal,
                                    color = if (sortBy == option) GoldenCaramel else TextWhitePrimary
                                )
                            },
                            onClick = {
                                viewModel.setSortBy(option)
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        }

        // Popular Cakes Carousel
        item {
            val popularCakes = remember(cakes) {
                val best = cakes.filter { it.isBestSeller || it.isChefSpecial }
                if (best.isNotEmpty()) best else cakes.take(5)
            }

            if (popularCakes.isNotEmpty()) {
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Popular Cakes",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = TextWhitePrimary
                            )
                        )
                        Text(
                            text = "See all",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextGreySecondary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            ),
                            modifier = Modifier
                                .clickable { viewModel.setCategory(CakeCategory.ALL) }
                                .padding(4.dp)
                        )
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        items(popularCakes, key = { "pop_${it.id}" }) { cake ->
                            Card(
                                modifier = Modifier
                                    .width(170.dp)
                                    .clickable { viewModel.selectCakeForDetail(cake) }
                                    .testTag("popular_cake_${cake.id}"),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                                border = BorderStroke(1.dp, DarkBorderSubtle)
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                                    ) {
                                        CakeThumbnailImage(
                                            cake = cake,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        // Rating pill
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(8.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.Black.copy(alpha = 0.65f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFFD54F),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "${cake.rating}",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = cake.name,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextWhitePrimary,
                                                fontSize = 14.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "₹${cake.basePrice.toInt()}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = GoldenCaramel,
                                                fontSize = 16.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bakery Menu Section Title
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Bakery Menu",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextWhitePrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkCardElevated)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${cakes.size} items",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GoldenCaramel,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // 8. Category Chips
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                items(CakeCategory.entries.toTypedArray()) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setCategory(category) },
                        label = {
                            Text(
                                text = "${category.iconEmoji} ${category.displayName}",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BerryBurgundy,
                            selectedLabelColor = Color.White,
                            containerColor = DarkCardSurface,
                            labelColor = TextGreySecondary
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("category_chip_${category.name}")
                    )
                }
            }
        }

        // 9. Dietary Allergen Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = isEgglessOnly,
                    onClick = { viewModel.toggleEgglessOnly() },
                    leadingIcon = { PureVegIcon() },
                    label = { Text("Pure Veg", fontSize = 11.sp) },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BerryBurgundy,
                        selectedLabelColor = Color.White,
                        containerColor = DarkCardSurface,
                        labelColor = TextGreySecondary
                    ),
                    modifier = Modifier.testTag("filter_eggless_chip")
                )

                FilterChip(
                    selected = isGlutenFreeOnly,
                    onClick = { viewModel.toggleGlutenFreeOnly() },
                    label = { Text("Gluten-Free", fontSize = 11.sp) },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BerryBurgundy,
                        selectedLabelColor = Color.White,
                        containerColor = DarkCardSurface,
                        labelColor = TextGreySecondary
                    ),
                    modifier = Modifier.testTag("filter_gluten_free_chip")
                )

                FilterChip(
                    selected = isNutFreeOnly,
                    onClick = { viewModel.toggleNutFreeOnly() },
                    label = { Text("Nut-Free", fontSize = 11.sp) },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BerryBurgundy,
                        selectedLabelColor = Color.White,
                        containerColor = DarkCardSurface,
                        labelColor = TextGreySecondary
                    ),
                    modifier = Modifier.testTag("filter_nut_free_chip")
                )
            }
        }

        // Cake List Items
        if (cakes.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🎂", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No cakes match your filters",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Try clearing allergen filters or search terms",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }
        } else {
            items(cakes, key = { it.id }) { cake ->
                CakeItemCard(
                    cake = cake,
                    onClick = { viewModel.selectCakeForDetail(cake) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}

    // Modal Bottom Sheet for deep dive & quick ordering
    selectedCakeDetail?.let { cake ->
        CakeDetailSheet(
            cake = cake,
            sheetState = sheetState,
            onDismiss = { viewModel.selectCakeForDetail(null) },
            onAddToCart = { weightKg, pipingText ->
                viewModel.addCatalogCakeToCart(cake, weightKg, pipingText) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Added ${cake.name} ($weightKg kg) to cart!")
                    }
                }
            }
        )
    }

    // Modal Bottom Sheet for Location Selection & GPS
    if (showLocationPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLocationPickerSheet = false },
            sheetState = locationSheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (fulfillmentType == FulfillmentType.PICKUP) "Select Pickup Branch" else "Delivery Location",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = BerryBurgundy
                        )
                    )
                    TextButton(onClick = { showLocationPickerSheet = false }) {
                        Text("Done", color = BerryBurgundy, fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    text = if (fulfillmentType == FulfillmentType.PICKUP) {
                        "Choose the bakery kitchen studio where you want to collect your confectionery order."
                    } else {
                        "Select your locality or use GPS to route your order to the nearest live oven studio."
                    },
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (fulfillmentType == FulfillmentType.PICKUP) {
                    Text(
                        text = "AVAILABLE BAKERY KITCHENS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = BerryBurgundy,
                            letterSpacing = 0.5.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    sortedBranches.forEach { branch ->
                        val isSelected = selectedBranch?.id == branch.id
                        val distanceKm = viewModel.calculateDistanceKm(branch.latitude, branch.longitude)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    viewModel.selectBranch(branch)
                                    showLocationPickerSheet = false
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("📍 Selected: ${branch.name}")
                                    }
                                }
                                .testTag("sheet_branch_${branch.id}"),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) HoneyButter else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = if (isSelected) DarkAmber else BerryBurgundy,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = branch.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = branch.address,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "🕒 ${branch.operatingHours}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.5.sp
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${String.format(java.util.Locale.getDefault(), "%.1f", distanceKm)} km",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = BerryBurgundy
                                        )
                                    )
                                    if (isSelected) {
                                        Text(
                                            text = "Selected",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = PistachioGreen,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // GPS Detection Button
                    Button(
                        onClick = {
                            if (hasLocationPermission) {
                                viewModel.detectDeviceLocation(context) { locName ->
                                    showLocationPickerSheet = false
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("📍 Delivering to: $locName")
                                    }
                                }
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BerryBurgundy),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("sheet_use_gps_button")
                    ) {
                        Icon(imageVector = Icons.Default.MyLocation, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isDetectingLocation) "Detecting Device GPS..." else "Use Current Device Location (GPS)",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "POPULAR BAKERY LOCALITIES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = BerryBurgundy,
                            letterSpacing = 0.5.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    viewModel.presetLocations.forEach { preset ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    viewModel.selectPresetLocation(preset)
                                    showLocationPickerSheet = false
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("📍 Selected: ${preset.title}")
                                    }
                                }
                                .testTag("preset_location_${preset.city.lowercase()}"),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (userLocationName == preset.title) HoneyButter else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = if (userLocationName == preset.title) DarkAmber else BerryBurgundy,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = preset.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = preset.address,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = preset.city,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = BerryBurgundy
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showUserAccountDialog && isLoggedIn) {
        val user = userProfile ?: UserProfileEntity()
        AlertDialog(
            onDismissRequest = { showUserAccountDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(RoseContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isAdmin) "👨‍🍳" else "🎂",
                        fontSize = 26.sp
                    )
                }
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isAdmin) "Admin • Kitchen Manager" else user.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = BerryBurgundy
                        ),
                        textAlign = TextAlign.Center
                    )
                    if (user.email.isNotBlank()) {
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (user.deliveryAddress.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Delivery Address:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = user.deliveryAddress,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.logout()
                            showUserAccountDialog = false
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Signed out. Switched to Guest Mode.")
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("account_sign_out_dialog_btn")
                    ) {
                        Text("Sign Out (Guest Mode)")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showUserAccountDialog = false }) {
                    Text("Close", color = BerryBurgundy, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CakeItemCard(
    cake: CakeItemEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("cake_card_${cake.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
        border = BorderStroke(1.dp, DarkBorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                CakeThumbnailImage(
                    cake = cake,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )

                // Top Floating Badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (cake.isChefSpecial) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(BerryBurgundy)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Chef's Special",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    } else if (cake.isBestSeller) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkAmber)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "⭐ Best Seller",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    // Rating Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.65f))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${cake.rating}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = cake.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextWhitePrimary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = cake.tagline,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextGreySecondary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₹${cake.basePrice.toInt()}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = GoldenCaramel,
                                fontSize = 17.sp
                            )
                        )
                        Text(
                            text = "for ${cake.defaultWeightKg} kg",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextGreySecondary,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Badges
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (cake.isEggless) VegEgglessBadge()
                    if (cake.isGlutenFree) GlutenFreeBadge()
                    if (cake.isNutFree) NutFreeBadge()
                    LeadTimeBadge(leadTimeHours = cake.leadTimeHours)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actions row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Notes: ${cake.flavorNotes}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextGreySecondary.copy(alpha = 0.85f),
                            fontSize = 11.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BerryBurgundy),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("customize_order_button_${cake.id}")
                    ) {
                        Text(
                            text = "Order & Customize",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CatalogBranchDetailCard(
    branch: BranchEntity,
    distanceKm: Double,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onCall: () -> Unit,
    onDirections: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("catalog_branch_card_${branch.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) DarkCardElevated else DarkCardSurface
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) GoldenCaramel else DarkBorderSubtle
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Store icon, Branch Name, Selection Pill / Proximity
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) GoldenCaramel.copy(alpha = 0.2f) else BerryBurgundy.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = if (isSelected) GoldenCaramel else WarmRose,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = branch.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhitePrimary,
                            fontSize = 16.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Rating Pill
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${branch.rating}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhitePrimary,
                                    fontSize = 12.sp
                                )
                            )
                        }

                        Text(
                            text = "•",
                            color = TextGreySecondary,
                            fontSize = 11.sp
                        )

                        // Distance Pill
                        Text(
                            text = "${String.format(java.util.Locale.getDefault(), "%.1f", distanceKm)} km away",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = GoldenCaramel,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PistachioGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = PistachioGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Selected",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PistachioGreen,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Address Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = TextGreySecondary,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(top = 1.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = branch.address,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextGreySecondary,
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Operating Hours Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = TextGreySecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = branch.operatingHours,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextWhitePrimary.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pickup speed & service badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkCanvasBackground)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚡ Counter Pickup: Ready in 45-60 mins",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PistachioGreen,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp
                        )
                    )
                    Text(
                        text = "Free Pickup",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GoldenCaramel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Actions: Select for Pickup (or Active) + Call + Directions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onSelect,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("branch_select_button_${branch.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) GoldenCaramel else BerryBurgundy
                    )
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Storefront,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = if (isSelected) Color.Black else Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSelected) "Active Pickup Branch" else "Select for Pickup",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isSelected) Color.Black else Color.White
                        )
                    )
                }

                // Phone button
                IconButton(
                    onClick = onCall,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkCanvasBackground)
                        .border(1.dp, DarkBorderSubtle, RoundedCornerShape(10.dp))
                        .testTag("branch_call_button_${branch.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Call ${branch.name}",
                        tint = WarmRose,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Directions button
                IconButton(
                    onClick = onDirections,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkCanvasBackground)
                        .border(1.dp, DarkBorderSubtle, RoundedCornerShape(10.dp))
                        .testTag("branch_directions_button_${branch.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Directions,
                        contentDescription = "Directions to ${branch.name}",
                        tint = GoldenCaramel,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
