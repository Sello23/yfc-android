package com.yourfitness.coach.ui.features.payments.redeem_voucher

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yourfitness.common.domain.date.toMilliseconds
import com.yourfitness.coach.domain.date.toSeconds
import com.yourfitness.common.domain.date.today
import com.yourfitness.coach.domain.referral.VoucherResult
import com.yourfitness.coach.network.dto.ReferralVoucher
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.domain.date.today
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.shop.ui.constants.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Long.max
import javax.inject.Inject

@HiltViewModel
class RedeemVoucherViewModel @Inject constructor(
    private val navigator: Navigator,
    savedState: SavedStateHandle
) : MviViewModel<RedeemVoucherIntent, RedeemVoucherState>() {

    val flow = savedState[Constants.CORP_CODE_STATUS_FLOW] ?: VoucherResult.ERROR
    private val voucher: ReferralVoucher? = savedState[Constants.VOUCHER]

    private val actionMap = mapOf(
        VoucherResult.SUCCESS to (::dismiss to null),
        VoucherResult.INFLUENCER_DISCOUNT to (::dismiss to null),
        VoucherResult.ERROR to (::openEnterVoucher to ::dismiss)
    )

    private val mainAction = actionMap[flow]?.first
    private val secondaryAction = actionMap[flow]?.second

    init {
        loadData()
    }

    override fun handleIntent(intent: RedeemVoucherIntent) {
        when (intent) {
            is RedeemVoucherIntent.SecondaryActionButtonClicked -> secondaryAction?.invoke()
            is RedeemVoucherIntent.MainActionButtonClicked -> mainAction?.invoke()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                if (flow != VoucherResult.ERROR && voucher != null) {
                    val now = today().time
                    val expireTime = voucher.endDate?.toMilliseconds() ?: now
                    val timeDiff = max(expireTime - now, 0L)
                    val hours = timeDiff.toSeconds() / 3600
                    val minutes = kotlin.math.ceil(timeDiff.toSeconds() / 60.0).toLong()
                    state.postValue(
                        RedeemVoucherState.Loaded(
                            /*timeDiff <= MAX_EXPIRE_TIME_MS*/false,
                            hours,
                            minutes,
                            voucher.userRewardAmount ?: 0
                        )
                    )
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun openEnterVoucher() {
        navigator.navigate(Navigation.ReferralCode(true))
    }

    private fun dismiss() {
        state.value = RedeemVoucherState.Dismiss
    }

    companion object {
        private const val MAX_EXPIRE_TIME_MS = 25 * 60 * 1000
    }
}

open class RedeemVoucherState {
    object Dismiss : RedeemVoucherState()
    data class Loaded(
        val expiresSoon: Boolean,
        val hours: Long,
        val minutes: Long,
        val bonus: Int
    ) : RedeemVoucherState()
}

open class RedeemVoucherIntent {
    object SecondaryActionButtonClicked : RedeemVoucherIntent()
    object MainActionButtonClicked : RedeemVoucherIntent()
}
