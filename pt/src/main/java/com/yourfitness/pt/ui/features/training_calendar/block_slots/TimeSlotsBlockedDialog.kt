package com.yourfitness.pt.ui.features.training_calendar.block_slots

import android.os.Bundle
import android.view.View
import com.yourfitness.common.databinding.DialogOneActionBinding
import com.yourfitness.common.ui.base.BaseBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.pt.R

class TimeSlotsBlockedDialog : BaseBottomSheetDialogFragment() {

    override val binding: DialogOneActionBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        setupToolbar(
            binding.toolbar.root,
            getString(R.string.block_slot_success_title),
            dismissId = com.yourfitness.common.R.id.close
        )
        binding.message.text = getString(R.string.block_slot_success_msg)
        binding.buttonConfirm.setOnClickListener { dismiss() }
    }
}
