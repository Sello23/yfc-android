package com.yourfitness.coach.ui.features.facility.list

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayout
import com.yourfitness.coach.R
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.databinding.FragmentListBinding
import com.yourfitness.coach.domain.facility.Filters
import com.yourfitness.coach.domain.facility.isEmpty
import com.yourfitness.coach.network.dto.Classification
import com.yourfitness.coach.ui.features.bottom_menu.showBottomNavigationItem
import com.yourfitness.coach.ui.features.facility.map.REQUEST_CODE_FILTERS
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.utils.setupMapInsets
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.addOnTabSelectionListener
import com.yourfitness.common.ui.utils.selectTab
import com.yourfitness.common.ui.utils.setupTopInsets
import com.yourfitness.pt.ui.features.list.PtListFragment
import com.yourfitness.pt.ui.features.list.PtListIntent
import dagger.hilt.android.AndroidEntryPoint
import com.yourfitness.common.R as common
import com.yourfitness.pt.R as pt

@AndroidEntryPoint
class ListFragment : MviFragment<Any, Any, ListViewModel>() {

    override val binding: FragmentListBinding by viewBinding()
    override val viewModel: ListViewModel by viewModels()

    private val ptListFragment: PtListFragment? by lazy {
//        PtListFragment.newInstance()
        childFragmentManager.findFragmentById(R.id.pt_list_items) as PtListFragment?
    }

//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        requireActivity().supportFragmentManager
//            .beginTransaction()
//            .replace(R.id.pt_list_items, ptListFragment)
//            .commit()
//        return super.onCreateView(inflater, container, savedInstanceState)
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.listItems.isVisible = false
//        ptListFragment?.setVisibility(false)
//        showBottomNavigationItem(R.id.fragment_map)
        setupScreenInsets()
        showTab(viewModel.classification)
        showFiltersButton(viewModel.filters)
        binding.toolbar.toolbar.setNavigationOnClickListener { viewModel.navigator.navigate(Navigation.Search(selectedTab())) }
        binding.toolbar.tabLayout.addOnTabSelectionListener { onTabSelected(it) }
        binding.buttonMap.isVisible = false
        binding.buttonMap.setOnClickListener { viewModel.navigator.navigate(Navigation.Map(viewModel.classification, viewModel.filters)) }
        binding.toolbar.toolbar.setOnMenuItemClickListener { onMenuItemClicked(it) }
        viewModel.intent.value = ListIntent.ScreenOpened
    }

    override fun onStart() {
        super.onStart()
        ptListFragment?.setVisibility(false)
        showBottomNavigationItem(R.id.fragment_map)
    }

    private fun setupScreenInsets() {
        setupTopInsets(binding.toolbar.root)
        binding.listContainer.setupMapInsets(binding.toolbar.toolbar)
    }

    private fun onMenuItemClicked(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.filters -> openFilters()
        }
        return true
    }

    private fun openFilters() {
        setFragmentResultListener(REQUEST_CODE_FILTERS) { key, bundle ->
            if (key == REQUEST_CODE_FILTERS) {
                val filters = bundle["filters"] as Filters
                viewModel.intent.postValue(ListIntent.UpdateFilters(filters))
                showFiltersButton(filters)
            }
            clearFragmentResultListener(REQUEST_CODE_FILTERS)
        }
        viewModel.navigator.navigate(Navigation.Filter(selectedTab(), viewModel.filters))
    }

    private fun showFiltersButton(filters: Filters = Filters()) {
        val icon = if (filters.isEmpty()) common.drawable.ic_toolbar_filter else common.drawable.ic_toolbar_filter_selected
        val filtersItem = binding.toolbar.toolbar.menu.findItem(R.id.filters)
        filtersItem.setIcon(icon)
    }

    private fun onTabSelected(tab: TabLayout.Tab) {
        showFiltersButton()
        when (tab.position) {
            0 -> viewModel.intent.postValue(ListIntent.Load(Classification.GYM))
            1 -> {
                viewModel.intent.postValue(ListIntent.Load(Classification.TRAINER))
                ptListFragment?.viewModel?.intent?.postValue(PtListIntent.Load)
            }
            2 -> viewModel.intent.postValue(ListIntent.Load(Classification.STUDIO))
        }
    }

    override fun renderState(state: Any) {
        when (state) {
            is ListState.Loading -> showLoading(true)
            is ListState.FacilitiesLoaded -> showItems(state)
            is ListState.Error -> showError(state.error)
            is ListState.UpdatePtExpandedStates -> ptListFragment?.updatePtExpandedStates(state.expandedStates, state.pos)
        }
    }

    private fun showItems(state: ListState.FacilitiesLoaded) {
        val items = state.items

        showViewButton(items.size, state.ptItems.size)
        showLoading(false)
        if (viewModel.classification == Classification.TRAINER) {
            binding.listItems.isVisible = false
            ptListFragment?.showItems(state.ptItems, state.expandedStates, ::onPtExpandedClicked)
            ptListFragment?.setVisibility(true)
        } else {
            ptListFragment?.setVisibility(false)
            binding.listItems.adapter = FacilitiesAdapter(items, selectedTab(), state.location, ::onItemClicked)
            binding.listItems.post {
                binding.listItems.isVisible = true
                showLoading(false)
            }
        }
    }

    private fun showTab(classification: Classification) {
        val position = when (classification) {
            Classification.GYM -> 0
            Classification.TRAINER -> 1
            Classification.STUDIO -> 2
        }
        binding.toolbar.tabLayout.selectTab(position)
    }

    private fun onItemClicked(item: FacilityEntity) {
        viewModel.navigator.navigate(Navigation.FacilityDetails(item, selectedTab()))
    }

    private fun onPtExpandedClicked(id: String, pos: Int) {
        viewModel.intent.value = ListIntent.UpdatePtExpandedState(id, pos)
    }

    private fun showViewButton(count: Int, ptCount: Int) {
        val itemCountFormat = when (binding.toolbar.tabLayout.selectedTabPosition) {
            0 -> R.string.map_screen_gyms_count_format
            1 -> R.string.map_screen_trainers_count_format
            else -> R.string.map_screen_studios_count_format
        }
        binding.buttonMap.isVisible = true
        val countStringFormatted = if (binding.toolbar.tabLayout.selectedTabPosition == 1) {
            if (ptCount == 0) {
                binding.buttonMap.text = getString(pt.string.no_trainers_msg)
                return
            }
            getString(itemCountFormat, count, ptCount)
        } else {
            getString(itemCountFormat, count)
        }
        binding.buttonMap.text = getString(
            R.string.map_screen_view_button_format,
            getString(R.string.map_screen_map_view),
            countStringFormatted
        )
    }

    private fun selectedTab(): Classification {
        return when (binding.toolbar.tabLayout.selectedTabPosition) {
            0 -> Classification.GYM
            1 -> Classification.TRAINER
            else -> Classification.STUDIO
        }
    }
}
