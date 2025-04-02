package com.yourfitness.shop.data.entity

import androidx.room.*

data class ColorDataEntity(
    @Embedded val itemColor: ItemColorEntity,
    @Relation(
        parentColumn = "_id",
        entityColumn = "itemColorId",
    )
    val sizes: List<ItemSizeEntity>,
    @Relation(
        parentColumn = "_id",
        entityColumn = "itemColorId",
    )
    val images: List<ItemImageEntity>
)

data class ProductDataEntity(
    @Embedded val product: ProductEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "apparelId",
    )
    val favourite: FavoritesEntity?,

    @Relation(
        entity = ItemColorEntity::class,
        parentColumn = "_id",
        entityColumn = "apparelId"
    )
    val colors: List<ColorDataEntity>
)
