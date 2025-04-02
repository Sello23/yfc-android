package com.yourfitness.shop.ui.features.catalog.filters

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Paint
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.slider.RangeSlider
import com.yourfitness.common.ui.mvi.MviDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.*
import com.yourfitness.shop.R
import com.yourfitness.shop.databinding.DialogProductFiltersBinding
import com.yourfitness.shop.domain.products.ProductFilters
import com.yourfitness.shop.ui.features.catalog.REQUEST_CODE_PRODUCT_FILTERS
import dagger.hilt.android.AndroidEntryPoint
import com.yourfitness.common.R as common


const val REQUEST_CODE_ENTER_RANGE = "58"

@AndroidEntryPoint
open class ProductFiltersDialogFragment : MviDialogFragment<Any, Any, ProductFiltersViewModel>() {

    override val binding: DialogProductFiltersBinding by viewBinding()
    override val viewModel: ProductFiltersViewModel by viewModels()

    protected open val corrector = 0.01f

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        setupBottomInsets(binding.root)
        binding.toolbar.toolbar.setOnMenuItemClickListener { onMenuItemClicked(it) }
        binding.toolbar.textTitle.text = getString(common.string.filters_screen_filters)
        binding.toolbar.buttonResetAll.isVisible = false
        binding.toolbar.buttonResetAll.setOnClickListener { onResetAllClicked() }
        binding.buttonAction.setOnClickListener { onApplyClicked() }
        binding.content.limitsRange.setOnClickListener { openEnterRange() }
        binding.categoryContainer.apply {
            buttonTypesAll.setOnClickListener { onSeeAllCategoriesClicked() }
            textTypes.text = getString(R.string.filters_categories)
            textTypes.setTextAppearance(
                requireContext(),
                com.yourfitness.common.R.style.TextAppearance_YFC_Subheading7
            )
        }
        binding.brandContainer.apply {
            buttonTypesAll.setOnClickListener { onSeeAllBrandsClicked() }
            textTypes.text = getString(R.string.filters_brands)
            textTypes.setTextAppearance(
                requireContext(),
                com.yourfitness.common.R.style.TextAppearance_YFC_Subheading7
            )
        }
        binding.genderContainer.apply {
            buttonTypesAll.setOnClickListener { onSeeAllGendersClicked() }
            textTypes.text = getString(R.string.filters_gender)
            textTypes.setTextAppearance(
                requireContext(),
                com.yourfitness.common.R.style.TextAppearance_YFC_Subheading7
            )
        }
        setupRangeListener()

        (dialog as BottomSheetDialog).onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (viewModel.showAll || viewModel.showAllBrands || viewModel.showAllGenders) {
                viewModel.intent.value = FiltersIntent.ResetSeeAll
            } else {
                findNavController().navigateUp()
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.configureDialogView()
        return dialog
    }

