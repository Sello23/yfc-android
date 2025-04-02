package com.yourfitness.coach.ui.hard_update

import com.yourfitness.coach.domain.fb_remote_config.FirebaseRemoteConfigRepository
import com.yourfitness.common.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HardUpdateViewModule @Inject constructor(
    configRepository: FirebaseRemoteConfigRepository
) : MviViewModel<Any, Any>() {

    val title = configRepository.title
    val description = configRepository.description
}