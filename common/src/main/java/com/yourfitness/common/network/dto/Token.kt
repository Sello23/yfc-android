package com.yourfitness.common.network.dto

import com.google.gson.annotations.SerializedName

data class TokenData(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null
)