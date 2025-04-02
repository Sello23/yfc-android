package com.yourfitness.coach.ui.features.bottom_menu

import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.data.entity.SettingsEntity
import com.yourfitness.coach.data.entity.toLatLng
import com.yourfitness.coach.domain.facility.FacilityRepository
import com.yourfitness.coach.domain.models.BookedClass
import com.yourfitness.coach.domain.settings.SettingsManager
import com.yourfitness.coach.domain.subscription.SubscriptionManager
import com.yourfitness.coach.domain.subscription.isActive
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
class BottomMenuViewModel @Inject constructor(
    val navigator: Navigator,
    private val repository: FacilityRepository,
    private val locationRepository: LocationRepository,
    private val profileRepository: ProfileRepository,
    private val subscriptionManager: SubscriptionManager,
    private val settingsManager: SettingsManager,
) : MviViewModel<BottomMenuIntent, BottomMenuState>() {

    private var gyms: List<FacilityEntity>? = null
    private var settingGlobal: SettingsEntity? = null
    private var minDistance: Double = Double.MAX_VALUE
    private var nearestFacility = FacilityEntity()

    var subscriptionActive: Boolean = false
        private set

    init {
        loadFacilities()
    }

    override fun handleIntent(intent: BottomMenuIntent) {
        when (intent) {
            is BottomMenuIntent.ProcessQuickAccessTap -> getSubscription()
            is BottomMenuIntent.FindGym -> findNearestGym()
        }
    }

    private fun loadFacilities() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                state.postValue(BottomMenuState.SavedSubscriptionLoaded(isSubscriptionActive()))

                /// load settings and subscription and notify other screens to start loading using this data
                settingGlobal = settingsManager.getSettings()
                val subscription = subscriptionManager.getSubscription()
                subscriptionActive = subscription?.isActive() == true
                state.postValue(BottomMenuState.ConfigsLoaded(subscriptionActive))

                repository.downloadFacilitiesData()
                gyms = repository.loadFacilities(Classification.GYM).filter { it.isYfcGym }
                state.postValue(BottomMenuState.GymsLoaded)
            } catch (error: Exception) {
                state.postValue(BottomMenuState.Error(error))
                Timber.e(error)
            }
        }
    }

    private fun getSubscription() {
        viewModelScope.launch {
            val isSubscriptionActive = isSubscriptionActive()
            state.postValue(BottomMenuState.LoadSubscriptionState(isSubscriptionActive))
        }
    }

    private fun findNearestGym() {
        viewModelScope.launch {
            try {
                val latLng = locationRepository.lastLocation()
                val profile = profileRepository.getProfile()
                nearestFacility = FacilityEntity()
                minDistance = Double.MAX_VALUE

                nearestFacility = findNearestGymFromList(
                    if (profile.gender != Gender.FEMALE) gyms?.filter { it.femaleOnly == false }
                    else gyms,
                    latLng
                )

                val navDestination = if (minDistance < (settingGlobal?.gymsNearbyMetersLimit ?: 0)) {
                    Navigation.AreYouHereRightNow(nearestFacility, profile, latLng)
                } else {
                    Navigation.SomethingWentWrongGym(profileRepository.isPtRole())
                }
                navigator.navigate(navDestination)
                state.postValue(BottomMenuState.Loading(false))
            } catch (error: Exception) {
                Timber.e(error)
            }
        }
    }

    private fun findNearestGymFromList(gyms: List<FacilityEntity>?, latLng: LatLng): FacilityEntity {
        var nearestItem = FacilityEntity()
        gyms?.forEach {
            val distance = SphericalUtil.computeDistanceBetween(it.toLatLng(), latLng)
            if (distance < minDistance) {
                minDistance = distance
                nearestItem = it
            }
        }
        return nearestItem
    }

