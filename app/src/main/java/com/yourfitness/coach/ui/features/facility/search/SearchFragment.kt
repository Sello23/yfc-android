package com.yourfitness.coach.ui.features.facility.search

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.model.LatLng
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.network.dto.Classification
import com.yourfitness.coach.ui.features.facility.map.REQUEST_CODE_COMMON
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.common.R
import com.yourfitness.common.databinding.FragmentSearchBinding
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.pt.data.entity.PersonalTrainerEntity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import reactivecircus.flowbinding.android.widget.textChanges

@AndroidEntryPoint
class SearchFragment : MviFragment<Any, Any, SearchViewModel>() {

    override val binding: FragmentSearchBinding by viewBinding()
    override val viewModel: SearchViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            binding.toolbar.editSearch.textChanges().debounce(100).collect { onQueryChanged(it) }
        }
        binding.toolbar.buttonCancel.setOnClickListener {
            setFragmentResult(REQUEST_CODE_COMMON, bundleOf())
            findNavController().navigateUp()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            setFragmentResult(REQUEST_CODE_COMMON, bundleOf())
            findNavController().navigateUp()
        }
    }

    private fun onQueryChanged(query: CharSequence) {
        viewModel.intent.postValue(SearchIntent.Search(query.toString()))
    }

    override fun renderState(state: Any) {
        when (state) {
            is SearchState.Result -> showItems(state.items, state.ptItems, state.location, state.query)
            is SearchState.Error -> showError(state.error)
        }
    }

    private fun showItems(
        items: List<FacilityEntity>,
        ptItems: List<PersonalTrainerEntity>,
        location: LatLng,
        query: String
    ) {
        showLoading(false)
        val isEmpty = if (viewModel.classification == Classification.TRAINER) {
            binding.textEmpty.text = getString(R.string.map_screen_no_pt_found)
            ptItems.isEmpty()
        } else {
            items.isEmpty()
        }
        if (!isEmpty) {
            binding.listItems.adapter = if (viewModel.classification == Classification.TRAINER) {
                PtSearchAdapter(ptItems, query, ::onPtItemClicked)
            } else {
                SearchAdapter(items, query, location, ::onItemClicked)
            }
        }
        binding.listItems.isVisible = !isEmpty
        binding.textEmpty.isVisible = isEmpty
    }

    private fun onItemClicked(item: FacilityEntity) {
        viewModel.navigator.navigate(Navigation.FacilityDetails(item, viewModel.classification))
    }

    private fun onPtItemClicked(item: PersonalTrainerEntity) {
        viewModel.intent.value = SearchIntent.PtDetailsTapped(item.id.orEmpty())
    }
}
