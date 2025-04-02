package com.yourfitness.coach.ui.features.thirty_day_challenge

import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.data.entity.ProfileEntity
import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccessThirtyDayChallengeViewModel @Inject constructor(
    val navigator: Navigator) : MviViewModel<Any, Any>() {


    val showWebView: MutableStateFlow<WebViewState> = MutableStateFlow(WebViewState.LOADING)

    val reps: MutableStateFlow<Int> = MutableStateFlow(0)
    val mistake: MutableStateFlow<String> = MutableStateFlow("")

    val isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)

    fun setMistake(it: String) {
        viewModelScope.launch {
            mistake.emit(it)
        }
    }

    fun setReps(it: Int) {
        viewModelScope.launch {
            reps.emit(it)
        }
    }
}