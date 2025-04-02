package com.yourfitness.coach.ui.features.more

import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.CommonPreferencesStorage
import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.domain.settings.RegionSettingsManager
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.community.ui.navigation.CommunityNavigation
import com.yourfitness.community.ui.navigation.CommunityNavigator
import com.yourfitness.pt.ui.navigation.PtNavigation
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
class MoreViewModel @Inject constructor(
    private val navigator: Navigator,
    private val ptNavigator: PtNavigator,
    private val shopNavigator: ShopNavigator,
    private val communityNavigator: CommunityNavigator,
    private val regionSettingsManager: RegionSettingsManager,
    private val shopStorage: ShopStorage,
    private val commonStorage: CommonPreferencesStorage,
    private val profileRepository: ProfileRepository
) : MviViewModel<MoreIntent, Any>() {

    override fun handleIntent(intent: MoreIntent) {
        when (intent) {
            is MoreIntent.FAQ -> navigator.navigate(Navigation.FAQ)
//            is MoreIntent.Profile -> navigator.navigate(Navigation.Profile)
            is MoreIntent.FitnessCalendar -> {
                openFitnessCalendar()
            }
            is MoreIntent.Challenges -> navigator.navigate(Navigation.Challenges)
            is MoreIntent.Shop -> openShop()
            is MoreIntent.Community -> communityNavigator.navigate(CommunityNavigation.FriendsList)
        }
    }

    private fun openFitnessCalendar() {
//        ptNavigator.navigate(PtNavigation.ComingSoon) // TODO coming soon
        viewModelScope.launch {
            try {
                ptNavigator.navigate(
                    if (profileRepository.isPtRole()) PtNavigation.TrainingCalendarPt()
                    else PtNavigation.TrainingCalendar
                )
            } catch (error: Exception) {
                Timber.e(error)
            }
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
}

open class MoreIntent {
    object Challenges : MoreIntent()
    object FAQ : MoreIntent()
    object FitnessCalendar : MoreIntent()
    object Community : MoreIntent()
    object Shop : MoreIntent()
//    object Profile : MoreIntent()
}
