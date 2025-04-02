package com.yourfitness.coach.ui.features.progress

import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.data.entity.SubscriptionEntity
import com.yourfitness.coach.domain.fb_remote_config.FirebaseRemoteConfigRepository
import com.yourfitness.coach.domain.settings.SettingsManager
import com.yourfitness.coach.domain.subscription.SubscriptionManager
import com.yourfitness.coach.network.dto.Classification
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.coach.ui.utils.openDubai30x30Details
import com.yourfitness.common.CommonPreferencesStorage
import com.yourfitness.common.data.entity.ProfileEntity
import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.domain.date.addDays
import com.yourfitness.common.domain.date.toDate
import com.yourfitness.common.domain.date.today
import com.yourfitness.common.domain.settings.RegionSettingsManager
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.community.ui.navigation.CommunityNavigation
import com.yourfitness.community.ui.navigation.CommunityNavigator
import com.yourfitness.pt.domain.calendar.SessionsRepository
import com.yourfitness.pt.domain.pt.PtRepository
import com.yourfitness.pt.ui.navigation.PtNavigator
import com.yourfitness.shop.data.ShopStorage
import com.yourfitness.shop.ui.navigation.ShopNavigation
import com.yourfitness.shop.ui.navigation.ShopNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
open class ProgressViewModel @Inject constructor(
    private val ptNavigator: PtNavigator,
    private val communityNavigator: CommunityNavigator,
    private val ptRepository: PtRepository,
    private val settingsManager: SettingsManager,
    private val subscriptionManager: SubscriptionManager,
    private val rmConfig: FirebaseRemoteConfigRepository,
    private val sessionsRepository: SessionsRepository,
    private val shopStorage: ShopStorage,
    private val shopNavigator: ShopNavigator,
    protected val regionSettingsManager: RegionSettingsManager,
    protected val commonStorage: CommonPreferencesStorage,
    val navigator: Navigator
) : MviViewModel<ProgressIntent, ProgressState>() {

    private var previewStart = commonStorage.dubaiStart.toDate()?.addDays(-30)
    var isDubai30x30Active = rmConfig.dubai3030Enabled
        private set
    var isDubai30x30Dates = commonStorage.dubaiStart != null && commonStorage.dubaiEnd != null &&
            today().after(commonStorage.dubaiStart.toDate()) && !today().after(commonStorage.dubaiEnd.toDate())
        private set
    var isDubai30x30Preview = commonStorage.dubaiStart != null && previewStart != null &&
            today().after(previewStart) && !today().after(commonStorage.dubaiStart.toDate())
        private set
    var isDubai30x30Ended = commonStorage.dubaiEnd != null && (commonStorage.dubaiEnd?.toDate()
        ?: today()).before(today())
        private set
    var isIntercorporateStarted = commonStorage.intercorporateStart != null && today().after(
        commonStorage.intercorporateStart?.toDate() ?: today()
    )
        private set
    var isIntercorporateEnded =
        commonStorage.intercorporateEnd != null && (commonStorage.intercorporateEnd?.toDate()
            ?: today()).before(today())
        private set
    private var profile: ProfileEntity? = null

    init {
        getSubscription()
        loadUserSessions()
        checkSettings()
    }

    override fun handleIntent(intent: ProgressIntent) {
        when (intent) {
            is ProgressIntent.OpenCommunityFriends -> openCommunityFriends()
            is ProgressIntent.OpenDubai30x30Details -> openDubaiDetails()
            is ProgressIntent.PersonalTrainerCardClicked -> openPtScreen()
            is ProgressIntent.OpenShop -> openShop()
        }
    }

    private fun openShop() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val coinsValue = commonStorage.availableCoins.toInt()
                if (shopStorage.introShown) navigator.navigate(Navigation.Shop)
                else if (coinsValue >= 0) {
                    val regionSettings = regionSettingsManager.getSettings()
                    val coinsCost = regionSettings?.coinValue?.toFloat()
                    val currency = regionSettings?.currency
                    shopNavigator.navigate(ShopNavigation.ShopIntro(coinsValue, coinsCost ?: 0f, currency))
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun openCommunityFriends() {
        communityNavigator.navigate(CommunityNavigation.FriendsList)
    }

    private fun checkSettings() {
        if (commonStorage.intercorporateStart == null || commonStorage.intercorporateEnd == null ||
            commonStorage.dubaiStart == null || commonStorage.dubaiEnd == null) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    settingsManager.getSettings(true)
                    initParams()
                    state.postValue(ProgressState.ParamsUpdated)
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }

    }

    private fun initParams() {
        isDubai30x30Active = rmConfig.dubai3030Enabled
        isDubai30x30Dates = commonStorage.dubaiStart != null && commonStorage.dubaiEnd != null &&
                today().after(commonStorage.dubaiStart.toDate()) && !today().after(commonStorage.dubaiEnd.toDate())
        isDubai30x30Preview = commonStorage.dubaiStart != null && previewStart != null &&
                today().after(previewStart) && !today().after(commonStorage.dubaiStart.toDate())
        isIntercorporateStarted = commonStorage.intercorporateStart != null && today().after(
            commonStorage.intercorporateStart?.toDate() ?: today()
        )
        isIntercorporateEnded =
            commonStorage.intercorporateEnd != null && (commonStorage.intercorporateEnd?.toDate()
                ?: today()).before(today())

    }

    private fun loadUserSessions() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                sessionsRepository.downloadUserSessions()
            } catch (error: Exception) {
                Timber.e(error)
            }
        }
    }

    private fun getSubscription() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val subscription = subscriptionManager.getSubscription()
                val newState = if (subscription == null) ProgressState.SubscriptionError
                else ProgressState.SubscriptionValue(subscription)
                state.postValue(newState)
            } catch (error: Exception) {
                Timber.e(error)
            }
        }
    }

    fun accessWorkoutPlans(): Boolean {
       return commonStorage.accessWorkoutPlans
    }

    private fun openDubaiDetails() =
        viewModelScope.launch { navigator.openDubai30x30Details(settingsManager, commonStorage) }

    private fun openPtScreen() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                navigator.navigate(Navigation.Map(Classification.TRAINER))

//                val ptBalanceList = ptRepository.getBalanceList()
//                if (ptBalanceList.isEmpty()) {
//                    navigator.navigate(Navigation.Map(Classification.TRAINER))
//                } else if (ptBalanceList.size == 1) {
//                    if (ptBalanceList.all { it.amount == 0 }) ptNavigator.navigate(PtNavigation.MyTrainersList)
//                    else ptNavigator.navigate(PtNavigation.Calendar(ptBalanceList.first().ptId))
//                } else {
//                    ptNavigator.navigate(PtNavigation.MyTrainersList)
//                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }
}

open class ProgressState {
    data class Error(val error: Exception) : ProgressState()
    data class SubscriptionValue(val subscription: SubscriptionEntity) : ProgressState()
    object SubscriptionError : ProgressState()
    data class VoucherLoaded(val voucher: String?) : ProgressState()
    object ParamsUpdated : ProgressState()
}

open class ProgressIntent {
    object OpenCommunityFriends : ProgressIntent()
    object OpenShop : ProgressIntent()
    object PersonalTrainerCardClicked : ProgressIntent()
    object OpenDubai30x30Details : ProgressIntent()
}
