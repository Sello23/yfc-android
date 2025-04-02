package com.yourfitness.coach.ui.features.facility.dialog.you_have_upcoming_class

import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.data.entity.SubscriptionEntity
import com.yourfitness.coach.data.entity.toLatLng
import com.yourfitness.coach.domain.facility.FacilityRepository
import com.yourfitness.coach.domain.settings.SettingsManager
import com.yourfitness.coach.domain.subscription.SubscriptionManager
import com.yourfitness.coach.network.YFCRestApi
import com.yourfitness.coach.network.dto.Classification
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.data.entity.ProfileEntity
import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.domain.location.LocationRepository
import com.yourfitness.common.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class YouHaveUpcomingClassViewModel @Inject constructor(
    val navigator: Navigator,
    private val repository: FacilityRepository,
    private val locationRepository: LocationRepository,
    private val restApi: YFCRestApi,
    private val subscriptionManager: SubscriptionManager,
    private val profileRepository: ProfileRepository,
    private val settingsManager: SettingsManager
) : MviViewModel<Any, Any>() {

    fun findNearestGym() {
        viewModelScope.launch {
            try {
                state.postValue(DialogState.Loading)
                val gyms = repository.loadFacilities(Classification.GYM)
                val settingsGlobal = settingsManager.getSettings()
                state.postValue(DialogState.Loading)
                val location = locationRepository.lastLocation()
                val profile = profileRepository.getProfile()
                var minDistance: Double = Double.MAX_VALUE
                var itemMinDistance = FacilityEntity()
                val latLng = LatLng(location.latitude, location.longitude)
                gyms.forEach {
                    val distance = SphericalUtil.computeDistanceBetween(it.toLatLng(), latLng)
                    if (distance < minDistance) {
                        minDistance = distance
                        itemMinDistance = it
                    }
                }
                if (minDistance < (settingsGlobal?.gymsNearbyMetersLimit ?: 0)) {
                    state.postValue(DialogState.NearestGym(itemMinDistance, profile, latLng))
                } else {
                    throw throw IllegalArgumentException("no gym near")
                }
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(DialogState.BookedClassError(error))
            }
        }
    }

    fun getSubscription() {
        viewModelScope.launch {
            try {
                val response = subscriptionManager.getSubscription() ?: throw Exception("No subscription")
                state.postValue(DialogState.LoadSubscription(response))
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(DialogState.SubscriptionError(error))
            }
        }
    }
}

open class DialogState {
    object Loading : DialogState()
    data class NearestGym(val item: FacilityEntity, val profile: ProfileEntity, val latLng: LatLng) : DialogState()
    data class BookedClassError(val error: Exception) : DialogState()
    data class LoadSubscription(val subscription: SubscriptionEntity) : DialogState()
    data class SubscriptionError(val error: Exception) : DialogState()
}
