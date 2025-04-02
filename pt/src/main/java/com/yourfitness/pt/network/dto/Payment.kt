package com.yourfitness.pt.network.dto

import com.google.gson.annotations.SerializedName
import com.yourfitness.common.network.dto.PtPackages

data class PtPackagePaymentRequest(
    @SerializedName("package") val data: PtPackages,
    @SerializedName("personalTrainerID") val id: String
)