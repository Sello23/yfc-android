package com.yourfitness.coach.network.firebase

import com.yourfitness.coach.network.firebase.dto.WebhookHealthData
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FirebaseRestApi {

    @GET("/api/spike/records/{spikeUserId}/{providerId}/{dataType}")
    suspend fun getHealthData(
        @Path("spikeUserId") spikeUserId: String,
        @Path("providerId") providerId: String,
        @Path("dataType") dataType: String,
        @Query("fromDate") fromDate: String,
        @Query("toDate") toDate: String
    ): WebhookHealthData
}
