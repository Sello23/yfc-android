package com.yourfitness.common.network.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.yourfitness.common.network.dto.Gender
import kotlinx.parcelize.Parcelize

@Parcelize
data class Challenge(
    @SerializedName("corporateID") val corporateID: String? = null,
    @SerializedName("createdAt") val createdAt: Long? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("endDate") val endDate: Long? = null,
    @SerializedName("gender") val gender: Gender? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("imageID") val imageID: String? = null,
    @SerializedName("measurement") val measurement: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("region") val region: String? = null,
    @SerializedName("rules") val rules: String? = null,
    @SerializedName("startDate") val startDate: Long? = null,
    @SerializedName("updatedAt") val updatedAt: Long? = null,
    @SerializedName("private") val private: Boolean? = null,
) : Parcelable
