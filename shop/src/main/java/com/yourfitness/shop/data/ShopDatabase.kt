package com.yourfitness.shop.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yourfitness.common.data.CommonConverters
import com.yourfitness.shop.data.dao.AddressDao
import com.yourfitness.shop.data.dao.CartDao
import com.yourfitness.shop.data.dao.OrderDao
import com.yourfitness.shop.data.dao.ProductsDao
import com.yourfitness.shop.data.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Database(
    entities = [
        ProductEntity::class,
        ItemColorEntity::class,
        ItemSizeEntity::class,
        ItemImageEntity::class,
        CartEntity::class,
        AddressEntity::class,
        FavoritesEntity::class,
        OrderInfoEntity::class,
        GoodsOrderItemEntity::class,
        ServicesOrderItemEntity::class
    ],
    version = 10,
    exportSchema = true
)

@TypeConverters(CommonConverters::class)
abstract class ShopDatabase : RoomDatabase() {
    abstract fun productDao(): ProductsDao
    abstract fun cartDao(): CartDao
    abstract fun addressDao(): AddressDao
    abstract fun orderDao(): OrderDao

    suspend fun clearDb() = withContext(Dispatchers.IO) { clearAllTables() }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE address ADD COLUMN details TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE products ADD COLUMN stockLevel TEXT")
        database.execSQL("ALTER TABLE products ADD COLUMN subcategory TEXT")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE goods_order_items ADD COLUMN brandName TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE products ADD COLUMN gender TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE products ADD COLUMN brandImage TEXT")
    }
}
