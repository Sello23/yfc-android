package com.yourfitness.coach.ui.features.more.challenges.dialogs.leave

import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.network.YFCRestApi
import com.yourfitness.coach.ui.features.more.challenges.dialogs.join.ChallengeJoinState
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.network.dto.Challenge
import com.yourfitness.common.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ChallengeLeaveViewModel @Inject constructor(
    val navigator: Navigator,
    private val restApi: YFCRestApi,
) : MviViewModel<Any, Any>() {

    fun leaveChallenge(challenge: Challenge) {
        viewModelScope.launch {
            try {
                restApi.leaveChallenge(challenge.id ?: "")
                state.postValue(ChallengeLeaveState.Success(challenge))
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(ChallengeJoinState.Error(error))
            }
        }
    }
}

open class ChallengeLeaveState {
    object Loading : ChallengeLeaveState()
    data class Error(val error: Exception) : ChallengeLeaveState()
    data class Success(val challenge: Challenge) : ChallengeLeaveState()
}