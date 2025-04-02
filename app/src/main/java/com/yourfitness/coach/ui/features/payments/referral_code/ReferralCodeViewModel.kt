package com.yourfitness.coach.ui.features.payments.referral_code

import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.R
import com.yourfitness.coach.domain.entity.VoucherReward
import com.yourfitness.coach.domain.referral.ReferralManager
import com.yourfitness.coach.domain.referral.VoucherResult
import com.yourfitness.coach.network.dto.SignUpCodeException
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ReferralCodeViewModel @Inject constructor(
    private val navigator: Navigator,
    private val referralManager: ReferralManager
) : MviViewModel<ReferralCodeIntent, ReferralCodeState>() {

    var codeApplied = false
        private set

    fun checkCorporationCode(code: String) {
        viewModelScope.launch {
            try {
                if (code.isBlank()) {
                    navigator.navigate(Navigation.PopScreen())
                    return@launch
                }
                state.postValue(ReferralCodeState.Loading)
                val result = referralManager.checkCorporateCode(code, true)
                state.postValue(ReferralCodeState.Success)
                codeApplied = true
                when (result.first) {
                    VoucherResult.SUCCESS -> {
                        navigator.navigate(
                            Navigation.VoucherCodeStatus(
                                VoucherResult.INFLUENCER_PROMOTION,
                                result.second,
                                code,
                                true
                            )
                        )
                    }

                    VoucherResult.INFLUENCER_DISCOUNT -> {
                        navigator.navigate(
                            Navigation.VoucherCodeStatus(
                                VoucherResult.INFLUENCER_DISCOUNT2,
                                result.second,
                                code,
                                true
                            )
                        )
                    }

                    VoucherResult.ERROR -> {
                        codeApplied = false
                        navigator.navigate(
                            Navigation.VoucherCodeStatus(
                                VoucherResult.ERROR2,
                                result.second,
                                code,
                                true
                            )
                        )
                    }

                    VoucherResult.CORPORATE_REGIONAL_RATE_SUCCESS, VoucherResult.CORPORATE_NEW_RATE_SUCCESS,
                    VoucherResult.CORPORATE_COMPLIMENTARY_SUCCESS -> {
                        navigator.navigate(
                            Navigation.VoucherCodeStatus(
                                VoucherResult.CORPORATE_COMMON,
                                result.second,
                                code,
                                true
                            )
                        )
                    }

                    VoucherResult.REFERRAL_SUCCESS -> {
                        codeApplied = false
                        navigator.navigate(
                            Navigation.ProgressReward(
                                VoucherReward(
                                    result.second.firstOrNull()?.toInt() ?: 0,
                                    R.string.referral_reward_msg
                                ),
                                false
                            )
                        )
                    }

                    else -> {
                        navigator.navigate(Navigation.VoucherCodeStatus(result.first, result.second, code))
                    }
                }
            } catch (error: SignUpCodeException) {
                Timber.e(error)
                navigator.navigate(Navigation.VoucherCodeStatus(VoucherResult.ERROR2))
            } catch (error: Exception) {
                state.postValue(ReferralCodeState.Error(error))
                Timber.e(error)
            }
        }
    }
}

open class ReferralCodeIntent {
    data class Submit(val code: String): ReferralCodeIntent()
    object Skip: ReferralCodeIntent()
}

sealed class ReferralCodeState {
    object Loading: ReferralCodeState()
    object Success: ReferralCodeState()
    data class Error(val error: Exception) : ReferralCodeState()
}

