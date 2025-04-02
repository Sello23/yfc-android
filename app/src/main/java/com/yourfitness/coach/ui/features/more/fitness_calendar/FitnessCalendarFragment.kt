package com.yourfitness.coach.ui.features.more.fitness_calendar

import android.widget.TextView
import androidx.fragment.app.viewModels
import com.yourfitness.coach.R
import com.yourfitness.coach.ui.utils.ManageClass
import com.yourfitness.common.domain.models.CalendarView
import com.yourfitness.common.ui.features.fitness_calendar.BaseDayViewHolder
import com.yourfitness.common.ui.features.fitness_calendar.BaseFitnessCalendarFragment
import com.yourfitness.common.ui.features.fitness_calendar.BaseWeekViewHolder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FitnessCalendarFragment :
    BaseFitnessCalendarFragment<FitnessCalendarViewModel, MonthViewHolder, BaseWeekViewHolder, BaseDayViewHolder>() {

    override val viewModel: FitnessCalendarViewModel by viewModels()
    override val baseMonthAdapter by lazy { MonthAdapter(::onItemClick) }
    override val weekAdapter by lazy { WeekAdapter(::onItemClick) }
    override val dayAdapter by lazy { DayAdapter(::onItemClick) }

    private val manageClass = ManageClass(this)

    private fun onItemClick(view: TextView, text: String, bookedClass: CalendarView.CalendarItem) {
        onItemClick()

        when (text) {
            getString(R.string.fitness_calendar_screen_manage_class_text) -> {
                manageClass.init(view)
                manageClass.openMenu(
                    onRebookClick = { viewModel.onRebookClick(bookedClass) },
                    onCancelClick = { viewModel.onCancelClick(bookedClass) }
                )
            }
            getString(R.string.fitness_calendar_screen_get_access_text) -> {
                viewModel.goToClass(bookedClass)
            }
        }
    }
}
