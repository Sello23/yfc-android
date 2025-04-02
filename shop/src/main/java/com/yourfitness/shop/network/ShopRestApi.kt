package com.yourfitness.shop.network

import com.yourfitness.shop.network.dto.*
import retrofit2.http.*
import java.util.*

interface ShopRestApi {
    @GET("v2/product/apparel")
    suspend fun getApparelList(@Query("syncedAt") syncedAt: Long?): List<Product>

    @GET("v2/product/equipment")
    suspend fun getEquipmentList(@Query("syncedAt") syncedAt: Long?): List<Product>

    @GET("v2/product/accessory")
    suspend fun getAccessoriesList(@Query("syncedAt") syncedAt: Long?): List<Product>

    @GET("v2/product/service")
    suspend fun getServicesList(@Query("syncedAt") syncedAt: Long?): List<Product>

    @PUT("/product/favorites/replace")
    suspend fun saveFavorites(@Body favorites: Favorites)

    @GET("/product/favorites")
    suspend fun getFavorites() : List<String>?

    @POST("/order/goods")
    suspend fun createOrderPayment(@Body request: OrderPaymentRequest): OrderPaymentResponse

    @GET("/order/goods/list")
    suspend fun getGoodsOrders(@Query("syncedAt") syncedAt: String?) : List<OrderResponse>

    @GET("/order/vouchers/list")
    suspend fun getServicesOrders(@Query("syncedAt") syncedAt: String?) : List<ServiceOrderItem>

    @PUT("/order/goods/{id}/cancel")
    suspend fun cancelOrder(@Path("id") orderId: String)

    @PUT("/order/vouchers/{id}/cancel")
    suspend fun cancelServiceOrder(@Path("id") orderId: String)

    @GET("/payment/receipt/shop")
    suspend fun getInvoiceLink(@Query("id") orderId: String): OrderReceipt

    @GET("/order/status/{id}")
    suspend fun getOrderStatus(@Path("id") orderId: String): OrderStatus

    @PUT("/payment/shop/{id}/cancel")
    suspend fun cancelPayment(@Path("id") orderId: String)

    @PUT("/payment/shop/voucher/{id}/cancel")
    suspend fun cancelServicePayment(@Path("id") orderId: String)

    @POST("/order/vouchers")
    suspend fun createServicePayment(@Body request: OrderPaymentItem): ServicePaymentResponse

    @GET("/order/vouchers/{id}")
    suspend fun getServiceOrder(@Path("id") orderId: String): OrderStatus

    @PUT("/order/vouchers/{id}/claim")
    suspend fun claimVoucher(@Path("id") orderId: String): VoucherResponse
}
