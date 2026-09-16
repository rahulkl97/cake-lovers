package com.example.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.CakeCategory
import com.example.data.model.CakeItemEntity
import com.example.ui.components.CakeThumbnailImage
import com.example.ui.theme.BerryBurgundy
import com.example.ui.theme.DarkAmber
import com.example.ui.theme.PistachioGreen
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditCakeDialog(
    cake: CakeItemEntity,
    onDismiss: () -> Unit,
    onSave: (CakeItemEntity) -> Unit
) {
    var name by remember { mutableStateOf(cake.name) }
    var tagline by remember { mutableStateOf(cake.tagline) }
    var description by remember { mutableStateOf(cake.description) }
    var priceText by remember { mutableStateOf(cake.basePrice.toInt().toString()) }
    var category by remember { mutableStateOf(cake.category) }
    var leadTimeHoursText by remember { mutableStateOf(cake.leadTimeHours.toString()) }
    var flavorNotes by remember { mutableStateOf(cake.flavorNotes) }
    var defaultWeightKg by remember { mutableDoubleStateOf(cake.defaultWeightKg) }
    var isEggless by remember { mutableStateOf(cake.isEggless) }
    var isGlutenFree by remember { mutableStateOf(cake.isGlutenFree) }
    var isNutFree by remember { mutableStateOf(cake.isNutFree) }
    var isBestSeller by remember { mutableStateOf(cake.isBestSeller) }
    var isChefSpecial by remember { mutableStateOf(cake.isChefSpecial) }
    var photoUriString by remember { mutableStateOf(cake.localDrawableName) }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    // Photo picker launcher using system photo picker contract
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUriString = uri.toString()
        }
    }

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp)
                .testTag("admin_edit_cake_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Edit Cake Details & Inventory",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = BerryBurgundy
                            )
                        )
                        Text(
                            text = "ID: ${cake.id}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_edit_cake_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Photo Preview & Picker Section
                Text(
                    text = "Cake Photo & Presentation",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Temporary cake object for preview
                    val previewCake = cake.copy(
                        name = name.ifBlank { "Preview" },
                        localDrawableName = photoUriString
                    )
                    CakeThumbnailImage(
                        cake = previewCake,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Photo action overlay
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BerryBurgundy.copy(alpha = 0.9f)
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 12.dp,
                                vertical = 6.dp
                            ),
                            modifier = Modifier.testTag("admin_pick_cake_photo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Change Photo", fontSize = 12.sp)
                        }

                        if (photoUriString.startsWith("content://")) {
                            OutlinedButton(
                                onClick = { photoUriString = cake.category.let { "img_hero_banner" } },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                    horizontal = 10.dp,
                                    vertical = 6.dp
                                )
                            ) {
                                Text("Reset Default", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name & Tagline
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Cake Name *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_cake_name_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = tagline,
                    onValueChange = { tagline = it },
                    label = { Text("Tagline (e.g. Saffron pistachio luxury)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_cake_tagline_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Price & Prep Time Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) priceText = it },
                        label = { Text("Price (₹) *") },
                        prefix = { Text("₹ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_cake_price_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = leadTimeHoursText,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) leadTimeHoursText = it },
                        label = { Text("Lead Time (Hrs)") },
                        suffix = { Text("hrs") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_cake_lead_time_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Default Weight Selector
                Text(
                    text = "Base Portion Size: $defaultWeightKg kg",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0.5, 1.0, 1.5, 2.0).forEach { weight ->
                        FilterChip(
                            selected = defaultWeightKg == weight,
                            onClick = { defaultWeightKg = weight },
                            label = { Text("${weight}kg") },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BerryBurgundy,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                ) {
                    val currentCatDisplayName = try {
                        CakeCategory.valueOf(category).displayName
                    } catch (_: Exception) {
                        category
                    }
                    OutlinedTextField(
                        value = currentCatDisplayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("admin_cake_category_dropdown"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        CakeCategory.values().forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName) },
                                onClick = {
                                    category = cat.name
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Flavor Notes
                OutlinedTextField(
                    value = flavorNotes,
                    onValueChange = { flavorNotes = it },
                    label = { Text("Flavor Profile / Notes") },
                    placeholder = { Text("Cardamom, Rose petals, Belgium Ganache") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_cake_flavor_notes_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Ingredients") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_cake_description_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Dietary & Badges Switches
                Text(
                    text = "Dietary & Menu Badges",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("100% Eggless / Pure Veg", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Certified vegetarian kitchen", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isEggless,
                        onCheckedChange = { isEggless = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = PistachioGreen),
                        modifier = Modifier.testTag("admin_switch_eggless")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Gluten-Free Option", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Almond / oat flour sponge", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isGlutenFree,
                        onCheckedChange = { isGlutenFree = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = DarkAmber),
                        modifier = Modifier.testTag("admin_switch_gluten_free")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Nut-Free Certified", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Safe for nut allergies", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isNutFree,
                        onCheckedChange = { isNutFree = it },
                        modifier = Modifier.testTag("admin_switch_nut_free")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Best Seller Ribbon", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Featured on home popular scroll", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isBestSeller,
                        onCheckedChange = { isBestSeller = it },
                        modifier = Modifier.testTag("admin_switch_bestseller")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Chef's Signature Special", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Handcrafted artisanal badge", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isChefSpecial,
                        onCheckedChange = { isChefSpecial = it },
                        modifier = Modifier.testTag("admin_switch_chef_special")
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons: Cancel and Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_cancel_edit_cake_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    val isPriceValid = priceText.toDoubleOrNull()?.let { it > 0 } ?: false
                    val isFormValid = name.isNotBlank() && isPriceValid

                    Button(
                        onClick = {
                            val updatedCake = cake.copy(
                                name = name.trim(),
                                tagline = tagline.trim(),
                                description = description.trim(),
                                category = category,
                                basePrice = priceText.toDoubleOrNull() ?: cake.basePrice,
                                defaultWeightKg = defaultWeightKg,
                                leadTimeHours = leadTimeHoursText.toIntOrNull() ?: cake.leadTimeHours,
                                flavorNotes = flavorNotes.trim(),
                                isEggless = isEggless,
                                isGlutenFree = isGlutenFree,
                                isNutFree = isNutFree,
                                isBestSeller = isBestSeller,
                                isChefSpecial = isChefSpecial,
                                localDrawableName = photoUriString
                            )
                            onSave(updatedCake)
                        },
                        enabled = isFormValid,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_save_cake_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BerryBurgundy)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}
