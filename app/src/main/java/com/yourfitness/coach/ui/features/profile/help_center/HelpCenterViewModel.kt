package com.yourfitness.coach.ui.features.profile.help_center

import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.coach.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HelpCenterViewModel @Inject constructor(
    private val navigator: Navigator,
) : MviViewModel<Any, Any>() {}