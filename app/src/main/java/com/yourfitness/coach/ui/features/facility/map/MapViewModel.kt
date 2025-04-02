package com.yourfitness.coach.ui.features.facility.map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.yourfitness.coach.BannerState
import com.yourfitness.coach.PreferencesStorage
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.data.entity.SubscriptionEntity
import com.yourfitness.coach.domain.facility.FacilityRepository
import com.yourfitness.coach.domain.facility.Filters
import com.yourfitness.coach.domain.subscription.SubscriptionManager
import com.yourfitness.coach.domain.subscription.isActive
import com.yourfitness.coach.network.dto.Classification
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.domain.date.now
import com.yourfitness.common.domain.location.LocationRepository
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.pt.data.entity.fullName
import com.yourfitness.pt.domain.pt.PtRepository
import com.yourfitness.pt.ui.navigation.PtNavigation
import com.yourfitness.pt.ui.navigation.PtNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    val navigator: Navigator,
    private val ptNavigator: PtNavigator,
    private val repository: FacilityRepository,
    private val ptRepository: PtRepository,
    private val locationRepository: LocationRepository,
    private val preferencesStorage: PreferencesStorage,
    private val subscriptionManager: SubscriptionManager,
    savedState: SavedStateHandle,
) : MviViewModel<Any, Any>() {

    var classification = savedState.get<Classification>("classification") ?: preferencesStorage.facilityTab
    var filters = savedState.get<Filters>("filters") ?: Filters()
    var location: LatLng? = null
    var selectedFacility: FacilityEntity? = null

    init {
        downloadInitData()
        when (classification) {
            Classification.GYM -> getSubscription()
            Classification.TRAINER -> getSubscription()
            Classification.STUDIO -> getCredits()
        }
    }

    override fun handleIntent(intent: Any) {
        when (intent) {
            is MapIntent.Load -> {
                filters = Filters()
                loadFacilities(intent.classification, false)
            }
            is MapIntent.MyLocation -> fetchMyLocation()
            is MapIntent.UpdateFilters -> updateFilters(intent.filters)
            is MapIntent.LoadFacilityTrainers -> {
                selectedFacility = intent.facility
                if (classification == Classification.TRAINER) {
                    loadFacilityTrainers(intent.facility)
                }
            }
            is MapIntent.PtDetailsTapped -> openPtDetails(intent.id)
            is MapIntent.ComingSoon -> ptNavigator.navigate(PtNavigation.ComingSoon) // TODO Coming soon
        }
    }

    private fun updateFilters(filters: Filters) {
        this.filters = filters
        viewModelScope.launch(Dispatchers.IO) {
            try {
                state.postValue(MapState.Loading)
                val items = if (classification == Classification.TRAINER) {
                    repository.getFilteredFacilitiesForPt(filters)
                } else {
                    repository.getFilteredFacilities(classification, filters).filter { it.isYfcGym }
                }
                state.postValue(MapState.FacilitiesLoaded(items, false))
            } catch (error: Exception) {
                state.postValue(MapState.Error(error))
                Timber.e(error)
            }
        }
    }

    private fun downloadInitData() {
        viewModelScope.launch {
            val startTime = now()
            while (preferencesStorage.facilitiesLoaded != 1) {
                delay(100)
                if ((now() - startTime) > 10000) break
            }
            loadFacilities(classification, true)
        }
    }

    private fun loadFacilities(classification: Classification, zoomAll: Boolean) {
        this.classification = classification
        preferencesStorage.facilityTab = classification
        viewModelScope.launch(Dispatchers.IO) {
            try {
                state.postValue(MapState.Loading)
                location = locationRepository.lastLocation()
                filters = repository.configureFilters(classification, filters)
                val items = if (classification == Classification.TRAINER) {
                    repository.getFilteredFacilitiesForPt(filters, location)
                } else {
                    repository.getFilteredFacilities(classification, filters, location).filter { it.isYfcGym }
                }
                val lastSelectedFacility = if (classification == Classification.GYM) selectedFacility else null
                state.postValue(MapState.FacilitiesLoaded(items, zoomAll, lastSelectedFacility))
                if (classification == Classification.TRAINER) {
                    delay(300)
                    loadFacilityTrainers(selectedFacility)
                }
            } catch (error: Exception) {
                state.postValue(MapState.Error(error))
                Timber.e(error)
            }
        }
    }

    private fun fetchMyLocation() {
        viewModelScope.launch {
            try {
                val location = locationRepository.lastLocation()
                state.postValue(MapState.MyLocation(location))
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(MapState.Error(error))
            }
        }
    }

    private fun getSubscription() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (BannerState.isSubscriptionBannerClose) return@launch
                val subscription = subscriptionManager.getSubscription() ?: throw Exception("No Subscription")
                state.postValue(MapState.SubscriptionValue(subscription, subscription.isActive()))
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(MapState.SubscriptionError(error))
            }
        }
    }

    fun getCredits() {
        /*viewModelScope.launch(Dispatchers.IO) { // TODO temporary removed Studios
            try {
                if (!BannerState.isCreditsBannerClose) {
                    val credits = restApi.credits()
                    state.postValue(MapState.Credits(credits.toInt()))
                }
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(MapState.Error(error))
            }
        }*/
    }

    fun setSubscriptionBannerStateClose() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                BannerState.closeSubscriptionBanner()
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(MapState.Error(error))
            }
        }
    }

    fun setCreditsBannerStateClose() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                BannerState.closeCreditsBanner()
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(MapState.Error(error))
            }
        }
    }

    fun refreshListButton() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                loadFacilities(classification, false)
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(MapState.Error(error))
            }
        }
    }

    private fun loadFacilityTrainers(facility: FacilityEntity?) {
        if (facility == null) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val trainers = ptRepository.getAllPersonalTrainers()

                val filteredTrainers =
                    trainers.filter { facility.personalTrainersIds?.contains(it.id) == true }
                        .filter { item ->
                            val filterFocusAreas = filters.types.map { it.lowercase() }
                            filters.types.isEmpty() ||
                                    item.focusAreas.orEmpty()
                                        .map { it.lowercase() }
                                        .any { it in filterFocusAreas }
                        }
                        .associate {
                            it.id.orEmpty() to Triple(
                                it.mediaId.orEmpty(),
                                it.focusAreas.orEmpty(),
                                it.fullName
                            )
                        }

                state.postValue(MapState.FacilityTrainersLoaded(facility, filteredTrainers))
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(MapState.Error(error))
            }
        }
    }

    private fun openPtDetails(ptId: String) {
        navigator.navigate(Navigation.PtDetails(ptId))
        state.value = MapIntent.Stub
    }
}

open class MapIntent {
    object Stub : MapIntent()
    data class Load(val classification: Classification) : MapIntent()
    object MyLocation : MapIntent()
    data class UpdateFilters(val filters: Filters) : MapIntent()
    data class LoadFacilityTrainers(val facility: FacilityEntity) : MapIntent()
    data class PtDetailsTapped(val id: String): MapIntent()
    object ComingSoon : MapIntent()
}

open class MapState {
    object Loading : MapState()
    data class FacilitiesLoaded(
        val items: List<FacilityEntity>,
        val zoomAll: Boolean,
        val selectedFacility: FacilityEntity? = null
    ) : MapState()
    data class Error(val error: Exception) : MapState()
    data class MyLocation(val location: LatLng) : MapState()
    data class SubscriptionValue(val subscription: SubscriptionEntity, val isActive: Boolean) : MapState()
    data class SubscriptionError(val error: Exception) : MapState()
    data class Credits(val credits: Int) : MapState()
    data class FacilityTrainersLoaded(
        val facility: FacilityEntity,
        val trainers: PtShortcut
    ) : MapState()
}

typealias PtShortcut = Map<String, Triple<String, List<String>, String>>
