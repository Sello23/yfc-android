package com.yourfitness.pt.ui.features.dashboard

import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.yourfitness.common.domain.models.CalendarView
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.pt.domain.calendar.SessionsRepository
import com.yourfitness.pt.domain.calendar.actions.SessionActionManager
import com.yourfitness.pt.domain.dashboard.DashboardRepository
import com.yourfitness.pt.domain.models.ClientInductionData
import com.yourfitness.pt.domain.models.InductionInfo
import com.yourfitness.pt.domain.pt.PtRepository
import com.yourfitness.pt.network.dto.PtClientDto
import com.yourfitness.pt.ui.navigation.PtNavigation
import com.yourfitness.pt.ui.navigation.PtNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PtDashboardViewModel @Inject constructor(
    private val navigator: PtNavigator,
    private val dashboardRepository: DashboardRepository,
    private val ptRepository: PtRepository,
    private val sessionsRepository: SessionsRepository,
    private val actionManager: SessionActionManager
) : MviViewModel<DashboardIntent, DashboardState>() {

    override fun handleIntent(intent: DashboardIntent) {
        when (intent) {
            is DashboardIntent.GoToCalendarClicked -> {
                navigator.navigate(PtNavigation.TrainingCalendarPt(intent.sessionId))
            }
            is DashboardIntent.Refresh -> loadData()
            is DashboardIntent.OnSessionActionClicked -> onSessionActionClicked(intent.sessionId, intent.status)
            is DashboardIntent.SeeALllClientsClicked -> navigator.navigate(PtNavigation.ClientsList)
            is DashboardIntent.SeeALllInductionsClicked -> navigator.navigate(PtNavigation.InductionsList)
            is DashboardIntent.ClientCardClicked -> {
//                navigator.navigate(PtNavigation.ClientDetails(intent.client))
            }
            is DashboardIntent.InductionCardClicked -> {
                if (intent.client is InductionInfo) {
                    navigator.navigate(PtNavigation.CompleteInduction2(Gson().toJson(intent.client)))
                }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                Timber.tag("http -->").e("PtDashboardViewModel")
                sessionsRepository.downloadUserSessions()
                val summary = dashboardRepository.loadDashboardSummary()
                val data = listOf(
                    (summary?.conductedSessions ?: 0) to "",
                    (summary?.totalClients ?: 0) to "",
                    (summary?.commissionPercentage ?: 0) to "",
                    (summary?.totalCommissions ?: 0) to summary?.currency.orEmpty(),
                )

                val latestRequests = dashboardRepository.getLatestRequests()
                val latestRequestsObjects = ptRepository.mapSessionsToCompleteDataObject(latestRequests)
                val nextAppointments = dashboardRepository.getNextAppointments()
                val nextAppointmentsObjects = ptRepository.mapSessionsToCompleteDataObject(nextAppointments)

                val clients = dashboardRepository.loadPtClients()
                val inductions = dashboardRepository.loadPtInductions()
                val inductionsObjects = ptRepository.mapInductionsToFacilities(inductions)

                state.postValue(
                    DashboardState.DashboardDataLoaded(
                        data,
                        latestRequestsObjects,
                        nextAppointmentsObjects,
                        clients,
                        inductionsObjects
                    )
                )
            } catch (e: Exception) {
                Timber.e(e)
                state.postValue(DashboardState.Error)
            }
        }
    }

    private fun onSessionActionClicked(sessionId: String, status: String) {
        viewModelScope.launch {
            try {
                actionManager.startSessionAction(sessionId, status)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }
}

open class DashboardState {
    object Loading : DashboardState()
    object Error : DashboardState()
    data class DashboardDataLoaded(
        val data: List<Pair<Int, String>>,
        val latestRequests: List<CalendarView.CalendarItem>,
        val nextAppointments: List<CalendarView.CalendarItem>,
        val clients: List<PtClientDto>,
        val inductions: List<InductionInfo>
    ) : DashboardState()
}

open class DashboardIntent {
    data class GoToCalendarClicked(val sessionId: String?) : DashboardIntent()
    data class OnSessionActionClicked(
        val sessionId: String,
        val status: String
    ) : DashboardIntent()
    object Refresh : DashboardIntent()
    object SeeALllClientsClicked : DashboardIntent()
    object SeeALllInductionsClicked : DashboardIntent()
    data class ClientCardClicked(val client: ClientInductionData) : DashboardIntent()
    data class InductionCardClicked(val client: ClientInductionData) : DashboardIntent()
}