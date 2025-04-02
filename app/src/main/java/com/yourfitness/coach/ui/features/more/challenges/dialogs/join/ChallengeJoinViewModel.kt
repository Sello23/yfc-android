package com.yourfitness.coach.ui.features.more.challenges.dialogs.join

import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.domain.ProfileManager
import com.yourfitness.coach.network.YFCRestApi
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.network.dto.Challenge
import com.yourfitness.common.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ChallengeJoinViewModel @Inject constructor(
    val navigator: Navigator,
    private val restApi: YFCRestApi,
    private val tokenManager: ProfileManager
) : MviViewModel<Any, Any>() {

    fun joinChallenge(challenge: Challenge) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                restApi.joinChallenge(challenge.id ?: "")
                tokenManager.refreshToken()
                state.postValue(ChallengeJoinState.Success(challenge))
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(ChallengeJoinState.Error(error))
            }
        }
    }
}

open class ChallengeJoinState {
    object Loading : ChallengeJoinState()
    data class Success(val challenge: Challenge) : ChallengeJoinState()
    data class Error(val error: Exception) : ChallengeJoinState()
}