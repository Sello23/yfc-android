package com.yourfitness.coach.ui.features.facility.dialog.confirm_cancel_result

import android.os.Bundle
import android.view.View
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogConfirmCancelResultBinding
import com.yourfitness.common.ui.base.BaseBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding

class ConfirmCancelResultFragment : BaseBottomSheetDialogFragment() {

    override val binding: DialogConfirmCancelResultBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(
            binding.toolbar.root,
            getString(R.string.book_class_screen_your_class_is_cancelled),
            dismissId = R.id.close
        )
        val credits = requireArguments().getInt("refunded_credits")
        binding.tvCredits.text = getString(R.string.credits, credits)
        binding.btnGotIt.setOnClickListener { dismiss() }
    }

}