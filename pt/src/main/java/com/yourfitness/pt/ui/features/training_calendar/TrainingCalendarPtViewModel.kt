package com.yourfitness.pt.ui.features.training_calendar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.domain.models.CalendarView
import com.yourfitness.common.ui.features.fitness_calendar.BaseFitnessCalendarIntent
import com.yourfitness.common.ui.features.fitness_calendar.BaseFitnessCalendarState
import com.yourfitness.pt.domain.calendar.SessionsRepository
import com.yourfitness.pt.domain.calendar.actions.SessionActionManager
import com.yourfitness.pt.domain.models.StatusFilter
import com.yourfitness.pt.domain.pt.PtRepository
import com.yourfitness.pt.ui.features.training_calendar.TrainingCalendarPtFragment.Companion.SCHEDULE_TAB
import com.yourfitness.pt.ui.navigation.PtNavigation
import com.yourfitness.pt.ui.navigation.PtNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TrainingCalendarPtViewModel @Inject constructor(
    private val ptNavigator: PtNavigator,
    ptRepository: PtRepository,
    sessionsRepository: SessionsRepository,
    actionManager: SessionActionManager,
    profileRepository: ProfileRepository,
    savedState: SavedStateHandle
) : TrainingCalendarViewModel(ptRepository, sessionsRepository, actionManager, profileRepository, savedState) {

    val isMainScreen: Boolean = savedState.get<Boolean>("is_main") ?: true
    override var tabPosition = SCHEDULE_TAB

    var blockedSlotsNumber = 0
        private set

    override fun handleIntent(intent: BaseFitnessCalendarIntent) {
        when (intent) {
            is TrainingCalendarPtIntent.FilterItemTapped -> {
                updateFilter(intent)
                openTab()
            }
            is BaseFitnessCalendarIntent.OnTabChanged -> {
                val statusAmounts = statusFilter.statusAmounts
                statusFilter = StatusFilter().copy(statusAmounts = statusAmounts)
            }
            is TrainingCalendarPtIntent.ManageTimeSlots -> manageTimeSlots()
            is TrainingCalendarPtIntent.ShowBlockedTimeSlots -> showBlockedTimeSlots()
        }
        super.handleIntent(intent)
    }

    override suspend fun performBaseSetup() {
        blockedSlotsNumber = sessionsRepository.getBlockSlotsCount()
    }

    override fun getTabIndexToSelect(): Int {
        return if (tabPosition == SCHEDULE_TAB) 0
        else tabPosition + 1
    }

    override fun openTab() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                blockedSlotsNumber = sessionsRepository.getBlockSlotsCount()
                when (tabPosition) {
                    SCHEDULE_TAB -> configureScheduleTabData()
                    else -> super.openTab()
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun configureScheduleTabData() {
        viewModelScope.launch(Dispatchers.IO) {
            state.postValue(BaseFitnessCalendarState.Loading)
            try {
                val data = getScheduleTabData()
                updateFilters()
                state.postValue(
                    TrainingCalendarPtState.ScheduleTabLoaded(
                        data.first,
                        data.second,
                        data.third
                    )
                )
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private suspend fun getScheduleTabData(): Triple<List<CalendarView>, Int, Int?> {
        return sessionsRepository.getSessionsDataForScheduleCalendar(
            sessionIdToScroll,
            getSelectedStatuses()
        )
    }

    private fun updateFilter(intent: TrainingCalendarPtIntent.FilterItemTapped) {
        if (intent.value == StatusFilter.EMPTY_STATE) {
            statusFilter.selectedStatuses.clear()
            statusFilter.selectedStatuses.add(StatusFilter.EMPTY_STATE)
        } else {
            statusFilter.selectedStatuses.remove(StatusFilter.EMPTY_STATE)

            if (intent.added) {
                statusFilter.selectedStatuses.add(intent.value)
            } else {
                statusFilter.selectedStatuses.remove(intent.value)
            }

            if (statusFilter.selectedStatuses.isEmpty()) {
                statusFilter.selectedStatuses.add(StatusFilter.EMPTY_STATE)
            }
        }
    }

    private fun manageTimeSlots() {
        ptNavigator.navigate(PtNavigation.BlocTimeSlot)
    }

    private fun showBlockedTimeSlots() {
        ptNavigator.navigate(PtNavigation.BlockSlotsList)
    }
}

open class TrainingCalendarPtState {
    data class ScheduleTabLoaded(
        val scheduleItems: List<CalendarView>,
        val position: Int,
        val scrollToPosition: Int?
    ) : BaseFitnessCalendarState()
}

open class TrainingCalendarPtIntent {
    data class FilterItemTapped(
        val value: String,
        val added: Boolean
    ) : BaseFitnessCalendarIntent()
    object ManageTimeSlots : BaseFitnessCalendarIntent()
    object ShowBlockedTimeSlots : BaseFitnessCalendarIntent()
}