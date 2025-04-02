package com.yourfitness.coach.ui.navigation

import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.kinestex.kinestexsdkkotlin.KinesteXSDK
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.data.entity.Timetable
import com.yourfitness.coach.domain.entity.CoinReward
import com.yourfitness.coach.domain.entity.Reward
import com.yourfitness.coach.domain.entity.User
import com.yourfitness.coach.domain.facility.Filters
import com.yourfitness.coach.domain.leaderboard.LeaderboardId
import com.yourfitness.coach.domain.models.BookedClass
import com.yourfitness.coach.domain.models.BookingResultData
import com.yourfitness.coach.domain.models.CalendarBookClassData
import com.yourfitness.coach.domain.models.PaymentFlow
import com.yourfitness.coach.domain.progress.points.ProgressInfo
import com.yourfitness.coach.domain.referral.VoucherResult
import com.yourfitness.coach.network.dto.Classification
import com.yourfitness.coach.network.dto.ReferralVoucher
import com.yourfitness.coach.ui.features.more.challenges.ChallengeFlow
import com.yourfitness.coach.ui.features.profile.connected_devices.ProviderType
import com.yourfitness.coach.ui.features.profile.connected_devices.dialog.went_wrong.ErrorType
import com.yourfitness.coach.ui.features.sign_up.enter_phone.Flow
import com.yourfitness.coach.ui.utils.DubaiDetailsData
import com.yourfitness.common.data.entity.ProfileEntity
import com.yourfitness.common.domain.models.BuyOptionData
import com.yourfitness.common.network.dto.Challenge
import kotlinx.coroutines.delay
import kotlinx.parcelize.Parcelize
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Navigator @Inject constructor() {

    val navigation = MutableLiveData<Navigation?>()

    fun navigate(node: Navigation) {
        navigation.postValue(node)
    }

    suspend fun navigateDelayed(node: Navigation) {
        delay(500)
        navigate(node)
    }
}

open class Navigation {
    object HardUpdate : Navigation()
    data class EnterSurname(val user: User) : Navigation()
    data class EnterEmail(val user: User) : Navigation()
    data class EnterPhone(val user: User, val flow: Flow = Flow.SIGN_UP) : Navigation()
    data class ConfirmPhone(val user: User, val flow: Flow = Flow.SIGN_UP) : Navigation()
    data class SelectBirthday(val user: User) : Navigation()
    data class SelectGender(val user: User) : Navigation()
    data class EnterCorporateCode(val user: User) : Navigation()
    data class UpdatePhoto(val user: User) : Navigation()
    data class Story(val title: String) : Navigation()
    data class MyReferralCode(val referralCode: String?, val coinsToVoucherOwner: String?) : Navigation()
    data class PaymentIntent(val paymentIntent: String) : Navigation()
    object PaymentHistory : Navigation()
    object EnterName : Navigation()
    object WelcomeSlider : Navigation()
    object FAQ : Navigation()
    object More : Navigation()
    object WelcomeBack : Navigation()
    object AlmostThere : Navigation()
    object Splash : Navigation()
    object Profile : Navigation()
    object SignOut : Navigation()
    object ProfileSettings : Navigation()
    object UpdateProfile : Navigation()
    object ConnectedDevices : Navigation()
    data class GoogleFitConnected(val type: ProviderType) : Navigation()
    data class GoogleFitDisconnected(val type: ProviderType) : Navigation()
    data class WentWrong(val type: ErrorType = ErrorType.Default) : Navigation()
    object NeedPermission : Navigation()
    object HelpCenter : Navigation()
    data class Map(val classification: Classification = Classification.GYM, val filters: Filters = Filters()) : Navigation()
    data class List(val classification: Classification = Classification.GYM, val filters: Filters = Filters()) : Navigation()
    data class Search(val classification: Classification) : Navigation()
    data class NoResult(val classification: Classification) : Navigation()
    data class Filter(val classification: Classification, val filters: Filters = Filters()) : Navigation()
    data class FacilityDetails(val facility: FacilityEntity, val classification: Classification) : Navigation()
    data class Progress(val isPtRole: Boolean, val isBookable: Boolean, val accessWorkoutPlans: Boolean,) : Navigation()
    data class ProgressReward(val reward: Reward = CoinReward(), val startup: Boolean = false) : Navigation()
    data class PointsHint(val pointsPerStep: Double, val pointsPerCalorie: Double) : Navigation()
    object SubscriptionError : Navigation()
    data class NearestGym(val gym: FacilityEntity, val profile: ProfileEntity, val isPtRole: Boolean) : Navigation()
    object WhatYourPositionDialog : Navigation()
    data class AreYouHereRightNow(val gym: FacilityEntity, val profile: ProfileEntity, val latLng: LatLng) : Navigation()
    data class SomethingWentWrongGym(val isPtRole: Boolean) : Navigation()
    object NoPermission : Navigation()
    object NoCameraPermission : Navigation()
    data class ManualSearch(val latLng: LatLng, val profile: ProfileEntity) : Navigation()
    object FitnessCalendar : Navigation()
    data class YouHaveUpcomingClass(val fitnessClass: BookedClass, val profile: ProfileEntity) : Navigation()
    data class UpcomingClass(val fitnessClass: BookedClass, val profile: ProfileEntity) : Navigation()
    object BitQuitHere : Navigation()
    data class Subscription(val flow: PaymentFlow, val resetBackStack: Boolean = false) : Navigation()
    data class PaymentOptions(
        val duration: Int? = null,
        val price: Long,
        val oldPrice: Long? = null,
        val currency: String,
        val subscriptionType: String? = null,
        val creditData: BuyOptionData? = null,
        val bookClassData: CalendarBookClassData? = null,
        val flow: PaymentFlow
    ) : Navigation()

