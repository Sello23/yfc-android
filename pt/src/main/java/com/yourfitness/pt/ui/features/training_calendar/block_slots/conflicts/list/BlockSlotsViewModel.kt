package com.yourfitness.pt.ui.features.training_calendar.block_slots.conflicts.list

import androidx.lifecycle.viewModelScope
import com.yourfitness.common.domain.models.CalendarView
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.pt.domain.calendar.SessionsRepository
import com.yourfitness.pt.domain.calendar.actions.SessionActionManager
import com.yourfitness.pt.ui.features.training_calendar.actions.ConfirmFlow
import com.yourfitness.pt.ui.navigation.PtNavigation
import com.yourfitness.pt.ui.navigation.PtNavigator
import com.yourfitness.pt.ui.utils.SessionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BlockSlotsViewModel @Inject constructor(
    private val ptNavigator: PtNavigator,
    private val sessionsRepository: SessionsRepository,
    private val actionManager: SessionActionManager,
) : MviViewModel<BlockSlotsIntent, BlockSlotsState>() {

    var selectionMode = false
        private set
    private val blockedSlots = mutableListOf<Pair<Boolean, CalendarView.CalendarItem>>()

    init {
        loadData()
    }

    private fun loadData(isUpdated: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                selectionMode = false
                blockedSlots.clear()
                blockedSlots.addAll(sessionsRepository.getBlockSlots().map {
                    false to it
                }.toMutableList())
                state.postValue(BlockSlotsState.SlotsLoaded(blockedSlots, isUpdated))
            } catch (error: Exception) {
                Timber.e(error)
            }
        }
    }

    override fun handleIntent(intent: BlockSlotsIntent) {
        when (intent) {
            is BlockSlotsIntent.SlotClicked -> onRemoveSlot(intent.id)
            is BlockSlotsIntent.SlotRemoved -> loadData(true)
            is BlockSlotsIntent.ToolbarActionClicked -> {
                selectionMode = !selectionMode
                if (!selectionMode) {
                    val updatedSlots = blockedSlots.map { false to it.second }
                    blockedSlots.clear()
                    blockedSlots.addAll(updatedSlots)
                }
                state.value = BlockSlotsState.ActionStateUpdated(blockedSlots)
            }
            is BlockSlotsIntent.ListItemChecked -> updateCheckedItems(intent.id, intent.checked, intent.pos)
            is BlockSlotsIntent.RemoveClicked -> openDeleteDetailsScreen(intent.removeAll)
            is BlockSlotsIntent.SuccessDialogClosed -> {
                if (blockedSlots.isNotEmpty()) return
                state.value = BlockSlotsState.CloseScreen
            }
        }
    }

    private fun onRemoveSlot(id: String) {
        viewModelScope.launch {
            try {
                actionManager.startSessionAction(id, SessionStatus.BLOCKED_SLOT.value)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun updateCheckedItems(sessionId: String, selected: Boolean, pos: Int) {
        val index = blockedSlots.indexOfFirst { it.second.objectId.orEmpty() == sessionId }
        if (index >= 0) {
            blockedSlots[index] = blockedSlots[index].copy(first = selected)
            state.value = BlockSlotsState.ListItemUpdated(blockedSlots, pos)
        }
    }

    private fun openDeleteDetailsScreen(removeAll: Boolean) {
        val ids = if (removeAll) blockedSlots.mapNotNull { it.second.objectId }
        else blockedSlots.filter { it.first }.mapNotNull { it.second.objectId }
        ptNavigator.navigate(PtNavigation.DeleteSlotListFlow(ArrayList(ids), ConfirmFlow.CANCEL_BLOCK_LIST, false))
    }
}

open class BlockSlotsIntent {
    data class SlotClicked(val id: String) : BlockSlotsIntent()
    object SlotRemoved : BlockSlotsIntent()
    object ToolbarActionClicked : BlockSlotsIntent()
    data class ListItemChecked(
        val id: String,
        val checked: Boolean,
        val pos: Int
    ) : BlockSlotsIntent()
    data class RemoveClicked(val removeAll: Boolean) : BlockSlotsIntent()
    object SuccessDialogClosed : BlockSlotsIntent()
}

open class BlockSlotsState {
    object Loading : BlockSlotsState()
    data class SlotsLoaded(
        val slots: List<Pair<Boolean, CalendarView.CalendarItem>>,
        val isUpdated: Boolean
    ) : BlockSlotsState()
    data class Error(val error: Exception) : BlockSlotsState()
    data class ActionStateUpdated(
        val slots: List<Pair<Boolean, CalendarView.CalendarItem>>
    ) : BlockSlotsState()
    data class ListItemUpdated(
        val slots: List<Pair<Boolean, CalendarView.CalendarItem>>,
        val pos: Int
    ) : BlockSlotsState()
    object CloseScreen : BlockSlotsState()
}
