package com.yourfitness.coach.ui.features.sign_up.enter_surname

import androidx.lifecycle.SavedStateHandle
import com.yourfitness.coach.domain.entity.User
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EnterSurnameViewModel @Inject constructor(
    private val navigator: Navigator,
    savedState: SavedStateHandle
) : MviViewModel<EnterSurnameIntent, EnterSurnameState>() {

    private val user: User = savedState.get<User>("user") ?: User()

    override fun handleIntent(intent: EnterSurnameIntent) {
        when (intent) {
            is EnterSurnameIntent.Next -> {
                val user = user.copy(surname = intent.surname)
                navigator.navigate(Navigation.EnterEmail(user))
            }
        }
    }
}

open class EnterSurnameIntent {
    data class Next(val surname: String): EnterSurnameIntent()
}

open class EnterSurnameState {
}