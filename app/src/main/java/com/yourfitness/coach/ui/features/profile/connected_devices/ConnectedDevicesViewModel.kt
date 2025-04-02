package com.yourfitness.coach.ui.features.profile.connected_devices

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.PreferencesStorage
import com.yourfitness.coach.R
import com.yourfitness.coach.domain.date.today
import com.yourfitness.coach.domain.fitness_info.FitnessInfoService
import com.yourfitness.coach.domain.spike.SpikeApiRepository
import com.yourfitness.coach.ui.features.profile.connected_devices.dialog.went_wrong.ErrorType
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.CommonPreferencesStorage
import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.domain.settings.RegionSettingsManager
import com.yourfitness.common.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ConnectedDevicesViewModel @Inject constructor(
    private val preferencesStorage: PreferencesStorage,
    private val fitnessInfoService: FitnessInfoService,
    private val spikeApiRepository: SpikeApiRepository,
    private val commonStorage: CommonPreferencesStorage,
    private val profileRepository: ProfileRepository,
    private val regionSettingsManager: RegionSettingsManager,
    private val navigator: Navigator,
) : MviViewModel<ConnectedDevicesIntent, Any>() {

    var selectedProviderType: ProviderType? = preferencesStorage.fitDataProvider
        private set

    init {
        state.value = ConnectedDevicesState.ProvidersLoaded(providers)
        initialPermissionsStateCheck()
    }

    override fun handleIntent(intent: ConnectedDevicesIntent) {
        when (intent) {
            is ConnectedDevicesIntent.ProviderEnabled -> onProviderEnabled(intent.type, intent.selected)
            is ConnectedDevicesIntent.SpikeConnected -> onSpikeConnected(intent.userId)
            is ConnectedDevicesIntent.PermissionsGranted -> onPermissionsGranted()
            is ConnectedDevicesIntent.DisconnectApproved -> onDisconnectionApproved()
            is ConnectedDevicesIntent.PermissionDescriptionRequired -> openNeedPermissionDialog()
        }
    }

    private fun initialPermissionsStateCheck() {
        viewModelScope.launch(Dispatchers.IO) {
            if (selectedProviderType == ProviderType.HEALTH_CONNECT) {
                state.postValue(ConnectedDevicesState.Loading())
                val requiredPermissions = fitnessInfoService.getHealthConnectPermissions()
                if (requiredPermissions.isNotEmpty()) {
                    confirmProviderDisabled()
                } else {
                    updateEnabledProvidersState()
                }
            } else {
                updateEnabledProvidersState()
            }
        }
    }

    private fun onProviderEnabled(type: ProviderType, selected: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (selected) {
                    selectedProviderType = type
                    spikeApiRepository.disconnect() /// needed for cases when the user deleted the application or cleared the data and the integration was not deleted

                    if (type == ProviderType.HEALTH_CONNECT) {
                        if (!fitnessInfoService.isHealthConnectInstalled()) {
                            openErrorDialog(ErrorType.HealthConnectMissing)
                            return@launch
                        }

                        val requiredPermissions = fitnessInfoService.getHealthConnectPermissions()

                        if (requiredPermissions.isNotEmpty()) {
                            state.postValue(ConnectedDevicesState.AskSpikePermissions(requiredPermissions))
                        } else {
                            confirmProviderEnabled()
                        }
                    } else {
                        val userId = profileRepository.profileId()
                        state.postValue(ConnectedDevicesState.RequestSpikeApiConnection(userId, type.value))
                    }
                } else {
                    openDisconnectDialog()
                }
            } catch (e: Exception) {
                Timber.e(e)
                openErrorDialog()
            }
        }
    }

    private fun onSpikeConnected(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                commonStorage.spikeUserId = userId
                confirmProviderEnabled()
            } catch (error: Exception) {
                Timber.e(error)
                openErrorDialog()
            }
        }
    }

    private fun onPermissionsGranted() {
        viewModelScope.launch(Dispatchers.IO) {
            if (fitnessInfoService.getHealthConnectPermissions().isEmpty()) {
                confirmProviderEnabled()
            } else {
                confirmProviderDisabled()
            }
        }
    }

    private fun onDisconnectionApproved() {
        viewModelScope.launch(Dispatchers.IO) {
            state.postValue(ConnectedDevicesState.Loading(true))
            resetSelectedProviders()
            updateEnabledProvidersState()
        }
    }

    private suspend fun confirmProviderEnabled() {
        val offset = regionSettingsManager.getZoneOffset() ?: return
        preferencesStorage.providerLastConnectDate = offset.today().toEpochSecond()
        preferencesStorage.fitDataProvider = selectedProviderType
        updateEnabledProvidersState()
        openSuccessDialog()
    }

    private suspend fun confirmProviderDisabled() {
        resetSelectedProviders()
        updateEnabledProvidersState()
        openErrorDialog()
    }

    private suspend fun resetSelectedProviders() {
        if (!spikeApiRepository.disconnect()) {
            openErrorDialog()
            return
        }

        if (preferencesStorage.fitDataProvider == ProviderType.HEALTH_CONNECT) {
            preferencesStorage.fitDataProvider?.let { fitnessInfoService.closeSpikeConnections(it) }
        }

        selectedProviderType = null
        preferencesStorage.fitDataProvider = null
        regionSettingsManager.getZoneOffset()?.let {
            preferencesStorage.providerLastDisconnectDate = it.today().toEpochSecond()
        }
    }

    private fun updateEnabledProvidersState() {
        val providersData = providers.map {
            it.copy(
                checked = it.type == selectedProviderType,
                isEnabled = selectedProviderType == null || it.type == selectedProviderType
            )
        }
        val chosenProviders = providersData.filter { it.checked }
        val otherProviders = providersData.filter { !it.checked }

        state.postValue(ConnectedDevicesState.ProvidersLoaded(chosenProviders + otherProviders))
    }

    private fun openSuccessDialog() {
        selectedProviderType?.let {
            navigator.navigate(Navigation.GoogleFitConnected(it))
        }
    }

    private fun openDisconnectDialog() {
        selectedProviderType?.let {
            navigator.navigate(Navigation.GoogleFitDisconnected(it))
        }
    }

    private fun openErrorDialog(type: ErrorType = ErrorType.Default) {
        navigator.navigate(Navigation.WentWrong(type))
    }

    private fun openNeedPermissionDialog() {
        navigator.navigate(Navigation.NeedPermission)
    }
}

