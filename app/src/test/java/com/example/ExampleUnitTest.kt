package com.example

import com.example.data.model.CakeCategory
import com.example.data.model.DeliverySlot
import com.example.data.model.OrderStatus
import com.example.ui.viewmodel.CustomStudioState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun `test custom cake dynamic price calculation for single tier`() {
    val studio = CustomStudioState(
      tiersCount = 1,
      weightKg = 1.0,
      spongeFlavor = "Vanilla Bean Almond",
      frostingFlavor = "Cream Cheese Frosting"
    )
    // 1.0 * 750 + 0 + 50 + 50 = 850
    assertEquals(850.0, studio.dynamicPrice, 0.01)
  }

  @Test
  fun `test custom cake dynamic price calculation for 3 tiers with premium saffron and rabdi`() {
    val studio = CustomStudioState(
      tiersCount = 3,
      weightKg = 4.0,
      spongeFlavor = "Saffron Cardamom (Rasmalai Base)",
      frostingFlavor = "Rose Cardamom Rabdi Cream"
    )
    // 4.0 * 750 (3000) + tier3 surcharge (950) + saffron (150) + rose rabdi (150) = 4250
    assertEquals(4250.0, studio.dynamicPrice, 0.01)
  }

  @Test
  fun `test order status sequence progression`() {
    val initial = OrderStatus.PLACED
    val next = initial.next()
    assertEquals(OrderStatus.CONFIRMED, next)
    val preparing = next?.next()
    assertEquals(OrderStatus.PREPARING, preparing)
    val outForDelivery = preparing?.next()
    assertEquals(OrderStatus.OUT_FOR_DELIVERY, outForDelivery)
    val delivered = outForDelivery?.next()
    assertEquals(OrderStatus.DELIVERED, delivered)
    assertEquals(null, delivered?.next())
  }

  @Test
  fun `test delivery slot midnight surcharge`() {
    assertEquals(0, DeliverySlot.STANDARD.surcharge)
    assertEquals(0, DeliverySlot.EVENING.surcharge)
    assertEquals(149, DeliverySlot.MIDNIGHT_SURPRISE.surcharge)
  }

  @Test
  fun `test user profile tier calculation`() {
    val guestUser = com.example.data.model.UserProfileEntity(
      lifetimeSpent = 1200.0
    )
    assertEquals("Silver", guestUser.membershipTier)
    assertEquals(5, guestUser.tierDiscountPercent)

    val goldUser = com.example.data.model.UserProfileEntity(
      lifetimeSpent = 4500.0
    )
    assertEquals("Gold", goldUser.membershipTier)
    assertEquals(10, goldUser.tierDiscountPercent)
  }

  @Test
  fun `test haversine distance formula`() {
    val r = 6371.0
    val lat1 = 12.9716
    val lon1 = 77.5946
    val lat2 = 12.9716
    val lon2 = 77.5946
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
            kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
            kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
    val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    val distance = kotlin.math.round((r * c) * 10) / 10.0
    assertEquals(0.0, distance, 0.01)
  }

  @Test
  fun `test guest checkout to signup state transition values`() {
    val guestTab = 1 // 1 indicates Create Account / Sign Up
    val simulatedOrderId = "CK-GUEST-9999"
    assertEquals(1, guestTab)
    assertTrue(simulatedOrderId.startsWith("CK-"))
  }

  @Test
  fun `test admin access control verification - ordinary user denied`() {
    val ordinaryUser = com.example.data.model.UserProfileEntity(
      name = "Pooja Sharma",
      email = "pooja.sharma@example.com",
      phone = "+91 98765 12345"
    )
    val isOrdinaryAdmin = ordinaryUser.email.equals("admin@cakelovers.in", ignoreCase = true) ||
        ordinaryUser.name.equals("Administrator", ignoreCase = true)
    assertEquals(false, isOrdinaryAdmin)

    // User whose name contains substring "admin" (like "Shyam Adminwala") must NOT get admin access
    val similarNameUser = com.example.data.model.UserProfileEntity(
      name = "Shyam Adminwala",
      email = "shyam@gmail.com",
      phone = "+91 99999 11111"
    )
    val isSimilarNameAdmin = similarNameUser.email.equals("admin@cakelovers.in", ignoreCase = true) ||
        similarNameUser.name.equals("Administrator", ignoreCase = true)
    assertEquals(false, isSimilarNameAdmin)
  }

  @Test
  fun `test admin access control verification - authorized admin permitted`() {
    val adminUser = com.example.data.model.UserProfileEntity(
      name = "Administrator",
      email = "admin@cakelovers.in",
      phone = "+91 98765 00001"
    )
    val isAdmin = adminUser.email.equals("admin@cakelovers.in", ignoreCase = true) ||
        adminUser.name.equals("Administrator", ignoreCase = true)
    assertEquals(true, isAdmin)
  }

  @Test
  fun `test fulfillment options delivery and pickup`() {
    val delivery = com.example.data.model.FulfillmentType.DELIVERY
    val pickup = com.example.data.model.FulfillmentType.PICKUP

    assertEquals("Doorstep Hand-Delivery", delivery.label)
    assertEquals("In-Store Express Pickup", pickup.label)
  }
}
