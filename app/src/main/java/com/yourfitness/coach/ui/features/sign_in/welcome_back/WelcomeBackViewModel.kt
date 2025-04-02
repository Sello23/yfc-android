package com.yourfitness.coach.ui.features.sign_in.welcome_back

import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.domain.entity.User
import com.yourfitness.coach.ui.features.sign_up.enter_phone.Flow
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.network.CommonRestApi
import com.yourfitness.common.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WelcomeBackViewModel @Inject constructor(
    val navigator: Navigator,
    private val commonRestApi: CommonRestApi
) : MviViewModel<WelcomeBackIntent, WelcomeBackState>() {

    override fun handleIntent(intent: WelcomeBackIntent) {
        when (intent) {
            is WelcomeBackIntent.SignIn -> checkServerConnection {
                navigator.navigate(Navigation.EnterPhone(User(), Flow.SIGN_IN))
            }

            is WelcomeBackIntent.DontHaveAccount -> checkServerConnection {
                navigator.navigate(Navigation.EnterName)
            }
        }
    }

    private fun checkServerConnection(onNext: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                commonRestApi.checkConnection()
                onNext()
            } catch (e: Exception) {
                state.postValue(WelcomeBackState.ConnectionError)
            }
        }
    }
}

sealed class WelcomeBackIntent {
    object SignIn : WelcomeBackIntent()
    object DontHaveAccount : WelcomeBackIntent()
}

sealed class WelcomeBackState {
    object ConnectionError : WelcomeBackState()
}