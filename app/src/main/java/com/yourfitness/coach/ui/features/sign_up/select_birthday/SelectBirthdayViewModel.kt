package com.yourfitness.coach.ui.features.sign_up.select_birthday

import androidx.lifecycle.SavedStateHandle
import com.yourfitness.coach.domain.date.toSeconds
import com.yourfitness.coach.domain.entity.User
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SelectBirthdayViewModel @Inject constructor(
    private val navigator: Navigator,
    savedStateHandle: SavedStateHandle
) : MviViewModel<Any, Any>() {

    val user = savedStateHandle.get<User>("user") ?: User()

    override fun handleIntent(intent: Any) {
        when (intent) {
            is SelectBirthdayIntent.Next -> {
                val newUser = user.copy(birthday = intent.birthday.time.toSeconds())
                navigator.navigate(Navigation.SelectGender(newUser))
            }
        }
    }
}

open class SelectBirthdayIntent {
    data class Next(val birthday: Date): SelectBirthdayIntent()
}