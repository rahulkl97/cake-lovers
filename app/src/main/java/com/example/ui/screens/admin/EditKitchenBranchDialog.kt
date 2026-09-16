package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BranchEntity
import com.example.ui.theme.BerryBurgundy
import com.example.ui.theme.DarkAmber
import com.example.ui.theme.PistachioGreen
import com.example.ui.theme.RoseContainer
import java.util.UUID

@Composable
fun EditKitchenBranchDialog(
    branch: BranchEntity,
    onDismiss: () -> Unit,
    onSave: (BranchEntity) -> Unit
) {
    var name by remember { mutableStateOf(branch.name) }
    var address by remember { mutableStateOf(branch.address) }
    var phone by remember { mutableStateOf(branch.phone) }
    var operatingHours by remember { mutableStateOf(branch.operatingHours) }
    var radiusKm by remember { mutableDoubleStateOf(branch.deliveryRadiusKm) }
    var rating by remember { mutableDoubleStateOf(branch.rating) }
    var isActive by remember { mutableStateOf(branch.isActive) }

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 20.dp)
                .testTag("admin_edit_kitchen_branch_dialog"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(RoseContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = "Kitchen Edit",
                                tint = BerryBurgundy,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Edit Kitchen Branch",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = BerryBurgundy
                            )
                            Text(
                                text = "Update kitchen details, dispatch radius & status",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_edit_branch_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(16.dp))

                // Kitchen Active Status Switch
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) PistachioGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isActive) "Kitchen Operational (Active)" else "Kitchen Paused (Temporarily Closed)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isActive) PistachioGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (isActive) "Accepting orders & dispatching to riders" else "Orders temporarily routed to backup branches",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PistachioGreen
                            ),
                            modifier = Modifier.testTag("edit_branch_active_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Kitchen Branch Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Kitchen / Branch Name") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = BerryBurgundy
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_branch_name_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Kitchen Address
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Physical Bakery Address") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = BerryBurgundy
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_branch_address_input"),
                    minLines = 2,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Phone Contact
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Kitchen Contact Phone") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = BerryBurgundy
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_branch_phone_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Operating Hours
                OutlinedTextField(
                    value = operatingHours,
                    onValueChange = { operatingHours = it },
                    label = { Text("Operating Hours") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = BerryBurgundy
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_branch_hours_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Delivery Coverage Radius (km)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.NearMe,
                                    contentDescription = null,
                                    tint = BerryBurgundy,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Delivery Coverage Radius",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Text(
                                text = if (radiusKm > 0) "${"%.1f".format(radiusKm)} km" else "0.0 km (Disabled)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (radiusKm > 0) PistachioGreen else MaterialTheme.colorScheme.error
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Slider(
                            value = radiusKm.toFloat(),
                            onValueChange = { radiusKm = it.toDouble() },
                            valueRange = 0f..30f,
                            steps = 29,
                            colors = SliderDefaults.colors(
                                thumbColor = BerryBurgundy,
                                activeTrackColor = BerryBurgundy,
                                inactiveTrackColor = BerryBurgundy.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.testTag("edit_branch_radius_slider")
                        )

                        // Quick Radius Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(0.0, 5.0, 10.0, 15.0, 25.0).forEach { presetKm ->
                                val isSelected = kotlin.math.abs(radiusKm - presetKm) < 0.2
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { radiusKm = presetKm },
                                    label = {
                                        Text(
                                            if (presetKm == 0.0) "0 km (Off)" else "${presetKm.toInt()} km",
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BerryBurgundy.copy(alpha = 0.15f),
                                        selectedLabelColor = BerryBurgundy
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Rating modifier
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = DarkAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Customer Rating Score",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                    Text(
                        text = "⭐ ${"%.1f".format(rating)} / 5.0",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DarkAmber
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons (Cancel and Save)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("cancel_edit_branch_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val updated = branch.copy(
                                name = name.trim().ifEmpty { branch.name },
                                address = address.trim().ifEmpty { branch.address },
                                phone = phone.trim().ifEmpty { branch.phone },
                                operatingHours = operatingHours.trim().ifEmpty { branch.operatingHours },
                                deliveryRadiusKm = radiusKm,
                                rating = rating,
                                isActive = isActive
                            )
                            onSave(updated)
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_edit_branch_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = BerryBurgundy),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Kitchen Details", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddKitchenBranchDialog(
    onDismiss: () -> Unit,
    onSave: (BranchEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+91 80 ") }
    var operatingHours by remember { mutableStateOf("08:00 AM - 11:30 PM") }
    var radiusKm by remember { mutableDoubleStateOf(10.0) }
    var isActive by remember { mutableStateOf(true) }

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 20.dp)
                .testTag("admin_add_kitchen_branch_dialog"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(RoseContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = "Add Kitchen",
                                tint = BerryBurgundy,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Add New Kitchen Branch",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = BerryBurgundy
                            )
                            Text(
                                text = "Register a new bakery hub in the dispatch network",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_add_branch_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(16.dp))

                // Kitchen Branch Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Kitchen / Branch Name *") },
                    placeholder = { Text("e.g. Koramangala Artisan Hub") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = BerryBurgundy
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_branch_name_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Kitchen Address
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Bakery Address *") },
                    placeholder = { Text("Full physical address & landmark") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = BerryBurgundy
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_branch_address_input"),
                    minLines = 2,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Phone Contact
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Kitchen Contact Phone *") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = BerryBurgundy
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_branch_phone_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Operating Hours
                OutlinedTextField(
                    value = operatingHours,
                    onValueChange = { operatingHours = it },
                    label = { Text("Operating Hours") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = BerryBurgundy
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_branch_hours_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Delivery Coverage Radius (km)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Initial Delivery Radius",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${"%.1f".format(radiusKm)} km",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PistachioGreen
                                )
                            )
                        }

                        Slider(
                            value = radiusKm.toFloat(),
                            onValueChange = { radiusKm = it.toDouble() },
                            valueRange = 0f..30f,
                            steps = 29,
                            colors = SliderDefaults.colors(
                                thumbColor = BerryBurgundy,
                                activeTrackColor = BerryBurgundy
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("cancel_add_branch_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank() && address.isNotBlank()) {
                                val newBranch = BranchEntity(
                                    id = "branch_${UUID.randomUUID().toString().take(8)}",
                                    name = name.trim(),
                                    address = address.trim(),
                                    phone = phone.trim(),
                                    latitude = 12.9716 + (Math.random() - 0.5) * 0.05,
                                    longitude = 77.5946 + (Math.random() - 0.5) * 0.05,
                                    deliveryRadiusKm = radiusKm,
                                    operatingHours = operatingHours.trim(),
                                    rating = 5.0,
                                    isActive = isActive
                                )
                                onSave(newBranch)
                            }
                        },
                        enabled = name.isNotBlank() && address.isNotBlank(),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_add_branch_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = BerryBurgundy),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Create Kitchen Branch", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
