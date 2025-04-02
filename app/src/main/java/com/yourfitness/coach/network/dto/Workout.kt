package com.yourfitness.coach.network.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Workout(
    @SerializedName("createdAt") val createdAt: Long? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("trackedAt") val trackedAt: Long? = null,
    @SerializedName("userId") val userId: String? = null,
    @SerializedName("manual") val manual: Boolean = true
) : Parcelable

data class ManualWorkoutBody(
    @SerializedName("trackedAt") val trackedAtList: List<String>
)
