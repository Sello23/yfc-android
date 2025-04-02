package com.yourfitness.common.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yourfitness.common.data.entity.RegionSettingsEntity.Companion.REGION_SETTINGS_TABLE
import com.yourfitness.common.network.dto.Packages
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = REGION_SETTINGS_TABLE)
data class RegionSettingsEntity(
    @PrimaryKey
    val id: Int = 0,
    val packages: List<Packages>?,
    val subscriptionCost: Int?,
    val currency: String?,
    val coinsToVoucherOwner: String?,
    val coinValue: Double?,
    val lastUpdateTime: Long,
    val timeZoneOffset: Int,
    val region: String = "UAE",
    val country: String = "UAE",
    val isSynchronizing: Boolean = false
) : Parcelable {
    companion object {
        const val REGION_SETTINGS_TABLE = "region_settings"

        val empty
            get() = RegionSettingsEntity(
                0,
                null,
                null,
                null,
                null,
                null,
                0,
                0
            )
    }
}
