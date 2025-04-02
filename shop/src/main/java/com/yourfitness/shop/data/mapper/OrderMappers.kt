package com.yourfitness.shop.data.mapper

import com.yourfitness.shop.data.entity.OrderInfoEntity
import com.yourfitness.shop.data.entity.GoodsOrderItemEntity
import com.yourfitness.shop.data.entity.ServicesOrderItemEntity
import com.yourfitness.shop.network.dto.OrderItem
import com.yourfitness.shop.network.dto.OrderResponse
import com.yourfitness.shop.network.dto.ServiceOrderItem
import com.yourfitness.shop.network.dto.ServiceOrderResponse

fun OrderResponse.toEntity(): OrderInfoEntity {
    return OrderInfoEntity(
        createdAt = createdAt,
        id = id,
        status = status,
        number = number,
    )
}

fun OrderItem.toEntity(): GoodsOrderItemEntity {
    return GoodsOrderItemEntity(
        coinsValue = coinsValue,
        coinsCount = coinsCount,
        productName = productName,
        vendorName = vendorName,
        color = color.orEmpty(),
        id = id,
        imageId = imageId,
        orderId = orderId,
        sizeType = sizeType.orEmpty(),
        sizeValue = sizeValue.orEmpty(),
        status = status,
        price = price,
        productId = productId,
        quantity = quantity,
        brandName = brandName
    )
}

fun ServiceOrderItem.toEntity(): ServicesOrderItemEntity {
    return ServicesOrderItemEntity(
        id = id,
        status = status,
        productId = productId,
        productName = productName,
        vendorName = vendorName,
        vendorPhone = vendorPhone,
        vendorImage = vendorImage,
        address = address,
        price = price,
        coinsCount = coinsCount,
        coinsValue = coinsValue,
        defaultImageId = defaultImageId,
        latitude = latitude,
        longitude = longitude,
        dateBought = dateBought ?: 0,
        dateClaimed = dateClaimed,
    )
}

fun ServiceOrderResponse.toEntity(): OrderInfoEntity {
    return OrderInfoEntity(
        createdAt = 0,
        id = id,
        status = status,
        number = productId,
    )
}

