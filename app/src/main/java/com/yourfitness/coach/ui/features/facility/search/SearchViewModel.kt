package com.yourfitness.coach.ui.features.facility.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.data.entity.distance
import com.yourfitness.coach.domain.facility.FacilityRepository
import com.yourfitness.coach.network.dto.Classification
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.domain.location.LocationRepository
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.pt.data.entity.PersonalTrainerEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    val navigator: Navigator,
    private val repository: FacilityRepository,
    private val locationRepository: LocationRepository,
    savedState: SavedStateHandle
) : MviViewModel<Any, Any>() {

    val classification = savedState.get<Classification>("classification") ?: Classification.GYM

    init {
        search("")
    }

    override fun handleIntent(intent: Any) {
        when (intent) {
            is SearchIntent.Search -> search(intent.query)
            is SearchIntent.PtDetailsTapped -> openPtDetails(intent.ptId)
        }
    }

    private fun search(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                state.postValue(SearchState.Loading)
                val location = locationRepository.lastLocation()
                var ptItems = emptyList<PersonalTrainerEntity>()
                var items = emptyList<FacilityEntity>()
                if (classification == Classification.TRAINER) {
                    ptItems = repository.searchPersonalTrainers(query)
                } else {
                    items = repository.searchFacilities(query, classification)
                        .sortedBy { it.distance(location) }
                }
                state.postValue(SearchState.Result(items, ptItems, location, query))
            } catch (error: Exception) {
                state.postValue(SearchState.Error(error))
                Timber.e(error)
            }
        }
    }

    private fun openPtDetails(ptId: String) {
        navigator.navigate(Navigation.PtDetails(ptId))
    }
}

open class SearchIntent {
    data class Search(val query: String) : SearchIntent()
    data class PtDetailsTapped(val ptId: String) : SearchIntent()
}

open class SearchState {
    object Loading : SearchState()
    data class Result(
        val items: List<FacilityEntity> = emptyList(),
        val ptItems: List<PersonalTrainerEntity> = emptyList(),
        val location: LatLng,
        val query: String
    ) : SearchState()
    data class Error(val error: Exception) : SearchState()
}
