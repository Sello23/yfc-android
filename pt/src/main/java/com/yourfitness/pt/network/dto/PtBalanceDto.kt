package com.yourfitness.pt.network.dto

import com.google.gson.annotations.SerializedName

data class PtBalanceDto(
    @SerializedName("amount") val amount: Int?,
    @SerializedName("personalTrainerId") val ptId: String?,
    @SerializedName("profileId") val profileId: String?
)