package com.yourfitness.coach.ui.features.facility.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.domain.facility.FacilityRepository
import com.yourfitness.coach.domain.subscription.SubscriptionManager
import com.yourfitness.coach.network.dto.Classification
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.domain.location.LocationRepository
import com.yourfitness.pt.domain.models.TrainerCard
import com.yourfitness.pt.domain.pt.PtRepository
import com.yourfitness.pt.ui.features.list.PtListIntent
import com.yourfitness.pt.ui.navigation.PtNavigation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FacilityDetailsViewModel @Inject constructor(
    val navigator: Navigator,
    private val subscriptionManager: SubscriptionManager,
    private val facilityRepository: FacilityRepository,
    savedState: SavedStateHandle
) : MviViewModel<FacilityDetailsIntent, Any>() {

    val facility = savedState.get<FacilityEntity>("facility") ?: FacilityEntity()
    val classification = savedState.get<Classification>("classification") ?: Classification.GYM
    private val ptExpandedState: MutableMap<String, Boolean> = mutableMapOf()

    override fun handleIntent(intent: FacilityDetailsIntent) {
        when (intent) {
            is FacilityDetailsIntent.Load -> {}
            is FacilityDetailsIntent.UpdatePtExpandedState -> updateExpandedStates(
                intent.id,
                intent.pos
            )

            is FacilityDetailsIntent.PtDetailsTapped -> openPtDetails(intent.id)
        }
    }

    private fun openPtDetails(ptId: String) {
        navigator.navigate(Navigation.PtDetails(ptId))
    }

    private fun updateExpandedStates(id: String, pos: Int) {
        val isExpanded = ptExpandedState.getOrDefault(id, false)
        ptExpandedState[id] = !isExpanded
        state.value = FacilityDetailsState.UpdatePtExpandedStates(ptExpandedState, pos)
    }

    fun checkGymAccess() {
        viewModelScope.launch {
            try {
                state.postValue(FacilityDetailsState.Loading)
                val hasAccess = subscriptionManager.hasGymAccess(facility, classification)
                state.postValue(FacilityDetailsState.GymAccess(hasAccess))
                val trainers = facilityRepository.getTrainersPerFacility(facility)
                state.postValue(FacilityDetailsState.FacilitiesLoaded(trainers, ptExpandedState))
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(FacilityDetailsState.Error(error))
            }
        }
    }
}

open class FacilityDetailsState {
    object Loading : FacilityDetailsState()
    data class GymAccess(val hasAccess: Boolean) : FacilityDetailsState()
    data class Error(val error: Exception) : FacilityDetailsState()
    data class FacilitiesLoaded(
        val ptItems: List<TrainerCard>,
        val expandedStates: Map<String, Boolean>
    ) : FacilityDetailsState()

    data class UpdatePtExpandedStates(val expandedStates: Map<String, Boolean>, val pos: Int) :
        FacilityDetailsState()
}

open class FacilityDetailsIntent {
    object Load : FacilityDetailsIntent()
    data class UpdatePtExpandedState(val id: String, val pos: Int) : FacilityDetailsIntent()
    data class PtDetailsTapped(val id: String) : FacilityDetailsIntent()
}