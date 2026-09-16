package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BranchEntity
import com.example.data.model.CakeItemEntity
import com.example.data.model.CartItemEntity
import com.example.data.model.DeliveryPartnerEntity
import com.example.data.model.OrderEntity
import com.example.data.model.UserAccountEntity
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CakeDao {
    @Query("SELECT * FROM cakes")
    fun getAllCakes(): Flow<List<CakeItemEntity>>

    @Query("SELECT * FROM cakes")
    suspend fun getAllCakesDirect(): List<CakeItemEntity>

    @Query("SELECT * FROM cakes WHERE id = :id LIMIT 1")
    suspend fun getCakeById(id: String): CakeItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCakes(cakes: List<CakeItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCake(cake: CakeItemEntity)

    @Query("SELECT COUNT(*) FROM cakes")
    suspend fun getCakesCount(): Int

    @Query("DELETE FROM cakes WHERE id = :cakeId")
    suspend fun deleteCakeById(cakeId: String)

    @Update
    suspend fun updateCake(cake: CakeItemEntity)
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items ORDER BY cartId DESC")
    fun getCartItems(): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity): Long

    @Update
    suspend fun updateCartItem(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE cartId = :cartId")
    suspend fun deleteCartItem(cartId: Long)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAtTimestamp DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders ORDER BY createdAtTimestamp DESC")
    suspend fun getAllOrdersDirect(): List<OrderEntity>

    @Query("SELECT * FROM orders WHERE customerEmail = :email OR userId = :userId ORDER BY createdAtTimestamp DESC")
    fun getOrdersForUser(email: String, userId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE customerEmail = :email OR userId = :userId ORDER BY createdAtTimestamp DESC")
    suspend fun getOrdersForUserDirect(email: String, userId: String): List<OrderEntity>

    @Query("SELECT * FROM orders WHERE orderId = :orderId LIMIT 1")
    fun getOrderById(orderId: String): Flow<OrderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderEntity>)

    @Query("UPDATE orders SET status = :status, statusTimestamp = :timestamp WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String, timestamp: Long)

    @Query("SELECT COUNT(*) FROM orders")
    suspend fun getOrdersCount(): Int

    @Query("DELETE FROM orders WHERE orderId = :orderId")
    suspend fun deleteOrderById(orderId: String)

    @Query("DELETE FROM orders WHERE orderId IN ('CKL-7821', 'CKL-5612')")
    suspend fun deleteDefaultDemoOrders()

    @Query("DELETE FROM orders")
    suspend fun clearAllOrders()

    @Query("UPDATE orders SET status = 'UNCONFIRMED_PENDING', statusTimestamp = :timestamp WHERE orderId = :orderId")
    suspend fun resetOrderStatus(orderId: String, timestamp: Long)

    @Query("UPDATE orders SET driverName = 'None Assigned', driverPhone = '' WHERE orderId = :orderId")
    suspend fun removeDeliveryPerson(orderId: String)

    @Query("UPDATE orders SET driverName = :driverName, driverPhone = :driverPhone WHERE orderId = :orderId")
    suspend fun assignDeliveryPartner(orderId: String, driverName: String, driverPhone: String)

    @Query("UPDATE orders SET customPhotoUri = :photoUri, hasCustomDesign = 1 WHERE orderId = :orderId")
    suspend fun updateOrderCustomPhoto(orderId: String, photoUri: String)
}

@Dao
interface BranchDao {
    @Query("SELECT * FROM branches")
    fun getAllBranches(): Flow<List<BranchEntity>>

    @Query("SELECT * FROM branches")
    suspend fun getAllBranchesDirect(): List<BranchEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBranches(branches: List<BranchEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBranch(branch: BranchEntity)

    @Update
    suspend fun updateBranch(branch: BranchEntity)

    @Query("SELECT COUNT(*) FROM branches")
    suspend fun getBranchesCount(): Int

    @Query("DELETE FROM branches WHERE id = :branchId")
    suspend fun deleteBranchById(branchId: String)

    @Query("UPDATE branches SET deliveryRadiusKm = 0.0 WHERE id = :branchId")
    suspend fun resetDeliveryRadius(branchId: String)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE id = 'user_default' LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 'user_default' LIMIT 1")
    suspend fun getUserProfileDirect(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(user: UserProfileEntity)

    @Query("UPDATE user_profile SET loyaltyPoints = loyaltyPoints + :points, lifetimeSpent = lifetimeSpent + :spent, totalOrdersCount = totalOrdersCount + 1 WHERE id = 'user_default'")
    suspend fun recordOrderPurchase(points: Int, spent: Double)

    @Query("UPDATE user_profile SET loyaltyPoints = MAX(0, loyaltyPoints - :redeemedPoints) WHERE id = 'user_default'")
    suspend fun deductLoyaltyPoints(redeemedPoints: Int)
}

@Dao
interface DeliveryPartnerDao {
    @Query("SELECT * FROM delivery_partners ORDER BY name ASC")
    fun getAllDeliveryPartners(): Flow<List<DeliveryPartnerEntity>>

    @Query("SELECT * FROM delivery_partners ORDER BY name ASC")
    suspend fun getAllPartnersDirect(): List<DeliveryPartnerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartners(partners: List<DeliveryPartnerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartner(partner: DeliveryPartnerEntity)

    @Query("SELECT COUNT(*) FROM delivery_partners")
    suspend fun getPartnersCount(): Int

    @Query("DELETE FROM delivery_partners WHERE id = :partnerId")
    suspend fun deletePartnerById(partnerId: String)

    @Query("UPDATE delivery_partners SET status = :newStatus WHERE id = :partnerId")
    suspend fun updatePartnerStatus(partnerId: String, newStatus: String)
}

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts WHERE LOWER(email) = LOWER(:identifier) OR phone = :identifier OR LOWER(id) = LOWER(:identifier) LIMIT 1")
    suspend fun findAccount(identifier: String): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: UserAccountEntity)

    @Query("SELECT COUNT(*) FROM user_accounts")
    suspend fun getAccountCount(): Int

    @Query("SELECT * FROM user_accounts")
    suspend fun getAllAccounts(): List<UserAccountEntity>
}

