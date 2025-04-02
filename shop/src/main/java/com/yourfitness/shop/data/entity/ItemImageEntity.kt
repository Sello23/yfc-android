package com.yourfitness.shop.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.yourfitness.shop.data.entity.ItemImageEntity.Companion.ITEM_IMAGE_TABLE

@Entity(
    tableName = ITEM_IMAGE_TABLE,
    foreignKeys = [ForeignKey(
        entity = ItemColorEntity::class,
        childColumns = ["itemColorId"],
        parentColumns = ["_id"],
        onDelete = ForeignKey.CASCADE
    )],
)
data class ItemImageEntity(
    @PrimaryKey(autoGenerate = true) val _id: Int = 0,
    val image: String,
    val itemColorId: Int,
) {
    companion object {
        const val ITEM_IMAGE_TABLE = "item_images"
    }
}
