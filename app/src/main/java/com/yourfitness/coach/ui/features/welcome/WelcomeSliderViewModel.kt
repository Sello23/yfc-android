package com.yourfitness.coach.ui.features.welcome

import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.domain.entity.User
import com.yourfitness.coach.ui.features.sign_up.enter_phone.Flow
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.network.CommonRestApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WelcomeSliderViewModel @Inject constructor(
    private val navigator: Navigator,
    private val commonRestApi: CommonRestApi
) : MviViewModel<WelcomeSliderIntent, WelcomeSliderState>() {

    override fun handleIntent(intent: WelcomeSliderIntent) {
        when (intent) {
            is WelcomeSliderIntent.SignUp -> checkServerConnection {
                navigator.navigate(Navigation.EnterName)
            }

            is WelcomeSliderIntent.HaveAccount -> checkServerConnection {
                navigator.navigate(Navigation.EnterPhone(User(), Flow.SIGN_IN))
            }
        }
    }

    private fun checkServerConnection(onNext: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                commonRestApi.checkConnection()
                onNext()
            } catch (e: Exception) {
                state.postValue(WelcomeSliderState.ConnectionError)
            }
        }
    }
}

sealed class WelcomeSliderIntent {
    object SignUp : WelcomeSliderIntent()
    object HaveAccount : WelcomeSliderIntent()
}

sealed class WelcomeSliderState {
    object ConnectionError : WelcomeSliderState()
}