package com.yourfitness.coach.ui.features.facility.manual_search

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.tabs.TabLayout
import com.yourfitness.coach.R
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.databinding.FragmentManualSearchBinding
import com.yourfitness.coach.network.dto.Classification
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.common.CommonPreferencesStorage
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.addOnTabSelectionListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ManualSearchFragment : MviFragment<ManualSearchIntent, ManualSearchState, ManualSearchViewModel>() {

    override val binding: FragmentManualSearchBinding by viewBinding()
    override val viewModel: ManualSearchViewModel by viewModels()

    private val adapter by lazy { ManualSearchAdapter(::onItemClicked) }
    private val latLng by lazy { requireArguments().get("latLng") as LatLng }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.toolbar.root)
        binding.tabLayout.addOnTabSelectionListener { onTabSelected(it) }
        binding.toolbar.toolbarSolid.title = getString(R.string.manual_search_screen_gym_access_text)
        viewModel.intent.postValue(ManualSearchIntent.Load(Classification.GYM, requireActivity(), 0))
        binding.inputText.doOnTextChanged { text, _, _, _ -> viewModel.findGyms(text?.trim().toString()) }
    }


    private fun onTabSelected(tab: TabLayout.Tab) {
        when (tab.position) {
            0 -> viewModel.intent.postValue(ManualSearchIntent.Load(Classification.GYM, requireActivity(), 0))
            1 -> viewModel.intent.postValue(ManualSearchIntent.LoadRecentGym(GYM_RECENT, 1))
        }
    }

    override fun renderState(state: ManualSearchState) {
        when (state) {
            is ManualSearchState.Loading -> showLoading(true)
            is ManualSearchState.FacilitiesLoaded -> showItems(state)
            is ManualSearchState.Error -> showError()
        }
    }

    private fun showItems(state: ManualSearchState.FacilitiesLoaded) {
        showLoading(false)
        when {
            state.items.isEmpty() && state.type == GYM_RECENT && !state.isVisit -> {
                binding.groupManualSearchLocationError.isVisible = false
                binding.textNotVisited.isVisible = true
                binding.listItems.isVisible = false
            }
            state.items.isNotEmpty() -> {
                binding.groupManualSearchLocationError.isVisible = false
                binding.textNotVisited.isVisible = false
                binding.listItems.isVisible = true
                binding.listItems.adapter = adapter
                adapter.updateItems(state.items, state.latLng)
            }
            state.items.isEmpty() && state.type == GYM -> {
                binding.groupManualSearchLocationError.isVisible = false
                binding.textNotVisited.isVisible = false
                binding.listItems.isVisible = false
            }
            state.items.isEmpty() && state.type == GYM_RECENT && state.isVisit -> {
                binding.groupManualSearchLocationError.isVisible = false
                binding.textNotVisited.isVisible = false
                binding.listItems.isVisible = false
            }
        }
    }

    private fun onItemClicked(item: FacilityEntity) {
        viewModel.navigator.navigate(Navigation.AreYouHereRightNow(item, viewModel.profile, latLng))
    }

    private fun showError() {
        showLoading(false)
        binding.listItems.isVisible = false
        binding.groupManualSearchLocationError.isVisible = true
        binding.viewManualSearchLocationError.buttonGoToSettings.setOnClickListener {
            activity?.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            viewModel.intent.value = ManualSearchIntent.ActionButtonClicked
        }
    }

    companion object {
        const val GYM_RECENT = "Recent"
        const val GYM = "Gym"
    }
}
