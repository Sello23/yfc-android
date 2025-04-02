package com.yourfitness.coach.ui.features.more.challenges.dialogs.join_private

import com.yourfitness.coach.network.YFCRestApi
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.coach.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChallengePrivateJoinViewModel @Inject constructor(
    val navigator: Navigator,
    private val restApi: YFCRestApi,
) : MviViewModel<Any, Any>() {}