package com.yourfitness.pt.ui.features.dashboard.inductions

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.yourfitness.common.databinding.DialogOneActionBinding
import com.yourfitness.common.ui.base.BaseBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.pt.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConfirmSuccessfulDialog : BaseBottomSheetDialogFragment() {

    override val binding: DialogOneActionBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        setupToolbar(
            binding.toolbar.root,
            getString(R.string.confirm_induction_success_title),
            dismissId = com.yourfitness.common.R.id.close
        )
        binding.buttonConfirm.setOnClickListener { dismiss() }
        binding.message.text = getString(R.string.confirm_induction_success_msg)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        setFragmentResult(RESULT, bundleOf())
    }

    companion object {
        const val RESULT = "confirm_induction_success_dialog_result"
    }
}

