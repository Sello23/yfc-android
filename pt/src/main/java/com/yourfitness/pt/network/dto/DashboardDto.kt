package com.yourfitness.pt.network.dto

import com.google.gson.annotations.SerializedName

data class DashboardDto(
    @SerializedName("commissionLevelId") var commissionLevelId: String? = null,
    @SerializedName("commissionPercentage") var commissionPercentage: Int? = null,
    @SerializedName("conductedSessions") var conductedSessions: Int? = null,
    @SerializedName("currency") var currency: String? = null,
    @SerializedName("totalClients") var totalClients: Int? = null,
    @SerializedName("totalCommissions") var totalCommissions: Int? = null
)

data class InductionNote(
    @SerializedName("note") var note: String,
)