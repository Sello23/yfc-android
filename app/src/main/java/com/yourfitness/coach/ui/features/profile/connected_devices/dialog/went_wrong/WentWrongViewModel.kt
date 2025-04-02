package com.yourfitness.coach.ui.features.profile.connected_devices.dialog.went_wrong

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.pt.domain.values.DIALOG_ERROR_TYPE
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WentWrongViewModel @Inject constructor(
    savedState: SavedStateHandle
) : MviViewModel<Any, Any>() {
    val type: ErrorType = savedState[DIALOG_ERROR_TYPE] ?: ErrorType.Default
}

enum class ErrorType {
    Default,
    HealthConnectMissing
}

data class ErrorDialogState(
    @StringRes val message: Int,
    val onConfirm: () -> Unit = {},
)
