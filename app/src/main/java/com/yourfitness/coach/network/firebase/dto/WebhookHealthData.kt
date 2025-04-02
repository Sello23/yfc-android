package com.yourfitness.coach.network.firebase.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.yourfitness.coach.network.spike.dto.SpikeFitnessData
import kotlinx.parcelize.Parcelize

data class WebhookHealthData (
    @SerializedName("data") val data: WebhookData? = null,
)

data class WebhookData (
    @SerializedName("stream") val stream: List<WebhookStream>? = null,
    @SerializedName("summary") val summary: List<WebhookSummary>? = null,
)

data class WebhookStream (
    @SerializedName("activity") val activity: StreamActivity? = null,
)

data class WebhookSummary (
    @SerializedName("activity") val activity: SpikeFitnessData? = null,
)

@Parcelize
data class StreamActivity(
    @SerializedName("date") val date: String? = null,
    @SerializedName("time_start") val timeStart: String? = null,
    @SerializedName("time_end") val timeEnd: String? = null,
    @SerializedName("calories") val calories: Long? = null,
    @SerializedName("steps") val steps: Long? = null,
    @SerializedName("distance") val distance: Long? = null,
    @SerializedName("manual") val manual: Boolean = false,
    @SerializedName("moving_time") val movingTime: Long? = null,
) : Parcelable
