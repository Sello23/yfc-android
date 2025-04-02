package com.yourfitness.pt.ui.features.dashboard.clients

import androidx.lifecycle.viewModelScope
import com.yourfitness.pt.domain.dashboard.DashboardRepository
import com.yourfitness.pt.domain.models.ClientInductionData
import com.yourfitness.pt.domain.pt.PtRepository
import com.yourfitness.pt.network.dto.PtClientDto
import com.yourfitness.pt.network.dto.fullName
import com.yourfitness.pt.ui.features.dashboard.inductions.BaseClientsListState
import com.yourfitness.pt.ui.features.dashboard.inductions.InductionsListViewModel
import com.yourfitness.pt.ui.navigation.PtNavigation
import com.yourfitness.pt.ui.navigation.PtNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ClientsListViewModel @Inject constructor(
    navigator: PtNavigator,
    dashboardRepository: DashboardRepository,
    ptRepository: PtRepository
) : InductionsListViewModel(navigator, dashboardRepository, ptRepository) {

    var isSearchActive: Boolean = false

    override fun handleIntent(intent: Any) {
        when (intent) {
            is ClientsListIntent.Search -> searchClients(intent.query)
            is ClientsListIntent.OnClientClick -> {
                if (intent.client is PtClientDto)
                    navigator.navigate(PtNavigation.ClientDetails(intent.client))
            }
            else -> super.handleIntent(intent)
        }
    }

    override fun loadData() {
        viewModelScope.launch {
            try {
                clients = dashboardRepository.loadPtClients()
                state.postValue(BaseClientsListState.ClientsLoaded(clients))
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun searchClients(searchQuery: String) {
        viewModelScope.launch {
            try {
                val normalizedQuery = searchQuery.trim().lowercase()
                val searchedClients = clients
                    .filterIsInstance<PtClientDto>()
                    .filter {
                    it.profileInfo?.fullName?.lowercase()?.contains(normalizedQuery) == true
                }
                state.postValue(BaseClientsListState.ClientsLoaded(searchedClients))
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }
}

open class ClientsListIntent {
    data class Search(val query: String) : ClientsListIntent()
    data class OnClientClick(val client: ClientInductionData) : ClientsListIntent()
}
