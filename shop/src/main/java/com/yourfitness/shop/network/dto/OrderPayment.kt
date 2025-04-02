package com.yourfitness.shop.network.dto

import com.google.gson.annotations.SerializedName
import com.yourfitness.common.network.dto.PackagePaymentResponse

data class OrderPaymentItem(
    @SerializedName("coinsCount") val coinsCount: Long,
    @SerializedName("colorId") val colorId: String? = null,
    @SerializedName("price") val price: Long,
    @SerializedName("productId") val productId: String,
    @SerializedName("sizeId") val sizeId: Int? = null,
    @SerializedName("quantity") val quantity: Int? = null
)

data class OrderPaymentRequest(
    @SerializedName("clientAddress") val clientAddress: String,
    @SerializedName("clientPhone") val clientPhone: String,
    @SerializedName("clientEmail") val clientEmail: String,
    @SerializedName("clientName") val clientName: String,
    @SerializedName("items") val items: List<OrderPaymentItem>,
)

data class OrderPaymentResponse(
    @SerializedName("details") val details: PackagePaymentResponse,
    @SerializedName("order") val order: OrderResponse,
)

data class ServicePaymentResponse(
    @SerializedName("details") val details: PackagePaymentResponse,
    @SerializedName("voucher") val voucher: ServiceOrderResponse
)

data class OrderResponse(
    @SerializedName("createdAt") val createdAt: Long,
    @SerializedName("id") val id: String,
    @SerializedName("status") val status: String,
    @SerializedName("number") val number: String,
    @SerializedName("items") val items: List<OrderItem>
)

data class ServiceOrderResponse(
    @SerializedName("coinsCount") val coinsCount: Long,
    @SerializedName("coinsValue") val coinsValue: Long,
    @SerializedName("dateBought") val dateBought: String,
    @SerializedName("dateClaimed") val dateClaimed: String,
    @SerializedName("defaultImageId") val defaultImageId: String,
    @SerializedName("id") val id: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("price") val price: Long,
    @SerializedName("productId") val productId: String,
    @SerializedName("productName") val productName: String,
    @SerializedName("status") val status: String,
    @SerializedName("vendorContact") val vendorContact: String,
    @SerializedName("vendorName") val vendorName: String,
)

data class OrderItem(
    @SerializedName("coinsValue") val coinsValue: Long,
    @SerializedName("coinsCount") val coinsCount: Long,
    @SerializedName("productName") val productName: String,
    @SerializedName("vendorName") val vendorName: String,
    @SerializedName("color") val color: String?,
    @SerializedName("id") val id: String,
    @SerializedName("imageId") val imageId: String,
    @SerializedName("orderId") val orderId: String,
    @SerializedName("sizeType") val sizeType: String?,
    @SerializedName("sizeValue") val sizeValue: String?,
    @SerializedName("status") val status: String,
    @SerializedName("price") val price: Long,
    @SerializedName("productId") val productId: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("brandName") val brandName: String
)

data class ServiceOrderItem(
    @SerializedName("id") val id: String,
    @SerializedName("status") val status: String,
    @SerializedName("productId") val productId: String,
    @SerializedName("productName") val productName: String,
    @SerializedName("vendorName") val vendorName: String,
    @SerializedName("vendorPhone") val vendorPhone: String,
    @SerializedName("vendorImage") val vendorImage: String,
    @SerializedName("address") val address: String,
    @SerializedName("price") val price: Long,
    @SerializedName("coinsCount") val coinsCount: Long,
    @SerializedName("coinsValue") val coinsValue: Long,
    @SerializedName("defaultImageId") val defaultImageId: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("dateBought") val dateBought: Long?,
    @SerializedName("dateClaimed") val dateClaimed: Long?,
)

data class OrderReceipt(
    @SerializedName("receipt") val receipt: String
)

data class OrderStatus(
    @SerializedName("status") val status: String
)
