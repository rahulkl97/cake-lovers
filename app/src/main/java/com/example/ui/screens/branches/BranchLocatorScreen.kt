package com.example.ui.screens.branches

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BranchEntity
import com.example.data.model.FulfillmentType
import com.example.ui.theme.BerryBurgundy
import com.example.ui.theme.PistachioGreen
import com.example.ui.viewmodel.CakeLoversViewModel
import java.util.Locale

@Composable
fun BranchLocatorScreen(
    viewModel: CakeLoversViewModel,
    modifier: Modifier = Modifier
) {
    val branches by viewModel.allBranches.collectAsStateWithLifecycle()
    val selectedBranch by viewModel.selectedBranch.collectAsStateWithLifecycle()
    val fulfillmentType by viewModel.fulfillmentType.collectAsStateWithLifecycle()
    val hasLocationPermission by viewModel.hasLocationPermission.collectAsStateWithLifecycle()
    val isDetectingLocation by viewModel.isDetectingLocation.collectAsStateWithLifecycle()
    val userLocationName by viewModel.userLocationName.collectAsStateWithLifecycle()
    val userLat by viewModel.userLatitude.collectAsStateWithLifecycle()
    val userLon by viewModel.userLongitude.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Request Location Permission Launcher (Multiple Permissions for Fine + Coarse)
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

    // Check existing permission on launch
    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (fineGranted || coarseGranted) {
            viewModel.setLocationPermission(true)
        }
    }

    // Sort branches by calculated distance from user's location
    val sortedBranches = remember(branches, userLat, userLon) {
        branches.sortedBy { viewModel.calculateDistanceKm(it.latitude, it.longitude) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("branch_locator_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Location Permission & Nearest Branch Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("location_permission_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (hasLocationPermission) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (hasLocationPermission) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (hasLocationPermission) Icons.Default.LocationOn else Icons.Default.MyLocation,
                                contentDescription = "Location status",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (hasLocationPermission) "Delivery Location Active" else "Location Access Requested",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasLocationPermission) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Text(
                                text = if (hasLocationPermission) {
                                    userLocationName
                                } else {
                                    "Allow location permission to auto-detect nearest artisan kitchen & calculate express delivery time"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (hasLocationPermission) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Location Permission Request Action / Status
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("request_location_permission_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BerryBurgundy)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Allow Location Access", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = PistachioGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Permission Granted (Live GPS)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }

                            TextButton(
                                onClick = { detectDeviceLocation(context, viewModel) },
                                modifier = Modifier.testTag("refresh_location_button")
                            ) {
                                if (isDetectingLocation) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = BerryBurgundy
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Locating...", fontSize = 12.sp)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh GPS",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Refresh GPS", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Fulfillment mode toggle
                    Text(
                        text = "Preferred Fulfillment Type",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = fulfillmentType == FulfillmentType.DELIVERY,
                            onClick = { viewModel.setFulfillmentType(FulfillmentType.DELIVERY) },
                            label = { Text("Doorstep Hand-Delivery") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DeliveryDining,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("fulfillment_delivery_chip"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BerryBurgundy,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            )
                        )

                        FilterChip(
                            selected = fulfillmentType == FulfillmentType.PICKUP,
                            onClick = { viewModel.setFulfillmentType(FulfillmentType.PICKUP) },
                            label = { Text("Express Pickup") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Store,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("fulfillment_pickup_chip"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BerryBurgundy,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Artisanal Kitchen Branches (${sortedBranches.size}) • Sorted by Proximity",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }

        // Branch List (Sorted by Proximity)
        items(sortedBranches, key = { it.id }) { branch ->
            val isSelected = selectedBranch?.id == branch.id || (selectedBranch == null && branch == sortedBranches.firstOrNull())
            val distanceKm = viewModel.calculateDistanceKm(branch.latitude, branch.longitude)

            BranchCard(
                branch = branch,
                distanceKm = distanceKm,
                isSelected = isSelected,
                onSelect = { viewModel.selectBranch(branch) },
                onCall = {
                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${branch.phone.replace(" ", "")}")
                    }
                    context.startActivity(dialIntent)
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}

/**
 * Reads Android LocationManager to obtain device coordinates or gracefully falls back.
 */
fun detectDeviceLocation(context: Context, viewModel: CakeLoversViewModel) {
    viewModel.setDetectingLocation(true)
    try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager != null) {
            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )
            var bestLocation: Location? = null
            for (provider in providers) {
                try {
                    val loc = locationManager.getLastKnownLocation(provider)
                    if (loc != null && (bestLocation == null || loc.accuracy < bestLocation.accuracy)) {
                        bestLocation = loc
                    }
                } catch (_: SecurityException) {
                    // SecurityException handled safely
                }
            }

            if (bestLocation != null) {
                viewModel.setUserLocation(
                    lat = bestLocation.latitude,
                    lon = bestLocation.longitude,
                    locationName = "GPS: ${String.format(Locale.getDefault(), "%.4f", bestLocation.latitude)}, ${String.format(Locale.getDefault(), "%.4f", bestLocation.longitude)}"
                )
            } else {
                viewModel.setUserLocation(
                    lat = 12.9716,
                    lon = 77.5946,
                    locationName = "Indiranagar, Bengaluru (Kitchen Hub)"
                )
            }
        } else {
            viewModel.setUserLocation(12.9716, 77.5946, "Central Bengaluru")
        }
    } catch (_: Exception) {
        viewModel.setUserLocation(12.9716, 77.5946, "Central Bengaluru")
    } finally {
        viewModel.setDetectingLocation(false)
    }
}

@Composable
fun BranchCard(
    branch: BranchEntity,
    distanceKm: Double,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("branch_card_${branch.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder(isSelected)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = branch.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected kitchen",
                                tint = PistachioGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = branch.address,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                // Distance Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$distanceKm km away",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = branch.operatingHours,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.5.sp
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Phone quick-dial button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable { onCall() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("branch_call_button_${branch.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Call branch",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Call Kitchen",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (branch.deliveryRadiusKm > 0) {
                    "⚡ Delivery Radius: Up to ${branch.deliveryRadiusKm.toInt()} km • Express Oven Batch"
                } else {
                    "⚠️ Delivery Radius: 0 km (Delivery Paused / Radius Deleted)"
                },
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (branch.deliveryRadiusKm > 0) PistachioGreen else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}
