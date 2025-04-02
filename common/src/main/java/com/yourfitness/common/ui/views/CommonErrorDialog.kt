package com.yourfitness.common.ui.views

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.yourfitness.common.databinding.DialogErrorOneActionBinding
import com.yourfitness.common.ui.base.BaseBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setupBottomInsets
import dagger.hilt.android.AndroidEntryPoint
import com.yourfitness.common.R as common

@AndroidEntryPoint
class CommonErrorDialog : BaseBottomSheetDialogFragment() {

    override val binding: DialogErrorOneActionBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        setupToolbar(
            binding.toolbar.root,
            getString(common.string.common_error_dialog_title),
            dismissId = common.id.close
        )
        binding.message.text = getString(common.string.common_error_dialog_msg)
        binding.buttonConfirm.background = ContextCompat.getDrawable(
            requireContext(),
            com.yourfitness.common.R.drawable.button_background
        )
        binding.buttonConfirm.text = getString(common.string.got_it)
        binding.buttonConfirm.setOnClickListener { dismiss() }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        setFragmentResult(RESULT, bundleOf())
    }

    companion object {
        const val RESULT = "common_error_dialog_result"
    }
}