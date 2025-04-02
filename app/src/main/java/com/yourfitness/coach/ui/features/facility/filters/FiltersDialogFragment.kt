package com.yourfitness.coach.ui.features.facility.filters

import android.app.Dialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogFiltersBinding
import com.yourfitness.coach.domain.facility.FILTERS_NO_LIMIT
import com.yourfitness.coach.domain.facility.Filters
import com.yourfitness.coach.domain.facility.isEmpty
import com.yourfitness.coach.network.dto.Classification
import com.yourfitness.coach.ui.features.facility.map.REQUEST_CODE_FILTERS
import com.yourfitness.common.ui.mvi.MviDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setTextColorRes
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.common.ui.utils.showChips
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import reactivecircus.flowbinding.android.widget.progressChanges
import com.yourfitness.common.R as common

@AndroidEntryPoint
class FiltersDialogFragment : MviDialogFragment<Any, Any, FiltersViewModel>() {

    override val binding: DialogFiltersBinding by viewBinding()
    override val viewModel: FiltersViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.toolbar.setOnMenuItemClickListener { onMenuItemClicked(it) }
        binding.toolbar.textTitle.text = getString(common.string.filters_screen_filters)
        binding.toolbar.buttonResetAll.isVisible = false
        binding.toolbar.buttonResetAll.setOnClickListener { onResetAllClicked() }
        binding.buttonAction.setOnClickListener { onApplyClicked() }
        binding.content.typeContainer.buttonTypesAll.setOnClickListener { onSeeAllTypesClicked() }
        binding.content.buttonAmenitiesAll.setOnClickListener { onSeeAllAmenitiesClicked() }
        binding.content.sliderDistance.progressChanges().debounce(100).onEach { onDistanceChanged(it) }.launchIn(lifecycleScope)
    }

    private fun onResetAllClicked() {
        viewModel.intent.postValue(FiltersIntent.Reset)
    }

    private fun onApplyClicked() {
        val bundle = bundleOf("filters" to viewModel.filters)
        setFragmentResult(REQUEST_CODE_FILTERS, bundle)
        dismiss()
    }

    private fun onSeeAllTypesClicked() {
        showAllItems(viewModel.filters.allTypes, viewModel.filters.types, ::onTypesChanged, common.string.map_screen_type)
    }

    private fun onSeeAllAmenitiesClicked() {
        showAllItems(
            viewModel.filters.allAmenities,
            viewModel.filters.amenities,
            ::onAmenitiesChanged,
            if (viewModel.classification == Classification.TRAINER) com.yourfitness.common.R.string.facilities
            else R.string.facility_details_amenities,
            if (viewModel.classification == Classification.TRAINER) viewModel.filters.images
            else null
        )
    }

    private fun onDistanceChanged(value: Int) {
        val isNoLimit = value == binding.content.sliderDistance.max
        val color = if (isNoLimit) common.color.black else common.color.gray_light
        binding.content.textMaxDistance.setTextColorRes(color)
        viewModel.intent.postValue(FiltersIntent.DistanceChanged(value.toFiltersValue()))
    }

    private fun onTypesChanged(selected: List<String>) {
        viewModel.intent.postValue(FiltersIntent.TypesChanged(selected))
    }

    private fun onAmenitiesChanged(selected: List<String>) {
        viewModel.intent.postValue(FiltersIntent.AmenitiesChanged(selected))
    }

    private fun onMenuItemClicked(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.close -> dismiss()
        }
        return true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.configureDialogView()
        return dialog
    }

    override fun Dialog.configureDialogView() {
        setOnShowListener { dialogInterface ->
            setupBottomInsets(binding.root)
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            lifecycleScope.launch {
                delay(150)
                bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    override fun renderState(state: Any) {
        when (state) {
            is FiltersState.Loading -> showLoading(true, R.id.content)
            is FiltersState.Loaded -> showFilters(state.filters)
            is FiltersState.Updated -> showFacilityCount(state.count)
        }
    }

    private fun showFilters(filters: Filters) {
        showLoading(false, R.id.content)
        showTypes(filters)
        showAmenities(filters)
        showResetAll(filters)
        binding.content.sliderDistance.progress = filters.distance.toSliderValue()
    }

    private fun showResetAll(filters: Filters) {
        binding.toolbar.buttonResetAll.isVisible = !filters.isEmpty() && binding.content.root.isVisible
    }

    private fun showFacilityCount(count: Int) {
        val hasFacilities = count > 0
        val text = if (hasFacilities) {
            val formatRes = when (viewModel.classification) {
                Classification.GYM -> R.plurals.filters_show_gyms_format
                Classification.TRAINER -> com.yourfitness.pt.R.plurals.show_n_trainers
                Classification.STUDIO -> R.plurals.filters_show_studios_format
            }
            resources.getQuantityString(formatRes, count, count)
        } else {
            getString(when (viewModel.classification) {
                Classification.GYM -> R.string.filters_no_matching_gyms
                Classification.TRAINER -> com.yourfitness.pt.R.string.filters_no_matching_trainers
                Classification.STUDIO -> R.string.filters_no_matching_studios
            })
        }
        binding.buttonAction.text = text
        binding.buttonAction.isEnabled = hasFacilities
        showResetAll(viewModel.filters)
        showTypes(viewModel.filters)
        showAmenities(viewModel.filters)
    }

    private fun showTypes(filters: Filters) {
        binding.content.typeContainer.groupTypes.showChips(
            layoutInflater,
            filters.allTypes,
            filters.types,
            true,
            ::onTypesChanged
        )
        binding.content.typeContainer.buttonTypesAll.isVisible = filters.allTypes.size > 10
        binding.content.typeContainer.textTypes.text =
            if (viewModel.classification == Classification.TRAINER) getString(com.yourfitness.pt.R.string.focus_areas) else when {
                filters.types.isEmpty() -> getString(common.string.map_screen_type)
                else -> getString(R.string.filters_types_format, filters.types.size)
            }
    }

    private fun showAmenities(filters: Filters) {
        binding.content.groupAmenities.showChips(
            layoutInflater,
            filters.allAmenities,
            filters.amenities,
            true,
            ::onAmenitiesChanged,
            if (viewModel.classification == Classification.TRAINER) viewModel.filters.images
            else null
        )
        binding.content.buttonAmenitiesAll.isVisible = filters.allAmenities.size > 10
        binding.content.textAmenities.text =
            if (viewModel.classification == Classification.TRAINER) getString(common.string.facilities)
            else when {
                filters.amenities.isEmpty() -> getString(R.string.facility_details_amenities)
                else -> getString(R.string.filters_amenities_format, filters.amenities.size)
            }
    }

    private fun showAllItems(
        items: List<String>,
        selected: List<String>,
        onCheckedChanged: (selected: List<String>) -> Unit,
        @StringRes title: Int,
        images: List<String>? = null
    ) {
        binding.layoutAllItems.root.isVisible = true
        binding.layoutAllItems.groupItemsAll.showChips(layoutInflater, items, selected, false, onCheckedChanged, images)
        binding.content.root.isVisible = false
        binding.toolbar.textTitle.text = getString(title)
        binding.toolbar.buttonResetAll.isVisible = false
        binding.toolbar.toolbar.setNavigationIcon(common.drawable.ic_back_button)
        binding.toolbar.toolbar.setNavigationOnClickListener {
            binding.layoutAllItems.root.isVisible = false
            binding.layoutAllItems.groupItemsAll.removeAllViews()
            binding.content.root.isVisible = true
            binding.toolbar.textTitle.text = getString(common.string.filters_screen_filters)
            binding.toolbar.toolbar.navigationIcon = null
            binding.toolbar.toolbar.setNavigationOnClickListener(null)
            showFilters(viewModel.filters)
        }
    }

    private fun Int.toSliderValue(): Int {
        return if (this > 0) this else FILTERS_NO_LIMIT
    }

    private fun Int.toFiltersValue(): Int {
        return if (this >= FILTERS_NO_LIMIT) FILTERS_NO_LIMIT else this
    }
}