    private fun setupRangeListener() {
        binding.content.sliderRange.addOnSliderTouchListener(object :
            RangeSlider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: RangeSlider) {}

            override fun onStopTrackingTouch(slider: RangeSlider) {
                onStopTrackingTouch()
            }
        })
    }

    protected open fun onStopTrackingTouch() {
        val values = binding.content.sliderRange.values
        val min = values.first().toLong()
        val max = values.last().toLong()
        setupLimitRangeLabel(min, max, binding.content.sliderRange.valueTo.toLong())
        viewModel.intent.value = FiltersIntent.RangeChanged(min, max)
    }

    private fun onResetAllClicked() {
        viewModel.intent.postValue(FiltersIntent.Reset)
    }

    private fun onApplyClicked() {
        val bundle = bundleOf(
            "filters" to viewModel.filters,
        )
        setFragmentResult(REQUEST_CODE_PRODUCT_FILTERS, bundle)
        dismiss()
    }

    private fun onMenuItemClicked(item: MenuItem): Boolean {
        when (item.itemId) {
            common.id.close -> dismiss()
        }
        return true
    }

    override fun renderState(state: Any) {
        when (state) {
            is FiltersState.Loading -> showLoading(true, R.id.content)
            is FiltersState.Loaded -> {
                showFilters(state)
                showProductsCount(state.count)
            }
            is FiltersState.Updated -> {
                showProductsCount(state.count)
                setupChipFilters(state.filters)
            }
            is FiltersState.DataReset -> {
                setupSliderRange(state.filters.minPrice, state.filters.maxPrice)
                setupLimitRangeLabel(state.filters.minPrice, state.filters.maxPrice, state.filters.defaultMaxPrice)
                showProductsCount(state.count)
                setupChipFilters(state.filters)
            }
            is FiltersState.AllCategoriesLoaded -> {
                binding.content.root.isVisible = false
                binding.categoryContainer.root.isVisible = false
                binding.layoutAllItems.root.isVisible = true
            }
        }
    }

    private fun showFilters(state: FiltersState.Loaded) {
        showLoading(false, R.id.content)
        showResetAll()

        val filters = state.filters
        val defMinPrice = filters.defaultMinPrice
        val defMaxPrice = filters.defaultMaxPrice
        setupChipFilters(filters)
        var correction = 0f
        if (defMaxPrice == defMinPrice) {
            correction = corrector
        }
        binding.content.sliderRange.valueTo = defMaxPrice.toFloat() + correction
        binding.content.sliderRange.valueFrom = defMinPrice.toFloat()
        setupSliderRange(filters.minPrice, filters.maxPrice)
        setupLimitLabels()

        val sliderValues = getSliderValues()
        setupLimitRangeLabel(
            sliderValues.first,
            sliderValues.second,
            defMaxPrice
        )
    }

    protected open fun getSliderValues(): Pair<Long, Long> {
        return Pair(
            binding.content.sliderRange.values.first().toLong(),
            binding.content.sliderRange.values[1].toLong(),
        )
    }

    protected open fun setupSliderRange(min: Long, max: Long) {
        binding.content.sliderRange.apply {
            val minPrice = min.toFloat()
            val maxPrice = max.toFloat()
            val startValue = if (minPrice < valueFrom) valueFrom else minPrice
            val endValue = if (maxPrice > valueTo) valueTo else maxPrice
            values = listOf(startValue, endValue)
        }
    }

    private fun setupChipFilters(filters: ProductFilters) {
        val showAllDisabled = !viewModel.showAll && !viewModel.showAllBrands  && !viewModel.showAllGenders
        binding.categoryContainer.root.isVisible = showAllDisabled
        binding.brandContainer.root.isVisible = getBrandAreaVisibility(showAllDisabled)
        binding.genderContainer.root.isVisible = getBrandAreaVisibility(showAllDisabled)
        binding.content.root.isVisible = showAllDisabled
        binding.toolbar.buttonResetAll.isVisible = showAllDisabled
        binding.layoutAllItems.root.isVisible = !showAllDisabled

        if (showAllDisabled) {
            binding.toolbar.toolbar.navigationIcon = null
            binding.toolbar.textTitle.text = getString(common.string.filters_screen_filters)
        } else {
            binding.toolbar.toolbar.setNavigationIcon(common.drawable.ic_back_button)
            binding.toolbar.toolbar.setNavigationOnClickListener {
                viewModel.intent.value = FiltersIntent.ResetSeeAll
            }
        }
        setupSubCategoryFilter(filters)
        setupBrandFilter(filters)
        setupGenderFilter(filters)
    }

    protected open fun getBrandAreaVisibility(showAllDisabled: Boolean) = showAllDisabled

    private fun setupSubCategoryFilter(filters: ProductFilters) {
        if (viewModel.showAll) {
            binding.toolbar.textTitle.text = getString(R.string.filters_categories)
            binding.layoutAllItems.groupItemsAll.showChips(
                layoutInflater,
                filters.allTypes,
                filters.types,
                false,
                ::onTypesChanged
            )
        } else {
            binding.categoryContainer.buttonTypesAll.isVisible = filters.allTypes.size > 10
            binding.categoryContainer.groupTypes.showChips(
                layoutInflater,
                filters.allTypes,
                filters.types,
                true,
                ::onTypesChanged
            )
        }
    }

    private fun setupBrandFilter(filters: ProductFilters) {
        if (viewModel.showAllBrands) {
            binding.toolbar.textTitle.text = getString(R.string.filters_brands)
            binding.layoutAllItems.groupItemsAll.showChips(
                layoutInflater,
                filters.allBrands.map { it.first },
                filters.brands,
                false,
                ::onBrandsChanged
            )
        } else {
            binding.brandContainer.buttonTypesAll.isVisible = filters.allTypes.size > 10
            binding.brandContainer.groupTypes.showChips(
                layoutInflater,
                filters.allBrands.map { it.first },
                filters.brands,
                true,
                ::onBrandsChanged
            )
        }
    }

    private fun setupGenderFilter(filters: ProductFilters) {
        if (viewModel.showAllGenders) {
            binding.toolbar.textTitle.text = getString(R.string.filters_gender)
            binding.layoutAllItems.groupItemsAll.showChips(
                layoutInflater,
                filters.allGenders,
                filters.genders,
                false,
                ::onGendersChanged,
                updateGenders = true,
            )
        } else {
            binding.genderContainer.buttonTypesAll.isVisible = filters.allGenders.size > 10
            binding.genderContainer.groupTypes.showChips(
                layoutInflater,
                filters.allGenders,
                filters.genders,
                true,
                ::onGendersChanged,
                updateGenders = true,
            )
        }
    }

    protected open fun setupLimitLabels() {
        val maxPrice = binding.content.sliderRange.valueTo.toLong()
        val minPrice = binding.content.sliderRange.valueFrom.toLong()
        binding.content.textLimitMin.text = minPrice.formatAmount(viewModel.currency.uppercase())
        binding.content.textLimitMax.text = maxPrice.formatAmount()
    }

    protected open fun setupLimitRangeLabel(minPrice: Long, maxPrice: Long, limitMax: Long) {
        val minRange = minPrice.formatAmount(viewModel.currency.uppercase(), true)
        val maxRangeLabel =
            if (maxPrice >= limitMax) {
                getString(com.yourfitness.common.R.string.filters_screen_no_limits)
            } else {
                maxPrice.formatAmount(viewModel.currency.uppercase(), true)
            }
        binding.content.limitsRange.text =
            getString(R.string.price_range_value, minRange, maxRangeLabel)
        binding.content.limitsRange.paintFlags =
            binding.content.limitsRange.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    private fun showResetAll() {
        binding.toolbar.buttonResetAll.isVisible = binding.content.root.isVisible
    }

    private fun showProductsCount(count: Int) {
        val hasProducts = count > 0
        val text = if (hasProducts) {
            resources.getQuantityString(R.plurals.filters_show_items, count, count)
        } else {
            getString(R.string.filters_no_items)
        }
        binding.buttonAction.text = text
        binding.buttonAction.isEnabled = hasProducts
        showResetAll()
    }

    private fun openEnterRange() {
        setFragmentResultListener(REQUEST_CODE_ENTER_RANGE) { key, bundle ->
            if (key == REQUEST_CODE_ENTER_RANGE) {
                val priceMin = bundle.getLong("priceMin")
                val priceMax = bundle.getLong("priceMax")
                setupSliderRange(priceMin, priceMax)
                setupLimitRangeLabel(priceMin, priceMax, binding.content.sliderRange.valueTo.toLong())
                viewModel.intent.postValue(FiltersIntent.RangeChanged(priceMin, priceMax))
            }
            clearFragmentResultListener(REQUEST_CODE_ENTER_RANGE)
        }
        viewModel.intent.value = FiltersIntent.InputRange
    }

    private fun onSeeAllCategoriesClicked() {
        viewModel.intent.value = FiltersIntent.SeeAllCategories
    }

    private fun onSeeAllGendersClicked() {
        viewModel.intent.value = FiltersIntent.SeeAllGenders
    }

    private fun onSeeAllBrandsClicked() {
        viewModel.intent.value = FiltersIntent.SeeAllBrands
    }

    private fun onTypesChanged(selected: List<String>) {
        viewModel.intent.postValue(FiltersIntent.TypesChanged(selected))
    }

    private fun onBrandsChanged(selected: List<String>) {
        viewModel.intent.postValue(FiltersIntent.BrandsChanged(selected))
    }

    private fun onGendersChanged(selected: List<String>) {
        viewModel.intent.postValue(FiltersIntent.GendersChanged(selected))
    }
}
