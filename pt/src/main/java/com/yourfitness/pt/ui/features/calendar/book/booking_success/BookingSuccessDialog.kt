package com.yourfitness.pt.ui.features.calendar.book.booking_success

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import com.yourfitness.common.databinding.DialogOneActionBinding
import com.yourfitness.common.ui.base.BaseBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setCompoundDrawables
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.pt.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookingSuccessDialog : BaseBottomSheetDialogFragment() {

    override val binding: DialogOneActionBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        setupToolbar(
            binding.toolbar.root,
            getString(R.string.booking_success_title),
            dismissId = com.yourfitness.common.R.id.close
        )
        binding.buttonConfirm.setOnClickListener { dismiss() }
        binding.info.apply {
            isVisible = true
            text = resources.getQuantityString(R.plurals.sessions_number, 1, 1)
            setCompoundDrawables(start = R.drawable.ic_barbell)
        }
        binding.message.apply {
            setTextAppearance(
                requireContext(),
                com.yourfitness.common.R.style.TextAppearance_YFC_Hint
            )
            text = getString(R.string.booking_success_msg)
            (layoutParams as ConstraintLayout.LayoutParams).topMargin = 0
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        setFragmentResult(RESULT, bundleOf())
    }

    companion object {
        const val RESULT = "booking_success_dialog_result"
    }
}
