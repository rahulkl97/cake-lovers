package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cakes")
data class CakeItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val tagline: String,
    val description: String,
    val category: String, // from CakeCategory enum name
    val basePrice: Double, // Price for 0.5 kg
    val rating: Double,
    val reviewsCount: Int,
    val isEggless: Boolean, // 100% Pure Veg
    val isGlutenFree: Boolean,
    val isNutFree: Boolean,
    val leadTimeHours: Int, // e.g. 2 for quick, 24 for multi-tier
    val flavorNotes: String, // e.g. "Cardamom, Pistachio, Saffron milk, Rose petals"
    val defaultWeightKg: Double = 0.5,
    val isChefSpecial: Boolean = false,
    val isBestSeller: Boolean = false,
    val localDrawableName: String = "img_hero_banner" // local resource identifier
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val cartId: Long = 0,
    val cakeId: String,
    val cakeTitle: String,
    val weightKg: Double,
    val tiersCount: Int = 1,
    val spongeFlavor: String,
    val frostingFlavor: String,
    val customPipingText: String = "",
    val referencePhotoUri: String? = null,
    val celebrationKitIncluded: Boolean = true, // Free eco-friendly wooden knife + 2 sparklers
    val unitPrice: Double,
    val quantity: Int = 1,
    val isCustomStudio: Boolean = false
) {
    val totalAmount: Double
        get() = unitPrice * quantity
}

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val userId: String = "",
    val customerEmail: String = "",
    val createdAtTimestamp: Long = System.currentTimeMillis(),
    val status: String, // from OrderStatus
    val statusTimestamp: Long = System.currentTimeMillis(),
    val fulfillmentType: String, // DELIVERY or PICKUP
    val deliveryAddress: String,
    val deliverySlot: String, // STANDARD, EVENING, MIDNIGHT_SURPRISE
    val deliveryDate: String, // e.g. "Today, 8 Sep"
    val branchId: String,
    val branchName: String,
    val subtotal: Double,
    val deliveryFee: Double,
    val pointsDiscount: Double,
    val finalTotal: Double,
    val paymentMethod: String,
    val customerName: String,
    val customerPhone: String,
    val driverName: String = "Chef Rohan (Bakery Dispatch)",
    val driverPhone: String = "+919876543210",
    val itemsSummary: String, // Human-readable summary of items
    val customPhotoUri: String? = null,
    val hasCustomDesign: Boolean = false
)

@Entity(tableName = "branches")
data class BranchEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val phone: String,
    val latitude: Double,
    val longitude: Double,
    val deliveryRadiusKm: Double,
    val operatingHours: String,
    val rating: Double = 4.9,
    val isActive: Boolean = true
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String = "user_default",
    val name: String = "Pooja Sharma",
    val email: String = "pooja.sharma@example.com",
    val phone: String = "+91 98765 12345",
    val deliveryAddress: String = "Flat 402, Lotus Orchid, Palm Avenue, Indiranagar, Bengaluru",
    val loyaltyPoints: Int = 320, // 1 point = ₹1
    val totalOrdersCount: Int = 4,
    val lifetimeSpent: Double = 6450.0
) {
    val membershipTier: String
        get() = when {
            lifetimeSpent >= 10000.0 -> "Platinum"
            lifetimeSpent >= 3500.0 -> "Gold"
            else -> "Silver"
        }

    val tierDiscountPercent: Int
        get() = when (membershipTier) {
            "Platinum" -> 15
            "Gold" -> 10
            else -> 5
        }
}

@Entity(tableName = "delivery_partners")
data class DeliveryPartnerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val vehicleType: String, // e.g. "Chilled Cake Van (Hydraulic Shock Absorbers)", "Electric Scooter (Insulated Box)"
    val vehicleNumber: String, // e.g. "KA-01-CK-9901"
    val status: String = "AVAILABLE", // AVAILABLE, ON_DELIVERY, BUSY, OFF_DUTY
    val rating: Double = 4.95,
    val totalDeliveriesCompleted: Int = 120,
    val currentZone: String = "Indiranagar & Central Bengaluru"
)

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey val id: String, // email or phone (lowercase or numeric)
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val address: String,
    val role: String = "CUSTOMER" // "CUSTOMER" or "ADMIN"
)