//    fun getBookedClasses() {
//        viewModelScope.launch {
//            try {
//                val profile = profileRepository.getProfile()
//                val facilities = repository.fetchFacilities()
//                val calendar = Calendar.getInstance()
//                calendar.add(Calendar.DAY_OF_YEAR, -START_DATE)
//                val startDate = calendar.time.formatCalendarRFC()
//                calendar.add(Calendar.DAY_OF_YEAR, END_DATE)
//                val endDate = calendar.time.formatCalendarRFC()
//                val response = restApi.getSchedule(startDate, endDate)
//                val today = Calendar.getInstance()
//                scheduleDao.delete()
//                scheduleDao.writeSchedule(response.map { it.toEntity() })
//                val schedule = scheduleDao.readAll()
//                val fitnessClass = mutableListOf<ScheduleEntity>()
//                val bookedClass = mutableListOf<BookedClass>()
//                var isFound = false
//                schedule.forEach {
//                    if (it.from.toMilliseconds().toDate()
//                            ?.setDayStartTimeGMT() == today.time.setDayStartTimeGMT()
//                    ) {
//                        fitnessClass.add(it)
//                    }
//                }
//                fitnessClass.forEach { classSchedule ->
//                    val gym = facilities.firstOrNull { gym ->
//                        gym.customClasses?.any {
//                            classSchedule.customClassId == it.id
//                        } == true
//                    } ?: return@forEach
//                    val instructor =
//                        gym.instructors?.firstOrNull { it.id == classSchedule.instructorId }
//                    val classes =
//                        gym.customClasses?.firstOrNull { it.id == classSchedule.customClassId }
//                            ?: return@forEach
//                    bookedClass.add(
//                        BookedClass(
//                            gym.name ?: "",
//                            classes.name ?: "",
//                            classSchedule.from,
//                            classSchedule.from,
//                            gym.address ?: "",
//                            classes.type ?: "",
//                            gym.icon ?: "",
//                            settingGlobal?.classEntryLeadTime ?: 5,
//                            settingGlobal?.classCancellationTime ?: 5,
//                            gym.id ?: "",
//                            instructor?.name ?: "",
//                        )
//                    )
//                }
//                bookedClass.forEach {
//                    val difference = dateTimeDifference(
//                        it.time.toMilliseconds() ?: 0,
//                        today.timeInMillis,
//                        TimeDifference.MINUTE
//                    )
//                    if (difference in (-1) * (settingGlobal?.classEntryLeadTime
//                            ?: 15)..(settingGlobal?.classEntryLeadTime ?: 15)
//                    ) {
//                        state.postValue(BottomMenuState.BookedClassLoaded(it, profile))
//                        isFound = true
//                        return@forEach
//                    }
//                }
//                if (!isFound) {
//                    getSubscription()
//                }
//            } catch (error: Exception) {
//                Timber.e(error)
//                state.postValue(BottomMenuState.Loaded(isSubscriptionActive()))
//                navigator.navigate(Navigation.BitQuitHere)
//            }
//        }
//    }

    private suspend fun isSubscriptionActive(): Boolean {
        subscriptionActive = subscriptionManager.hasSubscription()
        return subscriptionActive
    }

    companion object {
        private const val START_DATE = 35
        private const val END_DATE = 90
    }
}

sealed class BottomMenuState {
    data class Loading(val active: Boolean) : BottomMenuState()
    object GymsLoaded : BottomMenuState()

    data class ConfigsLoaded(val subscriptionActive: Boolean) : BottomMenuState()
    data class SavedSubscriptionLoaded(val subscriptionActive: Boolean) : BottomMenuState()
    data class Error(val error: Exception) : BottomMenuState()
    data class LoadSubscriptionState(val isActive: Boolean) : BottomMenuState()

    data class BookedClassLoaded(
        val bookedClass: BookedClass?,
        val profile: ProfileEntity
    ) : BottomMenuState()
}

sealed class BottomMenuIntent {
    object ProcessQuickAccessTap : BottomMenuIntent()
    object FindGym : BottomMenuIntent()
}
