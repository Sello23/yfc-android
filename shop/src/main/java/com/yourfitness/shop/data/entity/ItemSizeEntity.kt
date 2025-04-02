package com.yourfitness.shop.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.yourfitness.shop.data.entity.ItemSizeEntity.Companion.ITEM_SIZE_TABLE

@Entity(
    tableName = ITEM_SIZE_TABLE,
    foreignKeys = [ForeignKey(
        entity = ItemColorEntity::class,
        childColumns = ["itemColorId"],
        parentColumns = ["_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class ItemSizeEntity(
    @PrimaryKey(autoGenerate = true) val _id: Int = 0,
    val size: String,
    val sizeId: Int,
    val stockLevel: String,
    val type: String,
    val itemColorId: Int,
    val sequence: Int
) {
    companion object {
        const val ITEM_SIZE_TABLE = "item_sizes"

        const val NO_STOCK = "No Stock"
        const val LIMITED_STOCK = "Limited Stock"
        const val IN_STOCK = "In Stock"
    }
}
