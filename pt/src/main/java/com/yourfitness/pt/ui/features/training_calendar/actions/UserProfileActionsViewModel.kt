package com.yourfitness.pt.ui.features.training_calendar.actions

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yourfitness.pt.R
import com.yourfitness.pt.data.entity.SessionEntity
import com.yourfitness.pt.domain.calendar.SessionsRepository
import com.yourfitness.pt.domain.pt.PtRepository
import com.yourfitness.pt.domain.values.ACTION_FLOW
import com.yourfitness.pt.domain.values.CLIENT
import com.yourfitness.pt.domain.values.SESSION_ID
import com.yourfitness.pt.network.dto.PtClientDto
import com.yourfitness.pt.ui.features.user_profile.UserProfileBaseIntent
import com.yourfitness.pt.ui.features.user_profile.UserProfileBaseState
import com.yourfitness.pt.ui.features.user_profile.UserProfileBaseViewModel
import com.yourfitness.pt.ui.navigation.PtNavigation
import com.yourfitness.pt.ui.navigation.PtNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class UserProfileActionsViewModel @Inject constructor(
    private val navigator: PtNavigator,
    private val sessionsRepository: SessionsRepository,
    private val ptRepository: PtRepository,
    savedState: SavedStateHandle
) : UserProfileBaseViewModel() {

    private val flowDataMap = mapOf(
        ActionFlow.CONFIRM to FlowData(
            R.string.new_request_details,
            R.string.confirm_booking,
            R.string.decline_request,
            ::onConfirm,
            ::onDeclineConfirm,
        )
    )

    private val sessionId = savedState.get<String?>(SESSION_ID).orEmpty()
    private val flow = savedState[ACTION_FLOW] ?: ActionFlow.CONFIRM
    val flowData: FlowData = flowDataMap[flow] ?: flowDataMap.values.first()

    init {
        loadData()
    }

    override fun handleIntent(intent: UserProfileBaseIntent) {
        when (intent) {
            is UserProfileActionsIntent.SecondaryActionButtonClicked -> flowData.onActionSecondary?.invoke()
            else -> super.handleIntent(intent)
        }
    }

    override fun loadData() {
        viewModelScope.launch {
            try {
                val session = sessionsRepository.getSession(sessionId) ?: return@launch
                val facilityData = ptRepository.getFacilityById(session.facilityId) ?: return@launch
                state.postValue(UserProfileActionsState.DataLoaded(session, facilityData))
            } catch (e: Exception) {
                Timber.e(e)
                state.postValue(UserProfileBaseState.Error(e))
            }
        }
    }

    private fun onConfirm() {
        navigator.navigate(PtNavigation.ConfirmCalendarActionFlow(sessionId, ConfirmFlow.CONFIRM))
    }

    private fun onDeclineConfirm() {
        navigator.navigate(PtNavigation.ConfirmCalendarActionFlow(sessionId, ConfirmFlow.DECLINE))
    }

    override fun onMainButtonClick() {
        flowData.onActionMain()
    }
}

open class UserProfileActionsState {
    data class DataLoaded(
        val session: SessionEntity,
        val facilityData: Triple<String, String, String>
    ) : UserProfileBaseState()
}

open class UserProfileActionsIntent {
    object SecondaryActionButtonClicked : UserProfileBaseIntent()
}

sealed class ActionFlow {
    companion object {
        const val CONFIRM = 0
        const val MANAGE = 1
        const val CANCEL = 2
    }
}

data class FlowData(
    @StringRes val tittle: Int,
    @StringRes val actionMain: Int,
    @StringRes val actionSecondary: Int?,
    val onActionMain: () -> Unit,
    val onActionSecondary: (() -> Unit)?
)
