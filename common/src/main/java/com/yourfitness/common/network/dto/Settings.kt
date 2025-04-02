package com.yourfitness.common.network.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


data class SettingsRegion(
    @SerializedName("packages") val packages: List<Packages>? = null,
    @SerializedName("subscriptionCost") val subscriptionCost: Int? = null,
    @SerializedName("currency") val currency: String? = null,
    @SerializedName("coinsToVoucherOwner") val coinsToVoucherOwner: String? = null,
    @SerializedName("coinValue") val coinValue: Double? = null,
    @SerializedName("personalTrainerPackages") val ptPackages: List<PtPackages>? = null,
    @SerializedName("region") val region: String? = null,
    @SerializedName("country") val country: String? = null,
    @SerializedName("timeZoneOffset") val timeZoneOffset: Int? = null
)

@Parcelize
data class Packages(
    @SerializedName("active") val active: Boolean? = null,
    @SerializedName("cost") val cost: Int? = null,
    @SerializedName("credits") val credits: Int? = null,
    @SerializedName("name") val name: String? = null,
) : Parcelable

@Parcelize
data class PtPackages(
    @SerializedName("active") val active: Boolean? = null,
    @SerializedName("cost") val cost: Int? = null,
    @SerializedName("sessions") val sessions: Int? = null,
    @SerializedName("name") val name: String? = null,
) : Parcelable
