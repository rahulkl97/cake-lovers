package com.example.ui.screens.admin

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.BranchEntity
import com.example.data.model.CakeCategory
import com.example.data.model.CakeItemEntity
import com.example.data.model.DeliveryPartnerEntity
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.ui.components.CakeThumbnailImage
import com.example.ui.theme.BerryBurgundy
import com.example.ui.theme.DarkAmber
import com.example.ui.theme.GoldenCaramel
import com.example.ui.theme.HoneyButter
import com.example.ui.theme.PistachioGreen
import com.example.ui.theme.RoseContainer
import com.example.ui.viewmodel.CakeLoversViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminKitchenPortalScreen(
    viewModel: CakeLoversViewModel,
    snackbarHostState: SnackbarHostState,
    onSignOut: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
    val allCakes by viewModel.allCakes.collectAsStateWithLifecycle()
    val allBranches by viewModel.allBranches.collectAsStateWithLifecycle()
    val allDeliveryPartners by viewModel.allDeliveryPartners.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastSyncTimestamp.collectAsStateWithLifecycle()
    val realtimeSyncStatus by viewModel.realtimeSyncStatus.collectAsStateWithLifecycle()
    val firestorePermissionWarning by viewModel.firestorePermissionWarning.collectAsStateWithLifecycle()
    val adminPortalSelectedTab by viewModel.adminPortalSelectedTab.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    if (!isAdmin) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp)
                .testTag("admin_access_restricted_view"),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(true),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(RoseContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Restricted Access",
                            tint = BerryBurgundy,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Admin Access Restricted",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = BerryBurgundy
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This management console is restricted to authorized Cake Lovers kitchen administrators. Regular member accounts do not have administrative privileges.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
        return
    }

    var selectedAdminTab by remember { mutableIntStateOf(0) } // 0: Orders Queue & Dispatch, 1: Cakes Catalog Management, 2: Kitchens & Radius
    LaunchedEffect(adminPortalSelectedTab) {
        selectedAdminTab = adminPortalSelectedTab
    }
    var filterStatus by remember { mutableStateOf<OrderStatus?>(null) }

    // Deletion confirmation dialog state
    var deleteDialogAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var deleteDialogTitle by remember { mutableStateOf("") }
    var deleteDialogMessage by remember { mutableStateOf("") }

    // Cake editing dialog state
    var editingCake by remember { mutableStateOf<CakeItemEntity?>(null) }

    // Kitchen Branch editing & adding dialog states
    var editingBranch by remember { mutableStateOf<BranchEntity?>(null) }
    var showAddBranchDialog by remember { mutableStateOf(false) }

    // Delivery Partner dialog states
    var assigningPartnerForOrder by remember { mutableStateOf<OrderEntity?>(null) }
    var assigningOrderForPartner by remember { mutableStateOf<DeliveryPartnerEntity?>(null) }
    var showAddPartnerDialog by remember { mutableStateOf(false) }

    // Custom Cake Photo Preview Dialog State
    var viewingPhotoForOrder by remember { mutableStateOf<OrderEntity?>(null) }

    val filteredOrders = if (filterStatus == null) {
        allOrders
    } else {
        allOrders.filter { it.status == filterStatus?.name }
    }

    val syncDateStr = if (lastSyncTime > 0) {
        SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date(lastSyncTime))
    } else {
        "Pending"
    }

    // Confirmation Dialog
    if (deleteDialogAction != null) {
        AlertDialog(
            onDismissRequest = { deleteDialogAction = null },
            title = {
                Text(
                    text = deleteDialogTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = deleteDialogMessage,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        deleteDialogAction?.invoke()
                        deleteDialogAction = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteDialogAction = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Cake Edit Dialog
    editingCake?.let { cakeToEdit ->
        EditCakeDialog(
            cake = cakeToEdit,
            onDismiss = { editingCake = null },
            onSave = { updatedCake ->
                editingCake = null
                viewModel.updateCake(updatedCake) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Cake '${updatedCake.name}' updated successfully!")
                    }
                }
            }
        )
    }

    // Kitchen Branch Edit Dialog
    editingBranch?.let { branchToEdit ->
        EditKitchenBranchDialog(
            branch = branchToEdit,
            onDismiss = { editingBranch = null },
            onSave = { updatedBranch ->
                editingBranch = null
                viewModel.updateBranch(updatedBranch) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Kitchen branch '${updatedBranch.name}' updated successfully!")
                    }
                }
            }
        )
    }

    // Kitchen Branch Add Dialog
    if (showAddBranchDialog) {
        AddKitchenBranchDialog(
            onDismiss = { showAddBranchDialog = false },
            onSave = { newBranch ->
                showAddBranchDialog = false
                viewModel.insertBranch(newBranch) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Added kitchen branch '${newBranch.name}'")
                    }
                }
            }
        )
    }

    // Assign Delivery Partner for Order Dialog
    assigningPartnerForOrder?.let { order ->
        AssignPartnerToOrderDialog(
            order = order,
            partners = allDeliveryPartners,
            onDismiss = { assigningPartnerForOrder = null },
            onAssign = { partner ->
                assigningPartnerForOrder = null
                viewModel.assignDeliveryPartner(order.orderId, partner.name, partner.phone) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Assigned ${partner.name} to order #${order.orderId}")
                    }
                }
            }
        )
    }

    // Assign Order to Delivery Partner Dialog
    assigningOrderForPartner?.let { partner ->
        AssignOrderToPartnerDialog(
            partner = partner,
            orders = allOrders.filter { it.status != OrderStatus.DELIVERED.name },
            onDismiss = { assigningOrderForPartner = null },
            onAssign = { order ->
                assigningOrderForPartner = null
                viewModel.assignDeliveryPartner(order.orderId, partner.name, partner.phone) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Assigned order #${order.orderId} to ${partner.name}")
                    }
                }
            }
        )
    }

    // Add Delivery Partner Dialog
    if (showAddPartnerDialog) {
        AddDeliveryPartnerDialog(
            onDismiss = { showAddPartnerDialog = false },
            onSave = { newPartner ->
                showAddPartnerDialog = false
                viewModel.addDeliveryPartner(newPartner) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Added delivery partner '${newPartner.name}'")
                    }
                }
            }
        )
    }

    // Custom Cake Photo Full-Screen Inspection Dialog
    viewingPhotoForOrder?.let { order ->
        val photoUri = order.customPhotoUri
        if (!photoUri.isNullOrBlank()) {
            CustomCakePhotoDialog(
                order = order,
                photoUri = photoUri,
                onDismiss = { viewingPhotoForOrder = null }
            )
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_kitchen_portal_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Kitchen Ops Header & Cloud Sync Controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "👨‍🍳 Kitchen Ops & Admin Control",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                            Text(
                                text = "Manage orders, dispatch, cakes, kitchens & delivery zones",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            )
                        }

                        // Actions: Cloud Sync & Sign Out
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    viewModel.syncWithCloud {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Cloud Sync completed successfully!")
                                        }
                                    }
                                },
                                enabled = !isSyncing,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BerryBurgundy),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("admin_cloud_sync_button")
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Syncing...", fontSize = 12.sp)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CloudSync,
                                        contentDescription = "Sync Cloud",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sync", fontSize = 12.sp)
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    if (onSignOut != null) {
                                        onSignOut()
                                    } else {
                                        viewModel.logout()
                                    }
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Admin signed out. Switched to Guest Mode.")
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("admin_ops_header_sign_out_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Sign Out",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sign Out", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(PistachioGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Firebase: $realtimeSyncStatus",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        Text(
                            text = "${allOrders.size} Orders • ${allCakes.size} Cakes",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        )
                    }

                    if (firestorePermissionWarning != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Note: If Cloud Firestore shows permission rules pending, in Firebase Console -> Firestore Database -> Rules, set: allow read, write: if true; (Local operations and Realtime DB continue normally).",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // Admin Management Tabs: Orders Queue | Cakes Catalog | Kitchens & Radius | Delivery Fleet
        item {
            TabRow(
                selectedTabIndex = selectedAdminTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = BerryBurgundy,
                indicator = { tabPositions ->
                    if (selectedAdminTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedAdminTab]),
                            color = BerryBurgundy
                        )
                    }
                }
            ) {
                Tab(
                    selected = selectedAdminTab == 0,
                    onClick = { selectedAdminTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Orders (${allOrders.size})", fontWeight = if (selectedAdminTab == 0) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
                Tab(
                    selected = selectedAdminTab == 1,
                    onClick = { selectedAdminTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Cake, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cakes (${allCakes.size})", fontWeight = if (selectedAdminTab == 1) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
                Tab(
                    selected = selectedAdminTab == 2,
                    onClick = { selectedAdminTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Kitchens (${allBranches.size})", fontWeight = if (selectedAdminTab == 2) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
                Tab(
                    selected = selectedAdminTab == 3,
                    onClick = { selectedAdminTab = 3 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DirectionsBike, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Fleet (${allDeliveryPartners.size})", fontWeight = if (selectedAdminTab == 3) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
            }
        }

        // TAB 0: ORDERS QUEUE & DISPATCH (With Order Deletion, Status Reset/Delete, Delivery Person Delete)
        if (selectedAdminTab == 0) {
            // Status Filter Chips
            item {
                Text(
                    text = "Filter Kitchen Queue",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = filterStatus == null,
                            onClick = { filterStatus = null },
                            label = { Text("All Orders (${allOrders.size})") },
                            shape = RoundedCornerShape(16.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BerryBurgundy,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    items(OrderStatus.entries.toTypedArray()) { status ->
                        val count = allOrders.count { it.status == status.name }
                        FilterChip(
                            selected = filterStatus == status,
                            onClick = { filterStatus = status },
                            label = { Text("${status.label} ($count)") },
                            shape = RoundedCornerShape(16.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BerryBurgundy,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            if (filteredOrders.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No orders currently in this queue status.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            } else {
                items(filteredOrders, key = { it.orderId }) { order ->
                    AdminOrderCard(
                        order = order,
                        onAdvanceStatus = { nextStatus ->
                            viewModel.updateOrderStatus(order.orderId, nextStatus)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Order ${order.orderId} advanced to ${nextStatus.label}")
                            }
                        },
                        onDeleteOrder = {
                            deleteDialogTitle = "Delete Order ${order.orderId}?"
                            deleteDialogMessage = "Are you sure you want to completely remove this order record from the system?"
                            deleteDialogAction = {
                                viewModel.deleteOrder(order.orderId) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Order ${order.orderId} deleted")
                                    }
                                }
                            }
                        },
                        onDeleteStatus = {
                            deleteDialogTitle = "Reset/Delete Status of Order ${order.orderId}?"
                            deleteDialogMessage = "This will reset the order's status to unconfirmed pending state."
                            deleteDialogAction = {
                                viewModel.deleteOrderStatus(order.orderId) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Status of order ${order.orderId} reset")
                                    }
                                }
                            }
                        },
                        onDeleteDeliveryPerson = {
                            deleteDialogTitle = "Remove Delivery Person from ${order.orderId}?"
                            deleteDialogMessage = "This will unassign driver '${order.driverName}' from this delivery."
                            deleteDialogAction = {
                                viewModel.deleteDeliveryPerson(order.orderId) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Delivery person removed from ${order.orderId}")
                                    }
                                }
                            }
                        },
                        onAssignPartner = {
                            assigningPartnerForOrder = order
                        },
                        onViewCustomPhoto = {
                            viewingPhotoForOrder = order
                        }
                    )
                }
            }
        }

        // TAB 1: CAKES CATALOG MANAGEMENT (With Cake Editing & Deletion)
        if (selectedAdminTab == 1) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Active Cake Recipes & Menu Items",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${allCakes.size} items in active inventory",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    Button(
                        onClick = {
                            val newId = "cake_${System.currentTimeMillis()}"
                            val newCake = CakeItemEntity(
                                id = newId,
                                name = "New Artisanal Cake",
                                tagline = "Freshly crafted in kitchen",
                                description = "Handcrafted with premium ingredients, fresh cream, and layered sponge.",
                                category = CakeCategory.CHOCOLATE_TRUFFLE.name,
                                basePrice = 750.0,
                                rating = 5.0,
                                reviewsCount = 1,
                                isEggless = true,
                                isGlutenFree = false,
                                isNutFree = false,
                                leadTimeHours = 2,
                                flavorNotes = "Fresh Cream, Vanilla sponge",
                                defaultWeightKg = 0.5,
                                localDrawableName = "img_hero_banner"
                            )
                            editingCake = newCake
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BerryBurgundy),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("admin_add_new_cake_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Cake", fontSize = 12.sp)
                    }
                }
            }

            if (allCakes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No cakes found in database.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(allCakes, key = { it.id }) { cake ->
                    AdminCakeCard(
                        cake = cake,
                        onEditCake = {
                            editingCake = cake
                        },
                        onDeleteCake = {
                            deleteDialogTitle = "Delete Cake '${cake.name}'?"
                            deleteDialogMessage = "Are you sure you want to delete this cake recipe from the active menu? Customers will no longer be able to order it."
                            deleteDialogAction = {
                                viewModel.deleteCake(cake.id) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Cake '${cake.name}' deleted from menu")
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }

        // TAB 2: KITCHENS & DELIVERY RADIUS (With Branch Editing, Addition, Deletion & Delivery Radius Reset/Delete)
        if (selectedAdminTab == 2) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bakery Kitchens & Delivery Zones",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${allBranches.size} kitchens • Edit hours, radius & details",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    Button(
                        onClick = { showAddBranchDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BerryBurgundy),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("admin_add_kitchen_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Kitchen",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add Kitchen",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            if (allBranches.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No kitchen branches found in database.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(allBranches, key = { it.id }) { branch ->
                    AdminBranchCard(
                        branch = branch,
                        onEditBranch = {
                            editingBranch = branch
                        },
                        onDeleteBranch = {
                            deleteDialogTitle = "Delete Kitchen Branch '${branch.name}'?"
                            deleteDialogMessage = "Are you sure you want to remove this bakery kitchen branch from the locator?"
                            deleteDialogAction = {
                                viewModel.deleteBranch(branch.id) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Branch '${branch.name}' deleted")
                                    }
                                }
                            }
                        },
                        onDeleteDeliveryRadius = {
                            deleteDialogTitle = "Delete/Reset Delivery Radius for '${branch.name}'?"
                            deleteDialogMessage = "This will set the delivery service radius to 0 km, pausing delivery dispatch for this branch."
                            deleteDialogAction = {
                                viewModel.deleteDeliveryRadius(branch.id) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Delivery radius for '${branch.name}' reset to 0 km")
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }

        // TAB 3: DELIVERY PARTNERS FLEET (List, Status Management, Order Assignment, Add Partner)
        if (selectedAdminTab == 3) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Delivery Fleet & Drivers",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${allDeliveryPartners.size} registered partners • ${allDeliveryPartners.count { it.status == "AVAILABLE" }} available for dispatch",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    Button(
                        onClick = { showAddPartnerDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BerryBurgundy),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("admin_add_partner_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Partner", fontSize = 12.sp)
                    }
                }
            }

            if (allDeliveryPartners.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No delivery partners registered in fleet.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(allDeliveryPartners, key = { it.id }) { partner ->
                    val partnerAssignedOrdersCount = allOrders.count {
                        it.driverName == partner.name &&
                        it.status != OrderStatus.DELIVERED.name
                    }
                    AdminDeliveryPartnerCard(
                        partner = partner,
                        assignedOrdersCount = partnerAssignedOrdersCount,
                        onAssignOrder = {
                            assigningOrderForPartner = partner
                        },
                        onStatusChange = { newStatus ->
                            viewModel.updateDeliveryPartnerStatus(partner.id, newStatus) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Status of ${partner.name} set to $newStatus")
                                }
                            }
                        },
                        onDelete = {
                            deleteDialogTitle = "Delete Partner '${partner.name}'?"
                            deleteDialogMessage = "Are you sure you want to remove this driver from the active delivery fleet?"
                            deleteDialogAction = {
                                viewModel.deleteDeliveryPartner(partner.id) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Partner '${partner.name}' removed from fleet")
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun AdminOrderCard(
    order: OrderEntity,
    onAdvanceStatus: (OrderStatus) -> Unit,
    onDeleteOrder: () -> Unit,
    onDeleteStatus: () -> Unit,
    onDeleteDeliveryPerson: () -> Unit,
    onAssignPartner: () -> Unit = {},
    onViewCustomPhoto: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentStatus = try {
        OrderStatus.valueOf(order.status)
    } catch (_: Exception) {
        null
    }

    val nextStatus = when (currentStatus) {
        OrderStatus.PLACED -> OrderStatus.CONFIRMED
        OrderStatus.CONFIRMED -> OrderStatus.PREPARING
        OrderStatus.PREPARING -> OrderStatus.OUT_FOR_DELIVERY
        OrderStatus.OUT_FOR_DELIVERY -> OrderStatus.DELIVERED
        OrderStatus.DELIVERED -> null
        null -> OrderStatus.CONFIRMED
    }

    val dateFormatter = remember { SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()) }
    val formattedTime = dateFormatter.format(Date(order.createdAtTimestamp))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("admin_order_card_${order.orderId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(true)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.orderId,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Received: $formattedTime • Slot: ${order.deliverySlot}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when (currentStatus) {
                                    OrderStatus.DELIVERED -> PistachioGreen.copy(alpha = 0.2f)
                                    OrderStatus.PREPARING -> HoneyButter
                                    null -> MaterialTheme.colorScheme.errorContainer
                                    else -> MaterialTheme.colorScheme.primaryContainer
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = currentStatus?.label ?: "Status: ${order.status}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = when (currentStatus) {
                                    OrderStatus.DELIVERED -> PistachioGreen
                                    OrderStatus.PREPARING -> DarkAmber
                                    null -> MaterialTheme.colorScheme.onErrorContainer
                                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                                }
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Delete Order Button
                    IconButton(
                        onClick = onDeleteOrder,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_order_button_${order.orderId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Order",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Items & Custom Piping:",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Text(
                text = order.itemsSummary,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )
            )

            // Custom Cake Photo & Customization Display
            val isCustomOrder = order.hasCustomDesign || !order.customPhotoUri.isNullOrBlank() ||
                    order.itemsSummary.contains("Custom", ignoreCase = true) ||
                    order.itemsSummary.contains("Tier", ignoreCase = true) ||
                    order.itemsSummary.contains("Piping", ignoreCase = true)

            if (isCustomOrder) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_custom_cake_section_${order.orderId}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = HoneyButter.copy(alpha = 0.35f)),
                    border = BorderStroke(1.dp, GoldenCaramel.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Customized Cake",
                                    tint = BerryBurgundy,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Customized Cake Order",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = BerryBurgundy
                                    )
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BerryBurgundy
                            ) {
                                Text(
                                    text = "CUSTOM DESIGN",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        if (!order.customPhotoUri.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.85f))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, GoldenCaramel.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .clickable { onViewCustomPhoto(order.customPhotoUri) }
                                        .testTag("admin_order_photo_thumbnail_${order.orderId}")
                                ) {
                                    AsyncImage(
                                        model = order.customPhotoUri,
                                        contentDescription = "Custom cake reference design photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ZoomIn,
                                            contentDescription = "Inspect photo",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Customer Reference Photo",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = BerryBurgundy
                                        )
                                    )
                                    Text(
                                        text = "Photo attached for baking & artisan piping precision.",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Button(
                                        onClick = { onViewCustomPhoto(order.customPhotoUri) },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = BerryBurgundy,
                                            contentColor = Color.White
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier
                                            .height(28.dp)
                                            .testTag("admin_view_photo_button_${order.orderId}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Visibility,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("See Photo", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "ℹ️ Custom studio cake: Customer customized tier count and flavors without an external photo attachment.",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = DarkAmber,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Customer: ${order.customerName} (${order.customerPhone})",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Text(
                    text = "Total: ₹${order.finalTotal.toInt()}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = BerryBurgundy
                    )
                )
            }

            // Delivery Driver info, assign partner and driver deletion option
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🚴 Driver: ${order.driverName.ifEmpty { "None Assigned" }}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(
                        onClick = onAssignPartner,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp).testTag("assign_partner_btn_${order.orderId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AssignmentInd,
                            contentDescription = "Assign driver",
                            tint = BerryBurgundy,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (order.driverName.isNotEmpty() && order.driverName != "None Assigned") "Change" else "Assign",
                            fontSize = 11.sp,
                            color = BerryBurgundy,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (order.driverName.isNotEmpty() && order.driverName != "None Assigned") {
                        TextButton(
                            onClick = onDeleteDeliveryPerson,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp).testTag("delete_delivery_person_${order.orderId}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonRemove,
                                contentDescription = "Remove driver",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Remove",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(10.dp))

            // Action row: Status Delete/Reset & Advance Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete / Reset Status Button
                OutlinedButton(
                    onClick = onDeleteStatus,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("delete_status_button_${order.orderId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = "Reset status",
                        tint = DarkAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Reset Status",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkAmber
                    )
                }

                // 1-Tap Advance Action
                if (nextStatus != null) {
                    Button(
                        onClick = { onAdvanceStatus(nextStatus) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BerryBurgundy),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_advance_button_${order.orderId}")
                    ) {
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "To: ${nextStatus.label}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Delivered",
                            tint = PistachioGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Delivered",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = PistachioGreen,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCakeCard(
    cake: CakeItemEntity,
    onEditCake: () -> Unit,
    onDeleteCake: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("admin_cake_card_${cake.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(true)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(RoseContainer),
                contentAlignment = Alignment.Center
            ) {
                CakeThumbnailImage(
                    cake = cake,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cake.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                val categoryName = try {
                    CakeCategory.valueOf(cake.category).displayName
                } catch (_: Exception) {
                    cake.category
                }
                Text(
                    text = "$categoryName • ₹${cake.basePrice.toInt()} (${cake.defaultWeightKg} kg)",
                    style = MaterialTheme.typography.bodySmall.copy(color = BerryBurgundy, fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = cake.tagline,
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    maxLines = 1
                )
            }

            // Edit Cake Button
            IconButton(
                onClick = onEditCake,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(BerryBurgundy.copy(alpha = 0.1f))
                    .testTag("edit_cake_button_${cake.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Cake",
                    tint = BerryBurgundy,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Delete Cake Button
            IconButton(
                onClick = onDeleteCake,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
                    .testTag("delete_cake_button_${cake.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Cake",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AdminBranchCard(
    branch: BranchEntity,
    onEditBranch: () -> Unit,
    onDeleteBranch: () -> Unit,
    onDeleteDeliveryRadius: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("admin_branch_card_${branch.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(true)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = branch.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (branch.isActive) PistachioGreen.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (branch.isActive) "Active" else "Paused",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (branch.isActive) PistachioGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = branch.address,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = "Phone: ${branch.phone} • Hours: ${branch.operatingHours}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Edit Kitchen Branch Button
                    OutlinedButton(
                        onClick = onEditBranch,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("edit_branch_button_${branch.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Kitchen",
                            tint = BerryBurgundy,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Edit Kitchen",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BerryBurgundy
                        )
                    }

                    // Delete Branch Button
                    IconButton(
                        onClick = onDeleteBranch,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
                            .testTag("delete_branch_button_${branch.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Branch",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(10.dp))

            // Delivery Radius Management Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Delivery Coverage Radius",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = if (branch.deliveryRadiusKm > 0) "${branch.deliveryRadiusKm} km coverage" else "0.0 km (Radius Deleted / Disabled)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (branch.deliveryRadiusKm > 0) PistachioGreen else MaterialTheme.colorScheme.error
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Quick edit radius option
                    OutlinedButton(
                        onClick = onEditBranch,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("edit_radius_button_${branch.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Adjust Radius",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Set Radius",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Delete / Reset Delivery Radius Button
                    OutlinedButton(
                        onClick = onDeleteDeliveryRadius,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("delete_radius_button_${branch.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOff,
                            contentDescription = "Delete delivery radius",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Delete Radius",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomCakePhotoDialog(
    order: OrderEntity,
    photoUri: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Custom Cake Reference",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = BerryBurgundy
                        )
                    )
                    Text(
                        text = "Order #${order.orderId} • ${order.customerName}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close photo")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, GoldenCaramel.copy(alpha = 0.5f)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "High-res custom cake design reference",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 220.dp, max = 380.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Customer Instructions & Piping:",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = order.itemsSummary,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Delivery Slot: ${order.deliverySlot} • Contact: ${order.customerPhone}",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = BerryBurgundy)
            ) {
                Text("Close Reference", color = Color.White)
            }
        }
    )
}
