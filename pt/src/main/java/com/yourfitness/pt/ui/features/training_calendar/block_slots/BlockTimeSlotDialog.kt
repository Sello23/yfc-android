package com.yourfitness.pt.ui.features.training_calendar.block_slots

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.clearFragmentResult
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.yourfitness.common.domain.date.toCustomFormat
import com.yourfitness.common.domain.date.toDateTimeMs
import com.yourfitness.common.ui.mvi.MviBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.pt.R
import com.yourfitness.pt.databinding.DialogBlockTimeSlotBinding
import com.yourfitness.pt.domain.values.SUCCESS
import com.yourfitness.pt.ui.features.training_calendar.actions.UserConfirmActionDialog
import com.yourfitness.pt.ui.features.training_calendar.block_slots.conflicts.ResolveConflictDialog
import com.yourfitness.pt.ui.navigation.PtNavigation
import com.yourfitness.pt.ui.views.SelectDateDialog
import com.yourfitness.pt.ui.views.SelectWeeksNumberDialog
import dagger.hilt.android.AndroidEntryPoint
import java.lang.StringBuilder
import com.yourfitness.common.R as common

@AndroidEntryPoint
class BlockTimeSlotDialog : MviBottomSheetDialogFragment<BlockTimeSlotIntent, BlockTimeSlotState, BlockTimeSlotViewModel>() {

    override val binding: DialogBlockTimeSlotBinding by viewBinding()
    override val viewModel: BlockTimeSlotViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        setupToolbar(
            binding.toolbar.root,
            getString(R.string.block_out_time_slots),
            dismissId = common.id.close
        )

        binding.actionMain.setOnClickListener {
            viewModel.intent.value = BlockTimeSlotIntent.BlockBtnClicked
        }

        binding.dateAction.setOnClickListener {
            viewModel.intent.value = BlockTimeSlotIntent.SetDateClicked
            setFragmentResultListener(SelectDateDialog.RESULT) { _, bundle ->
                val date = bundle.getLong("date")
                if (date != -1L) {
                    setupDateArea(date)
                    viewModel.intent.postValue(BlockTimeSlotIntent.DateChanged(date))
                }
                clearFragmentResult(SelectDateDialog.RESULT)
            }
        }

        binding.timeAction.setOnClickListener {
            viewModel.intent.value = BlockTimeSlotIntent.SetTimeClicked
//            setFragmentResultListener(SelectDateDialog.RESULT) { _, bundle ->
//                val start = bundle.getLong("start")
//                val end = bundle.getLong("end")
//                if (start != -1L && end != -1L) {
//                    setupTimeArea(start, end)
//                    viewModel.intent.postValue(BlockTimeSlotIntent.TimeChanged(start, end))
//                }
//                clearFragmentResult(SelectDateDialog.RESULT)
//            }
        }

