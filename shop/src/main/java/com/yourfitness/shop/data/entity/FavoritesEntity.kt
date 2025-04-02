package com.yourfitness.shop.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.yourfitness.shop.data.entity.FavoritesEntity.Companion.FAVORITES_TABLE

@Entity(
    tableName = FAVORITES_TABLE, indices = [Index(value = ["apparelId"], unique = true)]
)
data class FavoritesEntity(
    val id: Int,
    @PrimaryKey val apparelId: String,
) {
    companion object {
        const val FAVORITES_TABLE = "favorites"
    }
}
