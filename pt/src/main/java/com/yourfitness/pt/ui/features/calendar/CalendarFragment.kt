package com.yourfitness.pt.ui.features.calendar

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kizitonwose.calendar.core.Week
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekDayBinder
import com.yourfitness.common.domain.date.toDate
import com.yourfitness.common.domain.date.toLocalDate
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.pt.R
import com.yourfitness.pt.databinding.FragmentCalendarBinding
import com.yourfitness.pt.databinding.ViewUserCalendarDayBinding
import com.yourfitness.pt.domain.models.CalendarData
import com.yourfitness.pt.domain.models.FilledTimeSlot
import com.yourfitness.pt.domain.models.SelectedTimeSlot
import com.yourfitness.pt.domain.models.TimeSlot
import com.yourfitness.pt.ui.features.calendar.book.booking_error.BookingErrorDialog
import com.yourfitness.pt.ui.features.calendar.book.booking_success.BookingSuccessDialog
import com.yourfitness.pt.ui.features.calendar.confirm.ProcessSessionConfirmDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
open class CalendarFragment : MviFragment<CalendarIntent, CalendarState, CalendarViewModel>() {

    override val binding: FragmentCalendarBinding by viewBinding()
    override val viewModel: CalendarViewModel by viewModels()
    private val calendarAdapter: CalendarPagingAdapter by lazy {
        CalendarPagingAdapter(
            ::onFilledTimeSlotClick,
            ::onTimeSlotClick,
            ::onSelectedTimeSlotClick,
        )
    }

    protected var startMonth: LocalDate = LocalDate.of(2020, 1, 1)
    protected var endMonth: LocalDate = LocalDate.of(2025, 12, 1)
    protected open val disableNotSelectedDay = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.root.apply {
            setupToolbar(this)
            setContentInsetsAbsolute(0, contentInsetStartWithNavigation)
            menu.clear()
        }
        setupCalendarHeader()
        setupRecyclerView()
        binding.toolbar.title.text = ""
        binding.actionArea.root.isVisible = false
        binding.toolbar.sessions.root.isVisible = viewModel.actionsEnabled
    }

    override fun onResume() {
        super.onResume()

        registerTimeSlotUpdatesListener()
    }

    override fun renderState(state: CalendarState) {
        when (state) {
            is CalendarState.Loading -> showLoading(true)
            is CalendarState.Error -> {
                showLoading(false)
                showError(state.error)
            }
            is CalendarState.PtInfoLoaded -> {
                showLoading(false)
                updateCalendarList(state.calendarData)
                updateActionArea(state.calendarData)
                if (state.balance != null && state.ptName != null) {
                    updatePtInfo(state.balance, state.ptName)
                }
            }
            is CalendarState.CalendarDataUpdated -> {
                showLoading(false)
                updateCalendarList(state.calendarData)
                updateActionArea(state.calendarData)
            }
        }
    }

    private fun updatePtInfo(balance: Int, name: String) {
        binding.toolbar.title.text = name
        binding.toolbar.sessions.textSessions.text =
            resources.getQuantityString(R.plurals.sessions_number, balance, balance)
    }

    private fun updateCalendarList(calendarData: List<CalendarData>) {
        calendarAdapter.setData(calendarData)
        lifecycleScope.launch {
            val scrollState = binding.calendarContent.layoutManager?.onSaveInstanceState()
            binding.calendarContent.post {
                calendarAdapter.setData(calendarData)
                calendarAdapter.notifyDataSetChanged()
                binding.calendarContent.layoutManager?.onRestoreInstanceState(scrollState)
            }
        }
    }

    private fun updateActionArea(calendarData: List<CalendarData>) {
        val selectedItem = calendarData.firstOrNull {it is SelectedTimeSlot}
            binding.actionArea.root.isVisible = selectedItem != null
        if (selectedItem != null) {
            binding.actionArea.btnCheckout.text = getString(R.string.book_time_slot)
            binding.actionArea.btnCheckout.setOnClickListener {
                onActionBtnClicked(selectedItem)
            }
        }
    }

    protected open fun onActionBtnClicked(selectedItem: CalendarData?) {
        viewModel.intent.value = CalendarIntent.BookTimeSlotTapped(selectedItem as SelectedTimeSlot)
    }

    private fun setupCalendarHeader() {
        binding.calendarView.dayBinder = object : WeekDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view, ::onDateClick)

            override fun bind(container: DayViewContainer, data: WeekDay) {
                val date = viewModel.displayedDate.toLocalDate()
                container.bind(data, date, disableNotSelectedDay)
            }
        }

        binding.calendarView.apply {
            setup(startMonth, endMonth, DayOfWeek.MONDAY)
            scrollToDate(LocalDate.now())
            weekScrollListener = ::onWeekChanged
        }
    }

    private fun onWeekChanged(week: Week) {
        val dayToSelect = week.days.find {
                it.date.dayOfWeek == viewModel.displayedDate.toLocalDate().dayOfWeek
            }
        if (dayToSelect != null) onDateClick(dayToSelect)
    }

    private fun setupRecyclerView() {
        setupLayoutManager()
        binding.calendarContent.adapter = calendarAdapter
        binding.calendarContent.itemAnimator = null
    }

    private fun setupLayoutManager() {
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.calendarContent.apply {
            this.layoutManager = layoutManager
            addItemDecoration(SpacesItemDecoration())
        }
    }

    private fun onFilledTimeSlotClick(slot: FilledTimeSlot) {
        viewModel.intent.value = CalendarIntent.FilledTimeSlotTapped(slot)
    }

    private fun onTimeSlotClick(slot: TimeSlot) {
        viewModel.intent.value = CalendarIntent.TimeSlotTapped(slot)
    }

    private fun onSelectedTimeSlotClick(slot: SelectedTimeSlot) {
        viewModel.intent.value = CalendarIntent.SelectedTimeSlotTapped(slot)
    }

    protected open fun onDateClick(day: WeekDay) {
        val date = day.date
        val oldDate = viewModel.displayedDate.toLocalDate()
        viewModel.displayedDate = date.toDate()
        binding.calendarView.notifyDateChanged(oldDate)
        binding.calendarView.notifyDayChanged(day)
        viewModel.intent.value = CalendarIntent.DateClicked
    }

    private fun registerTimeSlotUpdatesListener() {
        registerBookingSuccessListener()
        registerSessionConfirmListener()
        registerBookingErrorListener()
    }

    private fun registerBookingErrorListener() {
        setFragmentResultListener(BookingErrorDialog.RESULT) { _, _ ->
            viewModel.intent.postValue(CalendarIntent.TimeSlotBusy)
            clearFragmentResultListener(BookingErrorDialog.RESULT)
            registerBookingErrorListener()
        }
    }

    private fun registerSessionConfirmListener() {
        setFragmentResultListener(ProcessSessionConfirmDialog.RESULT) { _, _ ->
            viewModel.intent.postValue(CalendarIntent.TimeSlotCompleted)
            clearFragmentResultListener(ProcessSessionConfirmDialog.RESULT)
            registerSessionConfirmListener()
        }
    }

    private fun registerBookingSuccessListener() {
        setFragmentResultListener(BookingSuccessDialog.RESULT) { _, _ ->
            viewModel.intent.postValue(CalendarIntent.TimeSlotBooked)
            clearFragmentResultListener(BookingSuccessDialog.RESULT)
            registerBookingSuccessListener()
        }
    }

    override fun onDestroy() {
        setFragmentResult(RESULT, bundleOf())
        super.onDestroy()
    }

    companion object {
        const val RESULT = "calendar_fragment_result"
    }
}

