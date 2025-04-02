package com.yourfitness.shop.data.dao

import androidx.room.*
import com.yourfitness.shop.data.entity.*
import com.yourfitness.shop.data.entity.OrderInfoEntity.Companion.ORDER_TABLE
import com.yourfitness.shop.data.entity.GoodsOrderItemEntity.Companion.GOODS_ORDER_ITEM_TABLE
import com.yourfitness.shop.data.entity.ServicesOrderItemEntity.Companion.SERVICES_ORDER_ITEM_TABLE
import com.yourfitness.shop.network.dto.ServiceOrderItem

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveOrderInfo(order: OrderInfoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveServiceOrderInfo(order: ServicesOrderItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveOrderItem(orderItem: GoodsOrderItemEntity)

    @Delete
    suspend fun deleteOrderInfo(order: OrderInfoEntity)

    @Transaction
    @Query("SELECT * FROM $ORDER_TABLE")
    suspend fun readAllOrders(): List<OrderDataEntity>

    @Query("SELECT * FROM $SERVICES_ORDER_ITEM_TABLE")
    suspend fun readAllServicesOrders(): List<ServicesOrderItemEntity>

    @Query("SELECT * FROM $GOODS_ORDER_ITEM_TABLE WHERE orderId = :id")
    suspend fun getOrderItemsByOrderId(id: String):  List<GoodsOrderItemEntity>

    @Query("SELECT * FROM $SERVICES_ORDER_ITEM_TABLE WHERE id = :id")
    suspend fun getServiceOrderItemsByOrderId(id: String): List<ServicesOrderItemEntity>

    @Query("SELECT status FROM $ORDER_TABLE WHERE id = :id")
    suspend fun getOrderStatusById(id: String): String

    @Query("SELECT status FROM $SERVICES_ORDER_ITEM_TABLE WHERE id = :id")
    suspend fun getServiceOrderStatusById(id: String): String
}
