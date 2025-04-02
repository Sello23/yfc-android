package com.yourfitness.shop.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.yourfitness.shop.data.entity.ServicesOrderItemEntity.Companion.SERVICES_ORDER_ITEM_TABLE

@Entity(
    tableName = SERVICES_ORDER_ITEM_TABLE,
    indices = [Index(value = ["id"], unique = true)],
)
data class ServicesOrderItemEntity(
    @PrimaryKey(autoGenerate = true) val _id: Int = 0,
    val id: String,
    val status: String,
    val productId: String,
    val productName: String,
    val vendorName: String,
    val vendorPhone: String,
    val vendorImage: String,
    val address: String,
    val price: Long,
    val coinsCount: Long,
    val coinsValue: Long,
    val defaultImageId: String,
    val latitude: Double,
    val longitude: Double,
    val dateBought: Long,
    val dateClaimed: Long?,
) {
    companion object {
        const val SERVICES_ORDER_ITEM_TABLE = "services_order_items"
    }
}
