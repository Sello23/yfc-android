package com.yourfitness.coach.ui.features.payments.payment_options

import android.content.res.Resources
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.domain.entity.SubscriptionVoucherReward
import com.yourfitness.coach.domain.models.PaymentFlow
import com.yourfitness.coach.domain.referral.ReferralManager
import com.yourfitness.coach.domain.subscription.SubscriptionManager
import com.yourfitness.coach.network.dto.Voucher
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.domain.models.PaymentData
import com.yourfitness.common.network.CommonRestApi
import com.yourfitness.common.ui.features.payments.payment_options.BasePaymentOptionsViewModel
import com.yourfitness.common.ui.features.payments.payment_options.PaymentOptionIntent
import com.yourfitness.common.ui.features.payments.payment_options.PaymentOptionState
import com.yourfitness.common.ui.navigation.CommonNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PaymentOptionsViewModel @Inject constructor(
    val navigator: Navigator,
    private val subscriptionManager: SubscriptionManager,
    private val profileRepository: ProfileRepository,
    resources: Resources,
    commonNavigator: CommonNavigator,
    commonRestApi: CommonRestApi,
    private val referralManager: ReferralManager,
    savedState: SavedStateHandle,
) : BasePaymentOptionsViewModel(
    commonNavigator, commonRestApi, resources, savedState
) {

    private val subscriptionType: String = savedState.get<String>("type").orEmpty()
    private val duration: Int? = savedState["duration"]
    val oldPrice: Long? = savedState["old_price"]
    private var refCode: Voucher? = null
    private var isRefCodeApplied: Boolean = false

    init {
        loadSavedCard()
    }

    override fun handleIntent(intent: PaymentOptionIntent) {
        when (intent) {
            is PaymentOptionIntent.OpenProgress -> openProgress()
        }
    }

    fun subscribe() {
        if (isPaymentMethodAdded()) {
            executeSubscription()
        } else {
            navigateAddCart()
        }
    }

//    private fun executeSubscription() {
//        state.value = PaymentOptionState.Loading
//        viewModelScope.launch {
//            try {
//                isRefCodeApplied = false
//                refCode = referralManager.fetchAppliedVoucher()
//                val code = refCode?.voucherValue
//                val result = subscriptionManager.createSubscription(
//                    price,
//                    type = subscriptionType,
//                    voucher = code
//                )
//                isRefCodeApplied = code?.isNotBlank() == true
//                val clientSecret = result.clientSecret.orEmpty()
//                paymentData.postValue(PaymentData(card, savedCardId, clientSecret))
//            } catch (e: Exception) {
//                state.postValue(PaymentOptionState.Error(e))
//            }
//        }
//    }

    private fun executeSubscription() {
        state.value = PaymentOptionState.Loading
        viewModelScope.launch {
            try {
                isRefCodeApplied = false
                refCode = referralManager.fetchAppliedVoucher()
                val code = refCode?.voucherValue
                val result = subscriptionManager.createOneTimeSubscription(
                    duration,
                    price,
                    type = subscriptionType,
                    voucher = code
                )
                isRefCodeApplied = code?.isNotBlank() == true
                val clientSecret = result.clientSecret.orEmpty()
                paymentData.postValue(PaymentData(card, savedCardId, clientSecret))
            } catch (e: Exception) {
                state.postValue(PaymentOptionState.Error(e))
            }
        }
    }

    fun subscriptionPaid(flow: PaymentFlow?) {
        if (isRefCodeApplied) {
            val rewards = refCode?.userRewardAmount ?: 0
            if (rewards == 0) flow?.let {
                navigator.navigate(
                    Navigation.SuccessPaymentSubscription(
                        it
                    )
                )
            }
            else navigator.navigate(Navigation.ProgressReward(SubscriptionVoucherReward(rewards)))
        } else {
            flow?.let { navigator.navigate(Navigation.SuccessPaymentSubscription(it)) }
        }
    }

    private fun openProgress() {
        viewModelScope.launch {
            try {
                profileRepository.getProfile().accessWorkoutPlans?.let {
                    Navigation.Progress(profileRepository.isPtRole(), profileRepository.isBookable(),
                        it
                    )
                }?.let { navigator.navigate(it) }
            } catch (error: Exception) {
                Timber.e(error)
            }
        }
    }
}
