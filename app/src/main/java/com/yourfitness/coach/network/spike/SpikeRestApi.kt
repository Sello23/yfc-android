package com.yourfitness.coach.network.spike

import com.yourfitness.coach.network.spike.dto.DeleteIntegrationData
import com.yourfitness.coach.network.spike.dto.DeleteIntegrationResponse
import com.yourfitness.coach.network.spike.dto.SpikeData
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SpikeRestApi {

    @GET("/init-user-integration")
    suspend fun init(
        @Query("provider") provider: String,
        @Query("user_id") userId: String,
        @Query("client_id") clientId: String,
    )

    @GET("/users/{user_id}")
    suspend fun getUser(@Path("user_id") userId: String)

    @GET("/v2/metrics/steps_intraday")
    suspend fun getSteps(
        @Query("user_id") userId: String,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("timezone_offset") timezoneOffset: Int,
    ): SpikeData

    @GET("/v2/metrics/activities_summary")
    suspend fun getActivitiesSummary(
        @Query("user_id") userId: String,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("timezone_offset") timezoneOffset: Int,
    ): SpikeData

    @POST("/users/{user_id}/remove-integrations")
    suspend fun deleteIntegration(@Path("user_id") userId: String, @Body providers: DeleteIntegrationData) : DeleteIntegrationResponse
}
