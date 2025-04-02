package com.yourfitness.coach.ui.features.facility.dialog.are_you_here_right_now

import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.data.entity.ProfileEntity
import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AreYouHereRightNowViewModel @Inject constructor(
    val navigator: Navigator,
    private val profileRepository: ProfileRepository
) : MviViewModel<AreYouHereRightNowIntent, AreYouHereRightNowState>() {

    override fun handleIntent(intent: AreYouHereRightNowIntent) {
        when (intent) {
            is AreYouHereRightNowIntent.ShowAccessCard -> showAccessCard(intent.gym, intent.profile)
        }
    }

    private fun showAccessCard(gym: FacilityEntity, profile: ProfileEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val isPt = profileRepository.isPtRole()
                navigator.navigate(Navigation.NearestGym(gym, profile, isPt))
            } catch (error: Exception) {
                Timber.e(error)
            }
        }
    }
}

sealed class AreYouHereRightNowIntent {
    data class ShowAccessCard(val gym: FacilityEntity, val profile: ProfileEntity) : AreYouHereRightNowIntent()
}

sealed class AreYouHereRightNowState {}