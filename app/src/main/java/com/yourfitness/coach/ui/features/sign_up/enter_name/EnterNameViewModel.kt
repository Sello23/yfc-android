package com.yourfitness.coach.ui.features.sign_up.enter_name

import com.yourfitness.coach.domain.entity.User
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EnterNameViewModel @Inject constructor(
    private val navigator: Navigator,
) : MviViewModel<EnterNameIntent, EnterNameState>() {

    override fun handleIntent(intent: EnterNameIntent) {
        when (intent) {
            is EnterNameIntent.Next -> {
                val user = User(name = intent.name)
                navigator.navigate(Navigation.EnterSurname(user))
            }
        }
    }
}

open class EnterNameIntent {
    data class Next(val name: String): EnterNameIntent()
}

open class EnterNameState {
}