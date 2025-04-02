package com.yourfitness.coach.network.dto

import com.google.gson.annotations.SerializedName


data class UpdateProfileRequest(
    @SerializedName("email") val email: String? = null,
    @SerializedName("instagram") val instagram: String? = null,
    @SerializedName("media_id") val mediaId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("platform") val platform: String? = null,
    @SerializedName("surname") val surname: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("region") val region: String? = null,
    @SerializedName("gender") val gender: Int? = null,
    @SerializedName("birthday") val birthday: Long? = null,
    @SerializedName("push_token") val pushToken: String? = null,
    @SerializedName("voucher") val voucher: String? = null,
)

data class PutAchievements(
    @SerializedName("steps") val steps: Int? = null,
    @SerializedName("calories") val calories: Int? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("duration") val duration: Long? = null,
    @SerializedName("healthServiceSteps") val healthServiceSteps: Long? = null,
    @SerializedName("manualSteps") val manualSteps: Long? = null,
)

data class GetAchievements(
    @SerializedName("calories") val calories: Int? = null,
    @SerializedName("trackedAt") val trackedAt: Long? = null,
    @SerializedName("score") val score: Double? = null,
    @SerializedName("steps") val steps: Int? = null,
    @SerializedName("duration") val duration: Long? = null,
    @SerializedName("distance") val distance: Double? = null,
)

data class PaymentHistory(
    @SerializedName("amount") val amount: Int? = null,
    @SerializedName("duration") val duration: Int? = null,
    @SerializedName("ptSessionsAmount") val ptSessionsAmount: Int? = null,
    @SerializedName("creditsAmount") val creditsAmount: Int? = null,
    @SerializedName("paymentDestination") val paymentDestination: String? = null,
    @SerializedName("paymentIntent") val paymentIntent: String? = null,
    @SerializedName("time") val time: String? = null
)

data class Subscription(
    @SerializedName("autoRenewal") val autoRenewal: Boolean? = null,
    @SerializedName("complimentaryAccess") val complimentaryAccess: Boolean? = null,
    @SerializedName("createdTime") val createdTime: Long? = null,
    @SerializedName("expiredDate") val expiredDate: Long? = null,
    @SerializedName("nextPaymentDate") val nextPaymentDate: Long? = null,
    @SerializedName("paidSubscription") val paidSubscription: Boolean? = null,
    @SerializedName("isOneTime") val isOneTime: Boolean? = null,
    @SerializedName("duration") val duration: Int? = null,
    @SerializedName("corporationName") val corporationName: String? = null,
)

data class Visits(
    @SerializedName("createdAt") val createdAt: Long? = null,
    @SerializedName("facilityID") val facilityID: String? = null,
    @SerializedName("scheduleID") val scheduleID: String? = null,
)

data class BonusCredits(
    @SerializedName("FacilityID") val facilityID: String? = null,
    @SerializedName("Amount") val amount: Int? = null
)
