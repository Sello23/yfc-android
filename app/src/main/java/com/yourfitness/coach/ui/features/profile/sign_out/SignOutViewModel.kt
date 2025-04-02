package com.yourfitness.coach.ui.features.profile.sign_out

import androidx.lifecycle.viewModelScope
import com.yourfitness.common.domain.session.TokenManager
import com.yourfitness.common.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SignOutViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : MviViewModel<Any, Any>() {


    override fun handleIntent(intent: Any) {
        when (intent) {
            is SignOutIntent.SignOut -> signOut()
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            try {
                tokenManager.logout()
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(SignOutState.Error(error))
            }
        }
    }
}

open class SignOutState {
    data class Error(val error: Exception) : SignOutState()
}

open class SignOutIntent {
    object SignOut : SignOutIntent()
}