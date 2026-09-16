package com.example.data.model

enum class CakeCategory(val displayName: String, val iconEmoji: String) {
    ALL("All Creations", "✨"),
    INDO_WESTERN_FUSION("Fusion Specials", "🪷"),
    CHOCOLATE_TRUFFLE("Signature Truffles", "🍫"),
    CHEESECAKE("Artisan Cheesecakes", "🧀"),
    MULTI_TIER("Celebration Multi-Tier", "🎂"),
    SEASONAL_SPECIAL("Seasonal Harvest", "🍓")
}

enum class OrderStatus(val stepIndex: Int, val label: String, val description: String) {
    PLACED(1, "Order Placed", "Order received by bakery system"),
    CONFIRMED(2, "Confirmed", "Master baker accepted order"),
    PREPARING(3, "Baking & Piping", "Crafting sponge and custom hand-piping"),
    OUT_FOR_DELIVERY(4, "Out for Delivery", "Express temperature-controlled transit"),
    DELIVERED(5, "Delivered", "Delivered fresh to celebration");

    fun next(): OrderStatus? {
        return when (this) {
            PLACED -> CONFIRMED
            CONFIRMED -> PREPARING
            PREPARING -> OUT_FOR_DELIVERY
            OUT_FOR_DELIVERY -> DELIVERED
            DELIVERED -> null
        }
    }
}

enum class DeliverySlot(val label: String, val window: String, val surcharge: Int) {
    STANDARD("Standard Daytime", "Delivered within 2-4 hours", 0),
    EVENING("Celebration Evening", "06:00 PM – 08:30 PM", 0),
    MIDNIGHT_SURPRISE("Midnight Surprise ✨", "11:15 PM – 11:55 PM (Strict)", 149)
}

enum class FulfillmentType(val label: String) {
    DELIVERY("Doorstep Hand-Delivery"),
    PICKUP("In-Store Express Pickup")
}

enum class PaymentMethod(val label: String, val subtitle: String) {
    UPI("Instant UPI", "Google Pay / PhonePe / Paytm"),
    CARD("Credit & Debit Card", "Visa, Mastercard, RuPay"),
    NET_BANKING("Net Banking", "All major Indian & international banks"),
    COD("Cash on Delivery", "Pay cash or QR at doorstep")
}
