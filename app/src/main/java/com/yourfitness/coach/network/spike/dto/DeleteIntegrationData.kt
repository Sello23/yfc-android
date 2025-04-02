package com.yourfitness.coach.network.spike.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeleteIntegrationData(
    @SerializedName("integrations_to_remove") val providers: List<String>? = null,
) : Parcelable

@Parcelize
data class DeleteIntegrationResponse(
    @SerializedName("removed_count") val count: Int? = null,
) : Parcelable
