package com.example.data.remote

import android.util.Log
import com.example.data.local.BranchDao
import com.example.data.local.CakeDao
import com.example.data.local.DeliveryPartnerDao
import com.example.data.local.OrderDao
import com.example.data.local.UserAccountDao
import com.example.data.local.UserDao
import com.example.data.model.BranchEntity
import com.example.data.model.CakeItemEntity
import com.example.data.model.DeliveryPartnerEntity
import com.example.data.model.OrderEntity
import com.example.data.model.UserAccountEntity
import com.example.data.model.UserProfileEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MemoryCacheSettings
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Firebase Manager coordinating automatic real-time synchronization with:
 * 1. Cloud Firestore collections ("users", "orders", "cakes", "branches", "delivery_partners")
 * 2. Firebase Realtime Database (/users, /orders, /cakes, /branches, /delivery_partners)
 */
class FirebaseSyncManager(
    private val userDao: UserDao,
    private val orderDao: OrderDao,
    private val cakeDao: CakeDao,
    private val branchDao: BranchDao,
    private val deliveryPartnerDao: DeliveryPartnerDao,
    private val userAccountDao: UserAccountDao? = null,
    private val coroutineScope: CoroutineScope
) {
    companion object {
        private const val TAG = "FirebaseSync"
        const val COLLECTION_USERS = "users"
        const val COLLECTION_USER_ACCOUNTS = "user_accounts"
        const val COLLECTION_ORDERS = "orders"
        const val COLLECTION_CAKES = "cakes"
        const val COLLECTION_BRANCHES = "branches"
        const val COLLECTION_PARTNERS = "delivery_partners"
    }

    private val _isLiveConnected = MutableStateFlow(true)
    val isLiveConnected: StateFlow<Boolean> = _isLiveConnected.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow("Live Connected")
    val lastSyncTimestamp: StateFlow<String> = _lastSyncTimestamp.asStateFlow()

    private val _firestorePermissionWarning = MutableStateFlow<String?>(null)
    val firestorePermissionWarning: StateFlow<String?> = _firestorePermissionWarning.asStateFlow()

    private var firebaseAuth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null
    private var realtimeDb: DatabaseReference? = null
    private var ordersListenerRegistration: ListenerRegistration? = null
    private var userOrdersListenerRegistration: ListenerRegistration? = null

    private fun getFirestore(): FirebaseFirestore? {
        if (firestore == null) {
            try {
                val db = FirebaseFirestore.getInstance()
                // MemoryCacheSettings ensures NO offline disk storage is used.
                // All data is stored purely in Cloud Firestore and held only in volatile memory during connection.
                val settings = FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
                    .build()
                db.firestoreSettings = settings
                firestore = db
                Log.d(TAG, "Firebase Firestore initialized with pure in-memory cache (disk storage disabled).")
            } catch (e: Exception) {
                try {
                    firestore = FirebaseFirestore.getInstance()
                } catch (fallbackEx: Exception) {
                    Log.w(TAG, "Firestore fallback error: ${fallbackEx.message}")
                }
                Log.w(TAG, "Firestore initialization note: ${e.message}")
            }
        }
        return firestore
    }

    private fun getRealtimeDb(): DatabaseReference? {
        if (realtimeDb == null) {
            try {
                val rtdb = FirebaseDatabase.getInstance()
                realtimeDb = rtdb.reference
                Log.d(TAG, "Firebase Realtime Database initialized.")
            } catch (e: Exception) {
                Log.w(TAG, "Realtime Database initialization error: ${e.message}")
            }
        }
        return realtimeDb
    }

    init {
        initFirebase()
    }

    private fun initFirebase() {
        try {
            firebaseAuth = FirebaseAuth.getInstance()
            if (firebaseAuth?.currentUser == null) {
                firebaseAuth?.signInAnonymously()
                    ?.addOnSuccessListener { result ->
                        Log.d(TAG, "FirebaseAuth anonymous sign-in success: ${result.user?.uid}")
                    }
                    ?.addOnFailureListener { e ->
                        Log.w(TAG, "FirebaseAuth anonymous auth note: ${e.message}")
                    }
            } else {
                Log.d(TAG, "FirebaseAuth already active: ${firebaseAuth?.currentUser?.uid}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseAuth setup note: ${e.message}")
        }

        getFirestore()
        getRealtimeDb()
        startRealtimeListeners()
    }

    private fun updateTimestamp() {
        val time = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())
        _lastSyncTimestamp.value = "Auto Live $time"
        _isLiveConnected.value = true
    }

    private fun handleFirestoreError(action: String, e: Exception) {
        if (e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
            _firestorePermissionWarning.value = "Firestore Security Rules: Go to Firebase Console -> Firestore Database -> Rules and set 'allow read, write: if true;' (or enable Anonymous Auth in Authentication tab)."
            Log.w(TAG, "Firestore permission notice for $action (handled gracefully): ${e.message}")
        } else if (e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true) {
            _firestorePermissionWarning.value = "Firestore Security Rules: Go to Firebase Console -> Firestore Database -> Rules and set 'allow read, write: if true;' (or enable Anonymous Auth in Authentication tab)."
            Log.w(TAG, "Firestore permission notice for $action (handled gracefully): ${e.message}")
        } else {
            Log.w(TAG, "Firestore sync note for $action: ${e.message}")
        }
    }

    // -------------------------------------------------------------
    // USER SYNC (Firestore "users" collection & RTDB /users)
    // -------------------------------------------------------------
    fun syncUserToFirebase(user: UserProfileEntity) {
        val userMap = hashMapOf(
            "id" to user.id,
            "name" to user.name,
            "email" to user.email,
            "phone" to user.phone,
            "deliveryAddress" to user.deliveryAddress,
            "loyaltyPoints" to user.loyaltyPoints,
            "totalOrdersCount" to user.totalOrdersCount,
            "lifetimeSpent" to user.lifetimeSpent,
            "membershipTier" to user.membershipTier,
            "tierDiscountPercent" to user.tierDiscountPercent,
            "updatedAt" to System.currentTimeMillis()
        )

        val docId = if (user.email.isNotBlank()) {
            user.email.replace("/", "_").replace(".", "_")
        } else {
            user.id
        }

        // 1. Write to Cloud Firestore collection "users"
        firestore?.collection(COLLECTION_USERS)?.document(docId)?.set(userMap, SetOptions.merge())
            ?.addOnSuccessListener {
                Log.d(TAG, "Successfully synced user to Firestore collection 'users': $docId")
                updateTimestamp()
            }
            ?.addOnFailureListener { e ->
                handleFirestoreError("syncing user", e)
            }

        // 2. Also write to Realtime Database
        realtimeDb?.child(COLLECTION_USERS)?.child(docId)?.setValue(userMap)
            ?.addOnSuccessListener { updateTimestamp() }
            ?.addOnFailureListener { e ->
                Log.w(TAG, "RTDB sync user note: ${e.message}")
            }
    }

    // -------------------------------------------------------------
    // USER ACCOUNT SYNC (Firestore "user_accounts" collection for multi-device login)
    // -------------------------------------------------------------
    fun syncUserAccountToFirebase(account: UserAccountEntity) {
        val accountMap = hashMapOf(
            "id" to account.id,
            "name" to account.name,
            "email" to account.email,
            "phone" to account.phone,
            "password" to account.password,
            "address" to account.address,
            "role" to account.role,
            "updatedAt" to System.currentTimeMillis()
        )
        val docId = account.id.replace("/", "_").replace(".", "_")
        firestore?.collection(COLLECTION_USER_ACCOUNTS)?.document(docId)?.set(accountMap, SetOptions.merge())
            ?.addOnSuccessListener {
                Log.d(TAG, "Synced user account to cloud for cross-device login: $docId")
                updateTimestamp()
            }
            ?.addOnFailureListener { e ->
                Log.w(TAG, "User account cloud sync note: ${e.message}")
            }
    }

    suspend fun fetchUserAccountFromCloud(identifier: String): UserAccountEntity? = suspendCancellableCoroutine { cont ->
        val clean = identifier.trim().lowercase()
        val docId = clean.replace("/", "_").replace(".", "_")
        val coll = firestore?.collection(COLLECTION_USER_ACCOUNTS)
        if (coll == null) {
            cont.resume(null)
            return@suspendCancellableCoroutine
        }

        coll.document(docId).get().addOnSuccessListener { doc ->
            if (doc != null && doc.exists()) {
                val acc = UserAccountEntity(
                    id = doc.getString("id") ?: clean,
                    name = doc.getString("name") ?: "Cake Lover",
                    email = doc.getString("email") ?: clean,
                    phone = doc.getString("phone") ?: "",
                    password = doc.getString("password") ?: "",
                    address = doc.getString("address") ?: "",
                    role = doc.getString("role") ?: "CUSTOMER"
                )
                cont.resume(acc)
            } else {
                // Try finding by email
                coll.whereEqualTo("email", clean).limit(1).get().addOnSuccessListener { snap ->
                    if (snap != null && !snap.isEmpty) {
                        val d = snap.documents[0]
                        val acc = UserAccountEntity(
                            id = d.getString("id") ?: clean,
                            name = d.getString("name") ?: "Cake Lover",
                            email = d.getString("email") ?: clean,
                            phone = d.getString("phone") ?: "",
                            password = d.getString("password") ?: "",
                            address = d.getString("address") ?: "",
                            role = d.getString("role") ?: "CUSTOMER"
                        )
                        cont.resume(acc)
                    } else {
                        // Try finding by phone
                        coll.whereEqualTo("phone", clean).limit(1).get().addOnSuccessListener { phoneSnap ->
                            if (phoneSnap != null && !phoneSnap.isEmpty) {
                                val d = phoneSnap.documents[0]
                                val acc = UserAccountEntity(
                                    id = d.getString("id") ?: clean,
                                    name = d.getString("name") ?: "Cake Lover",
                                    email = d.getString("email") ?: clean,
                                    phone = d.getString("phone") ?: "",
                                    password = d.getString("password") ?: "",
                                    address = d.getString("address") ?: "",
                                    role = d.getString("role") ?: "CUSTOMER"
                                )
                                cont.resume(acc)
                            } else {
                                cont.resume(null)
                            }
                        }.addOnFailureListener { cont.resume(null) }
                    }
                }.addOnFailureListener { cont.resume(null) }
            }
        }.addOnFailureListener { cont.resume(null) }
    }

    // -------------------------------------------------------------
    // ORDER SYNC (Firestore "orders" collection & RTDB /orders)
    // -------------------------------------------------------------
    fun syncOrderToFirebase(order: OrderEntity) {
        // Never sync old demo orders
        if (order.orderId == "CKL-7821" || order.orderId == "CKL-5612") {
            return
        }

        val orderMap = hashMapOf(
            "orderId" to order.orderId,
            "userId" to order.userId,
            "customerEmail" to order.customerEmail,
            "createdAtTimestamp" to order.createdAtTimestamp,
            "status" to order.status,
            "statusTimestamp" to order.statusTimestamp,
            "fulfillmentType" to order.fulfillmentType,
            "deliveryAddress" to order.deliveryAddress,
            "deliverySlot" to order.deliverySlot,
            "deliveryDate" to order.deliveryDate,
            "branchId" to order.branchId,
            "branchName" to order.branchName,
            "subtotal" to order.subtotal,
            "deliveryFee" to order.deliveryFee,
            "pointsDiscount" to order.pointsDiscount,
            "finalTotal" to order.finalTotal,
            "paymentMethod" to order.paymentMethod,
            "customerName" to order.customerName,
            "customerPhone" to order.customerPhone,
            "driverName" to order.driverName,
            "driverPhone" to order.driverPhone,
            "itemsSummary" to order.itemsSummary,
            "customPhotoUri" to (order.customPhotoUri ?: ""),
            "hasCustomDesign" to order.hasCustomDesign,
            "updatedAt" to System.currentTimeMillis()
        )

        // 1. Write to Cloud Firestore collection "orders"
        firestore?.collection(COLLECTION_ORDERS)?.document(order.orderId)?.set(orderMap, SetOptions.merge())
            ?.addOnSuccessListener {
                Log.d(TAG, "Successfully synced order to Firestore collection 'orders': ${order.orderId}")
                updateTimestamp()
            }
            ?.addOnFailureListener { e ->
                handleFirestoreError("syncing order", e)
            }

        // 2. Also write to Realtime Database
        realtimeDb?.child(COLLECTION_ORDERS)?.child(order.orderId)?.setValue(orderMap)
            ?.addOnSuccessListener { updateTimestamp() }
            ?.addOnFailureListener { e ->
                Log.w(TAG, "RTDB sync order note: ${e.message}")
            }
    }

    fun syncOrderStatusUpdate(orderId: String, newStatus: String, timestamp: Long) {
        val updates = mapOf(
            "status" to newStatus,
            "statusTimestamp" to timestamp,
            "updatedAt" to System.currentTimeMillis()
        )

        firestore?.collection(COLLECTION_ORDERS)?.document(orderId)?.update(updates)
            ?.addOnSuccessListener { updateTimestamp() }
            ?.addOnFailureListener { e -> handleFirestoreError("updating order status", e) }

        realtimeDb?.child(COLLECTION_ORDERS)?.child(orderId)?.updateChildren(updates)
            ?.addOnSuccessListener { updateTimestamp() }
            ?.addOnFailureListener { e -> Log.w(TAG, "RTDB update order status note: ${e.message}") }
    }

    fun syncOrderDriverAssignment(orderId: String, driverName: String, driverPhone: String) {
        val updates = mapOf(
            "driverName" to driverName,
            "driverPhone" to driverPhone,
            "status" to "OUT_FOR_DELIVERY",
            "statusTimestamp" to System.currentTimeMillis(),
            "updatedAt" to System.currentTimeMillis()
        )

        firestore?.collection(COLLECTION_ORDERS)?.document(orderId)?.update(updates)
            ?.addOnSuccessListener { updateTimestamp() }
            ?.addOnFailureListener { e -> handleFirestoreError("assigning driver", e) }

        realtimeDb?.child(COLLECTION_ORDERS)?.child(orderId)?.updateChildren(updates)
            ?.addOnSuccessListener { updateTimestamp() }
            ?.addOnFailureListener { e -> Log.w(TAG, "RTDB assign driver note: ${e.message}") }
    }

    fun syncOrderCustomPhotoUpdate(orderId: String, photoUri: String) {
        val updates = mapOf(
            "customPhotoUri" to photoUri,
            "hasCustomDesign" to true,
            "updatedAt" to System.currentTimeMillis()
        )

        firestore?.collection(COLLECTION_ORDERS)?.document(orderId)?.update(updates)
            ?.addOnSuccessListener { updateTimestamp() }
            ?.addOnFailureListener { e -> handleFirestoreError("updating custom cake photo", e) }

        realtimeDb?.child(COLLECTION_ORDERS)?.child(orderId)?.updateChildren(updates)
            ?.addOnSuccessListener { updateTimestamp() }
            ?.addOnFailureListener { e -> Log.w(TAG, "RTDB update custom cake photo note: ${e.message}") }
    }

    fun deleteOrderFromFirebase(orderId: String) {
        firestore?.collection(COLLECTION_ORDERS)?.document(orderId)?.delete()
            ?.addOnSuccessListener { updateTimestamp() }
            ?.addOnFailureListener { e -> handleFirestoreError("deleting order", e) }

        realtimeDb?.child(COLLECTION_ORDERS)?.child(orderId)?.removeValue()
            ?.addOnSuccessListener { updateTimestamp() }
            ?.addOnFailureListener { e -> Log.w(TAG, "RTDB delete order note: ${e.message}") }
    }

    // -------------------------------------------------------------
    // CAKE SYNC (Firestore "cakes" collection & RTDB /cakes)
    // -------------------------------------------------------------
    fun syncCakeToFirebase(cake: CakeItemEntity) {
        val cakeMap = hashMapOf(
            "id" to cake.id,
            "name" to cake.name,
            "tagline" to cake.tagline,
            "description" to cake.description,
            "category" to cake.category,
            "basePrice" to cake.basePrice,
            "rating" to cake.rating,
            "reviewsCount" to cake.reviewsCount,
            "isEggless" to cake.isEggless,
            "isGlutenFree" to cake.isGlutenFree,
            "isNutFree" to cake.isNutFree,
            "leadTimeHours" to cake.leadTimeHours,
            "flavorNotes" to cake.flavorNotes,
            "defaultWeightKg" to cake.defaultWeightKg,
            "isChefSpecial" to cake.isChefSpecial,
            "isBestSeller" to cake.isBestSeller,
            "localDrawableName" to cake.localDrawableName,
            "updatedAt" to System.currentTimeMillis()
        )

        firestore?.collection(COLLECTION_CAKES)?.document(cake.id)?.set(cakeMap, SetOptions.merge())
            ?.addOnSuccessListener { updateTimestamp() }
            ?.addOnFailureListener { e -> handleFirestoreError("syncing cake", e) }

        realtimeDb?.child(COLLECTION_CAKES)?.child(cake.id)?.setValue(cakeMap)
            ?.addOnSuccessListener { updateTimestamp() }
            ?.addOnFailureListener { e -> Log.w(TAG, "RTDB sync cake note: ${e.message}") }
    }

    fun deleteCakeFromFirebase(cakeId: String) {
        firestore?.collection(COLLECTION_CAKES)?.document(cakeId)?.delete()
            ?.addOnFailureListener { e -> handleFirestoreError("deleting cake", e) }
        realtimeDb?.child(COLLECTION_CAKES)?.child(cakeId)?.removeValue()
    }

    // -------------------------------------------------------------
    // BRANCH SYNC (Firestore "branches" collection & RTDB /branches)
    // -------------------------------------------------------------
    fun syncBranchToFirebase(branch: BranchEntity) {
        val branchMap = hashMapOf(
            "id" to branch.id,
            "name" to branch.name,
            "address" to branch.address,
            "phone" to branch.phone,
            "latitude" to branch.latitude,
            "longitude" to branch.longitude,
            "deliveryRadiusKm" to branch.deliveryRadiusKm,
            "operatingHours" to branch.operatingHours,
            "rating" to branch.rating,
            "isActive" to branch.isActive,
            "updatedAt" to System.currentTimeMillis()
        )

        firestore?.collection(COLLECTION_BRANCHES)?.document(branch.id)?.set(branchMap, SetOptions.merge())
            ?.addOnSuccessListener { updateTimestamp() }
            ?.addOnFailureListener { e -> handleFirestoreError("syncing branch", e) }

        realtimeDb?.child(COLLECTION_BRANCHES)?.child(branch.id)?.setValue(branchMap)
            ?.addOnSuccessListener { updateTimestamp() }
            ?.addOnFailureListener { e -> Log.w(TAG, "RTDB sync branch note: ${e.message}") }
    }

    fun deleteBranchFromFirebase(branchId: String) {
        firestore?.collection(COLLECTION_BRANCHES)?.document(branchId)?.delete()
            ?.addOnFailureListener { e -> handleFirestoreError("deleting branch", e) }
        realtimeDb?.child(COLLECTION_BRANCHES)?.child(branchId)?.removeValue()
    }

    // -------------------------------------------------------------
    // DELIVERY PARTNER SYNC (Firestore "delivery_partners" collection & RTDB)
    // -------------------------------------------------------------
    fun syncPartnerToFirebase(partner: DeliveryPartnerEntity) {
        val partnerMap = hashMapOf(
            "id" to partner.id,
            "name" to partner.name,
            "phone" to partner.phone,
            "vehicleType" to partner.vehicleType,
            "vehicleNumber" to partner.vehicleNumber,
            "status" to partner.status,
            "rating" to partner.rating,
            "totalDeliveriesCompleted" to partner.totalDeliveriesCompleted,
            "currentZone" to partner.currentZone,
            "updatedAt" to System.currentTimeMillis()
        )

        firestore?.collection(COLLECTION_PARTNERS)?.document(partner.id)?.set(partnerMap, SetOptions.merge())
            ?.addOnSuccessListener { updateTimestamp() }
            ?.addOnFailureListener { e -> handleFirestoreError("syncing partner", e) }

        realtimeDb?.child(COLLECTION_PARTNERS)?.child(partner.id)?.setValue(partnerMap)
            ?.addOnSuccessListener { updateTimestamp() }
            ?.addOnFailureListener { e -> Log.w(TAG, "RTDB sync partner note: ${e.message}") }
    }

    fun deletePartnerFromFirebase(partnerId: String) {
        firestore?.collection(COLLECTION_PARTNERS)?.document(partnerId)?.delete()
            ?.addOnFailureListener { e -> handleFirestoreError("deleting partner", e) }
        realtimeDb?.child(COLLECTION_PARTNERS)?.child(partnerId)?.removeValue()
    }

    // -------------------------------------------------------------
    // FULL TWO-WAY INITIAL PUSH & SYNC
    // -------------------------------------------------------------
    suspend fun pushAllLocalDataToFirebase() {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                // Remove legacy default demo orders from Firestore and RTDB
                firestore?.collection(COLLECTION_ORDERS)?.document("CKL-7821")?.delete()
                firestore?.collection(COLLECTION_ORDERS)?.document("CKL-5612")?.delete()
                realtimeDb?.child(COLLECTION_ORDERS)?.child("CKL-7821")?.removeValue()
                realtimeDb?.child(COLLECTION_ORDERS)?.child("CKL-5612")?.removeValue()

                // Clean up default demo orders locally
                orderDao.deleteDefaultDemoOrders()

                // 0. Sync All User Accounts for cloud auth & multi-device login
                val accounts = userAccountDao?.getAllAccounts() ?: emptyList()
                for (account in accounts) {
                    syncUserAccountToFirebase(account)
                }

                // 1. Sync User Profile
                val user = userDao.getUserProfileDirect()
                if (user != null) {
                    syncUserToFirebase(user)
                }

                // 2. Sync Real User Orders (excluding demo orders)
                val orders = orderDao.getAllOrdersDirect().filter { it.orderId != "CKL-7821" && it.orderId != "CKL-5612" }
                for (order in orders) {
                    syncOrderToFirebase(order)
                }

                // 3. Sync All Cakes
                val cakes = cakeDao.getAllCakesDirect()
                for (cake in cakes) {
                    syncCakeToFirebase(cake)
                }

                // 4. Sync All Branches
                val branches = branchDao.getAllBranchesDirect()
                for (branch in branches) {
                    syncBranchToFirebase(branch)
                }

                // 5. Sync All Delivery Partners
                val partners = deliveryPartnerDao.getAllPartnersDirect()
                for (partner in partners) {
                    syncPartnerToFirebase(partner)
                }

                updateTimestamp()
                Log.d(TAG, "Pushed ${orders.size} orders, ${cakes.size} cakes, ${branches.size} branches, and user to Firebase collections.")
            } catch (e: Exception) {
                Log.w(TAG, "Push local data note: ${e.message}")
            }
        }
    }

    // -------------------------------------------------------------
    // INDIVIDUAL USER ORDER CLOUD SYNC (Cross-Device & Login Sync)
    // -------------------------------------------------------------
    suspend fun syncUserOrdersFromFirestore(email: String, userId: String): Int = suspendCancellableCoroutine { cont ->
        val cleanEmail = email.trim().lowercase()
        val ordersColl = firestore?.collection(COLLECTION_ORDERS)
        if (ordersColl == null || (cleanEmail.isBlank() && userId.isBlank())) {
            cont.resume(0)
            return@suspendCancellableCoroutine
        }

        val query = if (cleanEmail.isNotBlank()) {
            ordersColl.whereEqualTo("customerEmail", cleanEmail)
        } else {
            ordersColl.whereEqualTo("userId", userId)
        }

        query.get().addOnSuccessListener { snapshot ->
            val userOrders = mutableListOf<OrderEntity>()
            if (snapshot != null) {
                for (doc in snapshot.documents) {
                    val data = doc.data ?: continue
                    val order = parseOrderFromMap(data)
                    if (order != null) {
                        userOrders.add(order)
                    }
                }
            }

            coroutineScope.launch(Dispatchers.IO) {
                if (userOrders.isNotEmpty()) {
                    orderDao.insertOrders(userOrders)
                    Log.d(TAG, "Synced ${userOrders.size} individual orders from cloud for $cleanEmail")
                }
                if (cont.isActive) {
                    cont.resume(userOrders.size)
                }
            }
        }.addOnFailureListener { e ->
            Log.w(TAG, "Failed syncing user orders: ${e.message}")
            if (cont.isActive) {
                cont.resume(0)
            }
        }
    }

    fun startListeningToUserOrders(email: String, userId: String) {
        userOrdersListenerRegistration?.remove()
        val cleanEmail = email.trim().lowercase()
        val ordersColl = firestore?.collection(COLLECTION_ORDERS) ?: return
        if (cleanEmail.isBlank() && userId.isBlank()) return

        val query = if (cleanEmail.isNotBlank()) {
            ordersColl.whereEqualTo("customerEmail", cleanEmail)
        } else {
            ordersColl.whereEqualTo("userId", userId)
        }

        userOrdersListenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Live user order listener note: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                coroutineScope.launch(Dispatchers.IO) {
                    val orders = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { parseOrderFromMap(it) }
                    }
                    if (orders.isNotEmpty()) {
                        orderDao.insertOrders(orders)
                        updateTimestamp()
                    }
                }
            }
        }
    }

    fun stopListeningToUserOrders() {
        userOrdersListenerRegistration?.remove()
        userOrdersListenerRegistration = null
    }

    // -------------------------------------------------------------
    // REAL-TIME LISTENERS (Pull live cloud changes to local state)
    // -------------------------------------------------------------
    private fun startRealtimeListeners() {
        // 1. Listen to Firestore orders collection for live cloud status updates of all orders
        ordersListenerRegistration = firestore?.collection(COLLECTION_ORDERS)?.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError("listening to orders", error)
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                coroutineScope.launch(Dispatchers.IO) {
                    val ordersList = mutableListOf<OrderEntity>()
                    for (doc in snapshot.documents) {
                        val data = doc.data ?: continue
                        val order = parseOrderFromMap(data)
                        if (order != null) {
                            ordersList.add(order)
                        }
                    }
                    if (ordersList.isNotEmpty()) {
                        orderDao.insertOrders(ordersList)
                    }
                    updateTimestamp()
                }
            }
        }

        // 2. Listen to Firestore cakes collection for live catalog updates
        firestore?.collection(COLLECTION_CAKES)?.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError("listening to cakes", error)
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                coroutineScope.launch(Dispatchers.IO) {
                    val cakes = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { parseCakeFromMap(it) }
                    }
                    if (cakes.isNotEmpty()) {
                        cakeDao.insertCakes(cakes)
                    }
                }
            }
        }

        // 3. Listen to Firestore branches collection for live bakery branch updates
        firestore?.collection(COLLECTION_BRANCHES)?.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError("listening to branches", error)
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                coroutineScope.launch(Dispatchers.IO) {
                    val branches = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { parseBranchFromMap(it) }
                    }
                    if (branches.isNotEmpty()) {
                        branchDao.insertBranches(branches)
                    }
                }
            }
        }

        // 4. Listen to Firestore delivery partners for real-time fleet updates
        firestore?.collection(COLLECTION_PARTNERS)?.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError("listening to delivery partners", error)
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                coroutineScope.launch(Dispatchers.IO) {
                    val partners = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { parsePartnerFromMap(it) }
                    }
                    if (partners.isNotEmpty()) {
                        deliveryPartnerDao.insertPartners(partners)
                    }
                }
            }
        }

        // Also listen to Realtime Database as fallback for status changes of existing orders
        realtimeDb?.child(COLLECTION_ORDERS)?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                coroutineScope.launch(Dispatchers.IO) {
                    for (child in snapshot.children) {
                        val map = child.value as? Map<*, *> ?: continue
                        val order = parseOrderFromMap(map)
                        if (order != null) {
                            orderDao.insertOrder(order)
                        }
                    }
                    updateTimestamp()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "RTDB orders listener cancelled: ${error.message}")
            }
        })
    }

    private fun parseCakeFromMap(map: Map<*, *>): CakeItemEntity? {
        val id = map["id"]?.toString() ?: return null
        return CakeItemEntity(
            id = id,
            name = map["name"]?.toString() ?: "Artisanal Cake",
            tagline = map["tagline"]?.toString() ?: "",
            description = map["description"]?.toString() ?: "",
            category = map["category"]?.toString() ?: "Chocolate",
            basePrice = (map["basePrice"] as? Number)?.toDouble() ?: 550.0,
            rating = (map["rating"] as? Number)?.toDouble() ?: 4.8,
            reviewsCount = (map["reviewsCount"] as? Number)?.toInt() ?: 120,
            isEggless = (map["isEggless"] as? Boolean) ?: true,
            isGlutenFree = (map["isGlutenFree"] as? Boolean) ?: false,
            isNutFree = (map["isNutFree"] as? Boolean) ?: false,
            leadTimeHours = (map["leadTimeHours"] as? Number)?.toInt() ?: 2,
            flavorNotes = map["flavorNotes"]?.toString() ?: "",
            defaultWeightKg = (map["defaultWeightKg"] as? Number)?.toDouble() ?: 1.0,
            isChefSpecial = (map["isChefSpecial"] as? Boolean) ?: false,
            isBestSeller = (map["isBestSeller"] as? Boolean) ?: false,
            localDrawableName = map["localDrawableName"]?.toString() ?: "cake_placeholder"
        )
    }

    private fun parseBranchFromMap(map: Map<*, *>): BranchEntity? {
        val id = map["id"]?.toString() ?: return null
        return BranchEntity(
            id = id,
            name = map["name"]?.toString() ?: "Bakery Branch",
            address = map["address"]?.toString() ?: "Bengaluru",
            phone = map["phone"]?.toString() ?: "",
            latitude = (map["latitude"] as? Number)?.toDouble() ?: 12.9716,
            longitude = (map["longitude"] as? Number)?.toDouble() ?: 77.5946,
            deliveryRadiusKm = (map["deliveryRadiusKm"] as? Number)?.toDouble() ?: 15.0,
            operatingHours = map["operatingHours"]?.toString() ?: "8:00 AM - 11:00 PM",
            rating = (map["rating"] as? Number)?.toDouble() ?: 4.9,
            isActive = (map["isActive"] as? Boolean) ?: true
        )
    }

    private fun parsePartnerFromMap(map: Map<*, *>): DeliveryPartnerEntity? {
        val id = map["id"]?.toString() ?: return null
        return DeliveryPartnerEntity(
            id = id,
            name = map["name"]?.toString() ?: "Delivery Partner",
            phone = map["phone"]?.toString() ?: "",
            vehicleType = map["vehicleType"]?.toString() ?: "EV Scooter",
            vehicleNumber = map["vehicleNumber"]?.toString() ?: "",
            status = map["status"]?.toString() ?: "AVAILABLE",
            rating = (map["rating"] as? Number)?.toDouble() ?: 4.9,
            totalDeliveriesCompleted = (map["totalDeliveriesCompleted"] as? Number)?.toInt() ?: 0,
            currentZone = map["currentZone"]?.toString() ?: "Central Bengaluru"
        )
    }

    private fun parseOrderFromMap(map: Map<*, *>): OrderEntity? {
        val orderId = map["orderId"]?.toString() ?: return null
        // Skip default demo orders
        if (orderId == "CKL-7821" || orderId == "CKL-5612") return null

        return OrderEntity(
            orderId = orderId,
            userId = map["userId"]?.toString() ?: "",
            customerEmail = map["customerEmail"]?.toString() ?: "",
            createdAtTimestamp = (map["createdAtTimestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            status = map["status"]?.toString() ?: "PLACED",
            statusTimestamp = (map["statusTimestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            fulfillmentType = map["fulfillmentType"]?.toString() ?: "DELIVERY",
            deliveryAddress = map["deliveryAddress"]?.toString() ?: "",
            deliverySlot = map["deliverySlot"]?.toString() ?: "STANDARD",
            deliveryDate = map["deliveryDate"]?.toString() ?: "Today",
            branchId = map["branchId"]?.toString() ?: "",
            branchName = map["branchName"]?.toString() ?: "",
            subtotal = (map["subtotal"] as? Number)?.toDouble() ?: 0.0,
            deliveryFee = (map["deliveryFee"] as? Number)?.toDouble() ?: 0.0,
            pointsDiscount = (map["pointsDiscount"] as? Number)?.toDouble() ?: 0.0,
            finalTotal = (map["finalTotal"] as? Number)?.toDouble() ?: 0.0,
            paymentMethod = map["paymentMethod"]?.toString() ?: "UPI",
            customerName = map["customerName"]?.toString() ?: "",
            customerPhone = map["customerPhone"]?.toString() ?: "",
            driverName = map["driverName"]?.toString() ?: "Chef & Rider Vikram Rao",
            driverPhone = map["driverPhone"]?.toString() ?: "+919876543210",
            itemsSummary = map["itemsSummary"]?.toString() ?: "",
            customPhotoUri = map["customPhotoUri"]?.toString()?.takeIf { it.isNotBlank() },
            hasCustomDesign = (map["hasCustomDesign"] as? Boolean) ?: (!map["customPhotoUri"]?.toString().isNullOrBlank())
        )
    }
}
