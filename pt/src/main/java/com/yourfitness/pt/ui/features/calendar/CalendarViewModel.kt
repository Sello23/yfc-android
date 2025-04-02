package com.yourfitness.pt.ui.features.calendar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yourfitness.common.domain.date.today
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.pt.data.entity.fullName
import com.yourfitness.pt.domain.calendar.SessionsRepository
import com.yourfitness.pt.domain.calendar.UserCalendarRepository
import com.yourfitness.pt.domain.models.*
import com.yourfitness.pt.domain.pt.PtRepository
import com.yourfitness.pt.domain.values.ACTIONS_ENABLED
import com.yourfitness.pt.domain.values.PT_ID
import com.yourfitness.pt.ui.navigation.PtNavigation
import com.yourfitness.pt.ui.navigation.PtNavigator
import com.yourfitness.pt.ui.utils.SessionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@HiltViewModel
open class CalendarViewModel @Inject constructor(
    protected val navigator: PtNavigator,
    protected val repository: PtRepository,
    protected val userCalendarRepository: UserCalendarRepository,
    protected val sessionsRepository: SessionsRepository,
    savedState: SavedStateHandle
) : MviViewModel<CalendarIntent, CalendarState>() {

    val actionsEnabled = savedState[ACTIONS_ENABLED] ?: true
    protected var ptId = savedState.get<String>(PT_ID).orEmpty()
    protected var ptName: String = ""
    private var ptFacilities: List<FacilityInfo>? = null

    open var displayedDate = today()
    protected open val multiselectEnabled = false

    init {
        loadData()
    }

    override fun handleIntent(intent: CalendarIntent) {
        when (intent) {
            is CalendarIntent.TimeSlotTapped -> if (actionsEnabled) onTimeSlotTapped(intent.slot)
            is CalendarIntent.SelectedTimeSlotTapped -> if (actionsEnabled) onSelectedTimeSlotTapped(intent.slot)
            is CalendarIntent.FilledTimeSlotTapped -> if (actionsEnabled) onFilledTimeSlotTapped(intent.slot)
            is CalendarIntent.DateClicked -> onDateClicked()
            is CalendarIntent.BookTimeSlotTapped -> if (actionsEnabled) onBookTimeSlotTapped(intent.slot.time)
            is CalendarIntent.TimeSlotBusy -> loadPtData()
            is CalendarIntent.TimeSlotBooked, CalendarIntent.TimeSlotCompleted -> updateUserData()
        }
    }

    protected open fun loadData() {
        state.value = CalendarState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                sessionsRepository.clearTemporarySessions()
                sessionsRepository.downloadUserSessions()
                sessionsRepository.downloadPtSessions(ptId)
                repository.downloadPtBalanceList()
                val balance = repository.getPtBalance(ptId)
                ptName = repository.getPtById(ptId)?.fullName.orEmpty()
                val dayCalendarData = userCalendarRepository.generateCalendarData(displayedDate, ptId)
                state.postValue(CalendarState.PtInfoLoaded(balance, ptName, dayCalendarData))
                ptFacilities = repository.getPtFacilities(ptId)
                updateStatuses()
                scheduleStatusUpdateTimer()
            } catch (e: Exception) {
                Timber.e(e)
                state.postValue(CalendarState.Error(e))
            }
        }
    }

    protected suspend fun scheduleStatusUpdateTimer() = withContext(Dispatchers.IO) {
        delay(40000)
        updateStatuses()
    }

    protected fun updateStatuses() {
        viewModelScope.launch {
            try {
                sessionsRepository.updateStatusesIfNeeded()
            } catch (e: Exception) {
                Timber.e(e)
            } finally {
                val dayCalendarData = userCalendarRepository.generateCalendarData(displayedDate, ptId)
                state.postValue(CalendarState.PtInfoLoaded(calendarData = dayCalendarData))
                scheduleStatusUpdateTimer()
            }
        }
    }

    private fun loadPtData() {
        state.value = CalendarState.Loading
        viewModelScope.launch {
            try {
                sessionsRepository.clearTemporarySessions()
                sessionsRepository.downloadPtSessions(ptId)
                val dayCalendarData = userCalendarRepository.generateCalendarData(displayedDate, ptId)
                state.postValue(CalendarState.PtInfoLoaded(calendarData = dayCalendarData))
            } catch (e: Exception) {
                Timber.e(e)
                state.postValue(CalendarState.Error(e))
            }
        }
    }

    private fun updateUserData() {
        viewModelScope.launch {
            try {
                sessionsRepository.clearTemporarySessions()
                val balance = repository.getPtBalance(ptId)
                val dayCalendarData = userCalendarRepository.generateCalendarData(displayedDate, ptId)
                state.postValue(CalendarState.PtInfoLoaded(balance, ptName, dayCalendarData))
            } catch (e: Exception) {
                Timber.e(e)
                state.postValue(CalendarState.Error(e))
            }
        }
    }

    private fun onTimeSlotTapped(slot: TimeSlot) {
        viewModelScope.launch {
            try {
                sessionsRepository.saveTemporarySession(slot, ptId, multiselectEnabled)
                val dayCalendarData = userCalendarRepository.generateCalendarData(displayedDate, ptId)
                state.postValue(CalendarState.CalendarDataUpdated(dayCalendarData))
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun onSelectedTimeSlotTapped(slot: SelectedTimeSlot) {
        viewModelScope.launch {
            try {
                if (multiselectEnabled) sessionsRepository.clearTemporarySessionById(slot.id)
                else sessionsRepository.clearTemporarySessions()
                val dayCalendarData = userCalendarRepository.generateCalendarData(displayedDate, ptId)
                state.postValue(CalendarState.CalendarDataUpdated(dayCalendarData))
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun onFilledTimeSlotTapped(slot: FilledTimeSlot) {
        viewModelScope.launch {
            try {
                sessionsRepository.clearTemporarySessions()
                val status = sessionsRepository.getSessionStatus(slot.id)
                if (status == SessionStatus.COMPLETED.value) {
                    val dayCalendarData = userCalendarRepository.generateCalendarData(displayedDate, ptId)
                    state.postValue(CalendarState.CalendarDataUpdated(dayCalendarData))
                    navigator.navigate(PtNavigation.ConfirmSession(slot.id))
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun onDateClicked() {
        viewModelScope.launch {
            try {
                sessionsRepository.clearTemporarySessions()
                val dayCalendarData = userCalendarRepository.generateCalendarData(displayedDate, ptId)
                state.postValue(CalendarState.CalendarDataUpdated(dayCalendarData))
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun onBookTimeSlotTapped(date: Date) {
        viewModelScope.launch {
            if (repository.getPtBalance(ptId) == 0) {
                navigator.navigate(PtNavigation.BuySessions(ptId, true))
                return@launch
            }
            var maxAttempts = 6
            while (ptFacilities == null || maxAttempts != 0) {
                delay(50)
                maxAttempts--
            }
            val facilities = ptFacilities?.map {
                it.copy(workTimeData = repository.getWorkTimeData(it.timetable, date))
            }
            navigator.navigate(PtNavigation.BookTimeSlot(ptId, ptName, date, facilities.orEmpty()))
        }
    }
}

open class CalendarState {
    object Loading : CalendarState()
    data class Error(val error: Exception) : CalendarState()
    data class PtInfoLoaded(
        val balance: Int? = null,
        val ptName: String? = null,
        val calendarData: List<CalendarData>
    ) : CalendarState()

    data class CalendarDataUpdated(val calendarData: List<CalendarData>) : CalendarState()
}

open class CalendarIntent {
    data class TimeSlotTapped(val slot: TimeSlot) : CalendarIntent()
    data class SelectedTimeSlotTapped(val slot: SelectedTimeSlot) : CalendarIntent()
    data class FilledTimeSlotTapped(val slot: FilledTimeSlot) : CalendarIntent()
    object DateClicked : CalendarIntent()
    data class BookTimeSlotTapped(val slot: SelectedTimeSlot) : CalendarIntent()
    object TimeSlotBusy : CalendarIntent()
    object TimeSlotBooked : CalendarIntent()
    object TimeSlotCompleted : CalendarIntent()
}
