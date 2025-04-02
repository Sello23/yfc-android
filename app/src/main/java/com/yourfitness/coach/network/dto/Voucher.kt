package com.yourfitness.coach.network.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class Voucher(
    @SerializedName("voucherType") val voucherType: String? = null,
    @SerializedName("voucherValue") val voucherValue: String? = null,
    @SerializedName("valid") val valid: Boolean? = null,
    @SerializedName("startDate") val startDate: Long? = null,
    @SerializedName("endDate") val endDate: Long? = null,
    @SerializedName("userRewardAmount") val userRewardAmount: Int? = null,
    @SerializedName("subscriptionCost") val subscriptionCost: Int? = null,
    @SerializedName("subscriptionCurrency") val subscriptionCurrency: String? = null
)

data class CodeType(
    @SerializedName("type") val type: String?,
    @SerializedName("challenge") val challenge: ChallengeVoucher?,
    @SerializedName("corporation") val corporation: CorporationVoucher?,
    @SerializedName("voucher") val referral: ReferralVoucher?
)

data class ChallengeVoucher(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("measurement") val measurement: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("imageId") val imageId: String?,
    @SerializedName("startDate") val startDate: Long?,
    @SerializedName("endDate") val endDate: Long?
)

data class CorporationVoucher(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("subscriptionType") val subscriptionType: String?,
    @SerializedName("subscriptionCost") val subscriptionCost: String?,
    @SerializedName("subscriptionCurrency") val subscriptionCurrency: String?
)

@Parcelize
data class ReferralVoucher(
    @SerializedName("id") val id: String?,
    @SerializedName("voucherType") val voucherType: String?,
    @SerializedName("subscriptionCost") val subscriptionCost: Int?,
    @SerializedName("subscriptionCurrency") val subscriptionCurrency: String?,
    @SerializedName("userRewardAmount") val userRewardAmount: Int?,
    @SerializedName("startDate") val startDate: Long?,
    @SerializedName("endDate") val endDate: Long?
) : Parcelable


data class CorporationVoucherBody(
    @SerializedName("corporationId") val corporationId: String
)

data class SubscriptionVoucherBody(
    @SerializedName("voucherId") val voucherId: String
)

data class ReferralVoucherBody(
    @SerializedName("voucherValue") val voucherValue: String
)