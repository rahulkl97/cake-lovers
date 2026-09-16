package com.example.ui.screens.cart

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.CartItemEntity
import com.example.data.model.DeliverySlot
import com.example.data.model.FulfillmentType
import com.example.data.model.PaymentMethod
import com.example.data.model.UserProfileEntity
import com.example.ui.components.CelebrationKitBanner
import com.example.ui.theme.BerryBurgundy
import com.example.ui.theme.DarkAmber
import com.example.ui.theme.GoldenCaramel
import com.example.ui.theme.HoneyButter
import com.example.ui.theme.PistachioGreen
import com.example.ui.theme.RoseContainer
import com.example.ui.viewmodel.CakeLoversViewModel
import java.util.Locale

@Composable
fun CartScreen(
    viewModel: CakeLoversViewModel,
    onNavigateToMenu: () -> Unit,
    onOrderPlaced: (String) -> Unit,
    onOrderPlacedAsGuest: (String) -> Unit = onOrderPlaced,
    onNavigateToLogin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val fulfillmentType by viewModel.fulfillmentType.collectAsStateWithLifecycle()
    val deliverySlot by viewModel.deliverySlot.collectAsStateWithLifecycle()
    val paymentMethod by viewModel.paymentMethod.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val selectedBranch by viewModel.selectedBranch.collectAsStateWithLifecycle()
    val allBranches by viewModel.allBranches.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val guestName by viewModel.guestName.collectAsStateWithLifecycle()
    val guestPhone by viewModel.guestPhone.collectAsStateWithLifecycle()
    val guestAddress by viewModel.guestAddress.collectAsStateWithLifecycle()
    val userLocationName by viewModel.userLocationName.collectAsStateWithLifecycle()
    val hasLocationPermission by viewModel.hasLocationPermission.collectAsStateWithLifecycle()
    val isDetectingLocation by viewModel.isDetectingLocation.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val granted = fine || coarse
        viewModel.setLocationPermission(granted)
        if (granted) {
            viewModel.detectDeviceLocation(context)
        }
    }

    val subtotal = cartItems.sumOf { it.totalAmount }
    val deliveryFee = if (fulfillmentType == FulfillmentType.DELIVERY) deliverySlot.surcharge.toDouble() else 0.0
    val grandTotal = (subtotal + deliveryFee).coerceAtLeast(0.0)

    if (cartItems.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp)
                .testTag("empty_cart_view"),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🛒", fontSize = 56.sp)
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Your Confectionery Cart is Empty",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Browse our Indo-Western fusion cakes or craft a custom tiered masterpiece in the studio!",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    ),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onNavigateToMenu,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BerryBurgundy),
                    modifier = Modifier.testTag("browse_menu_from_cart_button")
                ) {
                    Icon(imageVector = Icons.Default.Cake, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Explore Artisanal Menu")
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("cart_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Cart Header & Clear Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Celebration Cart (${cartItems.size})",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = "Clear All",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .clickable { viewModel.clearCart() }
                        .padding(4.dp)
                        .testTag("clear_cart_button")
                )
            }
        }

        // Guest Notice Banner (When browsing without login)
        if (!isLoggedIn) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("guest_checkout_notice_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = HoneyButter)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "✨ Guest Checkout Mode",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DarkAmber
                                )
                            )
                            Text(
                                text = "Ordering without signing in. Or sign in to use saved addresses.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = DarkAmber.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onNavigateToLogin,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkAmber),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("cart_sign_in_button")
                        ) {
                            Text(
                                text = "Sign In",
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

        // Cart Items List
        items(cartItems, key = { it.cartId }) { item ->
            CartItemRow(
                item = item,
                onRemove = { viewModel.removeCartItem(item.cartId) }
            )
        }

        // Complimentary Celebration Kit Value-Add Banner
        item {
            CelebrationKitBanner(isCartPage = true)
        }

        // Delivery Slot Scheduling Selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Delivery Slot Scheduling",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    listOf(
                        DeliverySlot.STANDARD to ("Standard Daytime Delivery" to "Delivered in 2-4 hours • ₹0"),
                        DeliverySlot.EVENING to ("Evening Celebration" to "06:00 PM – 08:30 PM • ₹0"),
                        DeliverySlot.MIDNIGHT_SURPRISE to ("Midnight Surprise Delivery ✨" to "11:15 PM – 11:55 PM (Strict Window) • +₹149")
                    ).forEach { (slot, textPair) ->
                        val isSelected = deliverySlot == slot
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .clickable { viewModel.setDeliverySlot(slot) }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .testTag("delivery_slot_${slot.name}")
                        ) {
                            Icon(
                                imageVector = if (slot == DeliverySlot.MIDNIGHT_SURPRISE) Icons.Default.Nightlight else Icons.Default.Schedule,
                                contentDescription = null,
                                tint = if (isSelected) BerryBurgundy else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = textPair.first,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = textPair.second,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = BerryBurgundy,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }

        // Delivery Recipient & Address Details / Store Pickup Details
        item {
            if (fulfillmentType == FulfillmentType.PICKUP) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pickup_recipient_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Store Pickup Details",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = "Self-Pickup",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PistachioGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        val currentBranch = selectedBranch ?: allBranches.firstOrNull()
                        if (currentBranch != null) {
                            val dist = viewModel.calculateDistanceKm(currentBranch.latitude, currentBranch.longitude)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = currentBranch.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${String.format(Locale.getDefault(), "%.1f", dist)} km away",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = GoldenCaramel,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = currentBranch.address,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = currentBranch.operatingHours,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 11.5.sp
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Phone,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = currentBranch.phone,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    }
                                }
                            }

                            // Quick Branch Switcher Chips
                            if (allBranches.size > 1) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Switch Pickup Branch:",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(allBranches) { br ->
                                        FilterChip(
                                            selected = currentBranch.id == br.id,
                                            onClick = { viewModel.selectBranch(br) },
                                            label = { Text(br.name.split(" ").firstOrNull() ?: br.name, fontSize = 11.sp) },
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Pickup Recipient Information",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (isLoggedIn && userProfile != null) {
                            val user = userProfile!!
                            Text(
                                text = "Pickup Person: ${user.name} • ${user.phone}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        } else {
                            OutlinedTextField(
                                value = guestName,
                                onValueChange = { viewModel.updateGuestDetails(it, guestPhone, guestAddress) },
                                label = { Text("Name of person picking up") },
                                placeholder = { Text("e.g. Aarti Mehta") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("guest_name_input"),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = guestPhone,
                                onValueChange = { viewModel.updateGuestDetails(guestName, it, guestAddress) },
                                label = { Text("Mobile Number (for counter collection)") },
                                placeholder = { Text("+91 98765 43210") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("guest_phone_input"),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("delivery_recipient_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isLoggedIn) "Delivery Address" else "Delivery Details (Guest)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            if (isLoggedIn) {
                                Text(
                                    text = "Saved Profile",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = PistachioGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        if (isLoggedIn && userProfile != null) {
                            val user = userProfile!!
                            Text(
                                text = "${user.name} • ${user.phone}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = user.deliveryAddress,
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        } else {
                            OutlinedTextField(
                                value = guestName,
                                onValueChange = { viewModel.updateGuestDetails(it, guestPhone, guestAddress) },
                                label = { Text("Your Name / Recipient") },
                                placeholder = { Text("e.g. Aarti Mehta") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("guest_name_input"),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = guestPhone,
                                onValueChange = { viewModel.updateGuestDetails(guestName, it, guestAddress) },
                                label = { Text("Phone Number") },
                                placeholder = { Text("+91 98765 43210") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("guest_phone_input"),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = guestAddress,
                                onValueChange = { viewModel.updateGuestDetails(guestName, guestPhone, it) },
                                label = { Text("Delivery Address") },
                                placeholder = { Text("Flat/House No, Building, Street, City") },
                                maxLines = 2,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("guest_address_input"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // GPS Location Auto-Detection
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        if (hasLocationPermission) {
                                            viewModel.detectDeviceLocation(context)
                                        } else {
                                            locationPermissionLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        }
                                    },
                                    modifier = Modifier.testTag("cart_detect_gps_button")
                                ) {
                                    if (isDetectingLocation) {
                                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Detecting GPS...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BerryBurgundy)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.MyLocation,
                                            contentDescription = null,
                                            tint = BerryBurgundy,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("📍 Auto-Detect Current GPS Location", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BerryBurgundy)
                                    }
                                }
                            }

                            // Quick Presets
                            Text(
                                text = "Quick Delivery Hubs:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(viewModel.presetLocations) { preset ->
                                    FilterChip(
                                        selected = guestAddress.contains(preset.city),
                                        onClick = { viewModel.selectPresetLocation(preset) },
                                        label = { Text(preset.title.split(",").firstOrNull() ?: preset.title, fontSize = 11.sp) },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }

                            selectedBranch?.let { branch ->
                                val dist = viewModel.calculateDistanceKm(branch.latitude, branch.longitude)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = PistachioGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Kitchen: ${branch.name} • ${String.format(Locale.getDefault(), "%.1f", dist)} km away",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = PistachioGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Payment Method Simulation
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Payment Method",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    PaymentMethod.entries.forEach { method ->
                        val isSelected = paymentMethod == method
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                                .clickable { viewModel.setPaymentMethod(method) }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .testTag("payment_method_${method.name}")
                        ) {
                            Icon(
                                imageVector = when (method) {
                                    PaymentMethod.UPI -> Icons.Default.Payment
                                    PaymentMethod.CARD -> Icons.Default.Payment
                                    PaymentMethod.NET_BANKING -> Icons.Default.Payment
                                    PaymentMethod.COD -> Icons.Default.LocalAtm
                                },
                                contentDescription = null,
                                tint = if (isSelected) BerryBurgundy else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = method.label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                                Text(
                                    text = method.subtitle,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = BerryBurgundy,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bill Summary Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Receipt Breakdown",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Item Subtotal", style = MaterialTheme.typography.bodyMedium)
                        Text("₹${subtotal.toInt()}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Celebration Kit (Knife & Sparklers)", style = MaterialTheme.typography.bodyMedium)
                        Text("FREE (₹0)", style = MaterialTheme.typography.bodyMedium.copy(color = PistachioGreen, fontWeight = FontWeight.Bold))
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Delivery Fee (${deliverySlot.label})", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            if (deliveryFee == 0.0) "FREE" else "₹${deliveryFee.toInt()}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (deliveryFee == 0.0) PistachioGreen else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Grand Total",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "₹${grandTotal.toInt()}",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = BerryBurgundy
                            )
                        )
                    }
                }
            }
        }

        // Place Order Action Button (Strict Authentication Enforcement for Guest Users)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        if (!isLoggedIn) {
                            // STRICT REQUIREMENT: Guest cannot place order without signing up first!
                            viewModel.updateGuestDetails(guestName, guestPhone, guestAddress)
                            viewModel.setPendingCheckoutAfterAuth(true)
                            viewModel.setAuthInitialTab(1) // Tab 1 = Create Account / Sign Up
                            onNavigateToLogin()
                        } else {
                            viewModel.placeOrder { orderId ->
                                onOrderPlaced(orderId)
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BerryBurgundy),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("place_order_button")
                ) {
                    Icon(
                        imageVector = if (!isLoggedIn) Icons.Default.PersonAdd else Icons.Default.ShoppingBag,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (!isLoggedIn) "Sign Up to Place Order • ₹${grandTotal.toInt()}" else "Place Artisanal Order • ₹${grandTotal.toInt()}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                if (!isLoggedIn) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("guest_signup_requirement_notice"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = HoneyButter),
                        border = CardDefaults.outlinedCardBorder(true)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = DarkAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sign-Up Required to Confirm Order",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DarkAmber
                                    )
                                )
                                Text(
                                    text = "Guest users must complete quick account registration or sign in before their order enters the live bakery oven queue.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = DarkAmber.copy(alpha = 0.9f),
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Already registered? ",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            text = "Sign In Here",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = BerryBurgundy,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .clickable {
                                    viewModel.updateGuestDetails(guestName, guestPhone, guestAddress)
                                    viewModel.setPendingCheckoutAfterAuth(true)
                                    viewModel.setAuthInitialTab(0) // Tab 0 = Sign In
                                    onNavigateToLogin()
                                }
                                .testTag("cart_sign_in_link")
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItemEntity,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cart_item_${item.cartId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.cakeTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "${String.format(Locale.getDefault(), "%.1f", item.weightKg)} kg • ${item.tiersCount} Tier Architecture",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = "Sponge: ${item.spongeFlavor} • Frosting: ${item.frostingFlavor}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.5.sp
                        )
                    )
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.testTag("remove_cart_item_${item.cartId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove item",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }

            if (item.customPipingText.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(HoneyButter)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Piped: \"${item.customPipingText}\"",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkAmber,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            if (item.referencePhotoUri != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = item.referencePhotoUri,
                        contentDescription = "Custom design reference",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Custom Design Reference Attached",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Qty: ${item.quantity}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = "₹${item.totalAmount.toInt()}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = BerryBurgundy
                    )
                )
            }
        }
    }
}
