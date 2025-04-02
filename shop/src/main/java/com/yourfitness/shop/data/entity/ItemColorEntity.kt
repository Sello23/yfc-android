package com.yourfitness.shop.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.yourfitness.shop.data.entity.ItemColorEntity.Companion.ITEM_COLOR_TABLE

@Entity(
    tableName = ITEM_COLOR_TABLE, foreignKeys = [ForeignKey(
        entity = ProductEntity::class,
        childColumns = ["apparelId"],
        parentColumns = ["_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class ItemColorEntity(
    @PrimaryKey(autoGenerate = true) val _id: Int = 0,
    val color: String,
    val colorId: String,
    val isDefault: Boolean,
    val defaultImageId: String,
    val apparelId: Int
) {
    companion object {
        const val ITEM_COLOR_TABLE = "item_colors"
    }
}
