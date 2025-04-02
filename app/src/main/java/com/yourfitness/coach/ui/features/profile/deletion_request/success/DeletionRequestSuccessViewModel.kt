package com.yourfitness.coach.ui.features.profile.deletion_request.success

import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.coach.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DeletionRequestSuccessViewModel @Inject constructor(
    val navigator: Navigator,
) : MviViewModel<Any, Any>()