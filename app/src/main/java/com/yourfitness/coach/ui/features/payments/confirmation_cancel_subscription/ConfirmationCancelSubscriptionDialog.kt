package com.yourfitness.coach.ui.features.payments.confirmation_cancel_subscription

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogConfirmationCancelSubscriptionBinding
import com.yourfitness.common.ui.base.BaseBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setupBottomInsets

class ConfirmationCancelSubscriptionDialog : BaseBottomSheetDialogFragment() {

    override val binding: DialogConfirmationCancelSubscriptionBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        setupToolbar(
            binding.toolbar.root,
            getString(R.string.confirm_cancel_subscription_title),
            dismissId = R.id.close
        )
        binding.btnConfirm.setOnClickListener {
            setFragmentResult(
                RESULT_KEY_CONFIRMATION,
                bundleOf()
            )
            dismiss()
        }
    }

    companion object {
        const val RESULT_KEY_CONFIRMATION = "result_key_confirmation"
    }
}
