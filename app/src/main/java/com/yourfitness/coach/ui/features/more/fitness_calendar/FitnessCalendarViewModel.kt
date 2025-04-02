package com.yourfitness.coach.ui.features.more.fitness_calendar

import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.data.dao.ScheduleDao
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.data.entity.ScheduleEntity
import com.yourfitness.coach.data.mapper.toEntity
import com.yourfitness.coach.domain.date.ddMmDdddToLong
import com.yourfitness.coach.domain.date.formatCalendarRFC
import com.yourfitness.coach.domain.date.setDayEndTime
import com.yourfitness.coach.domain.date.toMilliseconds
import com.yourfitness.coach.domain.date.toSeconds
import com.yourfitness.coach.domain.facility.FacilityRepository
import com.yourfitness.coach.domain.models.CalendarBookClassData
import com.yourfitness.coach.domain.settings.SettingsManager
import com.yourfitness.coach.network.YFCRestApi
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.domain.date.setDayStartTime
import com.yourfitness.common.domain.date.toDate
import com.yourfitness.common.domain.models.CalendarView
import com.yourfitness.common.domain.models.Week
import com.yourfitness.common.ui.features.fitness_calendar.BaseFitnessCalendarIntent
import com.yourfitness.common.ui.features.fitness_calendar.BaseFitnessCalendarState
import com.yourfitness.common.ui.features.fitness_calendar.BaseFitnessCalendarViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class FitnessCalendarViewModel @Inject constructor(
    private val navigator: Navigator,
    private val restApi: YFCRestApi,
    private val scheduleDao: ScheduleDao,
    private val repository: FacilityRepository,
    private val profileRepository: ProfileRepository,
    private val settingsManager: SettingsManager
) : BaseFitnessCalendarViewModel() {

    var items = mutableListOf<CalendarView.CalendarItem>()

    private var classes: List<ScheduleEntity> = emptyList()
    private var facilities: List<FacilityEntity> = emptyList()

    init {
        openTab()
    }

    override fun handleIntent(intent: BaseFitnessCalendarIntent) {
        super.handleIntent(intent)
        when (intent) {
        }
    }

    override suspend fun getMonthTabData(date: LocalDate): Pair<List<CalendarView.CalendarItem>, LocalDate?> {
        return try {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -START_DATE)
            val startDate = calendar.time.formatCalendarRFC()
            calendar.add(Calendar.DAY_OF_YEAR, END_DATE)
            val endDate = calendar.time.formatCalendarRFC()

            val response = restApi.getSchedule(startDate, endDate)
//            scheduleDao.delete()
            scheduleDao.writeSchedule(response.map { it.toEntity() })
            return loadFacilities(date.toDate()) to null
        } catch (error: Exception) {
            Timber.e(error)
            listOf<CalendarView.CalendarItem>() to null
        }
    }

    private suspend fun loadFacilities(date: Date): List<CalendarView.CalendarItem> {
        return try {
            val bookedClass = mutableListOf<CalendarView.CalendarItem>()
            classes = scheduleDao.readAll()
            repository.downloadFacilitiesData()
            facilities = repository.fetchFacilities()
            val settingsGlobal = settingsManager.getSettings()

            classes.forEach { schedule ->
                val gym = facilities.firstOrNull { gym ->
                    gym.customClasses?.any { schedule.customClassId == it.id } == true
                } ?: return@forEach
                val instructor = gym.instructors?.firstOrNull { it.id == schedule.instructorId }
                val fitnessClass =
                    gym.customClasses?.firstOrNull { it.id == schedule.customClassId }
                        ?: return@forEach
                bookedClass.add(
                    CalendarView.CalendarItem(
                        facilityName = gym.name ?: "",
                        coachName = instructor?.name ?: "",
                        objectName = fitnessClass.name ?: "",
                        time = schedule.from,
                        date = schedule.from,
                        address = gym.address ?: "",
                        types = fitnessClass.type ?: "",
                        icon = gym.icon ?: "",
                        classEntryLeadTime = settingsGlobal?.classEntryLeadTime ?: 5,
                        classCancellationTime = settingsGlobal?.classCancellationTime ?: 5,
                        facilityId = gym.id ?: "",
                        objectId = schedule.id,
                    )
                )
            }

            items = bookedClass.sortedBy { it.time }.toMutableList()
            items
        } catch (error: Exception) {
            Timber.e(error)
            listOf()
        }
    }

    private fun postLoadingState() {
        state.postValue(BaseFitnessCalendarState.Loading)
    }

    private fun postErrorState(error: Exception) {
        state.postValue(BaseFitnessCalendarState.Error(error))
    }

    private suspend fun onClassCanceled(data: CalendarBookClassData) {
        getMonthTabData()
    }

    fun onRebookClick(bookedClass: CalendarView.CalendarItem) {
        viewModelScope.launch {
            val facility = facilities.find { it.id == bookedClass.facilityId }
            val schedule = classes.find { it.id == bookedClass.objectId } ?: return@launch
            val classId = facility?.customClasses
                ?.find { it.id == schedule.customClassId }?.id ?: return@launch
            navigator.navigate(
                Navigation.BookingClassCalendar(
                    classId,
                    bookedClass.objectName,
                    facility,
                    true,
                    bookedClass.objectId
                )
            )
        }
    }

    fun onCancelClick(bookedClass: CalendarView.CalendarItem) {
        buildCancelBookData(bookedClass)?.let { cancelClass(it) }
    }

    protected fun cancelClass(data: CalendarBookClassData) {
        navigator.navigate(Navigation.ConfirmCancelClass(data))
    }

    fun cancelClassConfirmed(data: CalendarBookClassData) {
        viewModelScope.launch {
            try {
                postLoadingState()
                restApi.cancelBookedClass(data.scheduleId)
                navigator.navigate(Navigation.ConfirmCancelResult(data.credits))
                onClassCanceled(data)
            } catch (error: Exception) {
                Timber.e(error)
                postErrorState(error)
            }
        }
    }

    private fun buildCancelBookData(bookedClass: CalendarView.CalendarItem): CalendarBookClassData? {
        val facility = facilities.find { it.id == bookedClass.facilityId }
        return classes.find { it.id == bookedClass.objectId }?.let { schedule ->
            val facilityClass = facility?.customClasses?.find { it.id == schedule.customClassId }
            return@let CalendarBookClassData(
                scheduleId = schedule.id,
                className = facilityClass?.name.orEmpty(),
                conflictClassId = "",
                conflictClassName = "",
                time = schedule.from,
                date = schedule.from,
                coachName = bookedClass.coachName,
                address = bookedClass.address,
                facilityName = bookedClass.facilityName,
                icon = bookedClass.icon,
                credits = facilityClass?.priceCoins ?: 0,
                availableBonusCredits = 0
            )
        }
    }

    override suspend fun getWeekTabData(): Pair<List<CalendarView>, Int> {
        return try {
            val datesForPeriod = getDatesForPeriod()
            val firstAndLastDayOfWeek = mutableListOf<Week>()
            val today = Calendar.getInstance()
            var position = 0
            datesForPeriod.forEach {
                firstAndLastDayOfWeek.add(getFirstAndLastDayOfWeek(it))
            }
            val classesByWeek = mutableListOf<CalendarView>()
            firstAndLastDayOfWeek.toSet().forEach {
                classesByWeek.add(CalendarView.Header(it.startDate, it.endDate))
                if (today.time.setDayStartTime().timeInMillis in it.startDate..it.endDate) {
                    position = classesByWeek.size
                }
                datesForPeriod.forEach { day ->
                    if (day >= it.startDate && day < it.endDate) {
                        val classes = items.filter {
                            it.time.toMilliseconds().toDate()
                                ?.setDayStartTime()?.timeInMillis == day
                        }
                        if (classes.isNotEmpty()) {
                            classesByWeek.addAll(classes)
                        } else {
                            classesByWeek.add(CalendarView.CalendarItem.empty(day.toSeconds()))
                        }
                    }
                }
            }
            return classesByWeek to position
        } catch (error: Exception) {
            Timber.e(error)
            listOf<CalendarView>() to 0
        }
    }

    override suspend fun getDayTabData(date: LocalDate): Pair<List<CalendarView>, Int> {
        return try {
            val datesForPeriod = getDatesForPeriod()
            val today = Calendar.getInstance()
            var position = 0
            val classesByDay = mutableListOf<CalendarView>()
            var size = classesByDay.size
            datesForPeriod.toSet().forEach { day ->
                classesByDay.add(CalendarView.Header(day))
                if (today.time.setDayStartTime().timeInMillis == day) {
                    position = classesByDay.size - 1
                }
                val classes = items.filter {
                    it.time.toMilliseconds().toDate()?.setDayStartTime()?.timeInMillis == day
                }
                if (classes.isNotEmpty()) {
                    classesByDay.addAll(classes)
                }
                if (size + 1 == classesByDay.size) {
                    classesByDay.add(CalendarView.CalendarItem.empty(day.toSeconds()))
                }
                size += classesByDay.size - size
            }
            classesByDay to position
        } catch (error: Exception) {
            Timber.e(error)
            listOf<CalendarView>() to 0
        }
    }

    fun goToClass(bookedClass: CalendarView.CalendarItem) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
