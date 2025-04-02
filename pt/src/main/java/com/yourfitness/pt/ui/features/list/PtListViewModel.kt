package com.yourfitness.pt.ui.features.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.yourfitness.common.domain.location.LocationRepository
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.pt.domain.models.TrainerCard
import com.yourfitness.pt.domain.pt.PtRepository
import com.yourfitness.pt.domain.values.PT_ID
import com.yourfitness.pt.ui.navigation.PtNavigation
import com.yourfitness.pt.ui.navigation.PtNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PtListViewModel @Inject constructor(
    private val navigator: PtNavigator,
    private val locationRepository: LocationRepository,
    private val ptRepository: PtRepository,
    savedState: SavedStateHandle
) : MviViewModel<PtListIntent, PtListState>() {

    val isMyPt: Boolean = savedState.get<Boolean>("my_pt") ?: true

    private var isInited = false
    private val ptExpandedState: MutableMap<String, Boolean> = mutableMapOf()

    init {
        downloadInitData()
    }

    private fun downloadInitData() {
        viewModelScope.launch {
            if (isMyPt) loadData()
            isInited = true
        }
    }

    override fun handleIntent(intent: PtListIntent) {
        when (intent) {
            is PtListIntent.ScreenOpened -> if (isInited && isMyPt) loadFacilitiesAsync()
            is PtListIntent.Load -> {}
            is PtListIntent.UpdatePtExpandedState -> updateExpandedStates(intent.id, intent.pos)
            is PtListIntent.PtDetailsTapped -> openPtDetails(intent.id)
            is PtListIntent.PtBookTapped -> openBookPt(intent.id, intent.availableSessions)
            is PtListIntent.PtZeroSessionsTapped -> navigator.navigate(PtNavigation.Calendar(intent.id, false))
        }
    }

    private fun openPtDetails(ptId: String) {
        navigator.navigate(PtNavigation.Details(ptId))
    }

    private fun openBookPt(ptId: String, availableSessions: Int) {
        navigator.navigate(
            if (availableSessions > 0) PtNavigation.Calendar(ptId)
            else PtNavigation.BuySessions(ptId)
        )
    }

    private fun loadFacilitiesAsync() {
        viewModelScope.launch { loadData() }
    }

    private fun updateExpandedStates(id: String, pos: Int) {
        val isExpanded = ptExpandedState.getOrDefault(id, false)
        ptExpandedState[id] = !isExpanded
        state.value = PtListState.UpdatePtExpandedStates(ptExpandedState, pos)
    }

    private suspend fun loadData() {
        try {
            state.postValue(PtListState.Loading)
            val location = locationRepository.lastLocation()
            val trainers = ptRepository.getUserTrainers(location)
            state.postValue(PtListState.FacilitiesLoaded(trainers, ptExpandedState))
        } catch (error: Exception) {
            state.postValue(PtListState.Error(error))
            Timber.e(error)
        }

    }
}

open class PtListIntent {
    object Load: PtListIntent()
    data class UpdatePtExpandedState(val id: String, val pos: Int): PtListIntent()
    data class PtDetailsTapped(val id: String): PtListIntent()
    data class PtBookTapped(val id: String, val availableSessions: Int): PtListIntent()
    object ScreenOpened: PtListIntent()
    data class PtZeroSessionsTapped(val id: String): PtListIntent()
}

open class PtListState {
    object Loading: PtListState()
    data class FacilitiesLoaded(
        val ptItems: List<TrainerCard>,
        val expandedStates: Map<String, Boolean>
    ) : PtListState()
    data class Error(val error: Exception): PtListState()
    data class UpdatePtExpandedStates(val expandedStates: Map<String, Boolean>, val pos: Int) : PtListState()
}
