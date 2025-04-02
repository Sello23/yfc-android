package com.yourfitness.pt.ui.features.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.yourfitness.common.domain.date.today
import com.yourfitness.common.domain.location.LocationRepository
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.common.ui.utils.formatDistance
import com.yourfitness.pt.data.entity.PtCombinedEntity
import com.yourfitness.pt.domain.models.FacilityInfo
import com.yourfitness.pt.domain.pt.PtRepository
import com.yourfitness.pt.domain.values.FACILITY_LIST
import com.yourfitness.pt.domain.values.PT_ID
import com.yourfitness.pt.ui.navigation.PtNavigation
import com.yourfitness.pt.ui.navigation.PtNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PtDetailsViewModel @Inject constructor(
    private val navigator: PtNavigator,
    private val repository: PtRepository,
    private val locationRepository: LocationRepository,
    savedState: SavedStateHandle
) : MviViewModel<PtDetailsIntent, PtDetailsState>() {

    private val ptId: String = savedState.get<String>(PT_ID).orEmpty()
    private var facilities: List<FacilityInfo> = listOf()

    private var hasSessions: Boolean = false

    init {
        loadData()
    }

    override fun handleIntent(intent: PtDetailsIntent) {
        when (intent) {
            is PtDetailsIntent.FacilitiesSeeAll -> loadAllFacilities(true)
            is PtDetailsIntent.FacilitiesSeeLess -> loadAllFacilities(false)
            is PtDetailsIntent.BookSessionTapped -> navigator.navigate(
                if (hasSessions) PtNavigation.Calendar(ptId)
                else PtNavigation.BuySessions(ptId)
            )
            is PtDetailsIntent.UpdateGeneralInfo -> updateGeneralInfo()
            is PtDetailsIntent.OpenAvailabilityCalendar -> openPtAvailabilityCalendar()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val location = locationRepository.lastLocation()
                val pt = repository.getPtByIdCombined(ptId) ?: return@launch
                facilities = repository.getPtFacilities(ptId)
                    .sortedBy {
                        val itemLocation = LatLng(it.latitude, it.longitude)
                        SphericalUtil.computeDistanceBetween(itemLocation, location)
                    }
                hasSessions = (pt.balance?.amount ?: 0) > 0
                state.postValue(PtDetailsState.Loaded(pt, facilities, location))
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun loadAllFacilities(expanded: Boolean) {
        viewModelScope.launch {
            try {
                val location = locationRepository.lastLocation()
                state.postValue(PtDetailsState.FacilitiesUpdated(facilities, location, expanded))
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun updateGeneralInfo() {
        viewModelScope.launch {
            try {
                val pt = repository.getPtByIdCombined(ptId) ?: return@launch
                hasSessions = (pt.balance?.amount ?: 0) > 0
                state.postValue(PtDetailsState.GeneralInfoUpdated(pt))
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun openPtAvailabilityCalendar() {
        navigator.navigate(PtNavigation.Calendar(ptId, false))
    }
}

open class PtDetailsState {
    object Loading : PtDetailsState()
    data class Error(val error: Exception) : PtDetailsState()
    data class Loaded(
        val pt: PtCombinedEntity,
        val facilities: List<FacilityInfo>,
        val location: LatLng
    ) : PtDetailsState()
    data class FacilitiesUpdated(
        val facilities: List<FacilityInfo>,
        val location: LatLng,
        val expanded: Boolean
    ) : PtDetailsState()
    data class GeneralInfoUpdated(val pt: PtCombinedEntity) : PtDetailsState()
}

open class PtDetailsIntent {
    object FacilitiesSeeAll : PtDetailsIntent()
    object FacilitiesSeeLess : PtDetailsIntent()
    object BookSessionTapped : PtDetailsIntent()
    object UpdateGeneralInfo : PtDetailsIntent()
    object OpenAvailabilityCalendar : PtDetailsIntent()

}
