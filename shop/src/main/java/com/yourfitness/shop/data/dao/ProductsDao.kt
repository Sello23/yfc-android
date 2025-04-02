package com.yourfitness.shop.data.dao

import androidx.room.*
import com.yourfitness.shop.data.entity.*
import com.yourfitness.shop.data.entity.FavoritesEntity.Companion.FAVORITES_TABLE
import com.yourfitness.shop.data.entity.ItemColorEntity.Companion.ITEM_COLOR_TABLE
import com.yourfitness.shop.data.entity.ProductEntity.Companion.PRODUCTS_TABLE

@Dao
interface ProductsDao {
    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateApparels(apparels: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProduct(apparel: ProductEntity)

    @Query("SELECT _id FROM $PRODUCTS_TABLE WHERE id = :productId AND productId = :productTypeId")
    suspend fun getProductId(productId: String?, productTypeId: Int): Int?

    @Query("SELECT _id FROM $PRODUCTS_TABLE WHERE id = :apparelId")
    suspend fun getProductId(apparelId: String?): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveColorItem(colorItem: ItemColorEntity)

    @Query("SELECT _id FROM $ITEM_COLOR_TABLE WHERE color = :color AND apparelId = :apparelId LIMIT 1")
    suspend fun getColorId(color: String?, apparelId: Int): Int?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun saveImageItem(imageItem: ItemImageEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun saveSizeItem(sizeItem: ItemSizeEntity)

    @Transaction
    @Query("SELECT * FROM $PRODUCTS_TABLE WHERE active = :active AND productId = :productTypeId")
    suspend fun readAllProducts(productTypeId: Int, active: Boolean = true): List<ProductDataEntity>

    @Transaction
    @Query("SELECT * FROM $PRODUCTS_TABLE WHERE id = :id AND productId = :productId")
    suspend fun getProductById(id: String, productId: Int): ProductDataEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveToFavorites(item: FavoritesEntity)

    @Query("DELETE FROM $FAVORITES_TABLE WHERE apparelId = :apparelId")
    suspend fun deleteFromFavorites(apparelId: String)

    @Query("SELECT * FROM $FAVORITES_TABLE")
    suspend fun readAllFavorites(): List<FavoritesEntity>?

    @Query("SELECT * FROM $PRODUCTS_TABLE WHERE active = :active AND productId = :productTypeId")
    suspend fun readAllProductsSimple(productTypeId: Int, active: Boolean = true): List<ProductEntity>
}
