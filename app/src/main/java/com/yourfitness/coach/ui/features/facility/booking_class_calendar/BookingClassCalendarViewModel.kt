package com.yourfitness.coach.ui.features.facility.booking_class_calendar

import android.util.Range
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.data.dao.ScheduleDao
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.data.entity.ScheduleEntity
import com.yourfitness.coach.data.entity.classes
import com.yourfitness.coach.data.mapper.toEntity
import com.yourfitness.coach.domain.date.formatCalendarRFC
import com.yourfitness.coach.domain.date.toMilliseconds
import com.yourfitness.common.domain.date.setDayStartTime
import com.yourfitness.coach.domain.date.toSeconds
import com.yourfitness.coach.domain.date.toStartDayInMillis
import com.yourfitness.common.domain.date.today
import com.yourfitness.coach.domain.models.BookingResultData
import com.yourfitness.coach.domain.models.CalendarBookClassData
import com.yourfitness.coach.domain.models.toClassCalendarDayViewItem
import com.yourfitness.coach.domain.settings.SettingsManager
import com.yourfitness.coach.network.YFCRestApi
import com.yourfitness.coach.network.dto.ClassDto
import com.yourfitness.coach.network.dto.InstructorDto
import com.yourfitness.coach.network.dto.Schedule
import com.yourfitness.coach.ui.features.facility.class_operations.ClassOperationsViewModel
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.data.entity.ProfileEntity
import com.yourfitness.common.data.entity.fullName
import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.domain.date.today
import com.yourfitness.common.domain.models.ClassCalendarDayViewItem
import com.yourfitness.common.network.CommonRestApi
import com.yourfitness.common.network.dto.Gender
import com.yourfitness.common.network.dto.Profile
import com.yourfitness.common.network.dto.fullName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Integer.max
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ClassCalendarViewModel @Inject constructor(
    private val restApi: YFCRestApi,
    private val scheduleDao: ScheduleDao,
    private val profileRepository: ProfileRepository,
    private val settingsManager: SettingsManager,
    override val navigator: Navigator,
    savedState: SavedStateHandle
) : ClassOperationsViewModel<Any, ClassCalendarState>(restApi, navigator) {

    companion object {
        private const val DAYS_BEFORE_CURRENT_DATE = 35
        private const val DAYS_AFTER_CURRENT_DATE = 90
    }

    private val classId = savedState.get<String>("class_id").orEmpty()
    private val facility = savedState.get<FacilityEntity>("facility")
    private val isRebook = savedState.get<Boolean>("is_rebook") ?: false
    private var rebookClassId = savedState.get<String>("rebook_class_id").orEmpty()

    private var schedules = listOf<Schedule>()
    private var userClasses: List<ScheduleEntity> = emptyList()
    private var userCredits: Int = 0
    private var bonusCredits: Int = 0
    private var profile: ProfileEntity? = null
    private var allFacilitiesClasses: List<ClassDto> = emptyList()
    private var classCancellationTimeInSeconds = 0
    private var rebooCancelledScheduleId: String? = null

    override fun postLoadingState() {
        state.postValue(ClassCalendarState.Loading)
    }

    override fun postErrorState(error: Exception) {
        state.postValue(ClassCalendarState.Error(error))
    }

    fun loadData() {
        viewModelScope.launch {
            try {
                state.postValue(ClassCalendarState.Loading)
                profile = profileRepository.getProfile()
                classCancellationTimeInSeconds =
                    (settingsManager.getSettings(true)?.classCancellationTime ?: 0) * 60
//                allFacilitiesClasses = restApi.facilities().facilities
//                    ?.flatMap { it.customClasses.orEmpty() }.orEmpty()
                loadClassCalendar()
            } catch (error: Exception) {
                state.postValue(ClassCalendarState.Error(error))
            }
        }
    }

    private fun loadClassCalendar(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                state.postValue(ClassCalendarState.Loading)
                loadBookedClasses()
                userCredits = 0//restApi.credits().toInt() // TODO temporary removed Studios
                bonusCredits = restApi.getBonusCredits()
                    .find { it.facilityID == facility?.id }?.amount ?: 0
                state.postValue(ClassCalendarState.Credits(userCredits, bonusCredits))
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, -DAYS_BEFORE_CURRENT_DATE)
                val startDate = calendar.time.formatCalendarRFC()
                calendar.add(Calendar.DAY_OF_YEAR, DAYS_AFTER_CURRENT_DATE)
                val endDate = calendar.time.formatCalendarRFC()
                schedules = restApi.getClassSchedule(classId, startDate, endDate)
                    .sortedBy { it.from ?: it.to }
                prepareCalendarData()
                onComplete()
            } catch (error: Exception) {
                state.postValue(ClassCalendarState.Error(error))
            }
        }
    }

    private fun isAvailableBooking(): Boolean {
        return (profile?.gender == Gender.FEMALE && facility?.femaleOnly == true) || facility?.femaleOnly != true
    }

    private suspend fun loadBookedClasses() {
        try {
            state.postValue(ClassCalendarState.Loading)
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -DAYS_BEFORE_CURRENT_DATE)
            val startDate = calendar.time.formatCalendarRFC()
            calendar.add(Calendar.DAY_OF_YEAR, DAYS_AFTER_CURRENT_DATE)
            val endDate = calendar.time.formatCalendarRFC()
            userClasses = restApi.getSchedule(startDate, endDate).map { it.toEntity() }
            scheduleDao.delete()
            scheduleDao.writeSchedule(userClasses)
        } catch (error: Exception) {
            state.postValue(ClassCalendarState.Error(error))
        }
    }

    private fun prepareCalendarData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
