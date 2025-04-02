package com.yourfitness.coach.ui.features.progress.upcoming_class

import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.data.mapper.toEntity
import com.yourfitness.coach.domain.date.formatCalendarRFC
import com.yourfitness.coach.domain.facility.FacilityRepository
import com.yourfitness.coach.domain.models.BookedClass
import com.yourfitness.coach.domain.models.CalendarBookClassData
import com.yourfitness.coach.domain.settings.SettingsManager
import com.yourfitness.coach.network.YFCRestApi
import com.yourfitness.coach.network.dto.Schedule
import com.yourfitness.coach.ui.features.facility.class_operations.ClassOperationsViewModel
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.data.entity.ProfileEntity
import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.domain.date.toMilliseconds
import com.yourfitness.common.domain.date.TimeDifference
import com.yourfitness.common.domain.date.dateTimeDifference
import com.yourfitness.common.network.CommonRestApi
import com.yourfitness.common.network.dto.ProfileResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class UpcomingClassViewModel @Inject constructor(
    override val navigator: Navigator,
    private val restApi: YFCRestApi,
    private val profileRepository: ProfileRepository,
    private val settingsManager: SettingsManager,
    val repository: FacilityRepository
) : ClassOperationsViewModel<UpcomingClassIntent, UpcomingClassState>(restApi, navigator) {

    private var classes: List<Schedule> = emptyList()
    private var facilities: List<FacilityEntity> = emptyList()

    override fun handleIntent(intent: UpcomingClassIntent) {
        when (intent) {
            UpcomingClassIntent.LoadData -> loadUpcomingClass()
        }
    }

    override fun postLoadingState() {}

    override fun postErrorState(error: Exception) {
        Timber.e(error)
    }

    override fun onClassCanceled(data: CalendarBookClassData) {
        loadUpcomingClass()
    }

    private fun loadUpcomingClass() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val profile = profileRepository.getProfile()
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, START_DATE)
                val startDate = calendar.time.formatCalendarRFC()
                calendar.add(Calendar.DAY_OF_YEAR, END_DATE)
                val endDate = calendar.time.formatCalendarRFC()
                classes = restApi.getSchedule(startDate, endDate)
                facilities = repository.fetchFacilities()
                val bookedClass = mutableListOf<BookedClass>()
                val settingsGlobal = settingsManager.getSettings()
                classes.forEach { schedule ->
                    val gym = facilities.firstOrNull { gym ->
                        gym.customClasses?.any {
                            schedule.customClassId == it.id
                        } == true
                    } ?: return@forEach
                    val instructor = gym.instructors?.firstOrNull { it.id == schedule.instructorId }
                    val fitnessClass = gym.customClasses?.firstOrNull { it.id == schedule.customClassId } ?: return@forEach
                    bookedClass.add(
                        BookedClass(
                            gym.name ?: "",
                            fitnessClass.name ?: "",
                            schedule.from ?: 0L,
                            schedule.from ?: 0L,
                            gym.address ?: "",
                            fitnessClass.type ?: "",
                            gym.icon ?: "",
                            settingsGlobal?.classEntryLeadTime ?: 5,
                            settingsGlobal?.classCancellationTime ?: 5,
                            gym.id ?: "",
                            instructor?.name ?: "",
                            schedule.id
                        )
                    )
                }
                val today = Calendar.getInstance()
                var upcomingClassTime = Int.MAX_VALUE
                var upcomingClass: BookedClass? = null
                bookedClass.forEach {
                    val difference = dateTimeDifference(it.time.toMilliseconds(), today.timeInMillis, TimeDifference.MINUTE)
                    if (difference >= (-1) * (settingsGlobal?.classEntryLeadTime ?: 15) && difference < upcomingClassTime) {
                        upcomingClassTime = difference.toInt()
                        upcomingClass = it
                    }
                }
                state.postValue(UpcomingClassState.Success(upcomingClass, profile))
            } catch (error: Exception) {
                Timber.e(error)
            }
        }
    }

    fun onRebookClick(bookedClass: BookedClass) {
        viewModelScope.launch {
            val facility = facilities.find { it.id == bookedClass.facilityId }
            val schedule = classes.find { it.id == bookedClass.classId } ?: return@launch
            val classId = facility?.customClasses
                ?.find { it.id == schedule.customClassId }?.id ?: return@launch
            navigator.navigate(
                Navigation.BookingClassCalendar(classId, bookedClass.className, facility, true, bookedClass.classId)
            )
        }
    }

    fun onCancelClick(bookedClass: BookedClass) {
        buildCancelBookData(bookedClass)
            ?.let { cancelClass(it) }
    }

    private fun buildCancelBookData(bookedClass: BookedClass): CalendarBookClassData? {
        val facility = facilities.find { it.id == bookedClass.facilityId }
        return classes.find { it.id == bookedClass.classId }?.let { schedule ->
            val facilityClass = facility?.customClasses?.find { it.id == schedule.customClassId }
            return@let CalendarBookClassData(
                scheduleId = schedule.id.orEmpty(),
                className = facilityClass?.name.orEmpty(),
                conflictClassId = "",
                conflictClassName = "",
                time = schedule.from ?: 0,
                date = schedule.from ?: 0,
                coachName = bookedClass.coachName,
                address = bookedClass.address,
                facilityName = bookedClass.facilityName,
                icon = bookedClass.icon,
                credits = facilityClass?.priceCoins ?: 0,
                availableBonusCredits = 0
            )
        }
    }

    companion object {
        private const val START_DATE = -35
        private const val END_DATE = 90
    }
}

sealed class UpcomingClassState {
    data class Success(
        val upcomingClass: BookedClass?,
        val profile: ProfileEntity
    ) : UpcomingClassState()
}

sealed class UpcomingClassIntent {
    object LoadData: UpcomingClassIntent()
}
