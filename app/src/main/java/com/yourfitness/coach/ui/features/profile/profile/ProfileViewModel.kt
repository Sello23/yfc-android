package com.yourfitness.coach.ui.features.profile.profile

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.yourfitness.coach.domain.ProfileManager
import com.yourfitness.coach.domain.fitness_info.FitnessInfoService
import com.yourfitness.coach.domain.subscription.SubscriptionManager
import com.yourfitness.coach.domain.subscription.isActive
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.CommonPreferencesStorage
import com.yourfitness.common.data.entity.ProfileEntity
import com.yourfitness.common.data.entity.RegionSettingsEntity
import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.domain.date.today
import com.yourfitness.common.domain.settings.RegionSettingsManager
import com.yourfitness.common.network.CommonRestApi
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.shop.domain.cart_service.CoinsUsageService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    val navigator: Navigator,
    private val profileManager: ProfileManager,
    private val commonRestApi: CommonRestApi,
    private val profileRepository: ProfileRepository,
    private val regionSettingsManager: RegionSettingsManager,
    private val fitnessInfoService: FitnessInfoService,
    private val subscriptionManager: SubscriptionManager,
    private val commonStorage: CommonPreferencesStorage,
    private val coinsUsageService: CoinsUsageService,
) : MviViewModel<Any, Any>() {

    private var profile: ProfileEntity = ProfileEntity.empty

    init {
        loadSubscriptionStatus()
    }

    override fun handleIntent(intent: Any) {
        when (intent) {
            is ProfileIntent.UploadPhoto -> updatePhoto(intent)
            is ProfileIntent.SaveDbToRemoteStorage -> backupDb()
        }
    }

    private fun loadSubscriptionStatus() {
        viewModelScope.launch {
            try {
                val subscription = subscriptionManager.getSubscription()
                state.postValue(ProfileState.SavedSubscriptionLoaded(subscription?.isActive() == true))
            } catch (error: Exception) {
                Timber.e(error)
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            try {
                state.postValue(ProfileState.Loading)
                profile = profileRepository.getProfile()
                commonStorage.availableCoins = commonRestApi.coins()
                val credits = 0L//restApi.credits() // TODO temporary removed Studios
                val settingsRegion = regionSettingsManager.getSettings()
                state.postValue(ProfileState.Success(profile, coinsUsageService.notUsedCoins(), credits, settingsRegion))
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(ProfileState.Error(error))
            }
        }
    }

    private fun updatePhoto(intent: ProfileIntent.UploadPhoto) {
        viewModelScope.launch {
            try {
                state.postValue(ProfileState.Loading)
                val mediaId = profileManager.updatePhoto(intent.uri, profile)
                state.postValue(ProfileState.PhotoUploaded(mediaId))
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(ProfileState.Error(error))
            }
        }
    }

    private fun backupDb() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val today = today()
                val storage = Firebase.storage

                val fitDiffJson = fitnessInfoService.generateProviderFitnessDbJson()
                val storageRefDiff = storage.getReference("android/${profile.id}/$today/fitness_data_diff.json")

                val serverInfoJson = fitnessInfoService.generateServerFitnessDbJson()
                val storageRefServer = storage.getReference("android/${profile.id}/$today/server_data.json")

                val uploadTaskDiff = storageRefDiff.putBytes(fitDiffJson.toByteArray())
                uploadTaskDiff.addOnCompleteListener { diffResult ->
                    val uploadTaskServer = storageRefServer.putBytes(serverInfoJson.toByteArray())
                    uploadTaskServer.addOnCompleteListener { serverResult ->
                        state.postValue(ProfileState.FirebaseUploadResult(diffResult.isSuccessful && serverResult.isSuccessful))
                    }
                }
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(ProfileState.FirebaseUploadResult(false))
            }
        }
    }
}

open class ProfileIntent {
    data class UploadPhoto(val uri: Uri) : ProfileIntent()
    object SaveDbToRemoteStorage: ProfileIntent()
}

open class ProfileState {
    object Loading : ProfileState()
    data class PhotoUploaded(val mediaId: String) : ProfileState()
    data class Success(
        val profile: ProfileEntity,
        val coins: Long,
        val credits: Long,
        val settingsRegion: RegionSettingsEntity?,
    ) : ProfileState()

    data class Error(val error: Exception) : ProfileState()
    data class FirebaseUploadResult(val isSuccess: Boolean) : ProfileState()
    data class SavedSubscriptionLoaded(val subscriptionActive: Boolean) : ProfileState()
}