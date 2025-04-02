package com.yourfitness.common.ui.features.fitness_calendar

import androidx.lifecycle.viewModelScope
import com.yourfitness.common.domain.date.getWeekNumber
import com.yourfitness.common.domain.date.toMilliseconds
import com.yourfitness.common.domain.date.todayLocal
import com.yourfitness.common.domain.models.CalendarView
import com.yourfitness.common.domain.models.CalendarView.CalendarItem.Companion.isEmpty
import com.yourfitness.common.network.dto.ProfileResponse
import com.yourfitness.common.ui.features.fitness_calendar.BaseFitnessCalendarFragment.Companion.DAY_TAB
import com.yourfitness.common.ui.features.fitness_calendar.BaseFitnessCalendarFragment.Companion.MONTH_TAB
import com.yourfitness.common.ui.features.fitness_calendar.BaseFitnessCalendarFragment.Companion.WEEK_TAB
import com.yourfitness.common.ui.mvi.MviViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

typealias Events = Map<LocalDate, List<CalendarView.CalendarItem>>

open class BaseFitnessCalendarViewModel @Inject constructor(
) : MviViewModel<BaseFitnessCalendarIntent, BaseFitnessCalendarState>() {

    var events = mutableMapOf<LocalDate, List<CalendarView.CalendarItem>>()
        private set
    open var tabPosition = MONTH_TAB
        protected set
    var selectedDate: LocalDate = todayLocal()
        private set

    open fun getTabIndexToSelect(): Int = tabPosition
    override fun handleIntent(intent: BaseFitnessCalendarIntent) {
        when (intent) {
            is BaseFitnessCalendarIntent.OnTabChanged -> {
                selectedDate = todayLocal()
                tabPosition = intent.pos
                openTab()
            }
            is BaseFitnessCalendarIntent.DayTabSelectedDateUpdated -> selectedDate = intent.date
            is BaseFitnessCalendarIntent.SelectedDateChanged -> {
                if (intent.date == selectedDate) return
                when (tabPosition) {
                    MONTH_TAB -> {
                        val prevYear = selectedDate.year
                        val prevMonth = selectedDate.monthValue
                        selectedDate = intent.date
                        val needUpdate = prevMonth != selectedDate.monthValue || prevYear != selectedDate.year
                        configureMonthTabData(needUpdate)
                    }
                    DAY_TAB -> {
                        val prevWeekNumber = selectedDate.getWeekNumber()
                        val curWeekNumber = intent.date.getWeekNumber()
                        selectedDate = intent.date
                        val needUpdate = prevWeekNumber != curWeekNumber
                        configureDayTabData(needUpdate)
                    }
                }
            }
        }
    }

    protected open fun openTab() {
        when (tabPosition) {
            MONTH_TAB -> configureMonthTabData()
            WEEK_TAB -> configureWeekTabData()
            DAY_TAB -> configureDayTabData()
        }
    }

    private fun configureMonthTabData(needUpdate: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            state.postValue(BaseFitnessCalendarState.Loading)
            try {
                var dateToScroll: LocalDate? = null
                if (needUpdate) {
                    val monthData = getMonthTabData(selectedDate)
                    dateToScroll = monthData.second
                    events = monthData.first.groupBy {
                        Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
                    }.toMutableMap()
                }
                state.postValue(BaseFitnessCalendarState.MonthTabDataLoaded(events, dateToScroll))
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    protected open suspend fun getMonthTabData(date: LocalDate = todayLocal()): Pair<List<CalendarView.CalendarItem>, LocalDate?> =
        emptyList<CalendarView.CalendarItem>() to null

    private fun configureWeekTabData() {
        viewModelScope.launch(Dispatchers.IO) {
            state.postValue(BaseFitnessCalendarState.Loading)
            try {
                val data = getWeekTabData()
                state.postValue(BaseFitnessCalendarState.WeekDataLoaded(data.first, data.second))
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    protected open suspend fun getWeekTabData(): Pair<List<CalendarView>, Int> =
        emptyList<CalendarView>() to 0

    private fun configureDayTabData(needUpdate: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (needUpdate) {
                    state.postValue(BaseFitnessCalendarState.Loading)
                    val data = getDayTabData(selectedDate)
                    events = data.first.filter { it is CalendarView.CalendarItem && !it.isEmpty()}
                        .map { obj -> (obj as CalendarView.CalendarItem) }
                        .groupBy {
                        Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
                    }.toMutableMap()
                    state.postValue(BaseFitnessCalendarState.DayDataLoaded(data.first, data.second))
                } else {
                    state.postValue(BaseFitnessCalendarState.Position)
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    protected open suspend fun getDayTabData(date: LocalDate): Pair<List<CalendarView>, Int> =
        emptyList<CalendarView>() to 0

    fun getDayPosition(date: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val datesForPeriod = getDatesForPeriod()
                var position = 0
                datesForPeriod.forEachIndexed { index, day ->
                    if (day == date.toMilliseconds()) {
                        position = index
                    }
                }
//                state.postValue(BaseFitnessCalendarState.Position(position * 2))
            } catch (error: Exception) {
                Timber.e(error)
            }
        }
    }

    protected open fun getDatesForPeriod(): List<Long> = emptyList()

    companion object {
        private const val START_DATE = 35
        private const val END_DATE = 90
    }
}


open class BaseFitnessCalendarState {
    object Loading : BaseFitnessCalendarState()
    data class Error(val error: Exception) : BaseFitnessCalendarState()
    object Success : BaseFitnessCalendarState()
    data class MonthTabDataLoaded(
        val items: Events,
        val dateToScroll: LocalDate?
    ) : BaseFitnessCalendarState()
    data class WeekDataLoaded(
        val weekItems: List<CalendarView>,
        val position: Int
    ) : BaseFitnessCalendarState()

    data class DayDataLoaded(
        val dayItems: List<CalendarView>,
        val position: Int
    ) : BaseFitnessCalendarState()

    object Position : BaseFitnessCalendarState()
    data class GoToClass(val bookedClass: CalendarView.CalendarItem, val profile: ProfileResponse) :
        BaseFitnessCalendarState()
}

open class BaseFitnessCalendarIntent {
    data class OnTabChanged(val pos: Int) : BaseFitnessCalendarIntent()
    data class SelectedDateChanged(val date: LocalDate) : BaseFitnessCalendarIntent()
    data class DayTabSelectedDateUpdated(val date: LocalDate) : BaseFitnessCalendarIntent()
}
