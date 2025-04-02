package com.yourfitness.shop.data.dao

import androidx.room.*
import com.yourfitness.shop.data.entity.*
import com.yourfitness.shop.data.entity.CartEntity.Companion.CART_TABLE

@Dao
interface CartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveItemToCart(cartItem: CartEntity)

    @Transaction
    @Query("SELECT * FROM $CART_TABLE")
    suspend fun readCartItems(): List<CartEntity>

    @Transaction
    @Query("SELECT * FROM $CART_TABLE")
    suspend fun readCartItemsWithData(): List<CartDataEntity>

    @Delete
    suspend fun deleteFromCart(cartItem: CartEntity)

    @Query("DELETE FROM $CART_TABLE WHERE uuid = :uuid")
    suspend fun deleteById(uuid: String)

    @Query("UPDATE $CART_TABLE SET amount = :amount WHERE itemId = :id AND color =:color AND size =:size AND sizeType =:type")
    suspend fun updateItemAmount(id: String, color: String, size: String, type: String, amount: Int)

    @Query("UPDATE $CART_TABLE SET coinsPart = :coinsPart WHERE uuid = :uuid")
    suspend fun updateItemCoinsPart(uuid: String, coinsPart: Long)

    @Query("DELETE FROM $CART_TABLE")
    suspend fun emptyCart()

    @Query("SELECT COUNT(_id) FROM $CART_TABLE")
    suspend fun getCartSize(): Int

    @Query("SELECT * FROM $CART_TABLE WHERE itemId = :id AND color =:color AND size =:size AND sizeType =:type")
    suspend fun getItemByParams(id: String, color: String, size: String, type: String): CartEntity?

    @Query("SELECT * FROM $CART_TABLE WHERE uuid = :uuid")
    suspend fun getItemById(uuid: String): CartEntity?
}
