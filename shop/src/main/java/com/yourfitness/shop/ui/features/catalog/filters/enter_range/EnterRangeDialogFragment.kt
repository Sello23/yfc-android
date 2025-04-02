package com.yourfitness.shop.ui.features.catalog.filters.enter_range

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.google.android.material.textfield.TextInputEditText
import com.yourfitness.common.ui.mvi.MviBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.formatAmount
import com.yourfitness.shop.R
import com.yourfitness.shop.databinding.DialogInputRangeBinding
import com.yourfitness.shop.ui.features.catalog.filters.REQUEST_CODE_ENTER_RANGE
import dagger.hilt.android.AndroidEntryPoint
import com.yourfitness.common.R as common

@AndroidEntryPoint
class EnterRangeDialogFragment : MviBottomSheetDialogFragment<Any, Any, EnterRangeViewModel>() {

    override val binding: DialogInputRangeBinding by viewBinding()
    override val viewModel: EnterRangeViewModel by viewModels()

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        setupToolbar(
            toolbar = binding.toolbar.root,
            title = getString(R.string.price_range),
            com.yourfitness.common.R.id.close
        )
        binding.toolbar.toolbar.setOnMenuItemClickListener { onMenuItemClicked(it) }
        binding.buttonAction.setOnClickListener { onApplyClicked() }
        setupInputListeners()
    }

    override fun renderState(state: Any) {
        when (state) {
            is EnterRangeState.UpdateAction -> binding.buttonAction.isEnabled = state.isEnabled
        }
    }

    private fun onApplyClicked() {
        val bundle = bundleOf(
            "priceMin" to viewModel.priceMin,
            "priceMax" to viewModel.priceMax
        )
        setFragmentResult(REQUEST_CODE_ENTER_RANGE, bundle)
        dismiss()
    }

    private fun onMenuItemClicked(item: MenuItem): Boolean {
        when (item.itemId) {
            common.id.close -> dismiss()
        }
        return true
    }

    private fun setupInputListeners() {
        binding.inputRangeStart.hint =
            viewModel.rangeMin.formatAmount(viewModel.currency.uppercase())
        binding.inputRangeEnd.hint =
            viewModel.rangeMax.formatAmount(viewModel.currency.uppercase())

        binding.inputRangeStart.transformationMethod = HideReturnsTransformationMethod.getInstance()
        binding.inputRangeEnd.transformationMethod = HideReturnsTransformationMethod.getInstance()

        binding.inputRangeStart.requestFocus()
        binding.inputRangeStart.doOnTextChanged { min, _, _, _ ->
            onRangeChanged(min ?: "", binding.inputRangeStart) { value ->
                return@onRangeChanged EnterRangeIntent.PriceMinChanged(value)
            }
        }
        binding.inputRangeEnd.doOnTextChanged { max, _, _, _ ->
            onRangeChanged(max ?: "", binding.inputRangeEnd) { value ->
                return@onRangeChanged EnterRangeIntent.PriceMaxChanged(value)
            }
        }
    }

    private fun onRangeChanged(
        range: CharSequence,
        field: TextInputEditText,
        onUpdateRange: (value: Long) -> EnterRangeIntent
    ) {
        try {
            val rangeValue = range.toString().trim()
            if (field.text.toString() != rangeValue) field.setText(rangeValue)
            viewModel.intent.value = onUpdateRange(rangeValue.toLong())
        } catch (_: Exception) {}
    }
}
