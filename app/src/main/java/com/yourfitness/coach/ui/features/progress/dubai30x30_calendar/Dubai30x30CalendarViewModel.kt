package com.yourfitness.coach.ui.features.progress.dubai30x30_calendar

import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.data.entity.WorkoutEntity
import com.yourfitness.coach.domain.date.millisToLocalDate
import com.yourfitness.coach.domain.progress.workout.WorkoutManager
import com.yourfitness.coach.domain.settings.SettingsManager
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.domain.date.formatDateToISO
import com.yourfitness.common.domain.date.toDate
import com.yourfitness.common.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class Dubai30x30CalendarViewModel @Inject constructor(
    private val navigator: Navigator,
    private val workoutManager: WorkoutManager,
    private val settingsManager: SettingsManager
) : MviViewModel<Dubai30x30CalendarIntent, Dubai30x30CalendarState>() {

    private val uiWorkouts = mutableListOf<WorkoutCalendarItem>()

    init {
        loadData()
    }

    override fun handleIntent(intent: Dubai30x30CalendarIntent) {
        when (intent) {
            is Dubai30x30CalendarIntent.DaySelected -> addDay(intent.day)
            is Dubai30x30CalendarIntent.DayUnselected -> removeDay(intent.day)
            is Dubai30x30CalendarIntent.SaveChanges -> saveChanges()
        }
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var settings = settingsManager.getSettings()

                val savedWorkouts = workoutManager.getSavedWorkouts().toUiData()
                val savedStartDate = settings?.dubai3030ChallengeStartDate
                val savedEndDate = settings?.dubai3030ChallengeEndDate
                state.postValue(Dubai30x30CalendarState.Loaded(savedWorkouts, savedStartDate, savedEndDate))

                val workouts = workoutManager.fetchWorkouts()
                uiWorkouts.clear()
                uiWorkouts.addAll(workouts.toUiData())

                if (settings?.dubai3030ChallengeStartDate == null || settings.dubai3030ChallengeEndDate == null) {
                    settings = settingsManager.getSettings(true)
                }
                val startDate = settings?.dubai3030ChallengeStartDate
                val endDate = settings?.dubai3030ChallengeEndDate
                state.postValue(Dubai30x30CalendarState.Loaded(uiWorkouts, startDate,  endDate, true))
            } catch (error: Exception) {
                state.postValue(Dubai30x30CalendarState.Error(error))
                Timber.e(error)
            }
        }
    }

    private fun List<WorkoutEntity>.toUiData(): List<WorkoutCalendarItem> {
        return map {
            WorkoutCalendarItem(
                it.trackedAt.millisToLocalDate() ?: LocalDate.now(),
                it.manual
            )
        }.distinctBy { it.day.atStartOfDay() }
    }

    private fun addDay(day: LocalDate) {
        viewModelScope.launch {
            uiWorkouts.add(WorkoutCalendarItem(day))
            state.postValue(Dubai30x30CalendarState.DayUpdated(uiWorkouts, day))
        }
    }

    private fun removeDay(day: LocalDate) {
        viewModelScope.launch {
            uiWorkouts.removeIf { it.day.year == day.year && it.day.dayOfYear == day.dayOfYear }
            state.postValue(Dubai30x30CalendarState.DayUpdated(uiWorkouts, day))
        }
    }

    private fun saveChanges() {
        viewModelScope.launch {
            try {
                val savedWorkouts = workoutManager.getSavedWorkouts().filter { it.manual }
                val savedWorkoutsDates = savedWorkouts.mapNotNull { it.trackedAt.millisToLocalDate()?.atStartOfDay() }

                val newWorkouts = uiWorkouts.filter {
                    it.manual && !savedWorkoutsDates.contains(it.day.atStartOfDay())
                }
                if (newWorkouts.isNotEmpty()) {
                    val uploadedWorkouts = workoutManager.uploadNewWorkouts(newWorkouts.map {
                        it.day.toDate().formatDateToISO()
                    })
                    workoutManager.saveWorkouts(uploadedWorkouts)
                }

                val uiWorkoutDates = uiWorkouts.map { it.day.atStartOfDay() }
                val workoutsToDelete = savedWorkouts.filter {
                    it.manual && !uiWorkoutDates.contains(it.trackedAt.millisToLocalDate()?.atStartOfDay())
                }
                if (workoutsToDelete.isNotEmpty()) {
                    val ids = workoutsToDelete.map { it.id }
                    workoutManager.uploadDeletedWorkouts(ids.joinToString(","))
                    workoutManager.deleteByIds(ids)
                }

                navigator.navigate(Navigation.WorkoutsUpdated)
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(Dubai30x30CalendarState.Error(error))
            }
        }
    }
}

sealed class Dubai30x30CalendarIntent {
    data class DaySelected(val day: LocalDate) : Dubai30x30CalendarIntent()
    data class DayUnselected(val day: LocalDate) : Dubai30x30CalendarIntent()
    object SaveChanges : Dubai30x30CalendarIntent()
}

sealed class Dubai30x30CalendarState {
    data class Loaded(
        val workouts: List<WorkoutCalendarItem>,
        val startDate: Long?,
        val endDate: Long?,
        val actual: Boolean = false
    ) : Dubai30x30CalendarState()
    data class DayUpdated(
        val workouts: List<WorkoutCalendarItem>,
        val day: LocalDate
    ) : Dubai30x30CalendarState()

    data class Error(val error: Exception) : Dubai30x30CalendarState()
}
