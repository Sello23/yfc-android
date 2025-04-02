package com.yourfitness.coach.ui.features.more.challenges.challenge_details

import androidx.core.os.bundleOf
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChallengeDetailsViewModel @Inject constructor(
    val navigator: Navigator,
) : MviViewModel<Any, Any>() {
    var resultBundle = bundleOf()
}