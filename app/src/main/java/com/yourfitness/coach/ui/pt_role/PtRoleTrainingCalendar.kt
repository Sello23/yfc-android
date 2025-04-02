package com.yourfitness.coach.ui.pt_role

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.clearFragmentResult
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import com.yourfitness.coach.databinding.FragmentPtRoleTrainingCalendarBinding
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.pt.domain.values.SUCCESS
import com.yourfitness.pt.ui.features.calendar.CalendarFragmentPt
import com.yourfitness.pt.ui.features.training_calendar.TrainingCalendarPtFragment
import com.yourfitness.pt.ui.features.training_calendar.TrainingCalendarPtIntent
import com.yourfitness.pt.ui.features.training_calendar.actions.UserConfirmActionDialog
import com.yourfitness.pt.ui.features.training_calendar.block_slots.BlockTimeSlotDialog
import com.yourfitness.pt.ui.features.training_calendar.block_slots.conflicts.list.BlockSlotsFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PtRoleTrainingCalendar : Fragment() {
    private val binding: FragmentPtRoleTrainingCalendarBinding by viewBinding()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFragmentListener()
        setupBlockSlotListener()
        setupBlockSlotListUpdateListener()
        manageTimeSlots()
    }

    private fun setupFragmentListener() {
        setFragmentResultListener(UserConfirmActionDialog.RESULT) { _, bundle ->
            if (bundle.getBoolean(SUCCESS)) binding.calendar.getFragment<TrainingCalendarPtFragment>().refresh()
            clearFragmentResultListener(UserConfirmActionDialog.RESULT)
            setupFragmentListener()
        }
    }

    private fun setupBlockSlotListener() {
        setFragmentResultListener(BlockTimeSlotDialog.RESULT) { _, _ ->
            binding.calendar.getFragment<TrainingCalendarPtFragment>().refresh()
            clearFragmentResultListener(BlockTimeSlotDialog.RESULT)
            setupBlockSlotListener()
            setupFragmentListener()
        }
    }

    private fun setupBlockSlotListUpdateListener() {
        setFragmentResultListener(BlockSlotsFragment.RESULT) { _, _ ->
            binding.calendar.getFragment<TrainingCalendarPtFragment>().refresh()
            clearFragmentResultListener(BlockSlotsFragment.RESULT)
            setupBlockSlotListUpdateListener()
        }
    }

    private fun manageTimeSlots() {
        setFragmentResultListener(CalendarFragmentPt.RESULT) { _, bundle ->
            val openBlockDialog = bundle.getBoolean("openBlockDialog")
            if (openBlockDialog) {
                binding.calendar.getFragment<TrainingCalendarPtFragment>().openBlockDialog()
                manageTimeSlots()
            }
            clearFragmentResult(CalendarFragmentPt.RESULT)
        }
    }
}