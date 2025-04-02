package com.yourfitness.coach.network.spike.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SpikeData(
    @SerializedName("data") val data: List<SpikeFitnessData>? = null,
) : Parcelable

@Parcelize
data class SpikeFitnessData(
    @SerializedName("date") val date: String? = null,
    @SerializedName("calories_total") val caloriesTotal: Long? = null,
    @SerializedName("calories_active") val caloriesActive: Long? = null,
    @SerializedName("steps") val steps: Long? = null,
    @SerializedName("distance") val distance: Long? = null,
    @SerializedName("daily_movement") val dailyMovement: Long? = null,

    @SerializedName("value") val value: Long? = null,
    @SerializedName("intraday_data") val intradayData: List<IntradayData>? = null,
) : Parcelable

@Parcelize
data class IntradayData(
    @SerializedName("time_start") val timeStart: String? = null,
    @SerializedName("time_end") val timeEnd: String? = null,
    @SerializedName("value") val value: Int? = null,
    @SerializedName("metadata") val metadata: Map<String, String>? = null,
) : Parcelable