sealed class ConnectedDevicesState {
    data class Loading(val loading: Boolean = true) : ConnectedDevicesState()
    data class ProvidersLoaded(val providers: List<ProviderInfo>) : ConnectedDevicesState()
    data class AskSpikePermissions(val permissions: Set<String>) : ConnectedDevicesState()
    data class RequestSpikeApiConnection(
        val userId: String,
        val provider: String
    ) : ConnectedDevicesState()
}

sealed class ConnectedDevicesIntent {
    data class ProviderEnabled(
        val type: ProviderType,
        val selected: Boolean
    ) : ConnectedDevicesIntent()

    data object PermissionsGranted : ConnectedDevicesIntent()
    data object DisconnectApproved : ConnectedDevicesIntent()

    data class SpikeConnected(val userId: String) : ConnectedDevicesIntent()
    data object PermissionDescriptionRequired: ConnectedDevicesIntent()
}

data class ProviderInfo(
    val type: ProviderType,
    @StringRes val name: Int,
    @DrawableRes val logo: Int,
    val checked: Boolean = false,
    val isEnabled: Boolean = true,
    val hasBorder: Boolean = false,
)

val providers = listOf(
    ProviderInfo(
        type = ProviderType.HEALTH_CONNECT,
        name = R.string.data_provider_health_connect,
        logo = R.drawable.ic_health_connect,
        hasBorder = true
    ),
    ProviderInfo(
        type = ProviderType.GOOGLE_FIT,
        name = R.string.data_provider_google_fit,
        logo = R.drawable.google_fit
    ),
    ProviderInfo(
        type = ProviderType.GARMIN,
        name = R.string.data_provider_garmin,
        logo = R.drawable.ic_garmin
    ),
    /*ProviderInfo(
        type = ProviderType.POLAR,
        name = R.string.data_provider_polar,
        logo = R.drawable.ic_polar,
        hasBorder = true
    ),
    ProviderInfo(
        type = ProviderType.SUUNTO,
        name = R.string.data_provider_suunto,
        logo = R.drawable.ic_suunto
    ),*/
)

enum class ProviderType(val value: String) {
    HEALTH_CONNECT("health_connect"),
    GARMIN("garmin"),
    POLAR("polar"),
    SUUNTO("suunto"),
    GOOGLE_FIT("google_fit")
}
