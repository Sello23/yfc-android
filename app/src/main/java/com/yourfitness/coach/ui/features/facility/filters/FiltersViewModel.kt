package com.yourfitness.coach.ui.features.facility.filters

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.domain.facility.FacilityRepository
import com.yourfitness.coach.domain.facility.Filters
import com.yourfitness.coach.network.dto.Classification
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.pt.domain.pt.PtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FiltersViewModel @Inject constructor(
    private val repository: FacilityRepository,
    private val ptRepository: PtRepository,
    val navigator: Navigator,
    savedState: SavedStateHandle
) : MviViewModel<Any, Any>() {

    var filters = savedState.get<Filters>("filters") ?: Filters()
    var classification: Classification = savedState.get<Classification>("classification") ?: Classification.GYM

    init {
        loadFilters()
    }

    override fun handleIntent(intent: Any) {
        when (intent) {
            is FiltersIntent.DistanceChanged -> updateDistance(intent.distance)
            is FiltersIntent.TypesChanged -> updateTypes(intent.types)
            is FiltersIntent.AmenitiesChanged -> updateAmenities(intent.amenities)
            is FiltersIntent.Reset -> resetFilters()
        }
    }

    private fun updateDistance(distance: Int) {
        filters = filters.copy(distance = distance)
        fetchFacilitiesCount()
    }

    private fun updateTypes(types: List<String>) {
        filters = filters.copy(types = types)
        fetchFacilitiesCount()
    }

    private fun updateAmenities(amenities: List<String>) {
        filters = filters.copy(amenities = amenities)
        fetchFacilitiesCount()
    }

    private fun resetFilters() {
        viewModelScope.launch {
            filters = repository.configureFilters(classification)
            state.postValue(FiltersState.Loaded(filters))
            fetchFacilitiesCount()
        }
    }

    private fun loadFilters() {
        state.postValue(FiltersState.Loaded(filters))
    }

    private fun fetchFacilitiesCount() {
        viewModelScope.launch {
            try {
                if (classification == Classification.TRAINER) {
                    val filteredFacilities = repository.getFacilitiesFilteredByLocation(filters, classification)
                    val trainers = ptRepository.getFilteredTrainers(
                        filters.types,
                        filteredFacilities.mapNotNull { it.personalTrainersIds }.flatten().distinct()
                    )
                    state.postValue(FiltersState.Updated(trainers.size))
                } else {
                    val facilities = repository.getFilteredFacilities(classification, filters)
                    state.postValue(FiltersState.Updated(facilities.size))
                }
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(FiltersState.Error(error))
            }
        }
    }
}

open class FiltersIntent {
    data class DistanceChanged(val distance: Int) : FiltersIntent()
    data class TypesChanged(val types: List<String>) : FiltersIntent()
    data class AmenitiesChanged(val amenities: List<String>) : FiltersIntent()
    object Reset : FiltersIntent()
}

open class FiltersState {
    object Loading : FiltersState()
    data class Loaded(val filters: Filters) : FiltersState()
    data class Updated(val count: Int) : FiltersState()
    data class Error(val error: Exception) : FiltersState()
}
