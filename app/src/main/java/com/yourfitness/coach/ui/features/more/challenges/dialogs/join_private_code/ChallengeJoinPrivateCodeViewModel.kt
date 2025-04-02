package com.yourfitness.coach.ui.features.more.challenges.dialogs.join_private_code

import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.domain.ProfileManager
import com.yourfitness.coach.network.YFCRestApi
import com.yourfitness.coach.network.dto.AccessCode
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.network.dto.Challenge
import com.yourfitness.common.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ChallengeJoinPrivateCodeViewModel @Inject constructor(
    val navigator: Navigator,
    private val restApi: YFCRestApi,
    private val tokenManager: ProfileManager
) : MviViewModel<Any, Any>() {

    fun joinChallenge(challenge: Challenge, code: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                restApi.joinPrivateChallenge(challenge.id ?: "", AccessCode(code))
                tokenManager.refreshToken()
                state.postValue(ChallengeJoinPrivateCodeState.Success(challenge))
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(ChallengeJoinPrivateCodeState.Error(error))
            }
        }
    }
}

open class ChallengeJoinPrivateCodeState {
    object Loading : ChallengeJoinPrivateCodeState()
    data class Success(val challenge: Challenge) : ChallengeJoinPrivateCodeState()
    data class Error(val error: Exception) : ChallengeJoinPrivateCodeState()
}