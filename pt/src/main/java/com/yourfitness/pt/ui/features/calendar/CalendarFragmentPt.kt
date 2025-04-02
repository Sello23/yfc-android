package com.yourfitness.pt.ui.features.calendar

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.kizitonwose.calendar.core.WeekDay
import com.yourfitness.common.domain.date.toLocalDate
import com.yourfitness.pt.R
import com.yourfitness.pt.domain.models.CalendarData
import com.yourfitness.pt.domain.models.SelectedTimeSlot
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CalendarFragmentPt : CalendarFragment() {

    override val viewModel: CalendarViewModelPt by viewModels()
    override val disableNotSelectedDay = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        startMonth = viewModel.displayedDate.toLocalDate()
        endMonth = viewModel.displayedDate.toLocalDate()

        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.sessions.root.isVisible = false
        binding.actionArea.btnCheckout.text = getString(R.string.set_label)
    }

    override fun renderState(state: CalendarState) {
        when (state) {
            is CalendarState.Loading -> showLoading(true)
            is CalendarState.Error -> {
                showLoading(false)
                showError(state.error)
            }
           else -> super.renderState(state)
        }
    }

    override fun onActionBtnClicked(selectedItem: CalendarData?) {
        if (selectedItem != null) {
            viewModel.intent.value = CalendarIntent.BookTimeSlotTapped(selectedItem as SelectedTimeSlot)
        }
    }

    override fun onDestroy() {
        setFragmentResult(RESULT, bundleOf("openBlockDialog" to true))
        super.onDestroy()
    }

    override fun onDateClick(day: WeekDay) {}

    companion object {
        const val RESULT = "select_time_result"
    }
}
