package com.yourfitness.pt.ui.features.calendar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yourfitness.common.data.entity.fullName
import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.domain.date.toDate
import com.yourfitness.common.domain.date.today
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.pt.data.PtStorage
import com.yourfitness.pt.data.entity.fullName
import com.yourfitness.pt.domain.calendar.SessionsRepository
import com.yourfitness.pt.domain.calendar.UserCalendarRepository
import com.yourfitness.pt.domain.models.*
import com.yourfitness.pt.domain.pt.PtRepository
import com.yourfitness.pt.domain.values.DATE
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
class CalendarViewModelPt @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val ptStorage: PtStorage,
    navigator: PtNavigator,
    repository: PtRepository,
    userCalendarRepository: UserCalendarRepository,
    sessionsRepository: SessionsRepository,
    savedState: SavedStateHandle
) : CalendarViewModel(navigator, repository, userCalendarRepository, sessionsRepository, savedState) {

    override var displayedDate = savedState.get<Long>(DATE).toDate() ?: today()
    override val multiselectEnabled = true

    override fun loadData() {
        state.value = CalendarState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val profile = profileRepository.getProfile()
                ptId = profile.id
                ptName = profile.fullName

                sessionsRepository.clearTemporarySessions()
                sessionsRepository.downloadUserSessions()

                val dayCalendarData = userCalendarRepository.generateCalendarData(displayedDate, ptId)
                state.postValue(CalendarState.PtInfoLoaded(0, ptName, dayCalendarData))
                updateStatuses()
                scheduleStatusUpdateTimer()
            } catch (e: Exception) {
                Timber.e(e)
                state.postValue(CalendarState.Error(e))
            }
        }
    }

    override fun handleIntent(intent: CalendarIntent) {
        when (intent) {
            is CalendarIntent.BookTimeSlotTapped -> saveSelectedTimeSlot()
            else -> super.handleIntent(intent)
        }
    }

    private fun saveSelectedTimeSlot() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                ptStorage.hasSelectedTime = true
                navigator.navigate(PtNavigation.PopScreen)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }
}

open class CalendarStatePt {
}

open class CalendarIntentPt {

}
