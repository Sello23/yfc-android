package com.yourfitness.pt.ui.views

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.yourfitness.common.ui.base.BaseBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.pt.R
import com.yourfitness.pt.databinding.DialogSelectWeeksNumberBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectWeeksNumberDialog : BaseBottomSheetDialogFragment() {

    override val binding: DialogSelectWeeksNumberBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        binding.apply {
            mainAction.setOnClickListener {
                setResult(false)
                dismiss()
            }
            secondaryAction.setOnClickListener {
                setResult(true)
                dismiss()
            }

            datePicker.apply {
                minValue = 2
                maxValue = 53
                val weekPikerValues = Array(maxValue - minValue + 1) {
                    i -> resources.getQuantityString(R.plurals.weeks_number, i + minValue, i + minValue)
                }
                displayedValues = weekPikerValues
            }
        }
    }

    private fun setResult(save: Boolean) {
        binding.datePicker.apply {
            val data = if (save) value else -1
            setFragmentResult(RESULT, bundleOf("weeks" to data))
        }
    }

    companion object {
        const val RESULT = "select_weeks_number_result"
    }
}
