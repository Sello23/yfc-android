package com.yourfitness.shop.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.yourfitness.shop.data.entity.CartEntity.Companion.CART_TABLE

@Entity(
    tableName = CART_TABLE,
    indices = [Index(value = ["uuid"], unique = true)]
)
data class CartEntity(
    @PrimaryKey(autoGenerate = true) val _id: Int = 0,
    val uuid: String,
    val itemId: String,
    val color: String,
    val colorId: String,
    val size: String,
    val sizeId: Int,
    val sizeType: String,
    val coinsPart: Long,
    val amount: Int
) {
    companion object {
        const val CART_TABLE = "cart"
    }
}
