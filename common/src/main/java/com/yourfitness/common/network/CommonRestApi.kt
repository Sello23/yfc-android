package com.yourfitness.common.network

import com.yourfitness.common.network.dto.*
import retrofit2.http.*
import java.util.*

interface CommonRestApi {
    @GET("/settings/region")
    suspend fun getSettingsRegion(): SettingsRegion

    @GET("/profile/coins")
    suspend fun coins(): Long

    @POST("/credits-package/payment")
    suspend fun createPayment(@Body request: PackagePaymentRequest): PackagePaymentResponse

    @GET("/profile/cards")
    suspend fun getSavedCreditCard(): CreditCardResponse

    @GET("/profile")
    suspend fun profile(): ProfileResponse

    @GET("/healthz/startup")
    suspend fun checkConnection()

    @GET("/progress-level/all")
    suspend fun getProgressLevels(): List<ProgressLevel>
}
