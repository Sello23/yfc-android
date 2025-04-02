package com.yourfitness.pt.ui.features.calendar.confirm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.common.ui.navigation.CommonNavigation
import com.yourfitness.common.ui.navigation.CommonNavigator
import com.yourfitness.pt.data.entity.fullName
import com.yourfitness.pt.domain.calendar.SessionsRepository
import com.yourfitness.pt.domain.pt.PtRepository
import com.yourfitness.pt.domain.values.SESSION_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ProcessSessionConfirmViewModel @Inject constructor(
    private val sessionsRepository: SessionsRepository,
    private val ptRepository: PtRepository,
    private val commonNavigator: CommonNavigator,
    savedState: SavedStateHandle
) : MviViewModel<ConfirmSessionIntent, ConfirmSessionState>() {

    private val sessionId = savedState.get<String>(SESSION_ID).orEmpty()

    init {
        loadData()
    }

    override fun handleIntent(intent: ConfirmSessionIntent) {
        when (intent) {
            is ConfirmSessionIntent.ActionButtonClicked -> confirmBooking()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val session = sessionsRepository.getSession(sessionId) ?: return@launch
                val ptInfo = ptRepository.getPtById(session.personalTrainerId) ?: return@launch
                val facility = ptRepository.getFacilityById(session.facilityId) ?: return@launch
                state.postValue(
                    ConfirmSessionState.DataLoaded(
                        ptInfo.fullName,
                        Date(session.from),
                        Date(session.to),
                        facility.first,
                        facility.third,
                        facility.second
                    )
                )
            } catch (e: Exception) {
                Timber.e(e)
                state.postValue(ConfirmSessionState.Error(e))
            }
        }
    }

    private fun confirmBooking() {
        state.value = ConfirmSessionState.Loading
        viewModelScope.launch {
            try {
                sessionsRepository.confirmSessionCompleted(sessionId)
                state.postValue(ConfirmSessionState.Confirmed)
            } catch (e: Exception) {
                Timber.e(e)
                commonNavigator.navigate(CommonNavigation.CommonError)
            }
        }
    }
}

open class ConfirmSessionState {
    object Loading : ConfirmSessionState()
    data class Error(val error: Exception) : ConfirmSessionState()
    data class DataLoaded(
        val ptName: String,
        val startDate: Date,
        val endDate: Date,
        val facilityName: String,
        val address: String,
        val logo: String
    ) : ConfirmSessionState()
    object Confirmed : ConfirmSessionState()
}

open class ConfirmSessionIntent {
    object ActionButtonClicked : ConfirmSessionIntent()
}
