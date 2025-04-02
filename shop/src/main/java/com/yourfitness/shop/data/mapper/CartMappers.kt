package com.yourfitness.shop.data.mapper

import com.yourfitness.shop.data.entity.CartDataEntity
import com.yourfitness.shop.network.dto.OrderPaymentItem

fun CartDataEntity.toOrderPaymentItem(): OrderPaymentItem {
    return OrderPaymentItem(
        coinsCount = cartItem.coinsPart,
        colorId = cartItem.colorId.ifBlank { null },
        price = product.product.price,
        productId = cartItem.itemId,
        sizeId = cartItem.sizeId,
        quantity = 1
    )
}
