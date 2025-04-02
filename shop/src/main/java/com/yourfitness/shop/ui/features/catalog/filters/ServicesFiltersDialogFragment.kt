package com.yourfitness.shop.ui.features.catalog.filters

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.google.android.material.slider.LabelFormatter
import com.yourfitness.common.ui.utils.*
import com.yourfitness.common.R as common
import com.yourfitness.shop.domain.products.ProductFilters
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ServicesFiltersDialogFragment : ProductFiltersDialogFragment() {

    override val viewModel: ServicesFiltersViewModel by viewModels()

    override val corrector = 1f

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.brandContainer.root.isVisible = false
        binding.content.limitsRange.isVisible = false
        binding.content.sliderRange.labelBehavior = LabelFormatter.LABEL_VISIBLE
        binding.content.sliderRange.valueTo
        binding.content.priceRange.text = getString(common.string.filters_screen_distance)
    }

    override fun setupLimitLabels() {
        val minPrice = binding.content.sliderRange.valueFrom.toLong()
        binding.content.textLimitMin.text = minPrice.formatDistance()
        binding.content.textLimitMax.text =
            getString(com.yourfitness.common.R.string.filters_screen_no_limits)
    }

    override fun getSliderValues(): Pair<Long, Long> {
        return Pair(
            binding.content.sliderRange.values.first().toLong(),
            binding.content.sliderRange.values.first().toLong(),
        )
    }

    override fun setupSliderRange(min: Long, max: Long) {
        binding.content.sliderRange.apply {
            val maxDistance = if (max.toFloat() > valueTo) valueTo else max.toFloat()
            values = listOf(maxDistance)
            setLabelFormatter {
                it.formatDistance().orEmpty()
            }
        }
    }

    override fun onStopTrackingTouch() {
        val values = binding.content.sliderRange.values
        val min = values.first().toLong()
        viewModel.intent.value = FiltersIntent.RangeChanged(min, min)
    }

    override fun setupLimitRangeLabel(minPrice: Long, maxPrice: Long, limitMax: Long) {}

    override fun getBrandAreaVisibility(showAllDisabled: Boolean) = false

    override fun Dialog.configureDialogView() {
        setupBottomInsets(binding.root)
    }
}
