package com.yourfitness.coach.ui.features.facility.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.yourfitness.coach.PreferencesStorage
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.domain.facility.FacilityRepository
import com.yourfitness.coach.domain.facility.Filters
import com.yourfitness.coach.network.dto.Classification
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.domain.date.now
import com.yourfitness.common.domain.location.LocationRepository
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.pt.domain.models.TrainerCard
import com.yourfitness.pt.ui.navigation.PtNavigation
import com.yourfitness.pt.ui.navigation.PtNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    val navigator: Navigator,
    private val ptNavigator: PtNavigator,
    private val repository: FacilityRepository,
    private val locationRepository: LocationRepository,
    private val preferencesStorage: PreferencesStorage,
    savedState: SavedStateHandle
) : MviViewModel<Any, Any>() {
    private var isInited = false

    var classification = savedState.get<Classification>("classification") ?: preferencesStorage.facilityTab
        private set
    var filters = savedState.get<Filters>("filters") ?: Filters()
    private val ptExpandedState: MutableMap<String, Boolean> = mutableMapOf()

    init {
        downloadInitData()
    }

    private fun downloadInitData() {
        state.value = ListState.Loading
        viewModelScope.launch {
            val startTime = now()
            while (preferencesStorage.facilitiesLoaded != 1) {
                delay(100)
                if ((now() - startTime) > 10000) break
            }
            loadFacilities(classification)
            isInited = true
        }
    }

    override fun handleIntent(intent: Any) {
        when (intent) {
            is ListIntent.ScreenOpened -> if (isInited) loadFacilitiesAsync(this.classification)
            is ListIntent.Load -> {
                filters = Filters()
                loadFacilitiesAsync(intent.classification)
            }
            is ListIntent.UpdateFilters -> updateFilters(intent.filters)
            is ListIntent.UpdatePtExpandedState -> updateExpandedStates(
                intent.id,
                intent.pos
            )
            is ListIntent.PtDetailsTapped -> {
                openPtDetails(intent.id)
            }
            is ListIntent.ComingSoon -> ptNavigator.navigate(PtNavigation.ComingSoon) // TODO Coming soon
        }
    }

    private fun openPtDetails(ptId: String) {
        navigator.navigate(Navigation.PtDetails(ptId))
    }

    private fun loadFacilitiesAsync(classification: Classification) {
        viewModelScope.launch { loadFacilities(classification) }
    }

    private suspend fun loadFacilities(classification: Classification) {
        this.classification = classification
        preferencesStorage.facilityTab = classification
        filters = repository.configureFilters(classification, filters)
        loadData()
    }

    private fun updateFilters(filters: Filters) {
        this.filters = filters
        viewModelScope.launch {
            loadData()
        }
    }

    private fun updateExpandedStates(id: String, pos: Int) {
        val isExpanded = ptExpandedState.getOrDefault(id, false)
        ptExpandedState[id] = !isExpanded
        state.value = ListState.UpdatePtExpandedStates(ptExpandedState, pos)
    }

    private suspend fun loadData() {
        try {
            state.postValue(ListState.Loading)
            val location = locationRepository.lastLocation()
            var ptItems = emptyList<TrainerCard>()
            val items = if (classification == Classification.TRAINER) {
                repository.getFilteredFacilitiesForPt(filters, location)
            } else {
                repository.getFilteredFacilities(classification, filters, location).filter { it.isYfcGym }
            }
            if (classification == Classification.TRAINER) {
                ptItems = repository.getFilteredTrainers(filters, location, classification).shuffled()
            }
            state.postValue(ListState.FacilitiesLoaded(items, ptItems, ptExpandedState, location))
        } catch (error: Exception) {
            state.postValue(ListState.Error(error))
            Timber.e(error)
        }

    }
}

open class ListIntent {
    data class Load(val classification: Classification): ListIntent()
    data class UpdateFilters(val filters: Filters) : ListIntent()
    data class UpdatePtExpandedState(val id: String, val pos: Int): ListIntent()
    data class PtDetailsTapped(val id: String): ListIntent()
    object ScreenOpened: ListIntent()
    object ComingSoon: ListIntent()

}

open class ListState {
    object Loading: ListState()
    data class FacilitiesLoaded(
        val items: List<FacilityEntity>,
        val ptItems: List<TrainerCard>,
        val expandedStates: Map<String, Boolean>,
        val location: LatLng
    ) : ListState()
    data class Error(val error: Exception): ListState()
    data class UpdatePtExpandedStates(val expandedStates: Map<String, Boolean>, val pos: Int) : ListState()
}
