package com.yourfitness.shop.data

import android.content.Context
import androidx.room.Room
import com.yourfitness.shop.data.dao.AddressDao
import com.yourfitness.shop.data.dao.ProductsDao
import com.yourfitness.shop.data.dao.CartDao
import com.yourfitness.shop.data.dao.OrderDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): ShopDatabase {
        return Room.databaseBuilder(context, ShopDatabase::class.java, "shop-database")
            .fallbackToDestructiveMigration()
            .addMigrations(MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
            .build()
    }

    @Provides
    fun provideProductDao(database: ShopDatabase): ProductsDao {
        return database.productDao()
    }

    @Provides
    fun provideCartDao(database: ShopDatabase): CartDao {
        return database.cartDao()
    }

    @Provides
    fun provideAddressDao(database: ShopDatabase): AddressDao {
        return database.addressDao()
    }

    @Provides
    fun provideOrderDao(database: ShopDatabase): OrderDao {
        return database.orderDao()
    }
}