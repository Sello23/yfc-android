package com.yourfitness.coach.ui.features.sign_up.enter_email

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.domain.ProfileManager
import com.yourfitness.coach.domain.ProfileManagerException
import com.yourfitness.coach.domain.entity.User
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EnterEmailViewModel @Inject constructor(
    private val profileManager: ProfileManager,
    private val navigator: Navigator,
    savedState: SavedStateHandle
) : MviViewModel<EnterEmailIntent, Any>() {

    private val user: User = savedState.get<User>("user") ?: User()

    override fun handleIntent(intent: EnterEmailIntent) {
        when (intent) {
            is EnterEmailIntent.Next -> checkEmail(intent.email)
        }
    }

    private fun checkEmail(email: String) {
        viewModelScope.launch {
            try {
                state.postValue(EnterEmailState.Loading)
                profileManager.checkEmail(email)
                state.postValue(EnterEmailState.Success)
                val newUser = user.copy(email = email)
                navigator.navigate(Navigation.EnterPhone(newUser))
            } catch (error: ProfileManagerException.EmailException) {
                Timber.e(error)
                state.postValue(EnterEmailState.EmailError(error))
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(EnterEmailState.Error(error))
            }
        }
    }
}

open class EnterEmailState {
    object Loading: EnterEmailState()
    object Success: EnterEmailState()
    data class Error(val error: Exception): EnterEmailState()
    data class EmailError(val error: Exception): EnterEmailState()
}

open class EnterEmailIntent {
    data class Next(val email: String) : EnterEmailIntent()
}