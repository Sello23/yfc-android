package com.yourfitness.pt.ui.features.calendar.book

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yourfitness.common.domain.date.addMs
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.pt.domain.calendar.SessionsRepository
import com.yourfitness.pt.domain.calendar.UserCalendarRepository
import com.yourfitness.pt.domain.calendar.UserCalendarRepository.Companion.minSlotsInSession
import com.yourfitness.pt.domain.calendar.UserCalendarRepository.Companion.timeSlotDurationMs
import com.yourfitness.pt.domain.models.FacilityInfo
import com.yourfitness.pt.domain.pt.PtRepository
import com.yourfitness.pt.domain.values.PT_ID
import com.yourfitness.pt.domain.values.PT_NAME
import com.yourfitness.pt.domain.values.SESSION_DATE
import com.yourfitness.pt.domain.values.SESSION_LIST
import com.yourfitness.pt.ui.navigation.PtNavigation
import com.yourfitness.pt.ui.navigation.PtNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@HiltViewModel
class BookTimeSlotViewModel @Inject constructor(
    private val navigator: PtNavigator,
    private val sessionsRepository: SessionsRepository,
    private val ptRepository: PtRepository,
    savedState: SavedStateHandle
) : MviViewModel<BookTimeSlotIntent, BookTimeSlotState>() {

    private val ptId = savedState.get<String>(PT_ID).orEmpty()
    private val ptName = savedState.get<String>(PT_NAME).orEmpty()
    private val sessionDate = savedState.get<Date>(SESSION_DATE) ?: Date()
    val facilities: MutableList<FacilityInfo> = (savedState.get<List<FacilityInfo>>(SESSION_LIST)?.toMutableList() ?: mutableListOf())
        .also { items ->
            items.sortByDescending { it.workTimeData?.isAccessible == true }
        }

    private var selectedFacilityId: String? = null
    private var selectedFacilityPos: Int? = null
    private var currentStep = BookStep.SELECT

    override fun handleIntent(intent: BookTimeSlotIntent) {
        when (intent) {
            is BookTimeSlotIntent.CellClicked -> {
                val prevItemPos = selectedFacilityPos
                selectedFacilityPos = intent.itemPosition
                selectedFacilityId = intent.selectedId
                state.value = BookTimeSlotState.SelectedItemUpdated(
                    selectedFacilityId,
                    prevItemPos,
                    intent.itemPosition
                )
            }
            is BookTimeSlotIntent.ActionButtonClicked -> {
                if (currentStep == BookStep.CONFIRM) {
                    confirmBooking()
                } else {
                    currentStep = BookStep.CONFIRM
                    loadConfirmData()
                }
            }
        }
    }

    private fun loadConfirmData() {
        viewModelScope.launch {
            try {
                val selectedFacility = facilities.find { it.id == selectedFacilityId } ?: return@launch
                state.value = BookTimeSlotState.ConfirmStep(
                    ptName,
                    sessionDate,
                    sessionDate.addMs(minSlotsInSession * timeSlotDurationMs),
                    selectedFacility.name,
                    selectedFacility.address,
                    selectedFacility.image
                )
            } catch (e: Exception) {
                Timber.e(e)
                state.postValue(BookTimeSlotState.Error(e))
            }
        }
    }

    private fun confirmBooking() {
        state.value = BookTimeSlotState.Loading
        viewModelScope.launch {
            try {
                sessionsRepository.createBooking(
                    ptId,
                    selectedFacilityId!!,
                    sessionDate,
                    sessionDate.addMs(minSlotsInSession * timeSlotDurationMs),
                )
                ptRepository.updatePtBalance(ptId, -1)
                navigator.navigate(PtNavigation.BookingSuccess)
            } catch (e: Exception) {
                Timber.e(e)
                state.postValue(BookTimeSlotState.Error(e))
                navigator.navigate(PtNavigation.BookingError)
            }
        }
    }
}

open class BookTimeSlotState {
    object Loading : BookTimeSlotState()
    data class Error(val error: Exception) : BookTimeSlotState()
    data class SelectedItemUpdated(
        val id: String?,
        val prevPos: Int?,
        val curPos: Int
    ) : BookTimeSlotState()
    data class ConfirmStep(
        val ptName: String,
        val startDate: Date,
        val endDate: Date,
        val facilityName: String,
        val address: String,
        val logo: String
    ) : BookTimeSlotState()
}

open class BookTimeSlotIntent {
    data class CellClicked(
        val selectedId: String,
        val itemPosition: Int
    ) : BookTimeSlotIntent()
    object ActionButtonClicked : BookTimeSlotIntent()
}

enum class BookStep {
    SELECT,
    CONFIRM
}
