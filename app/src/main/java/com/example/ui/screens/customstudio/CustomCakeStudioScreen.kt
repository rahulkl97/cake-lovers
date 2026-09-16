package com.example.ui.screens.customstudio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.components.CelebrationKitBanner
import com.example.ui.theme.BerryBurgundy
import com.example.ui.theme.DarkAmber
import com.example.ui.theme.GoldenCaramel
import com.example.ui.theme.HoneyButter
import com.example.ui.theme.PistachioGreen
import com.example.ui.viewmodel.CakeLoversViewModel
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomCakeStudioScreen(
    viewModel: CakeLoversViewModel,
    onNavigateToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val studioState by viewModel.customStudio.collectAsStateWithLifecycle()
    val hasLocationPermission by viewModel.hasLocationPermission.collectAsStateWithLifecycle()
    val userLocationName by viewModel.userLocationName.collectAsStateWithLifecycle()
    val selectedBranch by viewModel.selectedBranch.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Location Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val granted = fineGranted || coarseGranted
        viewModel.setLocationPermission(granted)
        if (granted) {
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val loc = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (loc != null) {
                    viewModel.setUserLocation(
                        loc.latitude,
                        loc.longitude,
                        "GPS: ${String.format(Locale.getDefault(), "%.4f", loc.latitude)}, ${String.format(Locale.getDefault(), "%.4f", loc.longitude)}"
                    )
                } else {
                    viewModel.setUserLocation(12.9716, 77.5946, "Indiranagar, Bengaluru (Kitchen Hub)")
                }
            } catch (_: Exception) {
                viewModel.setUserLocation(12.9716, 77.5946, "Indiranagar, Bengaluru")
            }
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
        }
    }

    // Android Photo Picker Launcher (Zero-Permission compliance)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val localPath = try {
                val dir = java.io.File(context.filesDir, "custom_cake_photos")
                if (!dir.exists()) dir.mkdirs()
                val destFile = java.io.File(dir, "ref_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    java.io.FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                destFile.absolutePath
            } catch (e: Exception) {
                uri.toString()
            }
            viewModel.updateStudioPhotoUri(localPath)
        }
    }

    val spongeOptions = listOf(
        "Red Velvet Classic",
        "Belgian Dark Chocolate",
        "Saffron Cardamom (Rasmalai Base)",
        "Vanilla Bean Almond",
        "Alphonso Mango Sponge"
    )

    val frostingOptions = listOf(
        "Cream Cheese Frosting",
        "Belgian Dark Chocolate Ganache",
        "Salted Caramel Buttercream",
        "Alphonso Mango Whip",
        "Rose Cardamom Rabdi Cream"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("custom_cake_studio_screen")
    ) {
        // Header Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = R.drawable.img_custom_studio_banner),
                    contentDescription = "Custom Cake Studio Header",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                )
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomStart)
                ) {
                    Text(
                        text = "Artisan Custom Cake Studio",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Design multi-tier celebration cakes with live hand-piping preview",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Interactive Canvas Preview
        Text(
            text = "Live Cake Geometry & Piping Canvas",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(8.dp))

        CakeCanvasPreview(
            tiersCount = studioState.tiersCount,
            spongeFlavor = studioState.spongeFlavor,
            frostingFlavor = studioState.frostingFlavor,
            customPipingText = studioState.customPipingText
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 1. Tier Configuration
        Text(
            text = "1. Tier Architecture",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(
                1 to "Single Tier\n(0.5 - 2.0 kg)",
                2 to "2-Tier Arch\n(2.0 - 3.5 kg)",
                3 to "3-Tier Grand\n(3.5 - 5.0 kg)"
            ).forEach { (tierNum, label) ->
                val isSelected = studioState.tiersCount == tierNum
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.updateStudioTiers(tierNum) }
                        .testTag("tier_button_$tierNum"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) BerryBurgundy else MaterialTheme.colorScheme.surface
                    ),
                    border = CardDefaults.outlinedCardBorder(isSelected)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Weight Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total Cake Weight",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = "${String.format(Locale.getDefault(), "%.1f", studioState.weightKg)} kg",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
        val minWeight = when (studioState.tiersCount) {
            1 -> 0.5f
            2 -> 2.0f
            3 -> 3.5f
            else -> 0.5f
        }
        Slider(
            value = studioState.weightKg.toFloat(),
            onValueChange = { viewModel.updateStudioWeight(it.toDouble()) },
            valueRange = minWeight..5.0f,
            steps = ((5.0f - minWeight) / 0.5f).toInt() - 1,
            colors = SliderDefaults.colors(
                thumbColor = BerryBurgundy,
                activeTrackColor = BerryBurgundy
            ),
            modifier = Modifier.testTag("weight_slider")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Sponge Flavor Selection
        Text(
            text = "2. Sponge Base Flavor",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            spongeOptions.forEach { sponge ->
                val selected = studioState.spongeFlavor == sponge
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.updateStudioSponge(sponge) },
                    label = { Text(sponge, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("sponge_chip_${sponge.replace(" ", "_")}")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Frosting Selection
        Text(
            text = "3. Outer Frosting & Ganache",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            frostingOptions.forEach { frosting ->
                val selected = studioState.frostingFlavor == frosting
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.updateStudioFrosting(frosting) },
                    label = { Text(frosting, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldenCaramel.copy(alpha = 0.2f),
                        selectedLabelColor = DarkAmber
                    ),
                    modifier = Modifier.testTag("frosting_chip_${frosting.replace(" ", "_")}")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Custom Message Piping
        Text(
            text = "4. Hand-Piped Message on Cake",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = studioState.customPipingText,
            onValueChange = { viewModel.updateStudioPipingText(it) },
            placeholder = { Text("e.g., Happy 25th Silver Anniversary Mom & Dad!") },
            maxLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("custom_piping_text_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BerryBurgundy,
                cursorColor = BerryBurgundy
            ),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Reference Photo Upload (Android Photo Picker)
        Text(
            text = "5. Reference Inspiration Photo (Optional)",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            text = "Upload a photo or sketch of your desired cake design for our master decorator.",
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (studioState.referencePhotoUri != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp)
            ) {
                AsyncImage(
                    model = studioState.referencePhotoUri,
                    contentDescription = "Uploaded cake design reference",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Design Reference Attached",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Our baker will follow your reference photo",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
                IconButton(
                    onClick = { viewModel.updateStudioPhotoUri(null) },
                    modifier = Modifier.testTag("remove_reference_photo_button")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Remove photo")
                }
            }
        } else {
            Button(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("upload_reference_photo_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "Upload cake photo"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Reference Photo from Gallery")
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Or tap a Master Chef Design Inspiration preset:",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val inspirationPresets = listOf(
                    "Royal Golden Drip" to "android.resource://${context.packageName}/${R.drawable.img_custom_studio_banner}",
                    "Celebration Floral" to "android.resource://${context.packageName}/${R.drawable.img_hero_banner}",
                    "Berry Fruit Cascade" to "android.resource://${context.packageName}/${R.drawable.ic_category_pies}",
                    "Belgian Truffle Swirl" to "android.resource://${context.packageName}/${R.drawable.ic_category_cakes}"
                )
                inspirationPresets.forEach { (name, uri) ->
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.updateStudioPhotoUri(uri) },
                        label = { Text(name, fontSize = 11.5.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = DarkAmber
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Kitchen Fulfillment & Delivery Radius (Location Permission)
        Text(
            text = "6. Artisan Kitchen & Delivery Proximity",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            text = "Allow location access to match with the nearest master bakery kitchen for express custom delivery.",
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("custom_cake_location_card"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (hasLocationPermission) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (hasLocationPermission) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (hasLocationPermission) Icons.Default.LocationOn else Icons.Default.MyLocation,
                            contentDescription = "Location Access",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (hasLocationPermission) "Location Verified" else "Location Access Required",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (hasLocationPermission) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Text(
                            text = if (hasLocationPermission) {
                                val branchName = selectedBranch?.name ?: "Bandra West Flagship"
                                val distance = viewModel.calculateDistanceKm(selectedBranch?.latitude ?: 12.9716, selectedBranch?.longitude ?: 77.5946)
                                "$userLocationName • Closest: $branchName ($distance km)"
                            } else {
                                "Tap below to allow location access and detect nearest artisan studio"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (hasLocationPermission) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }

                if (!hasLocationPermission) {
                    Spacer(modifier = Modifier.height(10.dp))
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
                            .testTag("custom_cake_request_location_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BerryBurgundy)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Allow Location Access", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Free Celebration Kit Banner
        CelebrationKitBanner()

        Spacer(modifier = Modifier.height(20.dp))

        // Dynamic Price Engine Card & Checkout Action
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = HoneyButter),
            border = CardDefaults.outlinedCardBorder(true)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Dynamic Price Calculation",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DarkAmber
                            )
                        )
                        Text(
                            text = "${studioState.tiersCount} Tier • ${String.format(Locale.getDefault(), "%.1f", studioState.weightKg)} kg • Premium Fillings",
                            style = MaterialTheme.typography.bodySmall.copy(color = DarkAmber.copy(alpha = 0.8f))
                        )
                    }
                    Text(
                        text = "₹${studioState.dynamicPrice.toInt()}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = BerryBurgundy
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.addCustomCakeToCart {
                            onNavigateToCart()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BerryBurgundy),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("add_custom_cake_to_cart_button")
                ) {
                    Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Custom Cake to Cart",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
