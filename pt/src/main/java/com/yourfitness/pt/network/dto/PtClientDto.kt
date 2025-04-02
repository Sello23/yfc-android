package com.yourfitness.pt.network.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.yourfitness.pt.data.entity.ProfileInfo
import com.yourfitness.pt.domain.models.ClientInductionData
import kotlinx.parcelize.Parcelize

@Parcelize
data class PtClientDto(
    @SerializedName("conductedSessions") var conductedSessions: Int? = null,
    @SerializedName("remainingSessions") var remainingSessions: Int? = null,
    @SerializedName("profileInfo") var profileInfo: ProfileInfoDto? = null
) : Parcelable, ClientInductionData

@Parcelize
data class PtInductionDto(
    @SerializedName("id") var id: String? = null,
    @SerializedName("facilityId") var facilityId: String? = null,
    @SerializedName("profileId") var profileId: String? = null,
    @SerializedName("createdAt") var createdAt: Long? = null,
    @SerializedName("profileInfo") var profileInfo: ProfileInfoDto? = null
) : Parcelable
