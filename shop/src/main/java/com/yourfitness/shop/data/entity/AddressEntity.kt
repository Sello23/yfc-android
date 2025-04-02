package com.yourfitness.shop.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yourfitness.shop.data.entity.AddressEntity.Companion.ADDRESS_TABLE
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = ADDRESS_TABLE)
data class AddressEntity(
    @PrimaryKey val id: Int = 0,
    val city: String = "",
    val street: String = "",
    val details: String = "",
    val apartment: String = "",
    val building: String = "",
    val floor: String = "",
): Parcelable {
    companion object {
        const val ADDRESS_TABLE = "address"
    }
}
