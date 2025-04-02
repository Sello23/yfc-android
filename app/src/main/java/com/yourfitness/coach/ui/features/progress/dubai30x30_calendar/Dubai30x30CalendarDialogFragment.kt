package com.yourfitness.coach.ui.features.progress.dubai30x30_calendar

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.yourfitness.coach.databinding.DialogDubai30x30CalendarBinding
import com.yourfitness.coach.databinding.ViewCalendarDayDubai30x30Binding
import com.yourfitness.coach.domain.date.daysOfWeek
import com.yourfitness.coach.domain.date.millisToLocalDate
import com.yourfitness.common.ui.mvi.MviBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setTextColorRes
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import com.yourfitness.common.R as common


@AndroidEntryPoint
class Dubai30x30CalendarDialogFragment :
    MviBottomSheetDialogFragment<Dubai30x30CalendarIntent, Dubai30x30CalendarState, Dubai30x30CalendarViewModel>() {

    override val binding: DialogDubai30x30CalendarBinding by viewBinding()
    override val viewModel: Dubai30x30CalendarViewModel by viewModels()

    private val today = LocalDate.now()
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM")
    private var calendar: CalendarView? = null
    private val dayBinderContainer = WorkoutDayBinderContainer()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.configureDialogView()
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.close.setOnClickListener { dismiss() }
        binding.actionNegative.setOnClickListener { dismiss() }
        binding.actionPositive.setOnClickListener {
            showLoading(true)
            viewModel.intent.value = Dubai30x30CalendarIntent.SaveChanges
        }
        setupCalendar()
    }

    override fun renderState(state: Dubai30x30CalendarState) {
        when (state) {
            is Dubai30x30CalendarState.Loaded -> {
                if (state.actual) showLoading(false)
                dayBinderContainer.setData(state.workouts, state.startDate, state.endDate)
                binding.workoutCalendarContainer.calendarMonth.notifyCalendarChanged()
            }

            is Dubai30x30CalendarState.Error -> {
                showError(state.error)
                showLoading(false)
            }

            is Dubai30x30CalendarState.DayUpdated -> {
                updateCalendarDay(state.workouts, state.day)
            }
        }
    }

    override fun showLoading(isLoading: Boolean) {
        binding.workoutCalendarContainer.progress.root.isVisible = isLoading
        binding.progressSecondary.root.isVisible = isLoading
    }

    private fun setupCalendar() {
        val daysOfWeek = daysOfWeek()
        val currentMonth = YearMonth.now()

        binding.workoutCalendarContainer.apply {
            calendarMonth.apply {
                calendar = this

                setup(currentMonth.minusMonths(1), currentMonth.plusMonths(6), daysOfWeek.first())
                scrollToMonth(currentMonth)

                dayBinder = dayBinderContainer

                monthScrollListener = {
                    textCurrentMonth.text = getString(
                        common.string.fitness_calendar_screen_current_month_text,
                        monthTitleFormatter.format(it.yearMonth),
                        it.yearMonth.year.toString()
                    )

                }

                viewCalendarDayLegend.root.children.forEachIndexed { index, view ->
                    (view as TextView).apply {
                        text = daysOfWeek[index].getDisplayName(TextStyle.SHORT, Locale.US)
                            .replaceFirstChar { it.titlecaseChar() }
                        setTextColorRes(com.yourfitness.common.R.color.black)
                    }
                }

                buttonPrevMonth.setOnClickListener {
                    calendarMonth.findFirstVisibleMonth()?.let {
                        calendarMonth.smoothScrollToMonth(it.yearMonth.previousMonth)
                    }
                }
                buttonNextMonth.setOnClickListener {
                    calendarMonth.findFirstVisibleMonth()?.let {
                        calendarMonth.smoothScrollToMonth(it.yearMonth.nextMonth)
                    }
                }
            }
        }
    }

    private fun updateCalendarDay(workouts: List<WorkoutCalendarItem>, day: LocalDate) {
        dayBinderContainer.setData(workouts)
        binding.workoutCalendarContainer.calendarMonth.notifyDateChanged(day)
    }

    private inner class WorkoutDayBinderContainer : MonthDayBinder<DayViewContainer> {
        val workouts = mutableListOf<WorkoutCalendarItem>()

        private var startDate: LocalDate = today.minusMonths(1)
        private var endDate: LocalDate = today.plusMonths(1)

        fun setData(data: List<WorkoutCalendarItem>, start: Long? = null, end: Long? = null) {
            workouts.clear()
            workouts.addAll(data)

            if (start != null) {
                startDate = start.millisToLocalDate() ?: today.minusMonths(1)
            }
            if (end != null) {
                endDate = end.millisToLocalDate() ?: today.plusMonths(1)
            }
        }

        override fun create(view: View) = DayViewContainer(view)

        override fun bind(container: DayViewContainer, day: CalendarDay) {
            val workout = workouts.find {
                it.day.year == day.date.year && it.day.dayOfYear == day.date.dayOfYear
            }
            container.bind(day, workout, startDate, endDate)
        }
    }

    private inner class DayViewContainer(view: View) : ViewContainer(view) {
        val binding = ViewCalendarDayDubai30x30Binding.bind(view)

        fun bind(day: CalendarDay, workout: WorkoutCalendarItem?, startDate: LocalDate, endDate: LocalDate) {
            val textView = binding.workoutDay
            val filledMark = binding.viewSelectedMark
            textView.text = day.date.dayOfMonth.toString()

            if (day.position == DayPosition.MonthDate) {
                filledMark.isVisible = workout != null
                if (workout != null) {
                    textView.text = null
                } else {
                    textView.setTextColorRes(com.yourfitness.common.R.color.black)
                }

                if (workout?.manual == true) {
                    textView.isEnabled = true
                    textView.isSelected = true
                    binding.root.setOnClickListener {
                        viewModel.intent.postValue(Dubai30x30CalendarIntent.DayUnselected(day.date))
                    }
                } else if (workout?.manual == false) {
                    textView.isEnabled = false
                    textView.isSelected = true
                } else if (day.date == today) {
                    textView.setTextColorRes(com.yourfitness.common.R.color.main_active)
                    val typeface = ResourcesCompat.getFont(
                        requireContext(),
                        com.yourfitness.common.R.font.work_sans_bold
                    )
                    textView.typeface = typeface
                    if (day.date.isBefore(endDate)) {
                        textView.isEnabled = true
                        textView.isSelected = false
                        binding.root.setOnClickListener {
                            viewModel.intent.postValue(Dubai30x30CalendarIntent.DaySelected(day.date))
                        }
                    }
                } else if (day.date.isBefore(today) && !day.date.isBefore(startDate) && day.date.isBefore(endDate)) {
                    textView.isEnabled = true
                    textView.isSelected = false
                    binding.root.setOnClickListener {
                        viewModel.intent.postValue(Dubai30x30CalendarIntent.DaySelected(day.date))
                    }
                } else {
                    textView.isEnabled = false
                    textView.isSelected = false
                    if (day.date.isAfter(endDate) || day.date.isBefore(startDate)) {
                        textView.setTextColorRes(com.yourfitness.common.R.color.gray_light)
                    }
                }

            } else {
                if (day.date.isBefore(today) && !day.date.isBefore(startDate) && day.date.isBefore(endDate)) {
                    textView.isEnabled = true
                    textView.isSelected = false
                } else {
                    textView.isSelected = false
                    textView.isEnabled = false
                }
                textView.setTextColorRes(com.yourfitness.common.R.color.gray_light)
            }
        }
    }
}

data class WorkoutCalendarItem(
    val day: LocalDate,
    val manual: Boolean = true
)
