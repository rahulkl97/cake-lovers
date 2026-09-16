package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BranchEntity
import com.example.data.model.CakeItemEntity
import com.example.data.model.CartItemEntity
import com.example.data.model.DeliveryPartnerEntity
import com.example.data.model.OrderEntity
import com.example.data.model.UserAccountEntity
import com.example.data.model.UserProfileEntity

@Database(
    entities = [
        CakeItemEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        BranchEntity::class,
        UserProfileEntity::class,
        DeliveryPartnerEntity::class,
        UserAccountEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class CakeLoversDatabase : RoomDatabase() {
    abstract fun cakeDao(): CakeDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    abstract fun branchDao(): BranchDao
    abstract fun userDao(): UserDao
    abstract fun deliveryPartnerDao(): DeliveryPartnerDao
    abstract fun userAccountDao(): UserAccountDao

    companion object {
        @Volatile
        private var INSTANCE: CakeLoversDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `delivery_partners` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `phone` TEXT NOT NULL,
                        `vehicleType` TEXT NOT NULL,
                        `vehicleNumber` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `rating` REAL NOT NULL,
                        `totalDeliveriesCompleted` INTEGER NOT NULL,
                        `currentZone` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `user_accounts` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `email` TEXT NOT NULL,
                        `phone` TEXT NOT NULL,
                        `password` TEXT NOT NULL,
                        `address` TEXT NOT NULL,
                        `role` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `orders` ADD COLUMN `userId` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `orders` ADD COLUMN `customerEmail` TEXT NOT NULL DEFAULT ''")
                db.execSQL("DELETE FROM `orders` WHERE `orderId` IN ('CKL-7821', 'CKL-5612')")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `orders` ADD COLUMN `customPhotoUri` TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE `orders` ADD COLUMN `hasCustomDesign` INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): CakeLoversDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CakeLoversDatabase::class.java,
                    "cake_lovers_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
