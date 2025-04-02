package com.yourfitness.coach.ui.features.profile.connected_devices.dialog.disconnected

import androidx.lifecycle.SavedStateHandle
import com.yourfitness.coach.ui.features.profile.connected_devices.ProviderType
import com.yourfitness.coach.ui.features.profile.connected_devices.providers
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.pt.domain.values.PROVIDER_TYPE
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GoogleFitDisconnectedViewModel @Inject constructor(
    val navigator: Navigator,
    savedState: SavedStateHandle
) : MviViewModel<Any, Any>() {
    private val providerType: ProviderType = savedState[PROVIDER_TYPE] ?: ProviderType.HEALTH_CONNECT
    val providerInfo = providers.find { it.type == providerType }
}
