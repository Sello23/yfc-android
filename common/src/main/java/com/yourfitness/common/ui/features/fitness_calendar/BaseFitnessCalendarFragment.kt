package com.yourfitness.common.ui.features.fitness_calendar

import android.content.Context
import android.content.res.Resources
import android.graphics.PointF
import android.graphics.Rect
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.clearFragmentResult
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.Week
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import com.kizitonwose.calendar.view.*
import com.yourfitness.common.R
import com.yourfitness.common.databinding.FragmentFitnessCalendarBinding
import com.yourfitness.common.databinding.ViewCalendarMonthDayBinding
import com.yourfitness.common.databinding.ViewFitnessCalendarHorizontalDayBinding
import com.yourfitness.common.domain.date.daysOfWeek
import com.yourfitness.common.domain.date.toDate
import com.yourfitness.common.domain.date.toLocalDate
import com.yourfitness.common.domain.date.todayLocal
import com.yourfitness.common.domain.models.CalendarView as Calendar
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.addOnTabSelectionListener
import com.yourfitness.common.ui.utils.selectTab
import com.yourfitness.common.ui.utils.setTextColorRes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*


abstract class BaseFitnessCalendarFragment<VM : BaseFitnessCalendarViewModel,
        VH : AbstractMonthViewHolder, VH2 : BaseWeekViewHolder, VH3 : BaseDayViewHolder>
    : MviFragment<BaseFitnessCalendarIntent, BaseFitnessCalendarState, VM>() {

    override val binding: FragmentFitnessCalendarBinding by viewBinding()

    private val today = todayLocal()

    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM y")
    private val selectionFormatter = DateTimeFormatter.ofPattern("d MMMM")
    private val dayFormatter = DateTimeFormatter.ofPattern("EEE")
    private val dateFormatter = DateTimeFormatter.ofPattern("dd")

    private var calendar: CalendarView? = null
    private var weekCalendar: WeekCalendarView? = null

    protected abstract val baseMonthAdapter: BaseMonthAdapter<VH>
    protected abstract val weekAdapter: BaseWeekAdapter<VH2>
    protected abstract val dayAdapter: BaseDayAdapter<VH3>

    private val weekAdapterObserver by lazy { AdapterUpdateObserver(::scrollToWeekStartPosition) }
    private val dayAdapterObserver by lazy { AdapterUpdateObserver(::scrollToDayStartPosition) }

    protected val dayListScrollListener = DayListScrollListener()

    private var startPositionWeek = 0
    private var startPositionDay = 0
    private var currentPositionWeek = 0
    private var currentPositionDay = 0

    protected var trackDayScrollState = true
    protected var needScrollToState = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureTabBar()

        binding.apply {
            toolbar.toolbarSolid.title =
                getString(R.string.fitness_calendar_screen_fitness_calendar_text)
            setupToolbar(toolbar.root)
            recyclerViewWeek.apply {
                adapter = weekAdapter
                layoutManager = SnappingLinearLayoutManager(requireContext())
            }
            recyclerViewDay.apply {
                adapter = dayAdapter
                layoutManager = SnappingLinearLayoutManager(requireContext())
                addOnScrollListener(dayListScrollListener)
            }
            viewCalendarMonth.recyclerView.apply {
                adapter = baseMonthAdapter
                addItemDecoration(SpacesItemDecoration())
            }
            tabLayout.apply {
                selectTab(viewModel.getTabIndexToSelect())
                addOnTabSelectionListener {
                    needScrollToState = true
                    viewModel.intent.value = BaseFitnessCalendarIntent.OnTabChanged(getTabIndex(it.position))
                }
            }
        }
    }

    protected open fun configureTabBar() {
        binding.tabLayout.removeTabAt(0)
    }

    protected open fun getTabIndex(position: Int): Int = position

    override fun setupToolbar(toolbar: Toolbar) {
        super.setupToolbar(toolbar)
        setupOptionsMenu()
    }

    protected open fun setupOptionsMenu() {
        setupOptionsMenu(binding.toolbar.toolbarSolid, R.menu.fitness_calendar) {
            onMenuItemSelected(it)
        }
    }

    protected open fun onMenuItemSelected(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.fitness_calendar -> moveToCurrentDay()
        }
    }

    override fun onStart() {
        super.onStart()
        setFragmentResultListeners(this)
    }

    override fun onResume() {
        weekAdapter.registerAdapterDataObserver(weekAdapterObserver)
        dayAdapter.registerAdapterDataObserver(dayAdapterObserver)
        super.onResume()
    }

    override fun onPause() {
        weekAdapter.unregisterAdapterDataObserver(weekAdapterObserver)
        dayAdapter.unregisterAdapterDataObserver(dayAdapterObserver)
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        clearFragmentResultListeners(this)
    }

    override fun renderState(state: BaseFitnessCalendarState) {
        when (state) {
            is BaseFitnessCalendarState.Loading -> showLoading(true)
            is BaseFitnessCalendarState.Error -> showLoading(false)
            is BaseFitnessCalendarState.MonthTabDataLoaded -> showMonthTabData(state.items, state.dateToScroll)
            is BaseFitnessCalendarState.WeekDataLoaded -> showWeekTabData(state)
            is BaseFitnessCalendarState.DayDataLoaded -> showDayTabData(state)
            is BaseFitnessCalendarState.Position -> smoothScrollToSelectedPosition()
        }
    }

    private fun showMonthTabData(events: Events, dateToScroll: LocalDate?) {
        showLoading(false)
        if (dateToScroll != null) selectDate(dateToScroll)
        currentPositionWeek = 0
        currentPositionDay = 0
        updateAdapterForDate(events)
        setupMonthCalendarView()
        binding.apply {
            nestedScroll.isVisible = true
            recyclerViewSchedule.isVisible = false
            recyclerViewWeek.isVisible = false
            groupCalendarDay.isVisible = false
        }
    }

    private fun setupMonthCalendarView() {
        val daysOfWeek = daysOfWeek()

        binding.viewCalendarMonth.viewCalendar.calendarMonth.apply {
            if (dayBinder == null) {
                calendar = this

                dayBinder = MonthTabDayBinder()

                val currentMonth =
                    YearMonth.of(viewModel.selectedDate.year, viewModel.selectedDate.monthValue)
                setup(currentMonth.minusYears(1), currentMonth.plusYears(1), daysOfWeek.first())
                scrollToMonth(currentMonth)

                monthScrollListener = {
                    val lastDay = it.yearMonth.month.length(it.yearMonth.isLeapYear)
                    val selectedDate =
                        it.yearMonth.atDay(minOf(viewModel.selectedDate.dayOfMonth, lastDay))
                    if (!isSameMonth(selectedDate)) {
                        selectDate(selectedDate)
                    }
                }
            } else {
                calendar?.notifyCalendarChanged()
            }
        }

        binding.viewCalendarMonth.viewCalendar.apply {
            textCurrentMonth.text = viewModel.selectedDate.format(monthTitleFormatter)
            viewCalendarDayLegend.root.children.forEachIndexed { index, view ->
                (view as TextView).apply {
                    text = daysOfWeek[index].getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                        .replaceFirstChar { it.titlecaseChar() }
                    setTextColorRes(R.color.black)
                }
            }

            buttonNextMonth.setOnClickListener {
                calendarMonth.findFirstVisibleMonth()?.let {
                    calendarMonth.smoothScrollToMonth(it.yearMonth.nextMonth)
                }
            }

            buttonPrevMonth.setOnClickListener {
                calendarMonth.findFirstVisibleMonth()?.let {
                    calendarMonth.smoothScrollToMonth(it.yearMonth.previousMonth)
                }
            }
        }
    }

    private fun isSameMonth(date: LocalDate): Boolean {
        return viewModel.selectedDate.monthValue == date.monthValue
                && viewModel.selectedDate.year == date.year
    }

    private fun showWeekTabData(state: BaseFitnessCalendarState.WeekDataLoaded) {
        showLoading(false)
        weekAdapter.submitList(state.weekItems)
        currentPositionDay = 0
        startPositionWeek = state.position
        binding.apply {
            recyclerViewWeek.isVisible = true
            recyclerViewSchedule.isVisible = false
            nestedScroll.isVisible = false
            groupCalendarDay.isVisible = false
        }
    }

    private fun showDayTabData(state: BaseFitnessCalendarState.DayDataLoaded) {
        showLoading(false)
        dayAdapter.setData(state.dayItems)
        currentPositionWeek = 0
        startPositionDay = state.position
        setupCalendarDayView()
        binding.apply {
            recyclerViewSchedule.isVisible = false
            recyclerViewWeek.isVisible = false
            nestedScroll.isVisible = false
            groupCalendarDay.isVisible = true
        }

        smoothScrollToSelectedPosition()
    }

    private fun smoothScrollToSelectedPosition() {
        val indexToScroll = dayAdapter.currentList.indexOfFirst {
            it is Calendar.Header && viewModel.selectedDate == it.startDate.toDate()
                ?.toLocalDate()
        }
        if (indexToScroll < 0) return
        smoothScrollToDayPosition(indexToScroll)
        lifecycleScope.launch {
            delay(300)
            trackDayScrollState = true
        }
    }

    private fun setupCalendarDayView() {
        binding.viewCalendarDay.calendar.apply {
            if (dayBinder == null) {
                weekCalendar = this
                dayBinder = DayTabDayBinder()
                val currentMonth = todayLocal()
                val daysOfWeek = daysOfWeek()
                setup(
                    currentMonth.minusYears(1),
                    currentMonth.plusYears(1),
                    daysOfWeek.first()
                )
                trackDayScrollState = false
                scrollToDate(viewModel.selectedDate)
                weekScrollListener = ::onWeekChanged
            } else {
                weekCalendar?.notifyCalendarChanged()
            }
        }
    }

    private fun onWeekChanged(week: Week) {
        val dayToSelect = week.days.find {
            it.date.dayOfWeek == viewModel.selectedDate.dayOfWeek
        }
        if (dayToSelect != null) selectDayDate(dayToSelect.date)
    }

    private fun selectDate(date: LocalDate) {
        calendar?.notifyDateChanged(viewModel.selectedDate)
        calendar?.notifyDateChanged(date)
        viewModel.intent.value = BaseFitnessCalendarIntent.SelectedDateChanged(date)
    }

    private fun selectDayDate(date: LocalDate) {
        weekCalendar?.notifyDateChanged(viewModel.selectedDate)
        weekCalendar?.notifyDateChanged(date)
        viewModel.intent.value = BaseFitnessCalendarIntent.SelectedDateChanged(date)
    }

    private fun updateAdapterForDate(events: Events) {
        binding.viewCalendarMonth.textCurrentDay.text =
            getString(R.string.fitness_calendar_screen_day_and_day_week_text,
                selectionFormatter.format(viewModel.selectedDate),
                viewModel.selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                    .replaceFirstChar { it.titlecaseChar() })
        binding.viewCalendarMonth.textNoClassesBooked.isVisible =
            events[viewModel.selectedDate].isNullOrEmpty()
        baseMonthAdapter.setData(events[viewModel.selectedDate].orEmpty())
    }

    private fun smoothScrollToDayPosition(position: Int) {
        try {
            binding.recyclerViewDay.smoothScrollToPosition(position)
            currentPositionDay = position
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun scrollToCalendarDayPosition() {
        try {
            var pos = currentPositionDay
            var item = dayAdapter.currentList.getOrNull(currentPositionDay)
            while (item == null || item !is Calendar.Header) {
                pos += 1
                item = dayAdapter.currentList.getOrNull(pos)
            }
            val dateToScroll = item.startDate.toDate()?.toLocalDate()
            if (dateToScroll != null) {
                weekCalendar?.notifyDateChanged(viewModel.selectedDate)
                weekCalendar?.notifyDateChanged(dateToScroll)
                viewModel.intent.value =
                    BaseFitnessCalendarIntent.DayTabSelectedDateUpdated(dateToScroll)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    protected open fun moveToCurrentDay() {
        with(binding) {
            when (viewModel.tabPosition) {
                MONTH_TAB -> {
                    viewCalendarMonth.viewCalendar.calendarMonth.also { calendar ->
                        calendar.findFirstVisibleMonth()
                            ?.let { calendar.smoothScrollToMonth(YearMonth.now()) }
                    }
                    selectDate(today)
                }
                WEEK_TAB -> {
                    recyclerViewWeek.scrollToPosition(startPositionWeek)
                }
                DAY_TAB -> {
                    smoothScrollToDayPosition(startPositionDay)
                    viewCalendarDay.calendar.scrollToDate(today)
                    selectDayDate(today)
                }
            }
        }
    }

    protected fun onItemClick() {
        when (binding.tabLayout.selectedTabPosition) {
            WEEK_TAB -> {
                currentPositionWeek = binding.recyclerViewWeek.getCurrentPosition()
            }
            DAY_TAB -> {
                currentPositionDay = binding.recyclerViewDay.getCurrentPosition()
            }
        }
    }

    protected open fun setFragmentResultListeners(fragment: Fragment?) {
        fragment?.setFragmentResultListener(REQUEST_KEY_CANCEL_CONFIRMATION) { _, bundle ->
            onCancelConfirm(bundle)
            clearFragmentResult(REQUEST_KEY_CANCEL_CONFIRMATION)
        }
    }

    protected open fun onCancelConfirm(bundle: Bundle) {}

    protected open fun clearFragmentResultListeners(fragment: Fragment?) {
        fragment?.clearFragmentResultListener(REQUEST_KEY_CANCEL_CONFIRMATION)
    }

    private fun scrollToDayStartPosition() {
        if (!needScrollToState) return
        binding.recyclerViewDay.post {
            if (currentPositionDay != 0) {
                binding.recyclerViewDay.smoothScrollToPosition(currentPositionDay)
            } else {
                binding.recyclerViewDay.smoothScrollToPosition(startPositionDay)
            }
            needScrollToState = false
        }
    }

    private fun scrollToWeekStartPosition() {
        if (!needScrollToState) return
        binding.recyclerViewWeek.post {
            if (currentPositionWeek != 0) {
                binding.recyclerViewWeek.smoothScrollToPosition(currentPositionWeek)
            } else {
                binding.recyclerViewWeek.smoothScrollToPosition(startPositionWeek)
            }
            needScrollToState = false
        }
    }

    inner class DayTabViewContainer(view: View) : ViewContainer(view) {
        val bindingView = ViewFitnessCalendarHorizontalDayBinding.bind(view)
        lateinit var day: WeekDay

        init {
            view.setOnClickListener {
                trackDayScrollState = false
                selectDayDate(day.date)
//                viewModel.getDayPosition(
//                    day.date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
//                )
            }
        }
    }


    inner class DayViewContainer(view: View) : ViewContainer(view) {
        lateinit var day: CalendarDay
        val binding = ViewCalendarMonthDayBinding.bind(view)

        init {
            view.setOnClickListener {
                if (day.position == DayPosition.MonthDate) {
                    selectDate(day.date)
                }
            }
        }
    }

    inner class MonthTabDayBinder : MonthDayBinder<DayViewContainer> {
        override fun create(view: View) = DayViewContainer(view)
        override fun bind(
            container: BaseFitnessCalendarFragment<VM, VH, VH2, VH3>.DayViewContainer,
            day: CalendarDay
        ) {
            container.day = day
            val textView = container.binding.textCalendarMonthDay
            val dotView = container.binding.viewDot
            textView.text = day.date.dayOfMonth.toString()

            if (day.position == DayPosition.MonthDate) {
                textView.isVisible = true
                when (day.date) {
                    today -> {
                        textView.setTextColorRes(R.color.white)
                        textView.setBackgroundResource(R.drawable.calendar_selected_day_background)
                        dotView.setBackgroundResource(R.drawable.calendar_dot_event_selected)
                        dotView.isVisible = viewModel.events[day.date].orEmpty().isNotEmpty()
                        if (viewModel.selectedDate != today) {
                            textView.setTextColorRes(R.color.blue)
                            textView.background = null
                            dotView.setBackgroundResource(R.drawable.calendar_dot_event)
                            dotView.isVisible = viewModel.events[day.date].orEmpty().isNotEmpty()
                        }
                    }
                    viewModel.selectedDate -> {
                        textView.setTextColorRes(R.color.white)
                        textView.setBackgroundResource(R.drawable.calendar_selected_day_background)
                        dotView.setBackgroundResource(R.drawable.calendar_dot_event_selected)
                        dotView.isVisible = viewModel.events[day.date].orEmpty().isNotEmpty()
                    }
                    else -> {
                        textView.setTextColorRes(R.color.black)
                        textView.background = null
                        dotView.setBackgroundResource(R.drawable.calendar_dot_event)
                        dotView.isVisible = viewModel.events[day.date].orEmpty().isNotEmpty()
                    }
                }
            } else {
                textView.setTextColorRes(R.color.gray_light)
                dotView.isVisible = false
            }
        }
    }

    inner class DayTabDayBinder : WeekDayBinder<DayTabViewContainer> {
        override fun create(view: View) = DayTabViewContainer(view)
        override fun bind(container: DayTabViewContainer, day: WeekDay) {
            container.day = day
            val textCalendarDay = container.bindingView.textCalendarDay
            val textCalendarDayOfWeek = container.bindingView.textCalendarDayOfWeek
            val viewIndicator = container.bindingView.viewIndicator
            textCalendarDay.text = dateFormatter.format(day.date)
            textCalendarDayOfWeek.text = dayFormatter.format(day.date)
            viewIndicator.isVisible = day.date == viewModel.selectedDate

            when (day.date) {
                viewModel.selectedDate -> {
                    textCalendarDayOfWeek.setTextColorRes(R.color.blue)
                    textCalendarDay.setTextColorRes(R.color.blue)
                }
                else -> {
                    if (viewModel.events[day.date].orEmpty().isNotEmpty()) {
                        textCalendarDayOfWeek.setTextColorRes(R.color.grey)
                        textCalendarDay.setTextColorRes(R.color.black)
                    } else {
                        textCalendarDayOfWeek.setTextColorRes(R.color.gray_light)
                        textCalendarDay.setTextColorRes(R.color.gray_light)
                    }
                }
            }
        }
    }

    inner class DayListScrollListener: RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (!trackDayScrollState) return
            val newPos = binding.recyclerViewDay.getCurrentPosition()
            if (newPos != currentPositionDay) {
                currentPositionDay = newPos
                scrollToCalendarDayPosition()
            }
        }
    }

    inner class AdapterUpdateObserver(val scrollToStartPosition: () -> Unit) : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            scrollToStartPosition()
        }
    }

    companion object {
        const val REQUEST_KEY_CANCEL_CONFIRMATION = "request_key_cancel_confirmation"

        const val MONTH_TAB = 0
        const val WEEK_TAB = 1
        const val DAY_TAB = 2
    }
}

fun RecyclerView?.getCurrentPosition(): Int {
    return (this?.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
}

class SpacesItemDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.top = 6.toPx()
        outRect.bottom = 6.toPx()
    }
}

class SnappingLinearLayoutManager(
    context: Context?,
    orientation: Int = RecyclerView.VERTICAL,
    reverseLayout: Boolean = false
) : LinearLayoutManager(context, orientation, reverseLayout) {

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView, state: RecyclerView.State,
        position: Int
    ) {
        val smoothScroller: SmoothScroller = TopSnappedSmoothScroller(recyclerView.context)
        smoothScroller.targetPosition = position
        startSmoothScroll(smoothScroller)
    }

    private inner class TopSnappedSmoothScroller(context: Context?) :
        LinearSmoothScroller(context) {
        override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
            return this@SnappingLinearLayoutManager
                .computeScrollVectorForPosition(targetPosition)
        }

        override fun getVerticalSnapPreference(): Int {
            return SNAP_TO_START
        }
    }
}

fun Int.toPx() = (this * Resources.getSystem().displayMetrics.density).toInt()
