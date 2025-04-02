package com.yourfitness.coach.ui.features.profile.deletion_request.saddest_news_today

import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.network.YFCRestApi
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.coach.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DeletionRequestViewModel @Inject constructor(
    val navigator: Navigator,
    private val restApi: YFCRestApi,
) : MviViewModel<Any, Any>() {

    fun sentDeletionRequest() {
        viewModelScope.launch {
            try {
                state.postValue(DeletionRequestState.Loading)
                restApi.deleteUser()
                state.postValue(DeletionRequestState.Success)
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(DeletionRequestState.Error(error))
            }
        }
    }
}

open class DeletionRequestState {
    object Loading : DeletionRequestState()
    object Success : DeletionRequestState()
    data class Error(val error: Exception) : DeletionRequestState()
}