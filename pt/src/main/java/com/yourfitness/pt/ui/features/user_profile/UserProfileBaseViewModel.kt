package com.yourfitness.pt.ui.features.user_profile

import com.yourfitness.common.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
open class UserProfileBaseViewModel @Inject constructor() :
    MviViewModel<UserProfileBaseIntent, UserProfileBaseState>() {

    override fun handleIntent(intent: UserProfileBaseIntent) {
        when (intent) {
            is UserProfileBaseIntent.MainActionButtonClicked -> onMainButtonClick()
        }
    }

    protected open fun loadData() {}


    protected open fun onMainButtonClick() {}
}

open class UserProfileBaseState {
    object Loading : UserProfileBaseState()
    data class Error(val error: Exception) : UserProfileBaseState()
}

open class UserProfileBaseIntent {
    object MainActionButtonClicked : UserProfileBaseIntent()
}
