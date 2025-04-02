package com.yourfitness.common.network

import com.yourfitness.common.network.dto.TokenData
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface YFCAuthApi {

    @POST("/auth/refresh-token")
    fun refreshToken(@Body tokens: TokenData): Call<TokenData>
}