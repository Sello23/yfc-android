package com.yourfitness.coach.ui.features.sign_up.enter_voucher_code

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.R
import com.yourfitness.coach.domain.entity.CoinReward
import com.yourfitness.coach.domain.entity.User
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
class EnterCorporateCodeViewModel @Inject constructor(
    private val navigator: Navigator,
    private val referralManager: ReferralManager,
    savedState: SavedStateHandle
) : MviViewModel<EnterCorporateCodeIntent, EnterCorporateCodeState>() {

    override fun handleIntent(intent: EnterCorporateCodeIntent) {
        when (intent) {
            is EnterCorporateCodeIntent.Next -> {
                checkCorporationCode(intent.code)
            }
            is EnterCorporateCodeIntent.Skip -> navigator.navigate(Navigation.AlmostThere)
        }
    }

    private fun checkCorporationCode(code: String) {
        viewModelScope.launch {
            try {
                if (code.isBlank()) {
                    navigator.navigate(Navigation.AlmostThere)
                    return@launch
                }
                state.postValue(EnterCorporateCodeState.Loading)
                val result = referralManager.checkCorporateCode(code)
                state.postValue(EnterCorporateCodeState.Success)
                when (result.first) {
                    VoucherResult.SUCCESS -> navigator.navigate(Navigation.AlmostThere)

                    VoucherResult.REFERRAL_SUCCESS -> {
                        navigator.navigate(
                            Navigation.ProgressReward(
                                VoucherReward(
                                    result.second.firstOrNull()?.toInt() ?: 0,
                                    R.string.referral_reward_msg
                                ),
                                true
                            )
                        )
                    }

                    else -> {
                        navigator.navigate(Navigation.VoucherCodeStatus(result.first, result.second, code))
                    }
                }
            } catch (error: SignUpCodeException) {
                state.postValue(EnterCorporateCodeState.SubmitError(error.codeType))
                Timber.e(error)
                navigator.navigate(Navigation.VoucherCodeStatus(VoucherResult.ERROR))
            } catch (error: Exception) {
                state.postValue(EnterCorporateCodeState.Error(error))
                Timber.e(error)
            }
        }
    }
}

open class EnterCorporateCodeIntent {
    data class Next(val code: String): EnterCorporateCodeIntent()
    object Skip: EnterCorporateCodeIntent()
}

open class EnterCorporateCodeState {
    object Loading: EnterCorporateCodeState()
    object Success: EnterCorporateCodeState()
    data class Error(val error: Exception) : EnterCorporateCodeState()
    data class SubmitError(val codeType: String) : EnterCorporateCodeState()
}
