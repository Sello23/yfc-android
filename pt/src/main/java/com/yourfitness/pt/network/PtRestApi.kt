package com.yourfitness.pt.network

import com.yourfitness.common.network.dto.PackagePaymentResponse
import com.yourfitness.pt.network.dto.*
import retrofit2.http.*

interface PtRestApi {
    @POST("personal-trainer-session/payment")
    suspend fun createPackagePayment(@Body request: PtPackagePaymentRequest): PackagePaymentResponse

    @GET("/personal-trainer-session/balance/user-list")
    suspend fun getPtBalanceList(): List<PtBalanceDto>

    @GET("/personal-trainer-session/balance/{id}")
    suspend fun getPtBalance(@Path("id") id: String): PtBalanceDto

    @GET("/profile/personal-trainer-session")
    suspend fun getUserSessions(@Query("syncedAt") syncedAt: String?): List<SessionDto>?

    @GET("/personal-trainer/session")
    suspend fun getSessionsForTrainer(@Query("syncedAt") syncedAt: String?): List<SessionDto>?

    @GET("/personal-trainer-session")
    suspend fun getPtSessions(@Query("personalTrainerId") ptId: String): SessionDtoWrapper?

    @GET("/personal-trainer-session")
    suspend fun getUpcomingTraining(
        @Query("profileId") profileId: String? = null,
        @Query("from") from: String? = null,
        @Query("status") status: String,
        @Query("personalTrainerId") personalTrainerId: String? = null,
    ): SessionDtoWrapper?

    @POST("/personal-trainer-session")
    suspend fun bookSessions(@Body request: BookSessionDto): SessionDto?

    @PUT("/personal-trainer-session/{id}/status")
    suspend fun updateSessionStatus(@Path("id") id: String, @Body payload: StatusDto)

    @GET("/personal-trainer/dashboard")
    suspend fun getPtDashboardSummary(): DashboardDto?

    @GET("/personal-trainer/client")
    suspend fun getPtClients(): List<PtClientDto>

    @GET("/personal-trainer/induction")
    suspend fun getPtInductions(): List<PtInductionDto>

    @POST("/induction/{id}/complete")
    suspend fun completeInduction(@Path("id") id: String, @Body request: InductionNote)

    @POST("/v2/personal-trainer-session/blocked")
    suspend fun blockTimeSlots(@Body request: BlockTimeSlotsWrapper): List<SessionDto>?

    @DELETE("/personal-trainer-session/blocked-slot/{id}")
    suspend fun deleteBlockSlot(@Path("id") id: String)

    @POST("/personal-trainer-session/blocked/delete")
    suspend fun deleteBlockSlotList(@Body request: DeleteBlockSlotListDto)

    @DELETE("/personal-trainer-session/blocked-chain/{id}")
    suspend fun deleteChainBlock(@Path("id") id: String)
}