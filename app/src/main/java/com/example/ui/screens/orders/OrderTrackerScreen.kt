package com.example.ui.screens.orders

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.ui.theme.BerryBurgundy
import com.example.ui.theme.DarkAmber
import com.example.ui.theme.HoneyButter
import com.example.ui.theme.PistachioGreen
import com.example.ui.viewmodel.CakeLoversViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrderTrackerScreen(
    viewModel: CakeLoversViewModel,
    modifier: Modifier = Modifier
) {
    val userOrders by viewModel.userOrders.collectAsStateWithLifecycle()
    val activeOrderId by viewModel.activeTrackOrderId.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Live Tracking, 1: Order History
    var showCancelDialogForOrder by remember { mutableStateOf<OrderEntity?>(null) }

    if (showCancelDialogForOrder != null) {
        val targetOrder = showCancelDialogForOrder!!
        AlertDialog(
            onDismissRequest = { showCancelDialogForOrder = null },
            title = {
                Text(
                    text = "Cancel Order ${targetOrder.orderId}?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "Your order has been placed and is currently awaiting bakery kitchen confirmation. Once confirmed or baking begins, it cannot be cancelled. Would you like to cancel it now? Any payment will be refunded to your original payment method (${targetOrder.paymentMethod}).",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelOrder(targetOrder.orderId)
                        showCancelDialogForOrder = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_cancel_order_btn")
                ) {
                    Text("Yes, Cancel Order", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialogForOrder = null }) {
                    Text("Keep Order")
                }
            }
        )
    }

    val currentOrder = userOrders.firstOrNull { it.orderId == activeOrderId }
        ?: userOrders.firstOrNull { it.status != OrderStatus.DELIVERED.name }
        ?: userOrders.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("orders_screen")
    ) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("Live Order Status", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_live_tracking")
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("Order History (${userOrders.size})", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_order_history")
            )
        }

        if (selectedTabIndex == 0) {
            if (currentOrder == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "📦", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Active Orders",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Place your first cake order to view real-time 5-stage progress!",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            } else {
                LiveOrderTrackingView(
                    order = currentOrder,
                    onCallRider = {
                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${currentOrder.driverPhone.replace(" ", "")}")
                        }
                        context.startActivity(dialIntent)
                    },
                    onCancelOrder = {
                        showCancelDialogForOrder = currentOrder
                    }
                )
            }
        } else {
            // Order History Archive
            if (userOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🎂", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Order History",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "You haven't placed any cake orders yet. All your fresh orders will appear here!",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(userOrders, key = { it.orderId }) { order ->
                        OrderHistoryCard(
                            order = order,
                            isSelected = order.orderId == currentOrder?.orderId,
                            onSelectForTracking = {
                                viewModel.setActiveTrackOrder(order.orderId)
                                selectedTabIndex = 0
                            },
                            onCancelOrder = if (order.status == OrderStatus.PLACED.name) {
                                { showCancelDialogForOrder = order }
                            } else null
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun LiveOrderTrackingView(
    order: OrderEntity,
    onCallRider: () -> Unit,
    onCancelOrder: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isCancelled = order.status == "CANCELLED"
    val currentStatus = try {
        OrderStatus.valueOf(order.status)
    } catch (e: Exception) {
        OrderStatus.PLACED
    }

    val dateFormatter = remember { SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()) }
    val formattedDate = dateFormatter.format(Date(order.createdAtTimestamp))

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("live_order_tracking_view"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Active Order Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCancelled) MaterialTheme.colorScheme.errorContainer 
                    else MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Order ${order.orderId}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCancelled) MaterialTheme.colorScheme.onErrorContainer 
                                    else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                            Text(
                                text = "Placed at $formattedDate",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isCancelled) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f) 
                                    else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCancelled) MaterialTheme.colorScheme.error else BerryBurgundy)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (isCancelled) "Cancelled & Refunded" else currentStatus.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Dispatched From: ${order.branchName}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isCancelled) MaterialTheme.colorScheme.onErrorContainer 
                            else MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = "Delivery Destination: ${order.deliveryAddress}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isCancelled) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f) 
                            else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            fontSize = 11.5.sp
                        )
                    )
                }
            }
        }

        // Driver / Kitchen Contact Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(BerryBurgundy),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeliveryDining,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = order.driverName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Temperature-Controlled Delivery Specialist",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(BerryBurgundy)
                            .clickable { onCallRider() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("driver_direct_dial_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Direct Dial Rider",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Quick Call",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // 5-Stage Stepper Progression
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "5-Stage Order Progression",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OrderStatus.entries.forEachIndexed { index, stage ->
                        val isCompleted = stage.stepIndex < currentStatus.stepIndex
                        val isCurrent = stage == currentStatus
                        val isUpcoming = stage.stepIndex > currentStatus.stepIndex

                        Row(modifier = Modifier.fillMaxWidth()) {
                            // Stage Indicator Node & Vertical Line
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isCurrent -> BerryBurgundy
                                                isCompleted -> PistachioGreen
                                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Completed",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Text(
                                            text = stage.stepIndex.toString(),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }

                                if (index < OrderStatus.entries.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(36.dp)
                                            .background(
                                                if (isCompleted) PistachioGreen
                                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                            )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Stage Description
                            Column(modifier = Modifier.padding(bottom = if (index < OrderStatus.entries.size - 1) 20.dp else 0.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = stage.label,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                            color = if (isCurrent) BerryBurgundy else if (isCompleted) PistachioGreen else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    if (isCurrent) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(HoneyButter)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "IN PROGRESS",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = DarkAmber,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 9.sp
                                                )
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = stage.description,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Receipt Summary in Live Tracker
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ordered Items & Inscriptions",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = order.itemsSummary,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )
                    )

                    if (!order.customPhotoUri.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = order.customPhotoUri,
                                contentDescription = "Your Custom Design Photo Reference",
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Custom Cake Reference Photo",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Shared with artisan bakers for precision decoration",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total Paid via ${order.paymentMethod}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Text(
                            text = "₹${order.finalTotal.toInt()}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = BerryBurgundy
                            )
                        )
                    }

                    // Customer cancellation window (allowed ONLY while status == PLACED)
                    if (currentStatus == OrderStatus.PLACED && onCancelOrder != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = onCancelOrder,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cancel_order_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = "Cancel Order",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cancel Order (Pending Kitchen Confirmation)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun OrderHistoryCard(
    order: OrderEntity,
    isSelected: Boolean,
    onSelectForTracking: () -> Unit,
    onCancelOrder: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val formattedDate = dateFormatter.format(Date(order.createdAtTimestamp))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelectForTracking() }
            .testTag("order_history_card_${order.orderId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder(isSelected)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.orderId,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (order.status == OrderStatus.DELIVERED.name) PistachioGreen.copy(alpha = 0.15f)
                            else if (order.status == "CANCELLED") MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            else HoneyButter
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = order.status,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (order.status == OrderStatus.DELIVERED.name) PistachioGreen
                            else if (order.status == "CANCELLED") MaterialTheme.colorScheme.error
                            else DarkAmber,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = order.itemsSummary,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total: ₹${order.finalTotal.toInt()}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = BerryBurgundy
                    )
                )
                Text(
                    text = "Track / View Receipt →",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            if (order.status == OrderStatus.PLACED.name && onCancelOrder != null) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onCancelOrder,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_cancel_order_${order.orderId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "Cancel Order",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Cancel Placed Order",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
