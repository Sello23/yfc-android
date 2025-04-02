package com.yourfitness.pt.ui.features.training_calendar.actions

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yourfitness.common.domain.models.CalendarView
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.common.ui.navigation.CommonNavigation
import com.yourfitness.common.ui.navigation.CommonNavigator
import com.yourfitness.pt.R
import com.yourfitness.pt.domain.calendar.SessionsRepository
import com.yourfitness.pt.domain.values.ACTION_FLOW
import com.yourfitness.pt.domain.values.SESSION_ID
import com.yourfitness.pt.domain.values.SESSION_ID_LIST
import com.yourfitness.pt.ui.navigation.PtNavigation
import com.yourfitness.pt.ui.navigation.PtNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import com.yourfitness.common.R as common

@HiltViewModel
class UserConfirmActionViewModel @Inject constructor(
    val navigator: PtNavigator,
    private val sessionsRepository: SessionsRepository,
    private val commonNavigator: CommonNavigator,
    savedState: SavedStateHandle
) : MviViewModel<UserConfirmActionIntent, UserConfirmActionState>() {

    private var isAllInstances = false

    private val flowDataMap = mapOf(
        ConfirmFlow.CONFIRM to ConfirmFlowData(
            common.drawable.button_background,
            R.string.confirm_session_title,
            R.string.confirm_session_action_msg,
            null,
            ::onConfirm,
        ),
        ConfirmFlow.DECLINE to ConfirmFlowData(
            common.drawable.button_red_background,
            R.string.decline_session_title,
            R.string.decline_session_action_msg,
            R.string.decline_session_info_msg,
            ::onDecline,
        ),
        ConfirmFlow.CANCEL to ConfirmFlowData(
            common.drawable.button_red_background,
            R.string.cancel_session_title,
            R.string.cancel_session_action_msg,
            R.string.cancel_session_info_msg,
            ::onCancel,
        ),
        ConfirmFlow.CANCEL_BOOKED to ConfirmFlowData(
            common.drawable.button_red_background,
            R.string.cancel_conflict_session_title,
            R.string.cancel_conflict_session_action_msg,
            R.string.cancel_session_info_msg,
            ::onCancel,
        ),
        ConfirmFlow.CANCEL_CONFLICT to ConfirmFlowData(
            common.drawable.button_red_background,
            R.string.cancel_conflict_session_title,
            R.string.cancel_conflict_session_action_msg,
            R.string.cancel_conflict_session_info_msg,
            ::onCancelConflict,
        ),
        ConfirmFlow.CANCEL_BLOCK_CONFLICT to ConfirmFlowData(
            common.drawable.button_red_background,
            R.string.cancel_block_conflict_session_title,
            R.string.cancel_block_conflict_session_action_msg,
            null,
            ::onCancelBlock,
        ),
        ConfirmFlow.CANCEL_BLOCK to ConfirmFlowData(
            common.drawable.button_red_background,
            R.string.cancel_block_conflict_session_title,
            R.string.cancel_block_conflict_session_action_msg,
            null,
            ::onCancelBlock,
        ),
        ConfirmFlow.CANCEL_BLOCK_LIST to ConfirmFlowData(
            common.drawable.button_red_background,
            R.string.cancel_block_list_title,
            R.string.cancel_block_list_action_msg,
            null,
            ::onCancelBlockList,
        )
    )

    private val sessionId = savedState.get<String?>(SESSION_ID).orEmpty()
    private var sessionIdList = savedState.get<ArrayList<String>?>(SESSION_ID_LIST).orEmpty().toList()
    val flow = savedState.get<Int>(ACTION_FLOW) ?: ConfirmFlow.CONFIRM
    val flowData: ConfirmFlowData = flowDataMap[flow] ?: flowDataMap.values.first()

    init {
        loadData()
    }

    override fun handleIntent(intent: UserConfirmActionIntent) {
        when (intent) {
            is UserConfirmActionIntent.MainActionButtonClicked -> {
                isAllInstances = intent.isAll
                state.value = UserConfirmActionState.Loading
                flowData.onActionMain()
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val sessions = mutableListOf<CalendarView.CalendarItem>()
                if (sessionIdList.isNotEmpty()) {
                    sessionIdList.forEach {
                        val session = sessionsRepository.getSession(it) ?: return@launch
                        sessions.add(
                            sessionsRepository.getCalendarItemDataObject(session)
                                ?.copy(actionLabel = null, statusBuilder = null) ?: return@launch
                        )
                    }
                } else {
                    val session = sessionsRepository.getSession(sessionId) ?: return@launch
                    sessions.add(
                        sessionsRepository.getCalendarItemDataObject(session)
                            ?.copy(actionLabel = null, statusBuilder = null) ?: return@launch
                    )
                }
                state.postValue(UserConfirmActionState.DataLoaded(sessions))
            } catch (e: Exception) {
                Timber.e(e)
                state.postValue(UserConfirmActionState.Error(e))
            }
        }
    }

    private fun onConfirm() {
        viewModelScope.launch {
            try {
                sessionsRepository.confirmRequestedSession(sessionId)
                state.postValue(
                    UserConfirmActionState.ShouldDismiss(
                        PtNavigation.ResultCalendarActionFlow(ResultFlow.CONFIRM_SUCCESS)
                    )
                )
            } catch (e: Exception) {
                Timber.e(e)
                commonNavigator.navigate(CommonNavigation.CommonError)
            }
        }
    }

    private fun onDecline() {
        cancelSession(ResultFlow.DECLINE_SUCCESS)
    }

    private fun onCancel() {
        cancelSession(ResultFlow.CANCEl_SUCCESS)
    }

    private fun onCancelConflict() {
        cancelSession(navigator = CommonNavigation.CommonError2Pop)
    }

    private fun cancelSession(
        resultAction: Int? = null,
        navigator: CommonNavigation = CommonNavigation.CommonError
    ) {
        viewModelScope.launch {
            try {
                sessionsRepository.declineRequestedSession(sessionId)
                state.postValue(
                    UserConfirmActionState.ShouldDismiss(
                        resultAction?.let { PtNavigation.ResultCalendarActionFlow(it) }
                    )
                )
            } catch (e: Exception) {
                Timber.e(e)
                commonNavigator.navigate(navigator)
            }
        }
    }

    private fun onCancelBlock() {
        viewModelScope.launch {
            try {
                if (isAllInstances) {
                    sessionsRepository.cancelChainBlockSlot(sessionId)
                } else {
                    sessionsRepository.cancelBlockSlot(sessionId)
                }
                state.postValue(UserConfirmActionState.ShouldDismiss(
                    if (flow == ConfirmFlow.CANCEL_BLOCK) PtNavigation.ResultCalendarActionFlow(ResultFlow.REMOVE_BLOCK_SUCCESS)
                    else null
                ))
            } catch (e: Exception) {
                Timber.e(e)
                commonNavigator.navigate(CommonNavigation.CommonError)
            }
        }
    }

    private fun onCancelBlockList() {
        viewModelScope.launch {
            try {
                if (isAllInstances) {
                    val chainIds = mutableSetOf<String>()
                    val slotIds = mutableSetOf<String>()
                    sessionIdList.forEach {
                        val chainId = sessionsRepository.getChainId(it)
                        if (chainId != null) {
                            chainIds.add(chainId)
                        } else {
                            slotIds.add(it)
                        }
                    }
                    sessionsRepository.cancelBlockSlotList(slotIds.toList(), chainIds.toList())
                } else {
                    sessionsRepository.cancelBlockSlotList(sessionIdList, emptyList())
                }

                state.postValue(
                    UserConfirmActionState.ShouldDismiss(
                        PtNavigation.ResultCalendarActionFlow(
                            ResultFlow.REMOVE_MULTI_BLOCK_SUCCESS
                        )
                    )
                )
            } catch (e: Exception) {
                Timber.e(e)
                commonNavigator.navigate(CommonNavigation.CommonError)
            }
        }
    }
}

open class UserConfirmActionState {
    object Loading : UserConfirmActionState()
    data class Error(val error: Exception) : UserConfirmActionState()
    data class DataLoaded(
        val sessions: List<CalendarView.CalendarItem>,
    ) : UserConfirmActionState()
    data class ShouldDismiss(val navObj: PtNavigation? = null) : UserConfirmActionState()
}

open class UserConfirmActionIntent {
    data class MainActionButtonClicked(val isAll: Boolean = false) : UserConfirmActionIntent()
}

sealed class ConfirmFlow {
    companion object {
        const val CONFIRM = 0
        const val DECLINE = 1
        const val CANCEL = 2
        const val CANCEL_CONFLICT = 3
        const val CANCEL_BLOCK_CONFLICT = 4
        const val CANCEL_BLOCK = 5
        const val CANCEL_BOOKED = 6
        const val CANCEL_BLOCK_LIST = 7
    }
}

data class ConfirmFlowData(
    @DrawableRes val btnBackground: Int,
    @StringRes val tittle: Int,
    @StringRes val actionMain: Int,
    @StringRes val info: Int?,
    val onActionMain: () -> Unit,
)
