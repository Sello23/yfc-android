package com.yourfitness.pt.ui.features.dashboard.inductions

import androidx.lifecycle.viewModelScope
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.pt.domain.dashboard.DashboardRepository
import com.yourfitness.pt.domain.models.ClientInductionData
import com.yourfitness.pt.domain.models.InductionInfo
import com.yourfitness.pt.domain.pt.PtRepository
import com.yourfitness.pt.ui.navigation.PtNavigation
import com.yourfitness.pt.ui.navigation.PtNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
open class InductionsListViewModel @Inject constructor(
    protected val navigator: PtNavigator,
    protected val dashboardRepository: DashboardRepository,
    protected val ptRepository: PtRepository
) : MviViewModel<Any, BaseClientsListState>() {

    protected var clients = emptyList<ClientInductionData>()

    init {
        state.value = BaseClientsListState.Loading
        loadData()
    }

    override fun handleIntent(intent: Any) {
        when (intent) {
            is InductionListIntent.OnInductionClick -> {
                if (intent.client is InductionInfo)
                    navigator.navigate(PtNavigation.CompleteInduction(intent.client))
            }
            is InductionListIntent.Refresh -> loadData()
        }
    }

    protected open fun loadData() {
        viewModelScope.launch {
            try {
                val inductions = dashboardRepository.loadPtInductions()
                clients = ptRepository.mapInductionsToFacilities(inductions)
                state.postValue(BaseClientsListState.ClientsLoaded(clients))
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }
}

open class BaseClientsListState {
    object Loading : BaseClientsListState()
    data class ClientsLoaded(
        val clients: List<ClientInductionData>
    ) : BaseClientsListState()
}

open class InductionListIntent {
    data class OnInductionClick(val client: ClientInductionData) : InductionListIntent()
    object Refresh : InductionListIntent()
}