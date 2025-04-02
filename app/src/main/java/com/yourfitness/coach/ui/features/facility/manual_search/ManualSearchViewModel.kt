package com.yourfitness.coach.ui.features.facility.manual_search

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.yourfitness.coach.data.dao.FacilityDao
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.data.entity.toLatLng
import com.yourfitness.coach.domain.facility.FacilityRepository
import com.yourfitness.coach.domain.settings.SettingsManager
import com.yourfitness.coach.network.dto.Classification
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.data.entity.ProfileEntity
import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.domain.location.LocationRepository
import com.yourfitness.common.network.dto.Gender
import com.yourfitness.common.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ManualSearchViewModel @Inject constructor(
    val navigator: Navigator,
    private val repository: FacilityRepository,
    private val locationRepository: LocationRepository,
    private val facilityDao: FacilityDao,
    private val settingsManager: SettingsManager,
    private val profileRepository: ProfileRepository,
    savedState: SavedStateHandle
) : MviViewModel<ManualSearchIntent, ManualSearchState>() {

    private var gyms = mutableListOf<FacilityEntity>()
    private var latLng = LatLng(0.0, 0.0)
    private var tab = 0
    private var facilities = listOf<FacilityEntity>()
    private var isVisit = false
    val profile = savedState.get<ProfileEntity>("profile") ?: ProfileEntity.empty

    override fun handleIntent(intent: ManualSearchIntent) {
        when (intent) {
            is ManualSearchIntent.Load -> {
                loadFacilities(intent.classification)
                tab = intent.currentTab
            }
            is ManualSearchIntent.LoadRecentGym -> {
                loadRecentGym()
                tab = intent.currentTab
            }
            is ManualSearchIntent.ActionButtonClicked -> openScreen()
        }
    }

    private fun loadFacilities(classification: Classification) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                state.postValue(ManualSearchState.Loading)
                gyms.clear()
                val location = locationRepository.lastLocation()
                latLng = LatLng(location.latitude, location.longitude)
                val settingsGlobal = settingsManager.getSettings()
                val items = repository.loadFacilities(classification, location)
                items.forEach {
                    val distance = SphericalUtil.computeDistanceBetween(it.toLatLng(), latLng)
                    if (distance < (settingsGlobal?.gymsNearbyMetersLimit ?: 0)) {
                        gyms.add(it)
                    }
                }
                val filteredGyms = filterGyms(gyms)
                state.postValue(ManualSearchState.FacilitiesLoaded(filteredGyms.sortedBy {
                    SphericalUtil.computeDistanceBetween(it.toLatLng(), latLng)
                }, ManualSearchFragment.GYM, latLng, isVisit))
            } catch (error: Exception) {
                state.postValue(ManualSearchState.Error(error))
                Timber.e(error)
            }
        }
    }

    private fun loadRecentGym() {
        viewModelScope.launch {
            try {
                state.postValue(ManualSearchState.Loading)
                val gyms = facilityDao.readVisitedFacility().distinctBy { it.id }.filter { it.isYfcGym }
                facilities = gyms
                if (gyms.isNotEmpty()) isVisit = true
                val filteredGyms = filterGyms(gyms)
                state.postValue(ManualSearchState.FacilitiesLoaded(filteredGyms, ManualSearchFragment.GYM_RECENT, latLng, isVisit))
            } catch (error: Exception) {
                state.postValue(ManualSearchState.Error(error))
                Timber.e(error)
            }
        }
    }

    fun findGyms(name: String) {
        viewModelScope.launch {
            try {
                if (tab == 0) {
                    if (name == "") {
                        loadFacilities(Classification.GYM)
                    } else {
                        val facilities = mutableListOf<FacilityEntity>()
                        val normalizedQuery = name.trim().lowercase()
                        gyms.forEach {
                            if (it.name?.lowercase()?.contains(normalizedQuery) == true) {
                                facilities.add(it)
                            }
                        }
                        val filteredGyms = filterGyms(facilities)
                        state.postValue(ManualSearchState.FacilitiesLoaded(filteredGyms, ManualSearchFragment.GYM, latLng, isVisit))
                    }
                } else {
                    if (name == "") {
                        loadRecentGym()
                    } else {
                        val normalizedQuery = name.trim().lowercase()
                        val gyms = mutableListOf<FacilityEntity>()
                        facilities.forEach {
                            if (it.name?.lowercase()?.contains(normalizedQuery) == true) {
                                gyms.add(it)
                            }
                        }
                        val filteredGyms = filterGyms(gyms)
                        state.postValue(ManualSearchState.FacilitiesLoaded(filteredGyms, ManualSearchFragment.GYM_RECENT, latLng, isVisit))
                    }
                }
            } catch (error: Exception) {
                Timber.e(error)
            }
        }
    }

    private fun filterGyms(gyms: List<FacilityEntity>): List<FacilityEntity> {
        val filteredGyms = gyms.filter { it.isYfcGym }
        return if (profile.gender != Gender.FEMALE) {
            filteredGyms.filter { it.femaleOnly == false }
        } else {
            filteredGyms
        }
    }

    private fun openScreen() {
        viewModelScope.launch {
            try {
                navigator.navigate(
                    if (profileRepository.isPtRole()) Navigation.PtDashboard
                    else Navigation.Map()
                )
            } catch (error: Exception) {
                Timber.e(error)
            }
        }
    }
}

open class ManualSearchIntent {
    data class Load(val classification: Classification, val activity: Activity, val currentTab: Int) : ManualSearchIntent()
    data class LoadRecentGym(val recentGym: String, val currentTab: Int) : ManualSearchIntent()
    object ActionButtonClicked : ManualSearchIntent()
}

open class ManualSearchState {
    object Loading : ManualSearchState()
    data class FacilitiesLoaded(val items: List<FacilityEntity>, val type: String, val latLng: LatLng, val isVisit: Boolean) : ManualSearchState()
    data class Error(val error: Exception) : ManualSearchState()
}