//                val profile = commonRestApi.profile()
//                navigator.navigate(Navigation.YouHaveUpcomingClass(bookedClass, profile))
            } catch (error: Exception) {
                Timber.e(error)
            }
        }
    }

    override fun getDatesForPeriod(): List<Long> {
        val calendar = Calendar.getInstance()
        calendar.apply {
            add(Calendar.MONTH, -1)
            set(Calendar.HOUR_OF_DAY, 0)
            clear(Calendar.MINUTE)
            clear(Calendar.SECOND)
            clear(Calendar.MILLISECOND)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val startDate = calendar.timeInMillis
        calendar.add(Calendar.MONTH, 2)
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endDate = calendar.timeInMillis
        val date = mutableListOf<Long>()
        val start = Calendar.getInstance()
        start.timeInMillis = startDate
        val end = Calendar.getInstance()
        end.timeInMillis = endDate
        while (!start.after(end)) {
            date.add(start.timeInMillis)
            start.add(Calendar.DATE, 1)
        }
        return date
    }

    private fun getFirstAndLastDayOfWeek(date: Long): Week {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        val date1: Date = calendar.time
        val checkFormat = SimpleDateFormat("MM/yyyy", Locale.US)
        val currentCheckDate = checkFormat.format(date1)

        val week: Int = calendar.get(Calendar.WEEK_OF_MONTH)
        val month: Int = calendar.get(Calendar.MONTH)
        val year: Int = calendar.get(Calendar.YEAR)
        calendar.clear()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.WEEK_OF_MONTH, week)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.YEAR, year)

        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

        val date: Date = calendar.time
        val timeSixDayPlus: Long = calendar.timeInMillis + 518400000L
        val dateL = Date(timeSixDayPlus)
        var firstDate = simpleDateFormat.format(date)
        var lastDate = simpleDateFormat.format(dateL)
        val firstDateCheck = checkFormat.format(date)
        val lastDateCheck = checkFormat.format(dateL)

        if (!firstDateCheck.toString().equals(currentCheckDate, ignoreCase = true)) {
            var month = (calendar[Calendar.MONTH] + 2).toString()
            if (month.length < 2) {
                month = "0$month"
            }
            firstDate = "01" + "/" + month + "/" + calendar.get(Calendar.YEAR)
        }
        if (!lastDateCheck.toString().equals(currentCheckDate, ignoreCase = true)) {
            val ma: Int = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            var month = (calendar[Calendar.MONTH] + 1).toString()
            if (month.length < 2) {
                month = "0$month"
            }
            lastDate = ma.toString() + "/" + month + "/" + calendar.get(Calendar.YEAR)
        }
        return Week(
            firstDate.ddMmDdddToLong() ?: 0L,
            lastDate.ddMmDdddToLong().toDate()?.setDayEndTime() ?: 0L
        )
    }

    companion object {
        private const val START_DATE = 35
        private const val END_DATE = 90
    }
}

open class FitnessCalendarState {
}

open class FitnessCalendarIntent {
}
