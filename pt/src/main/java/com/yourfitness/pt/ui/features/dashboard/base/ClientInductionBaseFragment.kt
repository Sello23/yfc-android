package com.yourfitness.pt.ui.features.dashboard.base

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.pt.databinding.FragmentClientsListBinding
import com.yourfitness.pt.domain.models.ClientInductionData
import com.yourfitness.pt.ui.features.dashboard.ClientPagerAdapter
import com.yourfitness.pt.ui.features.dashboard.inductions.BaseClientsListState
import com.yourfitness.pt.ui.features.dashboard.inductions.InductionsListViewModel
import com.yourfitness.pt.ui.features.training_calendar.adapters.CalendarCardBuilder

abstract class ClientInductionBaseFragment<AT : ClientInductionData> :
    MviFragment<Any, BaseClientsListState, InductionsListViewModel>(),
    CalendarCardBuilder {

    override val binding: FragmentClientsListBinding by viewBinding()

    protected abstract val adapter: ClientPagerAdapter<AT>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar(binding.toolbar.root)
        setupSearchView()
        binding.clientsRecyclerView.adapter = adapter
    }

    override fun renderState(state: BaseClientsListState) {
        when (state) {
            is BaseClientsListState.Loading -> showLoading(true)
            is BaseClientsListState.ClientsLoaded -> {
                showLoading(false)
                showClientsList(state.clients)
                setupViewsVisibility(state.clients)
            }
        }
    }

    private fun showClientsList(clients: List<ClientInductionData>) {
        binding.emptyState.isVisible = clients.isEmpty()
        binding.clientsRecyclerView.isVisible = clients.isNotEmpty()
        setAdapterData(clients)
    }

    abstract fun setAdapterData(clients: List<ClientInductionData>)

    abstract fun setupViewsVisibility(clients: List<ClientInductionData>)

    abstract fun setupSearchView()
}
