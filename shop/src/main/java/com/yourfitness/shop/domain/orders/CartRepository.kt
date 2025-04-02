package com.yourfitness.shop.domain.orders

import com.yourfitness.shop.data.dao.CartDao
import com.yourfitness.shop.data.entity.CartDataEntity
import com.yourfitness.shop.data.entity.CartEntity
import javax.inject.Inject

class CartRepository @Inject constructor(
    private val dao: CartDao
) {
    suspend fun getCartList(): List<CartEntity> = dao.readCartItems()

    suspend fun saveItemToCart(item: CartEntity) = dao.saveItemToCart(item)

    suspend fun readCartItemsWithData(): List<CartDataEntity> = dao.readCartItemsWithData()

    suspend fun deleteFromCart(item: CartEntity) = dao.deleteFromCart(item)

    suspend fun deleteById(uuid: String,) =
        dao.deleteById(uuid)

    suspend fun updateItemAmount(id: String, color: String, size: String, type: String, amount: Int) =
        dao.updateItemAmount(id, color, size, type, amount)

    suspend fun updateItemCoinsPart(uuid: String, coinsPart: Long) =
        dao.updateItemCoinsPart(uuid, coinsPart)

    suspend fun emptyCart() = dao.emptyCart()

    suspend fun getCartSize(): Int = dao.getCartSize()

    suspend fun getItemByParams(id: String, color: String, size: String, type: String): CartEntity? =
        dao.getItemByParams(id, color, size, type)

    suspend fun getItemById(uuid: String): CartEntity? =
        dao.getItemById(uuid)
}
