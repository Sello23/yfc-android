package com.yourfitness.coach.ui.features.sign_up.select_gender

import androidx.lifecycle.SavedStateHandle
import com.yourfitness.coach.domain.entity.User
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.network.dto.Gender
import com.yourfitness.common.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SelectGenderViewModel @Inject constructor(
    private val navigator: Navigator,
    savedStateHandle: SavedStateHandle
) : MviViewModel<Any, Any>() {

    private val user = savedStateHandle.get<User>("user") ?: User()

    override fun handleIntent(intent: Any) {
        when (intent) {
            is SelectGenderIntent.Next -> {
                val newUser = user.copy(gender = intent.gender)
                navigator.navigate(Navigation.UpdatePhoto(newUser))
            }
        }
    }
}

open class SelectGenderIntent {
    data class Next(val gender: Gender): SelectGenderIntent()
}