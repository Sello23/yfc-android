package com.yourfitness.coach.ui.features.splash

import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.domain.fb_remote_config.FirebaseRemoteConfigRepository
import com.yourfitness.coach.domain.fcm.CloudMessagingManager
import com.yourfitness.coach.domain.session.SessionManager
import com.yourfitness.coach.domain.settings.SettingsManager
import com.yourfitness.coach.domain.subscription.SubscriptionManager
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.CommonPreferencesStorage
import com.yourfitness.common.data.dao.ProgressLevelDao
import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.domain.settings.RegionSettingsManager
import com.yourfitness.common.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    private val navigator: Navigator,
    private val sessionManager: SessionManager,
    private val configRepository: FirebaseRemoteConfigRepository,
    private val cloudMessagingManager: CloudMessagingManager,
    private val subscriptionManager: SubscriptionManager,
    private val profileRepository: ProfileRepository,
    private val settingsManager: SettingsManager,
    private val regionSettingsManager: RegionSettingsManager,
    private val progressLevelDao: ProgressLevelDao,
    private val commonStorage: CommonPreferencesStorage
) : MviViewModel<Any, Any>() {

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            configRepository.fetchRemoteConfigs()
            progressLevelDao.delete()
            if (configRepository.needUpdate) {
                return@launch
            } else if (sessionManager.isLoggedIn()) {
                subscriptionManager.getSubscription(true)
                settingsManager.getSettings(true)
                regionSettingsManager.getSettings(true)
                val isPtRole = profileRepository.isPtRole()
                val isBookable = profileRepository.isBookable()
                val accessWorkoutPlans = profileRepository.accessWorkoutPlans()
                commonStorage.isPtRole = isPtRole
                commonStorage.isBookable = isBookable
                commonStorage.accessWorkoutPlans = accessWorkoutPlans
                cloudMessagingManager.sendToken()
                navigator.navigate(Navigation.Progress(isPtRole, isBookable,accessWorkoutPlans))
            } else if (sessionManager.isFirstStart()) {
                navigator.navigate(Navigation.WelcomeSlider)
            } else {
                navigator.navigate(Navigation.WelcomeBack)
            }
        }
    }
}