    data class SuccessPaymentSubscription(val flow: PaymentFlow) : Navigation()
    object ConfirmationCancelSubscription : Navigation()
    data class SuccessCancelSubscription(val expiredDate: Long) : Navigation()
    object BackToFacilityDetails : Navigation()
    data class BonusHint(val currentMaxVisits: Int) : Navigation()
    object DeletionRequest : Navigation()
    object DeletionRequestSuccess : Navigation()
    object DeletionRequestError : Navigation()
    data class ReferralCode(val needPopUp: Boolean = false) : Navigation()
    object ReferralCodeInvalid : Navigation()
    data class ReferralCodeRedeemed(val bonusCredits: String, val newCost: String?) : Navigation()
    @Parcelize data class BookingClassCalendar(
        val classId: String = "",
        val className: String? = null,
        val facility: FacilityEntity? = null,
        val isRebook: Boolean = false,
        val rebookClassId: String? = null
    ) : Parcelable, Navigation()
    data class ConfirmCancelClass(val bookedClass: CalendarBookClassData) : Navigation()
    data class ConfirmCancelResult(val refundedCredits: Int) : Navigation()
    data class BuyCredits(val data: CalendarBookClassData? = null, val flow: PaymentFlow) : Navigation()
    data class ConfirmBooking(val data: CalendarBookClassData) : Navigation()
    data class CreditsNotEnough(val data: CalendarBookClassData) : Navigation()
    data class DoubleBooked(val data: CalendarBookClassData, val canReschedule: Boolean) : Navigation()
    object BackToClassCalendar : Navigation()
    data class ConfirmBookingResult(val data: BookingResultData) : Navigation()
    object PaymentError : Navigation()
    object Challenges : Navigation()
    data class AccessThirtyDayChallenge(val kinestexNavId: Int) : Navigation()
    object ThirtyDayChallenge : Navigation()
    object Shop : Navigation()
    object ShopOrderDetails : Navigation()
    data class ChallengeDetails(
        val challenge: Challenge,
        val flow: ChallengeFlow,
        val dubaiInfo: DubaiDetailsData? = null,
        val hideActions: Boolean = false
    ) : Navigation()
    data class ChallengeJoin(val challenge: Challenge) : Navigation()
    data class ChallengeJoined(val challenge: Challenge) : Navigation()
    data class ChallengeLeave(val challenge: Challenge) : Navigation()
    data class Leaderboard(
        val challenge: Challenge? = null,
        val leaderboardId: LeaderboardId
    ) : Navigation()
    data class ChallengeJoinPrivate(val challenge: Challenge) : Navigation()
    data class ChallengeJoinPrivateCode(val challenge: Challenge) : Navigation()
    data class VoucherCodeStatus(
        val flow: VoucherResult,
        val data: kotlin.collections.List<String> = emptyList(),
        val code: String? = null,
        val needPopUp: Boolean = false,
    ) : Navigation()
    data class RedeemVoucherResult(
        val flow: VoucherResult,
        val voucher: ReferralVoucher? = null,
    ) : Navigation()

    data class TimetableInfo(val workTime: Timetable?) : Navigation()

    data class LevelSystemDialog(
        val progress: ProgressInfo,
        val pointsPerStep: Double,
        val pointsPerCalorie: Double,
        var pointLevel: Long,
        var rewardLevel: Long
    ) : Navigation()

    object Dubai30x30Calendar : Navigation()
    object WorkoutsUpdated : Navigation()
    data class PtDetails(val ptId: String) : Navigation()
    data class BuyPtSessions(val ptId: String) : Navigation()
    object PtDashboard : Navigation()
    data class PopScreen(val times: Int = 1) : Navigation()
}