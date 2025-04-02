package com.yourfitness.coach.ui.features.setup.almost_there

import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AlmostThereViewModel @Inject constructor(
    private val navigator: Navigator,
    private val profileRepository: ProfileRepository,
) : MviViewModel<AlmostThereIntent, Any>() {

    init {
        navigateNext()
    }

    override fun handleIntent(intent: AlmostThereIntent) {
        when (intent) {
            is AlmostThereIntent.Next -> startApp()
        }
    }

    private fun navigateNext() {
        viewModelScope.launch {
            delay(2000)
            startApp()
        }
    }

    private fun startApp() {
        viewModelScope.launch {
            try {
                navigator.navigate(Navigation.Progress(profileRepository.isPtRole(), profileRepository.isBookable(),profileRepository.accessWorkoutPlans()))
            } catch (error: Exception) {
                Timber.e(error)
            }
        }
    }
}

open class AlmostThereIntent {
    object Next : AlmostThereIntent()
}

const val REWARD_VALUE = "reward_value"