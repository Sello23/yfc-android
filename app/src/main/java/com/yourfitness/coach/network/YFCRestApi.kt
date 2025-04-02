package com.yourfitness.coach.network

import com.yourfitness.coach.network.dto.*
import com.yourfitness.common.network.dto.Challenge
import com.yourfitness.common.network.dto.PaymentReceipt
import okhttp3.MultipartBody
import retrofit2.http.*
import java.util.*

interface YFCRestApi {

    @GET("/auth/create-code")
    suspend fun createOtpCode(
        @Query("phoneNumber") phoneNumber: String,
        @Query("otpSender") otpSender: String
    ): String

    @GET("/auth/resend-code")
    suspend fun resendCode(
        @Query("otpID", encoded = false) otpId: String,
        @Query("phone_number") phoneNumber: String,
        @Query("otpSender") otpSender: String
    )

    @POST("/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("/auth/login")
    suspend fun loginCorp(@Body request: LoginWithCorpRequest): LoginResponse

    @GET("/auth/verify-code")
    suspend fun verifyOtpCode(
        @Query("code") code: String,
        @Query("otpID") otpId: String,
        @Query("phone_number") phoneNumber: String
    )

    @Multipart
    @POST("/media")
    suspend fun createMedia(@Part media: MultipartBody.Part): String

    @POST("/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest)

    @GET("/profile/check-email")
    suspend fun checkEmail(@Query("email") email: String): Boolean

    @GET("/profile/check-phone")
    suspend fun checkPhone(@Query("phone_number") phoneNumber: String): Boolean

    @GET("/settings/global")
    suspend fun getSettingsGlobal(): SettingsGlobal

//    @GET("/profile/credits") // TODO temporary removed Studios
//    suspend fun credits(): Long

    @POST("v3/profile/achievements")
    suspend fun updateAchievements(@Body request: List<PutAchievements>): List<GetAchievements>

    @GET("profile/activities")
    suspend fun getAchievementsTimeZone(@Query("from") from: String): List<GetAchievements>

    @GET("/profile/achievements")
    suspend fun getAchievements(@Query("from") from: String): List<GetAchievements>

    @GET("/profile/payments")
    suspend fun getPaymentHistory(): List<PaymentHistory>

    @GET("/payment/receipt")
    suspend fun getStripeUrl(@Query("paymentIntent") paymentIntent: String): PaymentReceipt

    @GET("/profile/subscription")
    suspend fun getSubscription(): Subscription

    @POST("/subscription/create")
    suspend fun createSubscription(@Body request: CreateSubscriptionRequest): CreateSubscriptionResponse

    @POST("/subscription/v2/create")
    suspend fun createOneTimeSubscription(@Body request: CreateOneTimeSubscriptionRequest): CreateSubscriptionResponse

    @POST("/subscription/cancel")
    suspend fun cancelSubscription()

    @POST("/subscription/resubscribe")
    suspend fun resubscribeSubscription()

    @GET("/subscription/v2/price")
    suspend fun getSubscriptionPrice(): SubscriptionOptions

    @GET("/v2/facility")
    suspend fun facilities(): FacilityResponse

    @POST("/visit")
    suspend fun sendFacilityVisitIngo(@Body facilityVisitInfo: FacilityVisitInfo)

    @GET("/schedule")
    suspend fun getSchedule(@Query("from") from: String, @Query("to") to: String): List<Schedule>

    @GET("/profile/achievements/visits")
    suspend fun getVisits(): List<Visits>

    @GET("/profile/bonus-credits")
    suspend fun getBonusCredits(): List<BonusCredits>

    @POST("/profile/delete-request")
    suspend fun deleteUser()

    @GET("/voucher/v2")
    suspend fun getVoucher(@Query("voucherValue") voucherValue: String): Voucher

    @GET("/schedule/custom-class/{id}")
    suspend fun getClassSchedule(
        @Path("id") id: String,
        @Query("from") from: String,
        @Query("to") to: String
    ): List<Schedule>

    @POST("/schedule/{id}")
    suspend fun bookClass(@Path("id") id: String)

    @DELETE("/schedule/{id}")
    suspend fun cancelBookedClass(@Path("id") id: String)

    @GET("/challenge/available")
    suspend fun getAvailableChallenge(@Query("limit") limit: Int, @Query("offset") offset: Int): List<Challenge>

    @GET("/challenge/joined")
    suspend fun getJoinedChallenge(@Query("limit") limit: Int, @Query("offset") offset: Int): List<Challenge>

    @GET("/challenge/{id}")
    suspend fun getChallenge(@Path("id") id: String): Challenge

    @POST("/challenge/{id}/join")
    suspend fun joinChallenge(@Path("id") id: String)

    @POST("/challenge/{id}/join")
    suspend fun joinPrivateChallenge(@Path("id") id: String, @Body accessCode: AccessCode)

    @GET("/challenge/{id}/leaderboard-with-rank")
    suspend fun getChallengeLeaderboard(
        @Path("id") id: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("period") period: String
    ): ChallengeLeaderboard

    @GET("/friends/{friendId}/challenge/{id}/leaderboard")
    suspend fun getFriendChallengeLeaderboard(
        @Path("friendId") friendId: String,
        @Path("id") id: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("period") period: String
    ): ChallengeLeaderboard

    @GET("/challenge/{id}/leaderboard/rank")
    suspend fun getChallengeLeaderboardRank(
        @Path("id") id: String,
        @Query("period") period: Int
    ): Entries

    @POST("/challenge/{id}/leave")
    suspend fun leaveChallenge(@Path("id") id: String)

    @GET("/leader-board")
    suspend fun getLeaderboard(): List<Leaderboard>

    @GET("/code")
    suspend fun checkCorporationCode(@Query("code") period: String): CodeType

    @PUT("/profile/corporation")
    suspend fun applyCorporationVoucher(@Body request: CorporationVoucherBody)

    @PUT("/profile/subscription-voucher")
    suspend fun applySubscriptionVoucher(@Body request: SubscriptionVoucherBody)

    @POST("/voucher/redeem")
    suspend fun applyReferralVoucher(@Body request: ReferralVoucherBody)

    @GET("/activity/dubai3030/workout")
    suspend fun getWorkouts(): List<Workout>

    @POST("/activity/dubai3030/workout")
    suspend fun saveManualWorkouts(@Body request: ManualWorkoutBody): List<Workout>

    @DELETE("/activity/dubai3030/workout")
    suspend fun deleteManualWorkouts(@Query("ids") ids: String)

    @GET("/challenge/dubai3030/corporate")
    suspend fun getCorporateLeaderboard(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("type") type: String
    ): ChallengeLeaderboard

    @GET("/challenge/dubai3030/global-with-rank")
    suspend fun getGlobalLeaderboard(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ChallengeLeaderboard

    @GET("/challenge/corporate")
    suspend fun getCorporateLeaderboardTest(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("type") type: String
    ): ChallengeLeaderboard
}
