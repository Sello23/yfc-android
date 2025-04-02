package com.yourfitness.coach.ui.features.payments.referral_code_redeemed

import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.coach.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReferralCodeRedeemedViewModel @Inject constructor(
    val navigator: Navigator,
) : MviViewModel<Any, Any>()