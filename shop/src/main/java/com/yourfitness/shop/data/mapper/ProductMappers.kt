package com.yourfitness.shop.data.mapper

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.yourfitness.shop.data.entity.*
import com.yourfitness.shop.data.entity.ItemSizeEntity.Companion.NO_STOCK
import com.yourfitness.shop.network.dto.ColorData
import com.yourfitness.shop.network.dto.Product
import com.yourfitness.shop.network.dto.SizeData

fun Product.toEntity(productTypeId: ProductTypeId): ProductEntity {
    return ProductEntity(
        active = active,
        brandName = brandName,
        defaultImageId = defaultImageId.orEmpty(),
        images = Gson().toJson(images ?: listOf<String>()),
        description = description.orEmpty(),
        id = id.orEmpty(),
        name = name.orEmpty(),
        price = price ?: 0L,
        redeemableCoins = redeemableCoins ?: 0L,
        vendorName = vendorName.orEmpty(),
        info = Gson().toJson(info.orEmpty()),
        productId = productTypeId.value,
        latitude = latitude,
        longitude = longitude,
        vendorImageId = vendorImageId,
        vendorAddress = vendorAddress,
        subcategory = subcategory,
        stockLevel = stockLevel,
        gender = gender.orEmpty(),
        brandImage = brandImage
    )
}

fun ColorData.toEntity(apparelId: Int): ItemColorEntity {
    return ItemColorEntity(
        apparelId = apparelId,
        color = color.orEmpty(),
        colorId = colorId.orEmpty(),
        isDefault = default,
        defaultImageId = defaultImageId.orEmpty(),
    )
}

fun SizeData.toEntity(colorId: Int): ItemSizeEntity {
    return ItemSizeEntity(
        itemColorId = colorId,
        size = size.orEmpty(),
        sizeId = sizeId ?: 0,
        stockLevel = stockLevel.orEmpty(),
        type = type.orEmpty(),
        sequence = sequence ?: 0
    )
}

fun String.toImageEntity(colorId: Int): ItemImageEntity {
    return ItemImageEntity(
        itemColorId = colorId,
        image = this,
    )
}
