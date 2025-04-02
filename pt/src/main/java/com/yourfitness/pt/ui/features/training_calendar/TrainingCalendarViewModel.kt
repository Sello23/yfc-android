package com.yourfitness.pt.ui.features.training_calendar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.domain.models.CalendarView
import com.yourfitness.common.ui.features.fitness_calendar.BaseFitnessCalendarIntent
import com.yourfitness.common.ui.features.fitness_calendar.BaseFitnessCalendarViewModel
import com.yourfitness.pt.domain.calendar.SessionsRepository
import com.yourfitness.pt.domain.calendar.actions.SessionActionManager
import com.yourfitness.pt.domain.models.StatusFilter
import com.yourfitness.pt.domain.pt.PtRepository
import com.yourfitness.pt.ui.utils.SessionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
open class TrainingCalendarViewModel @Inject constructor(
    protected val ptRepository: PtRepository,
    protected val sessionsRepository: SessionsRepository,
    private val actionManager: SessionActionManager,
    private val profileRepository: ProfileRepository,
    savedState: SavedStateHandle
) : BaseFitnessCalendarViewModel() {

    protected var sessionIdToScroll: String = savedState.get<String>("session_id").orEmpty()

    var statusFilter = StatusFilter()
        protected set

    init {
        initData()
    }

    protected open fun initData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                performBaseSetup()
                sessionsRepository.downloadUserSessions()
                ptRepository.downloadPtBalanceList()
                openTab()
                updateStatuses()
                scheduleStatusUpdateTimer()
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    open suspend fun performBaseSetup() {}

    override fun handleIntent(intent: BaseFitnessCalendarIntent) {
        super.handleIntent(intent)
        when (intent) {
            is TrainingCalendarIntent.OnSessionActionClicked -> onSessionActionClick(intent.sessionId, intent.status)
            is TrainingCalendarIntent.Refresh -> openTab()
        }
    }

    override suspend fun getMonthTabData(date: LocalDate) : Pair<List<CalendarView.CalendarItem>, LocalDate?> {
        updateFilters()
        val scrollTo = sessionIdToScroll
        sessionIdToScroll = ""
        return sessionsRepository.getSessionsDataForMonthCalendar(date, getSelectedStatuses(), scrollTo)
    }

    override suspend fun getWeekTabData(): Pair<List<CalendarView>, Int> {
        updateFilters()
        return sessionsRepository.getSessionsDataForWeekCalendar(getSelectedStatuses())
    }

    override suspend fun getDayTabData(date: LocalDate): Pair<List<CalendarView>, Int> {
        updateFilters()
        return sessionsRepository.getSessionsDataForDayCalendar(date, getSelectedStatuses())
    }

    protected suspend fun updateFilters() {
        if (!profileRepository.isPtRole()) return
        statusFilter = statusFilter.copy(statusAmounts = sessionsRepository.getStatusesAmountInfo())
    }

    private fun onSessionActionClick(id: String, status: String) {
        viewModelScope.launch {
            try {
                actionManager.startSessionAction(id, status)
                if (status == SessionStatus.COMPLETED.value) openTab()
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    protected fun getSelectedStatuses(): List<String> {
        return statusFilter.selectedStatuses.filter { it != StatusFilter.EMPTY_STATE }
    }

    private suspend fun scheduleStatusUpdateTimer() = withContext(Dispatchers.IO) {
        delay(40000)
        updateStatuses(true)
    }

    private fun updateStatuses(needUpdate: Boolean = false) {
        viewModelScope.launch {
            try {
                sessionsRepository.updateStatusesIfNeeded()
            } catch (e: Exception) {
                Timber.e(e)
            } finally {
                if (needUpdate) openTab()
                scheduleStatusUpdateTimer()
            }
        }
    }
}

open class TrainingCalendarIntent {
    data class OnSessionActionClicked(val sessionId: String, val status: String) : BaseFitnessCalendarIntent()
    object Refresh : BaseFitnessCalendarIntent()
}