class DayViewContainer(
    view: View,
    private val onDateClicked: (day: WeekDay) -> Unit
) : ViewContainer(view) {
    private val cellBinding = ViewUserCalendarDayBinding.bind(view)

    fun bind(data: WeekDay, selectedDate: LocalDate, disableNotSelected: Boolean) {
        val isSelected =
            data.date.year == selectedDate.year && data.date.dayOfYear == selectedDate.dayOfYear
        cellBinding.textCalendarDayOfWeek.isEnabled = isSelected || !disableNotSelected
        cellBinding.textCalendarDayOfWeek.isSelected = isSelected
        cellBinding.textCalendarDayOfWeek.setTextAppearance(
            cellBinding.root.context,
            if (isSelected) com.yourfitness.common.R.style.TextAppearance_YFC_Subheading5
            else com.yourfitness.common.R.style.TextAppearance_YFC_Hint5
        )
        cellBinding.textCalendarDayOfWeek.text = data.date.monthFormatted()
        cellBinding.textCalendarDay.isEnabled = isSelected || !disableNotSelected
        cellBinding.textCalendarDay.isSelected = isSelected
        cellBinding.textCalendarDay.text = data.date.dayOfWeekFormatted()
        cellBinding.viewIndicator.isVisible = isSelected
        cellBinding.root.setOnClickListener { onDateClicked(data) }
    }
}

private fun LocalDate.dayOfWeekFormatted(): String {
    val formatters: DateTimeFormatter = DateTimeFormatter.ofPattern("d")
    return format(formatters)
}

private fun LocalDate.monthFormatted(): String {
    val formatters: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE")
    return format(formatters)
}
