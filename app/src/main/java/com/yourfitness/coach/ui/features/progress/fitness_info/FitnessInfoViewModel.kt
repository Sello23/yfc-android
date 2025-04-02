package com.yourfitness.coach.ui.features.progress.fitness_info

import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.data.entity.FitnessDataBackendEntity
import com.yourfitness.coach.data.entity.SettingsEntity
import com.yourfitness.coach.domain.fb_remote_config.FirebaseRemoteConfigRepository
import com.yourfitness.coach.domain.fitness_info.FitnessInfoService
import com.yourfitness.coach.domain.settings.SettingsManager
import com.yourfitness.coach.domain.subscription.SubscriptionManager
import com.yourfitness.coach.ui.features.progress.ProgressIntent
import com.yourfitness.coach.ui.features.progress.ProgressState
import com.yourfitness.coach.ui.features.progress.ProgressViewModel
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.CommonPreferencesStorage
import com.yourfitness.common.domain.date.atDayStart
import com.yourfitness.common.domain.date.todayZoned
import com.yourfitness.common.domain.date.todayZonedNull
import com.yourfitness.common.domain.settings.RegionSettingsManager
import com.yourfitness.common.network.CommonRestApi
import com.yourfitness.community.ui.navigation.CommunityNavigator
import com.yourfitness.pt.domain.calendar.SessionsRepository
import com.yourfitness.pt.domain.pt.PtRepository
import com.yourfitness.pt.ui.navigation.PtNavigator
import com.yourfitness.shop.data.ShopStorage
import com.yourfitness.shop.domain.cart_service.CoinsUsageService
import com.yourfitness.shop.ui.navigation.ShopNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.ZoneOffset
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class FitnessInfoViewModel @Inject constructor(
    private val fitnessInfoService: FitnessInfoService,
    private val commonRestApi: CommonRestApi,
    private val settingsManager: SettingsManager,
    private val coinsUsageService: CoinsUsageService,
    commonStorage: CommonPreferencesStorage,
    regionSettingsManager: RegionSettingsManager,
    ptNavigator: PtNavigator,
    communityNavigator: CommunityNavigator,
    ptRepository: PtRepository,
    rmConfig: FirebaseRemoteConfigRepository,
    subscriptionManager: SubscriptionManager,
    sessionsRepository: SessionsRepository,
    shopStorage: ShopStorage,
    shopNavigator: ShopNavigator,
    navigator: Navigator
) : ProgressViewModel(ptNavigator, communityNavigator, ptRepository, settingsManager, subscriptionManager,
    rmConfig, sessionsRepository, shopStorage, shopNavigator, regionSettingsManager, commonStorage, navigator
) {

    var zoneOffset: ZoneOffset? = commonStorage.zoneOffset
    var selectedDate: ZonedDateTime? = zoneOffset?.let { todayZoned(it).atDayStart() }
    var isProviderConnected: Boolean = false
        private set

    private var getDataJob: Job? = null

    init {
        loadCalendarData()
        loadData()
    }

    override fun handleIntent(intent: ProgressIntent) {
        when (intent) {
            is FitnessInfoIntent.SelectedDateChanged -> getGoogleFitDataCurrentDay(intent.date)
            is FitnessInfoIntent.UpdatePointsData -> getGoogleFitDataCurrentDay(isProviderConnected = isProviderConnected)
            else -> super.handleIntent(intent)
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                commonStorage.availableCoins = commonRestApi.coins()
                val credits = 0L//restApi.credits()
                state.postValue(FitnessInfoState.Success(coinsUsageService.notUsedCoins(), credits))
            } catch (error: Exception) {
                Timber.e(error)
            }
        }
    }

    private suspend fun checkPermissions(): Boolean {
        return fitnessInfoService.checkPermissions()
    }

    private fun loadCalendarData() {
        viewModelScope.launch(Dispatchers.IO) {
            isProviderConnected = checkPermissions()

            var isFirstLoad = false
            if (zoneOffset == null || selectedDate == null) {
                zoneOffset = regionSettingsManager.getZoneOffset()
                selectedDate = zoneOffset?.let { todayZoned(it).atDayStart()}
                isFirstLoad = true
            }
            getGoogleFitDataCurrentDay(isFirstLoad = isFirstLoad, isProviderConnected = isProviderConnected)
        }
    }

    private fun getGoogleFitDataCurrentDay(date: ZonedDateTime? = null, isFirstLoad: Boolean = false, isProviderConnected: Boolean? = null) {
        try { getDataJob?.cancel() } catch (_: Exception) {}
        getDataJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                if ((isProviderConnected != null && !isProviderConnected) || (isProviderConnected == null && !checkPermissions())) {
                    if (isFirstLoad) state.postValue(FitnessInfoState.LoadCalendar(isProviderConnected))
                    return@launch
                }

                selectedDate = date ?: todayZonedNull(zoneOffset)?.atDayStart()
                selectedDate?.let {
                    val googleFitData = fitnessInfoService.getFitnessDataForDay(it)
                    val settings = if (date == null) {
                        settingsManager.getSavedSettings()
                    } else {
                        settingsManager.getSettings()
                    }
                    state.postValue(FitnessInfoState.GoogleFitData(googleFitData, settings, isFirstLoad, isProviderConnected))
                }
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(FitnessInfoState.Error(error))
            }
        }
    }
}

sealed class FitnessInfoState {
    data class Success(val coins: Long = 0, val credits: Long = 0) : ProgressState()
    data class Error(val error: Exception) : ProgressState()
    data class GoogleFitData(
        val dailyGoogleFitData: FitnessDataBackendEntity = FitnessDataBackendEntity.empty,
        val settingsGlobal: SettingsEntity? = SettingsEntity.empty,
        val isFirstLoad: Boolean,
        val isProviderConnected: Boolean?,
    ) : ProgressState()
    data class LoadCalendar(val isProviderConnected: Boolean?) : ProgressState()
}

sealed class FitnessInfoIntent {
    data class SelectedDateChanged(val date: ZonedDateTime) : ProgressIntent()
    data object UpdatePointsData : ProgressIntent()
}
