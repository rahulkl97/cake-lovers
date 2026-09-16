package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BranchEntity
import com.example.data.model.CakeCategory
import com.example.data.model.CakeItemEntity
import com.example.data.model.CartItemEntity
import com.example.data.model.DeliveryPartnerEntity
import com.example.data.model.DeliverySlot
import com.example.data.model.FulfillmentType
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.UserProfileEntity
import com.example.data.repository.AuthResult
import com.example.data.repository.CakeLoversRepository
import com.example.util.NetworkConnectivityObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class SortOption(val label: String) {
    FEATURED("Featured"),
    RATING("Highest Rated"),
    PRICE_LOW_HIGH("Price: Low to High"),
    PRICE_HIGH_LOW("Price: High to Low"),
    LEAD_TIME("Fastest Prep")
}

data class PresetLocation(
    val title: String,
    val address: String,
    val lat: Double,
    val lon: Double,
    val city: String
)

data class CustomStudioState(
    val tiersCount: Int = 1,
    val weightKg: Double = 1.0,
    val spongeFlavor: String = "Red Velvet Classic",
    val frostingFlavor: String = "Cream Cheese Frosting",
    val customPipingText: String = "Happy Celebration!",
    val referencePhotoUri: String? = null,
    val celebrationKitIncluded: Boolean = true
) {
    val dynamicPrice: Double
        get() {
            // Dynamic price calculation engine
            val baseRatePerKg = 750.0
            val tierSurcharge = when (tiersCount) {
                1 -> 0.0
                2 -> 450.0
                3 -> 950.0
                else -> 0.0
            }
            val premiumFlavorBonus = when (spongeFlavor) {
                "Saffron Cardamom (Rasmalai Base)" -> 150.0
                "Belgian Dark Chocolate" -> 100.0
                else -> 50.0
            } + when (frostingFlavor) {
                "Rose Cardamom Rabdi Cream" -> 150.0
                "Belgian Dark Chocolate Ganache" -> 120.0
                else -> 50.0
            }
            return (weightKg * baseRatePerKg) + tierSurcharge + premiumFlavorBonus
        }
}

class CakeLoversViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CakeLoversRepository(application)

    // Network Connectivity Observer (App only works online with Cloud Firestore)
    private val networkObserver = NetworkConnectivityObserver(application)
    val isNetworkAvailable: StateFlow<Boolean> = networkObserver.isConnectedFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = networkObserver.isCurrentlyConnected()
        )

    fun checkNetworkConnection(): Boolean {
        return networkObserver.isCurrentlyConnected()
    }

    // User location state (Default: Central Indiranagar, Bengaluru)
    private val _userLatitude = MutableStateFlow(12.9716)
    val userLatitude: StateFlow<Double> = _userLatitude.asStateFlow()

    private val _userLongitude = MutableStateFlow(77.5946)
    val userLongitude: StateFlow<Double> = _userLongitude.asStateFlow()

    private val _userLocationName = MutableStateFlow("Indiranagar, Bengaluru")
    val userLocationName: StateFlow<String> = _userLocationName.asStateFlow()

    private val _hasLocationPermission = MutableStateFlow(false)
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()

    private val _isDetectingLocation = MutableStateFlow(false)
    val isDetectingLocation: StateFlow<Boolean> = _isDetectingLocation.asStateFlow()

    // Preset delivery localities for instant selection
    val presetLocations = listOf(
        PresetLocation("Indiranagar 100ft Rd, Bengaluru", "100 Feet Rd, Indiranagar, Bengaluru, Karnataka 560038", 12.9784, 77.6408, "Bengaluru"),
        PresetLocation("Koramangala 4th Block, Bengaluru", "80 Feet Rd, 4th Block, Koramangala, Bengaluru, Karnataka 560034", 12.9352, 77.6245, "Bengaluru"),
        PresetLocation("Whitefield ITPL Hub, Bengaluru", "ITPL Main Rd, Whitefield, Bengaluru, Karnataka 560066", 12.9854, 77.7310, "Bengaluru"),
        PresetLocation("Bandra West Linking Rd, Mumbai", "Linking Road, Bandra West, Mumbai, Maharashtra 400050", 19.0600, 72.8335, "Mumbai"),
        PresetLocation("Powai Hiranandani, Mumbai", "Central Ave, Hiranandani Gardens, Powai, Mumbai 400076", 19.1197, 72.9051, "Mumbai"),
        PresetLocation("Connaught Place, New Delhi", "Inner Circle, Connaught Place, New Delhi 110001", 28.6315, 77.2167, "New Delhi")
    )

    // Auth Navigation & Guest Order Sign-Up Flow
    private val _authInitialTab = MutableStateFlow(0) // 0 = Sign In, 1 = Create Account
    val authInitialTab: StateFlow<Int> = _authInitialTab.asStateFlow()

    private val _recentGuestOrderId = MutableStateFlow<String?>(null)
    val recentGuestOrderId: StateFlow<String?> = _recentGuestOrderId.asStateFlow()

    // True when checkout was initiated while unauthenticated (requires signup before placing order)
    private val _pendingCheckoutAfterAuth = MutableStateFlow(false)
    val pendingCheckoutAfterAuth: StateFlow<Boolean> = _pendingCheckoutAfterAuth.asStateFlow()

    fun setPendingCheckoutAfterAuth(pending: Boolean) {
        _pendingCheckoutAfterAuth.value = pending
    }

    fun setAuthInitialTab(tabIndex: Int) {
        _authInitialTab.value = tabIndex
    }

    fun setRecentGuestOrderId(orderId: String?) {
        _recentGuestOrderId.value = orderId
    }

    fun setLocationPermission(granted: Boolean) {
        _hasLocationPermission.value = granted
    }

    fun setDetectingLocation(detecting: Boolean) {
        _isDetectingLocation.value = detecting
    }

    fun selectPresetLocation(preset: PresetLocation) {
        setUserLocation(preset.lat, preset.lon, preset.title)
        _guestAddress.value = preset.address
    }

    fun detectDeviceLocation(context: Context, onComplete: ((String) -> Unit)? = null) {
        _isDetectingLocation.value = true
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
                        // Handled safely
                    }
                }

                if (bestLocation != null) {
                    val lat = bestLocation.latitude
                    val lon = bestLocation.longitude
                    var resolvedName = "GPS: ${String.format(Locale.getDefault(), "%.4f", lat)}, ${String.format(Locale.getDefault(), "%.4f", lon)}"
                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(lat, lon, 1)
                        val address = addresses?.firstOrNull()
                        if (address != null) {
                            val line = address.getAddressLine(0)
                            val locality = address.locality ?: address.subLocality
                            resolvedName = if (!line.isNullOrBlank()) line else (locality ?: resolvedName)
                        }
                    } catch (_: Exception) {
                        // Fallback to coordinates
                    }
                    setUserLocation(lat, lon, resolvedName)
                    _guestAddress.value = resolvedName
                    onComplete?.invoke(resolvedName)
                } else {
                    // Fallback to active flagship hub
                    val defaultName = "Indiranagar 100ft Rd, Bengaluru"
                    setUserLocation(12.9784, 77.6408, defaultName)
                    if (_guestAddress.value.isBlank()) {
                        _guestAddress.value = "100 Feet Rd, Indiranagar, Bengaluru, Karnataka 560038"
                    }
                    onComplete?.invoke(defaultName)
                }
            } else {
                val defaultName = "Indiranagar 100ft Rd, Bengaluru"
                setUserLocation(12.9784, 77.6408, defaultName)
                onComplete?.invoke(defaultName)
            }
        } catch (_: Exception) {
            val defaultName = "Indiranagar 100ft Rd, Bengaluru"
            setUserLocation(12.9784, 77.6408, defaultName)
            onComplete?.invoke(defaultName)
        } finally {
            _isDetectingLocation.value = false
        }
    }

    fun setUserLocation(lat: Double, lon: Double, locationName: String) {
        _userLatitude.value = lat
        _userLongitude.value = lon
        _userLocationName.value = locationName
        _isDetectingLocation.value = false
        _hasLocationPermission.value = true

        // Automatically pick the nearest branch
        val branches = allBranches.value
        if (branches.isNotEmpty()) {
            val nearest = branches.minByOrNull { calculateDistanceKm(it.latitude, it.longitude) }
            if (nearest != null) {
                _selectedBranch.value = nearest
            }
        }
    }

    // Repository Flows
    val allCakes = repository.allCakes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val cartItems = repository.cartItems.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allOrders = repository.allOrders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allBranches = repository.allBranches.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allDeliveryPartners = repository.allDeliveryPartners.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val userProfile = repository.userProfile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfileEntity())
    val realtimeSyncStatus = repository.realtimeSyncStatus.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Live Connected")
    val isLiveConnected = repository.isLiveConnected.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val firestorePermissionWarning = repository.firestorePermissionWarning.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Auth Session Persistence & State (Users stay logged in after closing the app until explicit logout)
    private val authPrefs = application.getSharedPreferences("cake_lovers_auth_session", Context.MODE_PRIVATE)
    private val _isLoggedIn = MutableStateFlow(authPrefs.getBoolean("is_logged_in", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private fun setLoggedInState(loggedIn: Boolean) {
        _isLoggedIn.value = loggedIn
        authPrefs.edit().putBoolean("is_logged_in", loggedIn).apply()
    }

    // Admin Access Detection (Restricted to verified admin accounts: admin@cakelovers.in or administrator credential)
    val isAdmin: StateFlow<Boolean> = combine(userProfile, _isLoggedIn) { profile, loggedIn ->
        loggedIn && profile != null && (
            profile.email.equals("admin@cakelovers.in", ignoreCase = true) ||
            profile.name.equals("Administrator", ignoreCase = true)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Individual user orders: Admin sees all bakery orders; individual customers only see their personal orders; guests only see orders placed in active session
    val userOrders: StateFlow<List<OrderEntity>> = combine(
        allOrders,
        userProfile,
        _isLoggedIn,
        isAdmin,
        _recentGuestOrderId
    ) { orders, profile, loggedIn, admin, recentGuestId ->
        if (admin) {
            orders
        } else if (loggedIn && profile != null) {
            val email = profile.email.trim().lowercase()
            val phone = profile.phone.replace(" ", "").trim()
            orders.filter { order ->
                val orderEmail = order.customerEmail.trim().lowercase()
                val orderPhone = order.customerPhone.replace(" ", "").trim()
                val orderUserId = order.userId.trim().lowercase()
                (email.isNotBlank() && (orderEmail == email || orderUserId == email)) ||
                (phone.isNotBlank() && (orderPhone == phone || (phone.length >= 8 && orderPhone.endsWith(phone.takeLast(8))))) ||
                (order.customerName.equals(profile.name, ignoreCase = true) && profile.name.isNotBlank() && profile.name != "Guest")
            }
        } else {
            // Guest mode: only orders placed in current session
            if (recentGuestId != null) {
                orders.filter { it.orderId == recentGuestId }
            } else {
                emptyList()
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Catalog UI Filters
    private val _selectedCategory = MutableStateFlow(CakeCategory.ALL)
    val selectedCategory: StateFlow<CakeCategory> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isEgglessOnly = MutableStateFlow(false)
    val isEgglessOnly: StateFlow<Boolean> = _isEgglessOnly.asStateFlow()

    private val _isGlutenFreeOnly = MutableStateFlow(false)
    val isGlutenFreeOnly: StateFlow<Boolean> = _isGlutenFreeOnly.asStateFlow()

    private val _isNutFreeOnly = MutableStateFlow(false)
    val isNutFreeOnly: StateFlow<Boolean> = _isNutFreeOnly.asStateFlow()

    private val _sortBy = MutableStateFlow(SortOption.FEATURED)
    val sortBy: StateFlow<SortOption> = _sortBy.asStateFlow()

    private val _selectedCakeDetail = MutableStateFlow<CakeItemEntity?>(null)
    val selectedCakeDetail: StateFlow<CakeItemEntity?> = _selectedCakeDetail.asStateFlow()

    // Filtered Cake Catalog Flow
    val filteredCakes: StateFlow<List<CakeItemEntity>> = combine(
        allCakes,
        _selectedCategory,
        _searchQuery,
        _isEgglessOnly,
        _isGlutenFreeOnly,
        _isNutFreeOnly,
        _sortBy
    ) { args: Array<Any> ->
        val cakes = args[0] as List<CakeItemEntity>
        val category = args[1] as CakeCategory
        val query = (args[2] as String).trim().lowercase()
        val egglessOnly = args[3] as Boolean
        val glutenFreeOnly = args[4] as Boolean
        val nutFreeOnly = args[5] as Boolean
        val sort = args[6] as SortOption

        cakes.filter { cake ->
            val matchCategory = category == CakeCategory.ALL || cake.category == category.name
            val matchQuery = query.isEmpty() ||
                    cake.name.lowercase().contains(query) ||
                    cake.tagline.lowercase().contains(query) ||
                    cake.flavorNotes.lowercase().contains(query)
            val matchEggless = !egglessOnly || cake.isEggless
            val matchGlutenFree = !glutenFreeOnly || cake.isGlutenFree
            val matchNutFree = !nutFreeOnly || cake.isNutFree

            matchCategory && matchQuery && matchEggless && matchGlutenFree && matchNutFree
        }.let { list ->
            when (sort) {
                SortOption.FEATURED -> list.sortedByDescending { it.isChefSpecial || it.isBestSeller }
                SortOption.RATING -> list.sortedByDescending { it.rating }
                SortOption.PRICE_LOW_HIGH -> list.sortedBy { it.basePrice }
                SortOption.PRICE_HIGH_LOW -> list.sortedByDescending { it.basePrice }
                SortOption.LEAD_TIME -> list.sortedBy { it.leadTimeHours }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Custom Cake Studio State
    private val _customStudio = MutableStateFlow(CustomStudioState())
    val customStudio: StateFlow<CustomStudioState> = _customStudio.asStateFlow()

    // Checkout options
    private val _fulfillmentType = MutableStateFlow(FulfillmentType.DELIVERY)
    val fulfillmentType: StateFlow<FulfillmentType> = _fulfillmentType.asStateFlow()

    private val _deliverySlot = MutableStateFlow(DeliverySlot.STANDARD)
    val deliverySlot: StateFlow<DeliverySlot> = _deliverySlot.asStateFlow()

    private val _paymentMethod = MutableStateFlow(PaymentMethod.UPI)
    val paymentMethod: StateFlow<PaymentMethod> = _paymentMethod.asStateFlow()

    private val _redeemLoyaltyPoints = MutableStateFlow(false)
    val redeemLoyaltyPoints: StateFlow<Boolean> = _redeemLoyaltyPoints.asStateFlow()

    private val _selectedBranch = MutableStateFlow<BranchEntity?>(null)
    val selectedBranch: StateFlow<BranchEntity?> = _selectedBranch.asStateFlow()

    private val _activeTrackOrderId = MutableStateFlow<String?>(null)
    val activeTrackOrderId: StateFlow<String?> = _activeTrackOrderId.asStateFlow()

    // Admin Kitchen Portal
    private val _adminStatusFilter = MutableStateFlow<OrderStatus?>(null)
    val adminStatusFilter: StateFlow<OrderStatus?> = _adminStatusFilter.asStateFlow()

    private val _adminPortalSelectedTab = MutableStateFlow(0)
    val adminPortalSelectedTab: StateFlow<Int> = _adminPortalSelectedTab.asStateFlow()

    fun setAdminPortalSelectedTab(tabIndex: Int) {
        _adminPortalSelectedTab.value = tabIndex
    }

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()
    val isSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(System.currentTimeMillis() - 120_000)
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    private val _lastCloudSyncInfo = MutableStateFlow("Synced with Firestore: cakes, branches, orders, users")
    val lastCloudSyncInfo: StateFlow<String> = _lastCloudSyncInfo.asStateFlow()

    private val _guestName = MutableStateFlow("")
    val guestName: StateFlow<String> = _guestName.asStateFlow()

    private val _guestPhone = MutableStateFlow("")
    val guestPhone: StateFlow<String> = _guestPhone.asStateFlow()

    private val _guestAddress = MutableStateFlow("")
    val guestAddress: StateFlow<String> = _guestAddress.asStateFlow()

    fun updateGuestDetails(name: String, phone: String, address: String) {
        _guestName.value = name
        _guestPhone.value = phone
        _guestAddress.value = address
    }

    fun loginWithCredentials(
        identifier: String,
        passwordOrPin: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            when (val result = repository.authenticateUser(identifier, passwordOrPin)) {
                is AuthResult.Success -> {
                    setLoggedInState(true)
                    onSuccess()
                }
                is AuthResult.Error -> {
                    onError(result.message)
                }
            }
        }
    }

    fun switchToAdminAccount(onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            when (repository.authenticateUser("admin@cakelovers.in", "admin123")) {
                is AuthResult.Success -> {
                    setLoggedInState(true)
                    onSuccess?.invoke()
                }
                is AuthResult.Error -> {
                    setLoggedInState(true)
                    onSuccess?.invoke()
                }
            }
        }
    }

    fun loginAsDemoUser(onSuccess: () -> Unit) {
        viewModelScope.launch {
            when (repository.authenticateUser("pooja.sharma@example.com", "password123")) {
                is AuthResult.Success -> {
                    setLoggedInState(true)
                    onSuccess()
                }
                is AuthResult.Error -> {
                    setLoggedInState(true)
                    onSuccess()
                }
            }
        }
    }

    fun registerNewUser(
        name: String,
        email: String,
        phone: String,
        address: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            when (val result = repository.registerUserAccount(name, email, phone, address, password)) {
                is AuthResult.Success -> {
                    setLoggedInState(true)
                    _recentGuestOrderId.value = null
                    _authInitialTab.value = 0
                    onSuccess()
                }
                is AuthResult.Error -> {
                    onError(result.message)
                }
            }
        }
    }

    fun logout() {
        setLoggedInState(false)
        _redeemLoyaltyPoints.value = false
        _recentGuestOrderId.value = null
        _authInitialTab.value = 0
        _activeTrackOrderId.value = null
        viewModelScope.launch {
            repository.onUserLogout()
        }
    }

    init {
        val wasLoggedIn = authPrefs.getBoolean("is_logged_in", false)
        if (wasLoggedIn) {
            viewModelScope.launch {
                userProfile.collect { profile ->
                    if (profile != null && profile.email.isNotBlank()) {
                        repository.syncUserOrdersFromCloud(profile.email, profile.id)
                    }
                }
            }
        }
        viewModelScope.launch {
            networkObserver.isConnectedFlow.collect { isOnline ->
                if (isOnline) {
                    repository.initializeSeedDataIfNeeded()
                    // Auto sync to cloud so Firestore collections are immediately refreshed
                    repository.syncWithCloud()
                }
            }
        }
    }

    // Catalog Actions
    fun setCategory(category: CakeCategory) { _selectedCategory.value = category }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun toggleEgglessOnly() { _isEgglessOnly.value = !_isEgglessOnly.value }
    fun toggleGlutenFreeOnly() { _isGlutenFreeOnly.value = !_isGlutenFreeOnly.value }
    fun toggleNutFreeOnly() { _isNutFreeOnly.value = !_isNutFreeOnly.value }
    fun setSortBy(sort: SortOption) { _sortBy.value = sort }
    fun selectCakeForDetail(cake: CakeItemEntity?) { _selectedCakeDetail.value = cake }

    // Custom Studio Actions
    fun updateStudioTiers(tiers: Int) {
        val minWeight = when (tiers) {
            1 -> 0.5
            2 -> 2.0
            3 -> 3.5
            else -> 0.5
        }
        _customStudio.value = _customStudio.value.copy(
            tiersCount = tiers,
            weightKg = maxOf(_customStudio.value.weightKg, minWeight)
        )
    }

    fun updateStudioWeight(weightKg: Double) {
        _customStudio.value = _customStudio.value.copy(weightKg = weightKg)
    }

    fun updateStudioSponge(sponge: String) {
        _customStudio.value = _customStudio.value.copy(spongeFlavor = sponge)
    }

    fun updateStudioFrosting(frosting: String) {
        _customStudio.value = _customStudio.value.copy(frostingFlavor = frosting)
    }

    fun updateStudioPipingText(text: String) {
        _customStudio.value = _customStudio.value.copy(customPipingText = text)
    }

    fun updateStudioPhotoUri(uri: String?) {
        _customStudio.value = _customStudio.value.copy(referencePhotoUri = uri)
    }

    fun addCustomCakeToCart(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val studio = _customStudio.value
            val item = CartItemEntity(
                cakeId = "custom_studio_${System.currentTimeMillis()}",
                cakeTitle = "Custom ${studio.tiersCount}-Tier Celebration Cake",
                weightKg = studio.weightKg,
                tiersCount = studio.tiersCount,
                spongeFlavor = studio.spongeFlavor,
                frostingFlavor = studio.frostingFlavor,
                customPipingText = studio.customPipingText,
                referencePhotoUri = studio.referencePhotoUri,
                celebrationKitIncluded = true,
                unitPrice = studio.dynamicPrice,
                quantity = 1,
                isCustomStudio = true
            )
            repository.addToCart(item)
            onSuccess()
        }
    }

    fun addCatalogCakeToCart(cake: CakeItemEntity, weightKg: Double, customPiping: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val multiplier = weightKg / cake.defaultWeightKg
            val unitPrice = cake.basePrice * multiplier
            val item = CartItemEntity(
                cakeId = cake.id,
                cakeTitle = cake.name,
                weightKg = weightKg,
                tiersCount = if (cake.category == CakeCategory.MULTI_TIER.name) 2 else 1,
                spongeFlavor = cake.flavorNotes.split(",").firstOrNull()?.trim() ?: "Artisan Sponge",
                frostingFlavor = "Chef's Signature Frosting",
                customPipingText = customPiping,
                referencePhotoUri = null,
                celebrationKitIncluded = true,
                unitPrice = unitPrice,
                quantity = 1,
                isCustomStudio = false
            )
            repository.addToCart(item)
            _selectedCakeDetail.value = null
            onSuccess()
        }
    }

    // Cart Actions
    fun removeCartItem(cartId: Long) {
        viewModelScope.launch { repository.removeCartItem(cartId) }
    }

    fun clearCart() {
        viewModelScope.launch { repository.clearCart() }
    }

    fun setFulfillmentType(type: FulfillmentType) { _fulfillmentType.value = type }
    fun setDeliverySlot(slot: DeliverySlot) { _deliverySlot.value = slot }
    fun setPaymentMethod(method: PaymentMethod) { _paymentMethod.value = method }
    fun toggleRedeemLoyaltyPoints() { _redeemLoyaltyPoints.value = !_redeemLoyaltyPoints.value }
    fun selectBranch(branch: BranchEntity) { _selectedBranch.value = branch }

    fun placeOrder(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            val currentCart = cartItems.value
            if (currentCart.isEmpty()) return@launch

            val subtotal = currentCart.sumOf { it.totalAmount }
            val deliveryFee = if (_fulfillmentType.value == FulfillmentType.DELIVERY) {
                _deliverySlot.value.surcharge.toDouble()
            } else 0.0

            val user = userProfile.value ?: UserProfileEntity()
            val isUserAuth = _isLoggedIn.value
            val customerName = if (isUserAuth) user.name else _guestName.value.ifBlank { "Guest Confectionery Lover" }
            val customerPhone = if (isUserAuth) user.phone else _guestPhone.value.ifBlank { "+91 98765 00000" }
            val customerEmail = if (isUserAuth) user.email else ""
            val userId = if (isUserAuth) user.email.ifBlank { user.phone } else ""
            val deliveryAddress = if (isUserAuth) user.deliveryAddress else _guestAddress.value.ifBlank { "Delivery to Door (Guest)" }
            val pointsToRedeem = if (isUserAuth && _redeemLoyaltyPoints.value) {
                minOf(user.loyaltyPoints, subtotal.toInt())
            } else 0

            val branch = _selectedBranch.value ?: allBranches.value.firstOrNull() ?: BranchEntity(
                id = "branch_default",
                name = "Downtown Flagship Studio",
                address = "No. 45, Brigade Road, Bengaluru",
                phone = "+91 98765 43210",
                latitude = 12.9716,
                longitude = 77.5946,
                deliveryRadiusKm = 15.0,
                operatingHours = "10:00 AM – 11:30 PM"
            )

            val orderId = repository.placeOrder(
                items = currentCart,
                subtotal = subtotal,
                deliveryFee = deliveryFee,
                redeemedPoints = pointsToRedeem,
                fulfillmentType = _fulfillmentType.value.name,
                deliveryAddress = deliveryAddress,
                deliverySlot = _deliverySlot.value.name,
                deliveryDate = "Today (Express Baked)",
                selectedBranch = branch,
                paymentMethod = _paymentMethod.value.name,
                customerName = customerName,
                customerPhone = customerPhone,
                customerEmail = customerEmail,
                userId = userId
            )

            _activeTrackOrderId.value = orderId
            _redeemLoyaltyPoints.value = false
            _pendingCheckoutAfterAuth.value = false
            _recentGuestOrderId.value = if (!isUserAuth) orderId else null
            onSuccess(orderId)
        }
    }

    fun setActiveTrackOrder(orderId: String) {
        _activeTrackOrderId.value = orderId
    }

    // Kitchen Portal Actions
    fun setAdminStatusFilter(status: OrderStatus?) {
        _adminStatusFilter.value = status
    }

    fun advanceOrderStatus(order: OrderEntity) {
        val current = OrderStatus.valueOf(order.status)
        val next = current.next() ?: return
        viewModelScope.launch {
            repository.updateOrderStatus(order.orderId, next.name)
        }
    }

    fun updateOrderStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status.name)
        }
    }

    fun cancelOrder(orderId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, "CANCELLED")
            onComplete()
        }
    }

    // Admin Entity Deletion Methods
    fun deleteCake(cakeId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteCake(cakeId)
            onComplete()
        }
    }

    fun updateCake(cake: CakeItemEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateCake(cake)
            onComplete()
        }
    }

    fun updateBranch(branch: BranchEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateBranch(branch)
            onComplete()
        }
    }

    fun insertBranch(branch: BranchEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.insertBranch(branch)
            onComplete()
        }
    }

    fun deleteBranch(branchId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteBranch(branchId)
            onComplete()
        }
    }

    fun deleteOrder(orderId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteOrder(orderId)
            onComplete()
        }
    }

    fun deleteOrderStatus(orderId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteOrderStatus(orderId)
            onComplete()
        }
    }

    fun deleteDeliveryPerson(orderId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteDeliveryPerson(orderId)
            onComplete()
        }
    }

    fun deleteDeliveryRadius(branchId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteDeliveryRadius(branchId)
            onComplete()
        }
    }

    fun assignDeliveryPartner(orderId: String, driverName: String, driverPhone: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.assignDeliveryPartner(orderId, driverName, driverPhone)
            onComplete()
        }
    }

    fun updateOrderCustomPhoto(orderId: String, photoUri: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateOrderCustomPhoto(orderId, photoUri)
            onComplete()
        }
    }

    fun addDeliveryPartner(partner: DeliveryPartnerEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.insertPartner(partner)
            onComplete()
        }
    }

    fun deleteDeliveryPartner(partnerId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deletePartner(partnerId)
            onComplete()
        }
    }

    fun updateDeliveryPartnerStatus(partnerId: String, newStatus: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updatePartnerStatus(partnerId, newStatus)
            onComplete()
        }
    }

    fun syncWithCloud(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isCloudSyncing.value = true
            val result = repository.syncWithCloud()
            _isCloudSyncing.value = false
            _lastSyncTimestamp.value = System.currentTimeMillis()
            _lastCloudSyncInfo.value = "Synced with Firestore (${result.timestampFormatted}) • ${result.totalRecordsSynced} documents active"
            onComplete()
        }
    }

    fun syncCloudFirestore() {
        syncWithCloud()
    }

    fun updateUserProfile(name: String, email: String, phone: String, address: String) {
        viewModelScope.launch {
            repository.updateProfile(name, email, phone, address)
        }
    }

    // Geolocation distance in km
    fun calculateDistanceKm(branchLat: Double, branchLon: Double): Double {
        val r = 6371.0 // Earth radius in km
        val userLat = _userLatitude.value
        val userLon = _userLongitude.value
        val dLat = Math.toRadians(branchLat - userLat)
        val dLon = Math.toRadians(branchLon - userLon)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(userLat)) * cos(Math.toRadians(branchLat)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return kotlin.math.round((r * c) * 10) / 10.0
    }
}

class CakeLoversViewModelFactory(private val application: android.app.Application) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CakeLoversViewModel::class.java)) {
            return CakeLoversViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

