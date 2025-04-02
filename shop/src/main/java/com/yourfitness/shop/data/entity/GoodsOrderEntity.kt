package com.yourfitness.shop.data.entity

import androidx.room.*
import com.yourfitness.shop.data.entity.OrderInfoEntity.Companion.ORDER_TABLE
import com.yourfitness.shop.data.entity.GoodsOrderItemEntity.Companion.GOODS_ORDER_ITEM_TABLE

data class OrderDataEntity(
    @Embedded val order: OrderInfoEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "orderId"
    )
    val items: List<GoodsOrderItemEntity>
)

@Entity(
    tableName = ORDER_TABLE,
    indices = [Index(value = ["id"], unique = true)]
)
data class OrderInfoEntity(
    @PrimaryKey(autoGenerate = true) val _id: Int = 0,
    val createdAt: Long,
    val id: String,
    val status: String,
    val number: String,
) {
    companion object {
        const val ORDER_TABLE = "orders"
    }
}

@Entity(
    tableName = GOODS_ORDER_ITEM_TABLE,
    indices = [Index(value = ["id"], unique = true)],
    foreignKeys = [ForeignKey(
        entity = OrderInfoEntity::class,
        childColumns = ["orderId"],
        parentColumns = ["id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class GoodsOrderItemEntity(
    @PrimaryKey(autoGenerate = true) val _id: Int = 0,
    val coinsValue: Long,
    val coinsCount: Long,
    val productName: String,
    val vendorName: String,
    val color: String,
    val id: String,
    val imageId: String,
    val orderId: String,
    val sizeType: String,
    val sizeValue: String,
    val status: String,
    val price: Long,
    val productId: String,
    val quantity: Int,
    val brandName: String
) {
    companion object {
        const val GOODS_ORDER_ITEM_TABLE = "goods_order_items"
    }
}
