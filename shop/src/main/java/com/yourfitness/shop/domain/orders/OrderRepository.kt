package com.yourfitness.shop.domain.orders

import com.yourfitness.shop.data.dao.OrderDao
import com.yourfitness.shop.data.entity.OrderDataEntity
import com.yourfitness.shop.data.entity.OrderInfoEntity
import com.yourfitness.shop.data.entity.GoodsOrderItemEntity
import com.yourfitness.shop.data.entity.ServicesOrderItemEntity
import com.yourfitness.shop.data.mapper.toEntity
import com.yourfitness.shop.network.ShopRestApi
import com.yourfitness.shop.ui.constants.Constants.Companion.PAYMENT_WAITING
import timber.log.Timber
import javax.inject.Inject

class OrderRepository @Inject constructor(
    private val dao: OrderDao,
    private val restApi: ShopRestApi
) {
    suspend fun deleteOrderInfo(order: OrderInfoEntity) = dao.deleteOrderInfo(order)

    suspend fun readAllOrders(): List<OrderDataEntity> = dao.readAllOrders()

    suspend fun saveOrderInfo(order: OrderInfoEntity) = dao.saveOrderInfo(order)

    suspend fun saveOrderItem(orderItem: GoodsOrderItemEntity) = dao.saveOrderItem(orderItem)

    suspend fun downloadGoodsOrders(now: String?): List<OrderDataEntity> {
        try {
            val response = restApi.getGoodsOrders(now)
            response.filter { it.status.lowercase() != PAYMENT_WAITING }
                .forEach { order ->
                    val orderEntity = order.toEntity()
                    dao.saveOrderInfo(orderEntity)

                    order.items.forEach { item ->
                        val orderItemEntity = item.toEntity()
                        dao.saveOrderItem(orderItemEntity)
                    }
                }
        } catch (e: Exception) {
            Timber.e(e)
        }
        return readGoodsOrders()
    }

    suspend fun readGoodsOrders(): List<OrderDataEntity> = dao.readAllOrders()

    suspend fun downloadServicesOrders(now: String?): List<ServicesOrderItemEntity> {
        val response = restApi.getServicesOrders(now)
        response.forEach { order ->
            val orderEntity = order.toEntity()
            dao.saveServiceOrderInfo(orderEntity)
        }
        return readServicesOrders()
    }

    suspend fun readServicesOrders(): List<ServicesOrderItemEntity> = dao.readAllServicesOrders()

    suspend fun getOrderItemsByOrderId(id: String): List<GoodsOrderItemEntity> {
        return dao.getOrderItemsByOrderId(id)
    }

    suspend fun getServiceOrderItemsByOrderId(id: String): List<ServicesOrderItemEntity> {
        return dao.getServiceOrderItemsByOrderId(id)
    }

    suspend fun getOrderStatusById(id: String): String {
        return dao.getOrderStatusById(id)
    }

    suspend fun getServiceOrderStatusById(id: String): String {
        return dao.getServiceOrderStatusById(id)
    }
}
