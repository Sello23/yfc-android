package com.yourfitness.coach.network.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email") val email: String? = null,
    @SerializedName("instagram") val instagram: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("platform") val platform: String? = null,
    @SerializedName("surname") val surname: String? = null,
    @SerializedName("otp_id") val otpId: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("region") val region: String? = null
)

data class LoginWithCorpRequest(
    @SerializedName("email") val email: String? = null,
    @SerializedName("instagram") val instagram: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("platform") val platform: String? = null,
    @SerializedName("surname") val surname: String? = null,
    @SerializedName("otp_id") val otpId: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("region") val region: String? = null,
    @SerializedName("corporation_id") val corporationId: String? = null
)

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("profile_id") val profileId: String? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null
)