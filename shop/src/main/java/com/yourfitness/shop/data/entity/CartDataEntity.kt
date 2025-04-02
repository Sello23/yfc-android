package com.yourfitness.shop.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class CartDataEntity(
    @Embedded val cartItem: CartEntity,
    @Relation(
        entity = ProductEntity::class,
        parentColumn = "itemId",
        entityColumn = "id"
    )
    val product: ProductDataEntity
)
