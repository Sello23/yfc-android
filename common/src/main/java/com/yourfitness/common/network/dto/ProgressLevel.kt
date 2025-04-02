package com.yourfitness.common.network.dto

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.yourfitness.common.network.dto.ProgressLevel.Companion.PROGRESS_LEVEL_TABLE
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = PROGRESS_LEVEL_TABLE)
data class ProgressLevel(
    @PrimaryKey(autoGenerate = true) val _id: Int = 0,
    @SerializedName("coinRewards") val coinRewards: Long = 0,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("mediaId") val mediaId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("pointLevel") val pointLevel: Long = 0,
    @SerializedName("updatedAt") val updatedAt: String? = null,
) : Parcelable {
    companion object {
        const val PROGRESS_LEVEL_TABLE = "progress_levels"
    }
}