        binding.repeatAction.setOnClickListener {
            if (binding.repeatAction.isChecked) {
                viewModel.intent.value = BlockTimeSlotIntent.SetRepeatedClicked
                setFragmentResultListener(SelectWeeksNumberDialog.RESULT) { _, bundle ->
                    val weeksNumber = bundle.getInt("weeks")
                    setupWeeksNumber(weeksNumber)

                    viewModel.intent.postValue(BlockTimeSlotIntent.RepeatedWeeksChanged(weeksNumber))
                    clearFragmentResult(SelectWeeksNumberDialog.RESULT)
                }
            } else {
                binding.repeatLabel.apply {
                    text = getString(R.string.repeat)
                    setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            com.yourfitness.common.R.color.text_gray
                        )
                    )
                    viewModel.intent.postValue(BlockTimeSlotIntent.RepeatedWeeksChanged())
                }
            }
        }
    }

    private fun setupWeeksNumber(weeksNumber: Int) {
        binding.repeatLabel.apply {
            text = if (weeksNumber != -1) resources.getQuantityString(
                R.plurals.repeat_weeks,
                weeksNumber,
                weeksNumber
            ) else getString(R.string.repeat)
            setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (weeksNumber != -1) com.yourfitness.common.R.color.black else com.yourfitness.common.R.color.text_gray
                )
            )
        }
        binding.repeatAction.isChecked = weeksNumber != -1
    }

    private fun setupTimeArea() {
        if (!viewModel.hasSelectedTime || viewModel.selectedTimeSet.isEmpty()) return
        binding.timeLabel.apply {
            val timeList = StringBuilder()
            viewModel.selectedTimeSet.forEachIndexed { index, it ->
                timeList.append(
                    context.getString(
                        com.yourfitness.common.R.string.training_calendar_screen_time_interval,
                        it.first.toDateTimeMs(),
                        it.second.toDateTimeMs()
                    )
                )
                if (index != viewModel.selectedTimeSet.size -1) {
                    timeList.append("\n")
                }
            }
            text = timeList
            setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    com.yourfitness.common.R.color.black
                )
            )
        }
        binding.timeAction.text = getString(R.string.change_time)
        binding.repeatAction.isEnabled = true
        binding.actionMain.isEnabled = true
    }

    private fun setupDateArea(date: Long) {
        binding.dateLabel.apply {
            text = date.toCustomFormat()
            setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    com.yourfitness.common.R.color.black
                )
            )
        }
        binding.dateAction.text = getString(R.string.change_date)
        binding.timeAction.isEnabled = true
    }

    override fun renderState(state: BlockTimeSlotState) {
        when (state) {
            is BlockTimeSlotState.Loading -> {
                showLoading(state.active)
                binding.actionMain.isClickable = !state.active
            }
            is BlockTimeSlotState.Error -> {
                binding.actionMain.isClickable = true
                showError(state.error)
            }
            is BlockTimeSlotState.Loaded -> {
                showLoading(false)
                setupViews()
            }
            is BlockTimeSlotState.SlotBlocked -> setResult()
            is BlockTimeSlotState.SetConflictListener -> {
                setupConflictsResultListener()
                setupConflictsPreviewListener()
            }
            is BlockTimeSlotState.ResetTimeAndRepeats -> {
                binding.timeLabel.apply {
                    text = context.getString(R.string.time)
                    setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            com.yourfitness.common.R.color.text_gray
                        )
                    )
                }
                binding.actionMain.isEnabled = false
            }
        }
    }

    override fun showLoading(isLoading: Boolean) {
        binding.progress.isVisible = isLoading
    }

    private fun setupViews() {
        val date = viewModel.selectedDate
        if (date != null) setupDateArea(date)

        /*val start = viewModel.selectedStartTime
        val end = viewModel.selectedEndTime
        if (start != null && end != null) */setupTimeArea()

        setupWeeksNumber(viewModel.selectedRepeatedWeeks)
    }

    private fun setupConflictsResultListener() {
        setFragmentResultListener(UserConfirmActionDialog.RESULT) { _, bundle ->
            binding.actionMain.isClickable = true
            if (bundle.getBoolean(SUCCESS)) {
                viewModel.intent.postValue(BlockTimeSlotIntent.ConflictUpdated)
            }
            else {
                showLoading(false)
                viewModel.intent.postValue(BlockTimeSlotIntent.FlowCancelled)
            }
            clearFragmentResult(UserConfirmActionDialog.RESULT)
            setupConflictsResultListener()
        }
    }

    private fun setupConflictsPreviewListener() {
        setFragmentResultListener(ResolveConflictDialog.RESULT) { _, bundle ->
            binding.actionMain.isClickable = true
            if (!bundle.getBoolean(SUCCESS)) {
                showLoading(false)
                viewModel.intent.postValue(BlockTimeSlotIntent.FlowCancelled)
            }
            clearFragmentResult(ResolveConflictDialog.RESULT)
            setupConflictsPreviewListener()
        }
    }

    private fun setResult() {
        setFragmentResult(RESULT, bundleOf())
        viewModel.navigator.navigate(PtNavigation.TimeSlotsBlocked)
    }

    companion object {
        const val RESULT = "block_slot_successful"
    }
}
