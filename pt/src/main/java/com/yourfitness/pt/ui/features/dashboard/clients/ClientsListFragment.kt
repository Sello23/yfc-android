package com.yourfitness.pt.ui.features.dashboard.clients

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.yourfitness.common.R
import com.yourfitness.pt.domain.models.ClientInductionData
import com.yourfitness.pt.domain.models.InductionInfo
import com.yourfitness.pt.network.dto.PtClientDto
import com.yourfitness.pt.ui.features.dashboard.ClientPagerAdapter
import com.yourfitness.pt.ui.features.dashboard.base.ClientInductionBaseFragment
import com.yourfitness.pt.ui.features.dashboard.inductions.InductionsListFragment
import com.yourfitness.pt.ui.features.training_calendar.adapters.CalendarCardBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import reactivecircus.flowbinding.android.widget.queryTextChanges
import com.yourfitness.pt.R as pt

@AndroidEntryPoint
class ClientsListFragment : ClientInductionBaseFragment<PtClientDto>(), CalendarCardBuilder {

    override val viewModel: ClientsListViewModel by viewModels()

    override val adapter by lazy { ClientPagerAdapter<PtClientDto>(false, onClick = ::onClientClick) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.root.title = getString(pt.string.list_of_clients)
    }

    override fun setupSearchView() {
        binding.toolbar.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.search) {
                viewModel.isSearchActive = true
                configureSearchMode(true)
                binding.toolbar.root.menu.findItem(R.id.search).isVisible = false
                binding.toolbar.root.invalidateMenu()
                binding.toolbar.root.title = ""
                binding.toolbar.searchView.isVisible = true
                with(binding.toolbar.searchView) {
                    requestFocus()
                    isIconifiedByDefault = true
                    isFocusable = true
                    isIconified = false
                    requestFocusFromTouch()
                }
            }

            true
        }

        binding.toolbar.searchView.setOnCloseListener { onSearchClose() }
        binding.toolbar.buttonCancel.setOnClickListener { onSearchClose() }

        lifecycleScope.launch {
            binding.toolbar.searchView.queryTextChanges().debounce(100)
                .collect { onQueryChanged(it) }
        }
    }

    override fun setAdapterData(clients: List<ClientInductionData>) {
        adapter.setData(clients.filterIsInstance<PtClientDto>())
        binding.clientsRecyclerView.post {
            adapter.notifyDataSetChanged()
        }
    }

    private fun onSearchClose(): Boolean {
        viewModel.isSearchActive = false
        configureSearchMode(false)
        binding.toolbar.root.menu.findItem(R.id.search).isVisible = true
        binding.toolbar.root.invalidateMenu()
        binding.toolbar.root.title = getString(pt.string.list_of_clients)
        binding.toolbar.searchView.isVisible = false
        onQueryChanged("")
        return true
    }

    private fun onQueryChanged(query: CharSequence) {
        if (!viewModel.isSearchActive) return
        viewModel.intent.postValue(ClientsListIntent.Search(query.toString()))
    }

    private fun dismissToolbarNavigation(toolbar: Toolbar) {
        toolbar.navigationIcon = null
    }

    override fun setupViewsVisibility(clients: List<ClientInductionData>) {
        binding.textResults.isVisible = viewModel.isSearchActive && clients.isNotEmpty()
        binding.textResults.text = getString(R.string.map_screen_results_format, clients.size)
    }

    private fun onClientClick(client: PtClientDto) {
        viewModel.intent.value = ClientsListIntent.OnClientClick(client)
    }

    private fun configureSearchMode(enabled: Boolean) {
        if (enabled) {
            dismissToolbarNavigation(binding.toolbar.root)
        } else {
            setupToolbar(binding.toolbar.root)
        }

        binding.clientsRecyclerView.background = ContextCompat.getDrawable(
            requireContext(),
            if (enabled) R.color.white else R.color.gray_background
        )
        binding.textResults.isVisible = enabled
        binding.toolbar.searchContainer.isVisible = enabled
        adapter.setSearchMode(enabled)
        adapter.notifyDataSetChanged()
    }
}
