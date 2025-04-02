package com.yourfitness.pt.ui.features.training_calendar.block_slots.conflicts

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yourfitness.common.domain.date.formatted
import com.yourfitness.common.domain.date.toCustomFormat
import com.yourfitness.common.domain.date.toDate
import com.yourfitness.common.domain.date.toDateTimeMs
import com.yourfitness.common.ui.mvi.MviBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.pt.R
import com.yourfitness.pt.databinding.DialogResolveConflictBinding
import com.yourfitness.pt.domain.values.SUCCESS
import dagger.hilt.android.AndroidEntryPoint
import com.yourfitness.common.R as common

@AndroidEntryPoint
class ResolveConflictDialog : MviBottomSheetDialogFragment<ResolveConflictIntent, ResolveConflictState, ResolveConflictViewModel>() {

    override val binding: DialogResolveConflictBinding by viewBinding()
    override val viewModel: ResolveConflictViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        setupToolbar(
            binding.toolbar.root,
            getString(R.string.scheduling_conflict),
            dismissId = common.id.close
        )

        binding.actionMain.setOnClickListener {
            binding.actionMain.isClickable = false
            viewModel.intent.value = ResolveConflictIntent.CancelConfirmed
        }

        binding.actionSecondary.setOnClickListener {
            setResult(false)
            findNavController().navigateUp()
        }
        dialog?.setCancelable(false)
    }

    override fun dismissDialog() {
        setResult(false)
        dismiss()
    }

    override fun renderState(state: ResolveConflictState) {
        when (state) {
            is ResolveConflictState.Loading -> {
                binding.actionMain.isClickable = !state.active
            }
            is ResolveConflictState.Error -> {
                binding.actionMain.isClickable = true
                showError(state.error)
            }
            is ResolveConflictState.Loaded -> showInfo(state)
        }
    }

    private fun showInfo(info: ResolveConflictState.Loaded) {
        binding.actionMain.text = getString(
            if (info.isSession) R.string.cancel_conflicting_appointment
            else R.string.remove_conflicting_blocked_time_slot
        )
        val date = info.from.toDate().formatted()
        val timeInterval = getString(
            com.yourfitness.common.R.string.training_calendar_screen_time_interval,
            info.from.toDateTimeMs(),
            info.to.toDateTimeMs()
        )
        binding.message.text = if (info.isSession) {
            getString(R.string.session_conflict_msg, date, timeInterval, info.traineeName)
        } else {
            getString(R.string.block_conflict_msg, date, timeInterval)
        }
    }

    private fun setResult(success: Boolean) {
        setFragmentResult(RESULT, bundleOf(SUCCESS to success))
    }

    companion object {
        const val RESULT = "resolve_conflict_result"
    }
}
