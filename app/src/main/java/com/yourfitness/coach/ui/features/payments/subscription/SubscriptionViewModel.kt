package com.yourfitness.coach.ui.features.payments.subscription

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.data.entity.SubscriptionEntity
import com.yourfitness.coach.domain.date.toSeconds
import com.yourfitness.coach.domain.models.PaymentFlow
import com.yourfitness.coach.domain.subscription.SubscriptionManager
import com.yourfitness.coach.network.YFCRestApi
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.domain.date.toMilliseconds
import com.yourfitness.common.domain.date.today
import com.yourfitness.common.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    val navigator: Navigator,
    private val subscriptionManager: SubscriptionManager,
    private val restApi: YFCRestApi,
    private val profileRepository: ProfileRepository,
    savedStateHandle: SavedStateHandle
) : MviViewModel<SubscriptionIntent, SubscriptionState>() {

    private val flow = savedStateHandle.get<PaymentFlow>("flow") ?: PaymentFlow.BUY_SUBSCRIPTION_FROM_PROFILE

    private var canUseVoucher = false
    private var voucherCode: String? = null

    private var monthlyOption: SubscriptionOption? = null
    private var oneTimeOption: SubscriptionOption? = null
    private var voucherOption: SubscriptionOption? = null

    private var subscriptionInfo: SubscriptionInfo? = null

    init {
        loadRegionSettings()
    }

    override fun handleIntent(intent: SubscriptionIntent) {
        when (intent) {
            is SubscriptionIntent.DiscardCode -> {
                navigator.navigate(Navigation.ReferralCode())
            }
            is SubscriptionIntent.MonthlyOptionChosen -> {
                if (voucherOption != null) {
                    voucherOption = voucherOption?.copy(isChosen = true)
                    monthlyOption = monthlyOption?.copy(isChosen = false)
                } else {
                    monthlyOption = monthlyOption?.copy(isChosen = true)
                }
                oneTimeOption = oneTimeOption?.copy(isChosen = false)
                prepareUiData()
            }
            is SubscriptionIntent.OneTimeOptionChosen -> {
                voucherOption = voucherOption?.copy(isChosen = false)
                monthlyOption = monthlyOption?.copy(isChosen = false)
                oneTimeOption = oneTimeOption?.copy(isChosen = true)
                prepareUiData()
            }
        }
    }

    private fun loadRegionSettings() {
        state.postValue(SubscriptionState.Loading)
        viewModelScope.launch {
            try {
                loadSubscription()
            } catch (e: Exception) {
                state.postValue(SubscriptionState.Error(e))
            }
        }
    }

    private suspend fun loadSubscription(onComplete: (SubscriptionEntity?) -> Unit = {}) {
        try {
            val subscription = subscriptionManager.getSubscription(true)
            val info = restApi.getSubscriptionPrice()
            val profile = profileRepository.getProfile(true)

            val complementaryAccess = subscription?.complimentaryAccess == true
            canUseVoucher = profile.corporationId == null && profile.complimentaryAccess == false && !complementaryAccess

            val expiredDate = subscription?.expiredDate
            if ((expiredDate != null && expiredDate > today().time.toSeconds()) || subscription?.complimentaryAccess == true) {
                var endDate = subscription.nextPaymentDate
                if (endDate == null || endDate <= 0) {
                    endDate = expiredDate
                }
                subscriptionInfo = SubscriptionInfo(
                    subscription.isOneTime,
                    subscription.complimentaryAccess,
                    subscription.autoRenewal,
                    complementaryAccess,
                    subscription.paidSubscription,
                    endDate?.toMilliseconds(),
                    subscription.duration,
                    subscription.corporationName.orEmpty()
                )
            }

            if (subscriptionInfo != null) {
                prepareUiData()
                onComplete(subscription)
                return
            }

            voucherOption = null
            if (canUseVoucher) {
                info.voucher?.let {
                    if (it.subscriptionCost != null) {
                        voucherOption = SubscriptionOption(
                            "discountVoucher",
                            it.subscriptionCurrency?.uppercase().orEmpty(),
                            it.subscriptionCost,
                        )
                    }

                    voucherCode = it.voucherValue
                }
            }

            info.options?.find {it.isOneTimeSubscription == false}?.let {
                monthlyOption = SubscriptionOption(
                    it.type.orEmpty(),
                    it.currency?.uppercase().orEmpty(),
                    it.subscriptionCost ?: 0,
                    corporationName = it.corporationName.orEmpty(),
                    isNewRate = it.type == "corporate"
                )
            }

            info.options?.find {it.isOneTimeSubscription == true}?.let {
                oneTimeOption = SubscriptionOption(
                    it.type.orEmpty(),
                    it.currency?.uppercase().orEmpty(),
                    it.subscriptionCost ?: 0,
                    it.subscriptionCost
                        ?.div(100)
                        ?.toDouble()
                        ?.div(it.duration ?: 1)
                        ?.roundToInt()
                        ?.times(100),
                    it.duration,
                    corporationName = it.corporationName.orEmpty(),
                )
            }

            if (oneTimeOption == null) {
                if (voucherOption != null) {
                    voucherOption = voucherOption?.copy(isChosen = true)
                } else {
                    monthlyOption = monthlyOption?.copy(isChosen = true)
                }
            }

            prepareUiData()
            onComplete(subscription)
        } catch (error: Exception) {
            state.postValue(SubscriptionState.Error(error))
            onComplete(null)
        }
    }

    private fun prepareUiData() {
        state.postValue(
            SubscriptionState.Subscription(
                canUseVoucher,
                voucherCode,
                subscriptionInfo,
                monthlyOption,
                oneTimeOption,
                voucherOption,
            )
        )
    }

    fun proceedToPayment() {
        val option = (
                if (oneTimeOption?.isChosen == true) oneTimeOption
                else if (voucherOption?.isChosen == true) voucherOption
                else if (monthlyOption?.isChosen == true) monthlyOption
                else null
        ) ?: return

        navigator.navigate(
            Navigation.PaymentOptions(
                duration = option.duration,
                price = option.price.toLong(),
                oldPrice = option.oldPrice?.toLong(),
                currency = option.currency,
                flow = flow,
                subscriptionType = option.type
            )
        )
    }

    fun cancelSubscription() {
        navigator.navigate(Navigation.ConfirmationCancelSubscription)
    }

    fun resubscribe() {
        viewModelScope.launch {
            state.postValue(SubscriptionState.Loading)
            try {
                subscriptionManager.resubscribeSubscription()
                loadSubscription()
            } catch (e: Exception) {
                state.postValue(SubscriptionState.Error(e))
            }
        }
    }

    fun confirmCancel() {
        viewModelScope.launch {
            state.postValue(SubscriptionState.Loading)
            try {
                subscriptionManager.cancelSubscription()
                loadSubscription(onComplete = {
                    val expiredDate = it?.expiredDate ?: 0
                    navigator.navigate(Navigation.SuccessCancelSubscription(expiredDate))
                })
            } catch (e: Exception) {
                state.postValue(SubscriptionState.Error(e))
            }
        }
    }

    fun checkReferralCode() {
        viewModelScope.launch {
            try {
                loadSubscription()
            } catch (e: Exception) {
                state.postValue(SubscriptionState.Error(e))
            }
        }
    }
}

sealed class SubscriptionState {
    object Loading : SubscriptionState()

    data class Subscription(
        val canUseVoucher: Boolean,
        val voucherCode: String?,
        val subscription: SubscriptionInfo?,
        val monthlyOption: SubscriptionOption?,
        val oneTimeOption: SubscriptionOption?,
        val voucherOption: SubscriptionOption?,
    ) : SubscriptionState()

    data class Error(val error: Exception) : SubscriptionState()
}

sealed class SubscriptionIntent {
    object DiscardCode: SubscriptionIntent()
    object MonthlyOptionChosen: SubscriptionIntent()
    object OneTimeOptionChosen: SubscriptionIntent()
}

data class SubscriptionOption(
    val type: String,
    val currency: String,
    val price: Int,
    val oldPrice: Int? = null,
    val duration: Int? = null,
    val corporationName: String? = null,
    val isChosen: Boolean = false,
    val isNewRate: Boolean = false,
)

data class SubscriptionInfo(
    val isOneTime: Boolean,
    val isComplimentary: Boolean,
    val autoRenewal: Boolean,
    val complementaryAccess: Boolean,
    val isPaidSubscription: Boolean,
    val nextPaymentDate: Long?,
    val duration: Int?,
    val corporationName: String
)
