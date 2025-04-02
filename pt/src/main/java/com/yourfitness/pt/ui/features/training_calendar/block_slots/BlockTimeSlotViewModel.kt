package com.yourfitness.pt.ui.features.training_calendar.block_slots

import androidx.lifecycle.viewModelScope
import com.yourfitness.common.domain.date.today
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.pt.data.PtStorage
import com.yourfitness.pt.data.entity.SessionEntity
import com.yourfitness.pt.domain.calendar.SessionsRepository
import com.yourfitness.pt.domain.pt.BlockRepository
import com.yourfitness.pt.ui.navigation.PtNavigation
import com.yourfitness.pt.ui.navigation.PtNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BlockTimeSlotViewModel @Inject constructor(
    val navigator: PtNavigator,
    private val ptStorage: PtStorage,
    private val blockRepository: BlockRepository,
    private val sessionsRepository: SessionsRepository
) : MviViewModel<BlockTimeSlotIntent, BlockTimeSlotState>() {

    var selectedDate: Long? = null
        private set
    var hasSelectedTime: Boolean = false
        private set
    val selectedTimeSet = mutableSetOf<Pair<Long, Long>>()
    var selectedRepeatedWeeks: Int = -1
        private set

    private val conflicts: MutableList<SessionEntity> = mutableListOf()

    init {
        loadData()
    }

    override fun handleIntent(intent: BlockTimeSlotIntent) {
        when (intent) {
            is BlockTimeSlotIntent.SetDateClicked -> onSetDate()
            is BlockTimeSlotIntent.SetTimeClicked -> onSetTime()
            is BlockTimeSlotIntent.SetRepeatedClicked -> onSetRepeated()
            is BlockTimeSlotIntent.DateChanged -> {
                if (selectedDate != intent.date) {
                    hasSelectedTime = false
                    selectedTimeSet.clear()
                }
                selectedDate = intent.date
                state.value = BlockTimeSlotState.ResetTimeAndRepeats
            }
            is BlockTimeSlotIntent.RepeatedWeeksChanged -> selectedRepeatedWeeks = intent.weeks
            is BlockTimeSlotIntent.BlockBtnClicked -> blockTimeSlot()
            is BlockTimeSlotIntent.ConflictUpdated -> performSecondaryConfirmClick()
            is BlockTimeSlotIntent.FlowCancelled -> conflicts.clear()
        }
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                selectedDate = ptStorage.selectedDate
                if (selectedDate != null) {
                    selectedTimeSet.addAll(sessionsRepository.getTempSlots().map {
                        it.from to it.to
                    })
                }
                hasSelectedTime = ptStorage.hasSelectedTime
                selectedRepeatedWeeks = ptStorage.selectedRepeatedWeeks

                state.postValue(BlockTimeSlotState.Loaded)

                clearLocalData()
            } catch (error: Exception) {
                Timber.e(error)
            }
        }
    }

    private fun clearLocalData() {
        ptStorage.selectedDate = null
        ptStorage.hasSelectedTime = false
        ptStorage.selectedRepeatedWeeks = -1
    }

    private fun onSetDate() {
        navigator.navigate(PtNavigation.SelectDate(selectedDate ?: 0L))
    }

    private fun onSetTime() {
        ptStorage.selectedDate = selectedDate
        ptStorage.selectedRepeatedWeeks = selectedRepeatedWeeks
        navigator.navigate(PtNavigation.CalendarPt(selectedDate ?: today().time))
    }

    private fun onSetRepeated() {
        navigator.navigate(PtNavigation.SelectWeeksNumber)
    }

    private fun blockTimeSlot() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                state.postValue(BlockTimeSlotState.Loading())

                if (selectedTimeSet.isEmpty()) return@launch

                sessionsRepository.clearTemporarySessions()

                selectedTimeSet.forEach { time ->
                    val start = time.first
                    val end = time.second
                    conflicts.addAll(
                        blockRepository.findConflicts(start, end, selectedRepeatedWeeks - 1)
                    )
                    conflicts.sortByDescending { it.from }
                }

                performBlockSlot()

            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(BlockTimeSlotState.Error(error))
            }
        }
    }

    private fun performSecondaryConfirmClick() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                performBlockSlot()
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(BlockTimeSlotState.Error(error))
            }
        }
    }

    private suspend fun performBlockSlot() {
        var sessionToResolve: SessionEntity? = null
        while (sessionToResolve == null && conflicts.isNotEmpty()) {
            sessionToResolve = sessionsRepository.getSession(conflicts.removeLast().id)
        }

        if (sessionToResolve != null) {
            state.postValue(BlockTimeSlotState.SetConflictListener)
            navigator.navigate(PtNavigation.ResolveConflicts(sessionToResolve))
        } else {
            val sessions = blockRepository.blockTimeSlots(selectedTimeSet.toList(), selectedRepeatedWeeks)
            sessionsRepository.saveSessions(sessions)
            state.postValue(BlockTimeSlotState.SlotBlocked)
        }
    }
}

open class BlockTimeSlotState {
    data class Loading(val active: Boolean = true) : BlockTimeSlotState()
    data class Error(val error: Exception) : BlockTimeSlotState()
    object Loaded : BlockTimeSlotState()
    object SlotBlocked : BlockTimeSlotState()
    object SetConflictListener : BlockTimeSlotState()
    object ResetTimeAndRepeats : BlockTimeSlotState()
}

open class BlockTimeSlotIntent {
    object SetDateClicked : BlockTimeSlotIntent()
    object SetTimeClicked : BlockTimeSlotIntent()
    object SetRepeatedClicked : BlockTimeSlotIntent()
    data class DateChanged(val date: Long) : BlockTimeSlotIntent()
//    data class TimeChanged(val start: Long, val end: Long) : BlockTimeSlotIntent()
    data class RepeatedWeeksChanged(val weeks: Int = -1) : BlockTimeSlotIntent()
    object BlockBtnClicked : BlockTimeSlotIntent()
    object ConflictUpdated : BlockTimeSlotIntent()
    object FlowCancelled : BlockTimeSlotIntent()
}
