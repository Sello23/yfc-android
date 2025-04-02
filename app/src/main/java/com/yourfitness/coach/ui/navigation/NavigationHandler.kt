package com.yourfitness.coach.ui.navigation

import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavOptions
import com.yourfitness.coach.R
import com.yourfitness.pt.domain.values.DIALOG_ERROR_TYPE
import com.yourfitness.pt.domain.values.PROVIDER_TYPE
import com.yourfitness.shop.ui.constants.Constants
import com.yourfitness.shop.ui.constants.Constants.Companion.CORP_CODE_STATUS_FLOW
import com.yourfitness.shop.ui.constants.Constants.Companion.DATA_LIST
import com.yourfitness.shop.ui.constants.Constants.Companion.REWARD
import com.yourfitness.shop.ui.constants.Constants.Companion.STARTUP
import com.yourfitness.shop.ui.constants.Constants.Companion.TIMETABLE
import com.yourfitness.shop.ui.constants.Constants.Companion.VOUCHER

class NavigationHandler constructor(
    private val navController: NavController,
    private val navigator: Navigator
) {

    fun observeNavigation(owner: LifecycleOwner) {
        navigator.navigation.observe(owner) {
            if (it != null) {
                navigator.navigation.value = null
                navigate(it)
            }
        }
    }

    private fun navigate(node: Navigation) {
        when (node) {
            is Navigation.Splash -> {
                val options = NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()
                navController.navigate(R.id.fragment_splash_screen, null, options)
            }
            is Navigation.HardUpdate -> {
                if (navController.currentDestination?.id == R.id.fragment_hard_update) return
                val options = NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()
                navController.navigate(R.id.fragment_hard_update, null, options)
            }
            is Navigation.EnterSurname -> {
                val args = bundleOf("user" to node.user)
                navController.navigate(R.id.fragment_enter_surname, args)
            }
            is Navigation.EnterEmail -> {
                val args = bundleOf("user" to node.user)
                navController.navigate(R.id.fragment_enter_email, args)
            }
            is Navigation.EnterPhone -> {
                val args = bundleOf(
                    "user" to node.user,
                    "flow" to node.flow
                )
                navController.navigate(R.id.fragment_enter_phone, args)
            }
            is Navigation.ConfirmPhone -> {
                val args = bundleOf(
                    "user" to node.user,
                    "flow" to node.flow
                )
                navController.navigate(R.id.fragment_confirm_phone, args)
            }
            is Navigation.SelectBirthday -> {
                val args = bundleOf("user" to node.user)
                val options = NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()
                navController.navigate(R.id.fragment_select_birthday, args, options)
            }
            is Navigation.SelectGender -> {
                val args = bundleOf("user" to node.user)
                navController.navigate(R.id.fragment_select_gender, args)
            }
            is Navigation.EnterCorporateCode -> {
                val args = bundleOf("user" to node.user)
                navController.navigate(R.id.fragment_enter_corporate_code, args)
            }
            is Navigation.EnterName -> {
                navController.navigate(R.id.fragment_enter_name)
            }
            is Navigation.UpdatePhoto -> {
                val args = bundleOf("user" to node.user)
                navController.navigate(R.id.fragment_upload_photo, args)
            }
            is Navigation.WelcomeSlider -> {
                navController.popBackStack()
                navController.navigate(R.id.fragment_welcome_slider)
            }
            is Navigation.FAQ -> {
                if (!navController.popBackStack(R.id.fragment_faq, false)) {
                    val options = NavOptions.Builder()
                        .setEnterAnim(R.anim.animation_slide_start_fragment)
                        .setPopExitAnim(R.anim.animation_slide_end_fragment)
                        .build()
                    navController.navigate(R.id.fragment_faq, null, options)
                }
            }
            is Navigation.More -> {
                val options = NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()
                navController.navigate(R.id.fragment_more, null, options)
            }
            is Navigation.WelcomeBack -> {
                navController.popBackStack()
                navController.navigate(R.id.fragment_welcome_back)
            }
            is Navigation.AlmostThere -> {
                val options = NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()
                navController.navigate(R.id.fragment_almost_there, null, options)
            }
            is Navigation.Story -> {
                val options = NavOptions.Builder()
                    .setEnterAnim(androidx.transition.R.anim.abc_slide_in_bottom)
                    .setPopExitAnim(androidx.transition.R.anim.abc_slide_out_bottom)
                    .build()
                val args = bundleOf("title" to node.title)
                navController.navigate(R.id.fragment_story, args, options)
            }
            is Navigation.Profile -> {
                val options = NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()
                navController.navigate(R.id.fragment_profile, null, options)
            }
            is Navigation.SignOut -> {
                navController.navigate(R.id.dialog_sign_out)
            }
            is Navigation.ProfileSettings -> {
                navController.navigate(R.id.fragment_profile_settings)
            }
            is Navigation.UpdateProfile -> {
                navController.navigate(R.id.fragment_update_profile)
            }
            is Navigation.ConnectedDevices -> {
                navController.navigate(R.id.fragment_connected_devices)
            }
            is Navigation.GoogleFitConnected -> {
                val args = bundleOf(PROVIDER_TYPE to node.type)
                navController.navigate(R.id.dialog_google_fit_connected, args)
            }
            is Navigation.GoogleFitDisconnected -> {
                val args = bundleOf(PROVIDER_TYPE to node.type)
                navController.navigate(R.id.dialog_google_fit_disconnected, args)
            }
            is Navigation.WentWrong -> {
                val args = bundleOf(DIALOG_ERROR_TYPE to node.type)
                navController.navigate(R.id.dialog_went_wrong, args)
            }
            is Navigation.NeedPermission -> {
                navController.navigate(R.id.dialog_need_permission)
            }
            is Navigation.HelpCenter -> {
                navController.navigate(R.id.dialog_help_center)
            }
            is Navigation.Map -> {
                val args = bundleOf(
                    "classification" to node.classification,
                    "filters" to node.filters
                )
                val options = NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()
                navController.navigate(R.id.fragment_map, args, options)
            }
            is Navigation.List -> {
                val args = bundleOf(
                    "classification" to node.classification,
                    "filters" to node.filters
                )
                val options = NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()
                navController.navigate(R.id.fragment_list, args, options)
            }
            is Navigation.Search -> {
                val args = bundleOf("classification" to node.classification)
                navController.navigate(R.id.fragment_search, args)
            }
            is Navigation.NoResult -> {
                val args = bundleOf("classification" to node.classification)
                navController.navigate(R.id.fragment_no_result, args)
            }
            is Navigation.Filter -> {
                val args = bundleOf(
                    "classification" to node.classification,
                    "filters" to node.filters
                )
                navController.navigate(R.id.dialog_filters, args)
            }
            is Navigation.FacilityDetails -> {
                val args = bundleOf(
                    "facility" to node.facility,
                    "classification" to node.classification
                )
                navController.navigate(R.id.fragment_facility_details, args)
            }
            is Navigation.Progress -> {
                val options = NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()

                val args = bundleOf(
                    "accessWorkoutPlans" to node.accessWorkoutPlans
                )

                navController.navigate(
                    if (node.isPtRole && node.isBookable) R.id.fragment_dashboard else R.id.fragment_progress,
                    args,
                    options
                )
            }
            is Navigation.ProgressReward -> {
                val args = bundleOf(REWARD to node.reward, STARTUP to node.startup)
                navController.navigate(R.id.fragment_coin_reward, args)
            }
            is Navigation.PointsHint -> {
                val args = bundleOf(
                    "pointsPerStep" to node.pointsPerStep,
                    "pointsPerCalorie" to node.pointsPerCalorie,
                )
                navController.navigate(R.id.dialog_points_hint, args)
            }
            is Navigation.MyReferralCode -> {
                val args = bundleOf(
                    "voucher" to node.referralCode,
                    "coinsToVoucherOwner" to node.coinsToVoucherOwner,
                )
                navController.navigate(R.id.dialog_my_referral_code, args)
            }
            is Navigation.PaymentHistory -> {
                navController.navigate(R.id.fragment_payment_history)
            }
            is Navigation.PaymentIntent -> {
                val args = bundleOf("paymentIntent" to node.paymentIntent)
                navController.navigate(R.id.fragment_payment_history_webview, args)
            }
            is Navigation.SubscriptionError -> {
                navController.navigate(R.id.fragment_subscription_error)
            }

            is Navigation.ThirtyDayChallenge -> {
                navController.navigate(R.id.dialog_thirty_day_challenge)
            }

            is Navigation.AccessThirtyDayChallenge -> {
                val args = bundleOf(
                    "kinestexNavId" to node.kinestexNavId
                )
                navController.navigate(R.id.fragment_access_thirty_dsay_challenges, args)
            }

            is Navigation.NearestGym -> {
                val args = bundleOf(
                    "facility" to node.gym,
                    "profile" to node.profile,
                    "is_pt_role" to node.isPtRole,
                )
                navController.navigate(R.id.dialog_nearest_gym, args)
            }
            is Navigation.WhatYourPositionDialog -> {
                navController.navigate(R.id.dialog_what_is_your_position)
            }
            is Navigation.AreYouHereRightNow -> {
                val args = bundleOf(
                    "facility" to node.gym,
                    "profile" to node.profile,
                    "latLng" to node.latLng
                )
                navController.navigate(R.id.dialog_are_your_here_right_now, args)
            }
            is Navigation.SomethingWentWrongGym -> {
                val args = bundleOf("is_pt_role" to node.isPtRole)
                navController.navigate(R.id.dialog_something_went_wrong_gym, args)
            }
            is Navigation.NoPermission -> {
                navController.navigate(R.id.dialog_no_permission)
            }

            is Navigation.NoCameraPermission -> {
                navController.navigate(R.id.dialog_no_camera_permission)
            }
            is Navigation.ManualSearch -> {
                val args = bundleOf("latLng" to node.latLng, "profile" to node.profile)
                navController.navigate(R.id.fragment_manual_search, args)
            }
            /* is Navigation.FitnessCalendar -> { // TODO temporary removed Studios
                 navController.navigate(R.id.fragment_fitness_calendar)
             }*/
            /*is Navigation.YouHaveUpcomingClass -> { // TODO temporary removed Studios
                val args = bundleOf("facility" to node.fitnessClass, "profile" to node.profile)
                navController.navigate(R.id.dialog_you_have_upcoming_class, args)
            }*/
            is Navigation.UpcomingClass -> {
                val args = bundleOf("facility" to node.fitnessClass, "profile" to node.profile)
                navController.navigate(R.id.dialog_upcoming_class, args)
            }
            is Navigation.BitQuitHere -> {
                navController.navigate(R.id.dialog_bit_quiet_here)
            }
            is Navigation.Subscription -> {
                val options = if (node.resetBackStack) {
                    NavOptions.Builder()
                        .setPopUpTo(R.id.nav_graph, true)
                        .build()
                } else null
                val args = bundleOf("flow" to node.flow)
                navController.navigate(R.id.fragment_subscription, args, options)
            }
            is Navigation.PaymentOptions -> {
                val args = bundleOf(
                    "duration" to node.duration,
                    "price" to node.price,
                    "old_price" to node.oldPrice,
                    "currency" to node.currency,
                    "type" to node.subscriptionType,
                    "credit_data" to node.creditData,
                    "confirm_booking_data" to node.bookClassData,
                    "flow" to node.flow,
                )
                navController.navigate(R.id.fragment_payment_options, args)
            }
            is Navigation.SuccessPaymentSubscription -> {
                val args = bundleOf("flow" to node.flow)
                navController.navigate(R.id.dialog_success_payment_subscription, args)
            }
            is Navigation.ConfirmationCancelSubscription -> {
                navController.navigate(R.id.dialog_confirmation_cancel_subscription)
            }
            is Navigation.SuccessCancelSubscription -> {
                val args = bundleOf("expired_date" to node.expiredDate)
                navController.navigate(R.id.dialog_success_cancel_subscription, args)
            }
            is Navigation.BackToFacilityDetails -> {
                navController.popBackStack(R.id.fragment_facility_details, false)
            }
            is Navigation.BonusHint -> {
                val args = bundleOf("visits" to node.currentMaxVisits)
                navController.navigate(R.id.dialog_bonus_hint, args)
            }
            is Navigation.DeletionRequest -> {
                navController.navigate(R.id.dialog_deletion_request)
            }
            is Navigation.DeletionRequestSuccess -> {
                navController.navigate(R.id.dialog_deletion_request_success)
            }
            is Navigation.DeletionRequestError -> {
                navController.navigate(R.id.dialog_deletion_request_error)
            }
            is Navigation.ReferralCode -> {
                if (node.needPopUp) {
                    navController.popBackStack()
                }
                navController.navigate(R.id.dialog_referral_code)
            }
            is Navigation.ReferralCodeInvalid -> {
                navController.navigate(R.id.dialog_referral_code_invalid)
            }
            is Navigation.ReferralCodeRedeemed -> {
                val args = bundleOf(
                    "bonusCredits" to node.bonusCredits,
                    "newCost" to node.newCost
                )
                navController.navigate(R.id.dialog_referral_code_redeemed, args)
            }
//            is Navigation.BookingClassCalendar -> { // TODO temporary removed Studios
//                val args = bundleOf(
//                    "class_id" to node.classId,
//                    "class_name" to node.className,
//                    "facility" to node.facility,
//                    "is_rebook" to node.isRebook,
//                    "rebook_class_id" to node.rebookClassId
//                )
//                navController.navigate(R.id.fragment_class_calendar, args)
//            }
            is Navigation.ConfirmCancelClass -> {
                val args = bundleOf("data" to node.bookedClass)
                navController.navigate(R.id.fragment_class_cancel, args)
            }
            is Navigation.ConfirmCancelResult -> {
                val args = bundleOf("refunded_credits" to node.refundedCredits)
                navController.navigate(R.id.fragment_confirm_cancel_result, args)
            }
            is Navigation.BuyCredits -> {
                val args = bundleOf(
                    "confirm_booking_data" to node.data,
                    "flow" to node.flow
                )
                navController.navigate(R.id.fragment_credits, args)
            }
            is Navigation.ConfirmBooking -> {
                val args = bundleOf("data" to node.data)
                navController.navigate(R.id.dialog_book_class, args)
            }
            is Navigation.CreditsNotEnough -> {
                val args = bundleOf("confirm_booking_data" to node.data)
                navController.navigate(R.id.dialog_credits_not_enough, args)
            }
            is Navigation.DoubleBooked -> {
                val args = bundleOf(
                    "data" to node.data,
                    "can_reschedule" to node.canReschedule
                )
                navController.navigate(R.id.dialog_double_booked, args)
            }
//            is Navigation.BackToClassCalendar -> { // TODO temporary removed Studios
//                navController.popBackStack(R.id.fragment_class_calendar, false)
//            }
            is Navigation.ConfirmBookingResult -> {
                val args = bundleOf("data" to node.data)
                navController.navigate(R.id.dialog_confirm_booking_result, args)
            }
            is Navigation.PaymentError -> {
                navController.navigate(R.id.dialog_payment_error)
            }
            is Navigation.Challenges -> {
                val options = NavOptions.Builder()
                    .setPopUpTo(R.id.fragment_more, false)
                    .build()
                navController.navigate(R.id.fragment_challenges, null, options)
            }
            is Navigation.Shop -> {
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://com.yourfitness.shop/fragment_shop_categories".toUri())
                    .build()
                navController.navigate(request)
            }
            is Navigation.ShopOrderDetails -> {
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://com.yourfitness.shop/fragment_orders_history".toUri())
                    .build()
                navController.navigate(request)
            }
            is Navigation.ChallengeDetails -> {
                val options = NavOptions.Builder()
                    .build()
                val args = bundleOf(
                    "challenge" to node.challenge,
                    "flow" to node.flow,
                    "dubai_info" to node.dubaiInfo,
                    "hide_actions" to node.hideActions,
                )
                navController.navigate(R.id.fragment_challenge_details, args, options)
            }
            is Navigation.ChallengeJoin -> {
                val args = bundleOf("challenge" to node.challenge)
                navController.navigate(R.id.dialog_challenge_join, args)
            }
            is Navigation.ChallengeJoined -> {
                val args = bundleOf("challenge" to node.challenge)
                navController.navigate(R.id.dialog_challenge_joined, args)
            }
            is Navigation.ChallengeLeave -> {
                val args = bundleOf("challenge" to node.challenge)
                navController.navigate(R.id.dialog_challenge_leave, args)
            }
            is Navigation.Leaderboard -> {
                val args = bundleOf(
                    "challenge" to node.challenge,
                    "leaderboard_id" to node.leaderboardId
                )
                navController.navigate(R.id.fragment_leaderboard, args)
            }
            is Navigation.ChallengeJoinPrivate -> {
                val args = bundleOf("challenge" to node.challenge)
                navController.navigate(R.id.dialog_challenge_join_private, args)
            }
            is Navigation.ChallengeJoinPrivateCode -> {
                val args = bundleOf("challenge" to node.challenge)
                navController.navigate(R.id.dialog_challenge_join_private_code, args)
            }
            is Navigation.VoucherCodeStatus -> {
                if (node.needPopUp) {
                    navController.popBackStack()
                }
                val args = bundleOf(
                    CORP_CODE_STATUS_FLOW to node.flow,
                    DATA_LIST to node.data,
                    Constants.CORP_CODE to node.code,
                )
                navController.navigate(R.id.dialog_corporate_code_status, args)
            }
            is Navigation.LevelSystemDialog -> {
                val args = bundleOf(
                    "progressInfo" to node.progress,
                    "pointsPerStep" to node.pointsPerStep,
                    "pointsPerCalorie" to node.pointsPerCalorie,
                    "pointLevel" to node.pointLevel,
                    "rewardLevel" to node.rewardLevel
                )
                navController.navigate(R.id.dialog_level_system, args)
            }
            is Navigation.PtDetails -> {
                val args = bundleOf("pt_id" to node.ptId)
                navController.navigate(R.id.fragment_pt_details_to, args)
            }
            is Navigation.BuyPtSessions -> {
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://com.yourfitness.pt/fragment_buy_sessions/${node.ptId}/3".toUri())
                    .build()

                navController.navigate(request)
            }
            is Navigation.PtDashboard -> {
                navController.navigate(R.id.fragment_dashboard)
            }
            is Navigation.RedeemVoucherResult -> {
                navController.popBackStack()
                val args = bundleOf(
                    CORP_CODE_STATUS_FLOW to node.flow,
                    VOUCHER to node.voucher
                )
                navController.navigate(R.id.dialog_redeem_voucher_result, args)
            }
            is Navigation.TimetableInfo -> {
                val args = bundleOf(TIMETABLE to node.workTime)
                navController.navigate(R.id.dialog_work_time, args)
            }
            is Navigation.Dubai30x30Calendar -> {
                navController.navigate(R.id.dialog_dubai30x30_calendar)
            }
            is Navigation.WorkoutsUpdated -> {
                navController.popBackStack()
                navController.navigate(R.id.workouts_updated)
            }
            is Navigation.PopScreen -> {
                for (i in 1..node.times) navController.popBackStack()
            }
            else -> Toast.makeText(navController.context, "Navigation destination not implemented", Toast.LENGTH_SHORT).show()
        }
    }
}
