package com.yourfitness.coach.ui.features.sign_up.enter_voucher_code

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.domain.ProfileManager
import com.yourfitness.coach.domain.models.PaymentFlow
import com.yourfitness.coach.domain.referral.ReferralManager
import com.yourfitness.coach.domain.referral.VoucherResult
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.shop.ui.constants.Constants.Companion.CORP_CODE
import com.yourfitness.shop.ui.constants.Constants.Companion.CORP_CODE_STATUS_FLOW
import com.yourfitness.shop.ui.constants.Constants.Companion.DATA_LIST
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CorporateCodeStatusViewModel @Inject constructor(
    private val navigator: Navigator,
    private val profileManager: ProfileManager,
    private val referralManager: ReferralManager,
    savedState: SavedStateHandle
) : MviViewModel<CorporateCodeStatusIntent, CorporateCodeStatusState>() {

    val flow = savedState[CORP_CODE_STATUS_FLOW] ?: VoucherResult.ERROR
    val dataList = savedState[DATA_LIST] ?: emptyList<String>()
    private val code: String? = savedState[CORP_CODE]

    var retry = false
        private set

    private val actionMap = mapOf(
        VoucherResult.GENDER_ERROR to (::onChangeGender to ::openMainScreen),
        VoucherResult.CHALLENGE_SUCCESS to (::openMainScreen to null),
        VoucherResult.CORPORATE_NEW_RATE_SUCCESS to (::openSubscriptionScreen to ::openMainScreen),
        VoucherResult.CORPORATE_REGIONAL_RATE_SUCCESS to (::openSubscriptionScreen to ::openMainScreen),
        VoucherResult.CORPORATE_COMPLIMENTARY_SUCCESS to (::openMainScreen to null),
        VoucherResult.ERROR to (::closeDialog to ::openMainScreen),
        VoucherResult.INFLUENCER_DISCOUNT to (::openSubscriptionScreen to ::openMainScreen),
        VoucherResult.INFLUENCER_DISCOUNT2 to (::closeDialog to null),
        VoucherResult.ERROR2 to (::openEnterCodeDialog to ::closeDialog),
        VoucherResult.CORPORATE_COMMON to (::closeDialog to null),
        VoucherResult.INFLUENCER_PROMOTION to (::closeDialog to null),
    )

    private val mainAction = actionMap[flow]?.first
    private val secondaryAction = actionMap[flow]?.second

    override fun handleIntent(intent: CorporateCodeStatusIntent) {
        when (intent) {
            is CorporateCodeStatusIntent.SecondaryActionButtonClicked -> secondaryAction?.invoke()
            is CorporateCodeStatusIntent.MainActionButtonClicked -> mainAction?.invoke()
        }
    }

    private fun openSubscriptionScreen() {
        navigator.navigate(Navigation.Subscription(PaymentFlow.BUY_SUBSCRIPTION_FROM_SIGN_UP, true))
    }

    private fun openMainScreen() {
        navigator.navigate(Navigation.AlmostThere)
    }

    private fun closeDialog() {
        retry = true
        state.postValue(CorporateCodeStatusState.Dismiss)
    }

    private fun openEnterCodeDialog() {
        retry = true
        navigator.navigate(Navigation.ReferralCode(true))
    }

    private fun onChangeGender() {
        if (code == null) return
        state.value = CorporateCodeStatusState.Loading
        viewModelScope.launch {
            try {
                profileManager.updateGender()
                val result = referralManager.checkCorporateCode(code)
                navigator.navigate(Navigation.VoucherCodeStatus(result.first, result.second, code, true))
            } catch (e: Exception) {
                Timber.e(e)
                navigator.navigate(Navigation.VoucherCodeStatus(VoucherResult.ERROR, needPopUp = true))
            }
        }
    }
}

open class CorporateCodeStatusState {
    object Loading : CorporateCodeStatusState()
    object Dismiss : CorporateCodeStatusState()
}

open class CorporateCodeStatusIntent {
    object SecondaryActionButtonClicked : CorporateCodeStatusIntent()
    object MainActionButtonClicked : CorporateCodeStatusIntent()
}
