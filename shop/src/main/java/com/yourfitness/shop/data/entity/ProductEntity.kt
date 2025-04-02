package com.yourfitness.shop.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.yourfitness.shop.data.entity.ProductEntity.Companion.PRODUCTS_TABLE

@Entity(
    tableName = PRODUCTS_TABLE,
    indices = [Index(value = ["id"], unique = true)]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val _id: Int = 0,
    val id: String,
    val active: Boolean,
    val brandName: String?,
    val defaultImageId: String,
    val images: String,
    val description: String,
    val name: String,
    val price: Long,
    val redeemableCoins: Long,
    val vendorName: String,
    val info: String,
    val productId: Int,
    val latitude: Double?,
    val longitude: Double?,
    val vendorImageId: String?,
    val vendorAddress: String?,
    val subcategory: String?,
    val stockLevel: String?,
    val gender: String,
    val brandImage: String?
) {
    companion object {
        const val PRODUCTS_TABLE = "products"
    }
}

enum class ProductTypeId(val value: Int) {
    APPAREL(0),
    EQUIPMENT(1),
    ACCESSORIES(2),
    SERVICES(3)
}