//                val instructors = restApi.facilities().facilities // TODO temporary removed Studios
//                    ?.flatMap { it.instructors.orEmpty() }.orEmpty()
                val today = Calendar.getInstance()
                var scrollPosition = 0
                val classesByDay = mutableListOf<ClassCalendarDayViewItem>()
                var classesByDaySize = 0
                getDatesForPeriod().toSet().forEach { dayInMillis ->
                    classesByDay.add(ClassCalendarDayViewItem.HeaderItem(dayInMillis))
                    if (today.time.setDayStartTime().timeInMillis == dayInMillis) {
                        scrollPosition = classesByDay.size - 1
                    }
//                    schedules.filter { it.from.toStartDayInMillis() == dayInMillis } // TODO temporary removed Studios
//                        .map { buildCalendarDayItem(it, instructors) }
//                        .also { classesByDay.addAll(it) }
                    if (classesByDaySize + 1 == classesByDay.size) {
                        classesByDay.add(ClassCalendarDayViewItem.Item(dayInMillis.toSeconds()))
                    }
                    classesByDaySize += classesByDay.size - classesByDaySize
                }
                state.postValue(ClassCalendarState.CalendarDay(classesByDay, scrollPosition))
            } catch (error: Exception) {
                state.postValue(ClassCalendarState.Error(error))
            }
        }
    }

    private fun buildCalendarDayItem(
        schedule: Schedule, instructors: List<InstructorDto>
    ): ClassCalendarDayViewItem.Item {
        val dayItem = schedule.toClassCalendarDayViewItem()
        dayItem.instructor = instructors.find { it.id == dayItem.instructor }?.name.orEmpty()
        val userClass = userClasses.find { it.id == dayItem.id }
        val facilityClass = facility?.classes?.find { it.id == classId }
        dayItem.isBooked = userClass != null
        dayItem.address = facility?.address
        dayItem.types = facilityClass?.type
        dayItem.icon = facility?.icon
        dayItem.availableSpots =
            max(0, (facilityClass?.availablePlaces ?: 0) - (schedule.bookedPlaces ?: 0))
        dayItem.credits = facilityClass?.priceCoins ?: 0
        dayItem.isNotAvailable = !isAvailableBooking()
        dayItem.isCancelAvailable = isCancelAvailable(schedule.from ?: 0)
        return dayItem
    }

    private fun getDatesForPeriod(): List<Long> {
        val calendar = Calendar.getInstance().apply {
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

    fun isEventExist(date: LocalDate): Boolean {
        return schedules.filter { schedule ->
            val from = schedule.from.toStartDayInMillis() ?: 0
            val to = schedule.to.toStartDayInMillis() ?: 0
            return@filter date.toMilliseconds() in from..to
        }.isNotEmpty()
    }

    fun onBookClassClick(item: ClassCalendarDayViewItem.Item) {
        if (isRebook) {
            buildBookClassData(item, userClasses.find { it.id == rebookClassId })
                ?.let { rescheduleConfirmed(it) }
        } else {
            buildBookClassData(item)
                ?.let { navigator.navigate(Navigation.ConfirmBooking(it)) }
        }
    }

    fun bookingClassConfirmed(data: CalendarBookClassData, purchasedCredits: Int? = null) {
        if (isCreditsAvailable(data)) {
            navigator.navigate(Navigation.CreditsNotEnough(data))
        } else if (!data.isRebook && data.isConflictAvailable) {
            navigator.navigate(Navigation.DoubleBooked(data, isCancelAvailable(data.date)))
        } else {
            bookSchedule(data, purchasedCredits)
        }
    }

    fun creditsPurchased(data: CalendarBookClassData, purchasedCredits: Int? = null) {
        loadClassCalendar(onComplete = {
            bookingClassConfirmed(data.copy(), purchasedCredits)
        })
    }

    fun rescheduleConfirmed(data: CalendarBookClassData) {
        viewModelScope.launch {
            try {
                val userClass = userClasses.find { it.id == data.conflictClassId }
                if (isCancelAvailable(userClass?.from ?: 0)) {
                    restApi.cancelBookedClass(data.conflictClassId)
                    rebooCancelledScheduleId = userClass?.customClassId
                }
                loadClassCalendar(onComplete = {
                    schedules.find { it.id == data.scheduleId }
                        ?.let { buildBookClassData(it.toClassCalendarDayViewItem()) }
                        ?.let { bookingClassConfirmed(data.copy(isRebook = true)) }
                })
            } catch (e: Exception) {
                state.postValue(ClassCalendarState.Error(e))
            }
        }
    }

    private fun isCreditsAvailable(data: CalendarBookClassData): Boolean {
        val balance = userCredits + bonusCredits
        return data.credits > balance
    }

    private fun isCancelAvailable(dateFromInSeconds: Long): Boolean {
        return dateFromInSeconds - (today().time / 1000) > classCancellationTimeInSeconds
    }

    private fun bookSchedule(data: CalendarBookClassData, purchasedCredits: Int?) {
        viewModelScope.launch {
            state.postValue(ClassCalendarState.Loading)
            try {
                restApi.bookClass(data.scheduleId)
                rebookClassId = data.scheduleId
                val resultData = buildBookingResultData(data, purchasedCredits)
                navigator.navigate(Navigation.ConfirmBookingResult(resultData))
                loadClassCalendar()
            } catch (e: Exception) {
                state.postValue(ClassCalendarState.Error(e))
            }
        }
    }

    private fun buildBookingResultData(data: CalendarBookClassData, purchasedCredits: Int?): BookingResultData {
        val rebookCredits =
            (data.credits - (allFacilitiesClasses.find { it.id == rebooCancelledScheduleId }?.priceCoins ?: 0))
            .takeIf { it > 0 } ?: 0
        val credits = if (data.isRebook) rebookCredits else
            if (data.availableBonusCredits > data.credits) 0 else data.credits - data.availableBonusCredits
        val bonusesCredits = if (data.availableBonusCredits > data.credits) data.credits else data.availableBonusCredits
        return BookingResultData(
            purchasedCredits = purchasedCredits,
            userName = profile?.fullName.orEmpty(),
            date = data.date,
            time = data.time,
            credits = credits,
            bonusesCredits = bonusesCredits,
            isRebook = data.isRebook
        )
    }

    fun onCancelBookedClassClick(item: ClassCalendarDayViewItem.Item) {
        buildBookClassData(item)?.let { cancelClass(it) }
    }

    override fun onClassCanceled(data: CalendarBookClassData) {
        loadClassCalendar()
    }

    private fun buildBookClassData(
        item: ClassCalendarDayViewItem.Item,
        conflictClass: ScheduleEntity? = null
    ): CalendarBookClassData? {
        return schedules.find { it.id == item.id }?.let { schedule ->
            val sRange = Range((schedule.from ?: 0) + 1, (schedule.to ?: 1) - 1)
            val conflictUserClass = userClasses.find {
                val range = Range(it.from, it.to)
                range.contains(sRange.lower) || range.contains(sRange.upper) || sRange.contains(range.lower) || sRange.contains(range.upper)
            }
            val conflictClassId = (conflictClass ?: conflictUserClass)?.id.orEmpty()
            val conflictClassName = (conflictClass
                ?: conflictUserClass)?.let { userClass -> allFacilitiesClasses.find { it.id == userClass.customClassId } }?.name.orEmpty()
            return@let CalendarBookClassData(
                scheduleId = schedule.id.orEmpty(),
                className = facility?.classes?.find { it.id == classId }?.name.orEmpty(),
                conflictClassId = conflictClassId,
                conflictClassName = conflictClassName,
                time = schedule.from ?: 0,
                date = schedule.from ?: 0,
                coachName = item.instructor.orEmpty(),
                address = facility?.address.orEmpty(),
                facilityName = facility?.name.orEmpty(),
                icon = facility?.icon.orEmpty(),
                credits = item.credits ?: 0,
                availableBonusCredits = bonusCredits,
                isConflictAvailable = conflictUserClass != null
            )
        }
    }
}

open class ClassCalendarState {
    object Loading : ClassCalendarState()
    data class Error(val error: Exception) : ClassCalendarState()
    data class CalendarDay(
        val dayItems: List<ClassCalendarDayViewItem>, val position: Int
    ) : ClassCalendarState()

    data class Credits(val credits: Int?, val bonusCredits: Int?) : ClassCalendarState()
    data class Position(val position: Int) : ClassCalendarState()
}