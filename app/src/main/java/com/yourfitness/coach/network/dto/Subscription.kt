package com.yourfitness.coach.network.dto

import com.google.gson.annotations.SerializedName


data class CreateSubscriptionRequest(
    @SerializedName("price") val price: Long,
    @SerializedName("subscriptionType") val subscriptionType: String,
    @SerializedName("voucherValue") val voucherValue: String?,
)

data class CreateOneTimeSubscriptionRequest(
    @SerializedName("duration") val duration: Int?,
    @SerializedName("isOneTimeSubscription") val isOneTimeSubscription: Boolean,
    @SerializedName("price") val price: Long,
    @SerializedName("subscriptionType") val subscriptionType: String,
    @SerializedName("voucherValue") val voucherValue: String?,
)

data class CreateSubscriptionResponse(
    @SerializedName("clientSecret") val clientSecret: String?,
    @SerializedName("currency") val currency: String?,
    @SerializedName("endDate") val endDate: Int?,
    @SerializedName("startDate") val startDate: Int?,
    @SerializedName("subDescription") val subDescription: String?,
    @SerializedName("id") val id: String?,
    @SerializedName("interval") val interval: String?,
    @SerializedName("amount") val amount: Int?,
    @SerializedName("amountDecimal") val amountDecimal: Double?,
)

data class SubscriptionCost(
    @SerializedName("cost") val subscriptionCost: Int? = null,
    @SerializedName("currency") val currency: String? = null,
    @SerializedName("duration") val duration: Int? = null,
    @SerializedName("isOneTimeSubscription") val isOneTimeSubscription: Boolean? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("corporationName") val corporationName: String? = null,
)

data class SubscriptionOptions(
    @SerializedName("voucher") val voucher: Voucher? = null,
    @SerializedName("prices") val options: List<SubscriptionCost>? = null,
)
