package com.yourfitness.coach.ui.features.facility.booking_class_calendar

 import android.os.Bundle
 import android.view.View
 import androidx.appcompat.widget.Toolbar
 import androidx.core.view.isVisible
 import androidx.fragment.app.*
 import androidx.recyclerview.widget.LinearLayoutManager
 import androidx.recyclerview.widget.RecyclerView
 import com.kizitonwose.calendar.core.WeekDay
 import com.kizitonwose.calendar.view.ViewContainer
 import com.kizitonwose.calendar.view.WeekDayBinder
 import com.yourfitness.coach.R
 import com.yourfitness.coach.databinding.FragmentBookingClassCalendarBinding
 import com.yourfitness.coach.domain.models.CalendarBookClassData
 import com.yourfitness.coach.domain.models.PaymentFlow
 import com.yourfitness.coach.ui.features.facility.class_operations.ClassOperationsFragment
 import com.yourfitness.coach.ui.navigation.Navigation
 import com.yourfitness.coach.ui.utils.SnappingLinearLayoutManager
 import com.yourfitness.coach.ui.utils.addOnScrollStateChangeListener
 import com.yourfitness.common.databinding.ViewFitnessCalendarHorizontalDayBinding
 import com.yourfitness.common.domain.date.daysOfWeek
 import com.yourfitness.common.domain.date.todayLocal
 import com.yourfitness.common.domain.models.ClassCalendarDayViewItem
 import com.yourfitness.common.ui.mvi.viewBinding
 import com.yourfitness.common.ui.utils.getColorCompat
 import dagger.hilt.android.AndroidEntryPoint
 import java.time.LocalDate
 import java.time.format.DateTimeFormatter
 import com.yourfitness.common.R as common

@AndroidEntryPoint
class BookingClassCalendarFragment :
    ClassOperationsFragment<Any, ClassCalendarState, ClassCalendarViewModel>(),
    BookingClassAdapter.ClickListener {

    override val binding: FragmentBookingClassCalendarBinding by viewBinding()
    override val viewModel: ClassCalendarViewModel by viewModels()

    private val dayAdapter by lazy { BookingClassAdapter(this) }
    private var selectedDateDay: LocalDate = LocalDate.now()

    private var daysScrollListener: RecyclerView.OnScrollListener? = null
    private val addingDaysScrollListenerRunnable: Runnable by lazy {
        Runnable {
            daysScrollListener = binding.recyclerViewDay.addOnScrollStateChangeListener(
                onScrollStateChanged = { onDaysScrollStateChanged() }
            )
        }
    }

    override fun setFragmentResultListeners(fragment: Fragment?) {
        super.setFragmentResultListeners(fragment)
        setFragmentResultListener(REQUEST_KEY_BOOKING_CONFIRMATION) { _, bundle ->
            (bundle.get("data") as? CalendarBookClassData)
                ?.let { viewModel.bookingClassConfirmed(it) }
            clearFragmentResult(REQUEST_KEY_BOOKING_CONFIRMATION)
        }
        setFragmentResultListener(REQUEST_KEY_RESCHEDULE_CONFIRMATION) { _, bundle ->
            (bundle.get("data") as? CalendarBookClassData)
                ?.let { viewModel.rescheduleConfirmed(it) }
            clearFragmentResult(REQUEST_KEY_RESCHEDULE_CONFIRMATION)
        }
        setFragmentResultListener(REQUEST_KEY_CREDITS_PURCHASED) { _, bundle ->
            (bundle.get("confirm_booking_data") as? CalendarBookClassData)
                ?.let {
                    val purchasedCredits = bundle.getInt("purchased_credits")
                    viewModel.creditsPurchased(it, purchasedCredits)
                }
            clearFragmentResult(REQUEST_KEY_CREDITS_PURCHASED)
        }
    }

    override fun clearFragmentResultListeners(fragment: Fragment?) {
        super.clearFragmentResultListeners(fragment)
        clearFragmentResultListener(REQUEST_KEY_BOOKING_CONFIRMATION)
        clearFragmentResultListener(REQUEST_KEY_RESCHEDULE_CONFIRMATION)
        clearFragmentResultListener(REQUEST_KEY_CREDITS_PURCHASED)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.toolbar)
        setupScrollListeners()
        binding.recyclerViewDay.layoutManager = SnappingLinearLayoutManager(requireContext())
        binding.recyclerViewDay.adapter = dayAdapter
        binding.viewCreditsAndBonusCredits.textCredits.setOnClickListener {
            viewModel.navigator.navigate(Navigation.BuyCredits(flow = PaymentFlow.BUY_CREDITS_FROM_SCHEDULE))
        }
    }

    override fun onStop() {
        super.onStop()
        binding.root.handler?.removeCallbacksAndMessages(null)
    }

    private fun setupScrollListeners() {
        binding.viewCalendarDay.calendar
            .addOnScrollStateChangeListener(onScrollStateChanged = { onCalendarScrollStateChanged(it) })
        configureDaysScrollListener()
    }

    private fun configureDaysScrollListener() {
        with(binding) {
            daysScrollListener?.let { recyclerViewDay.removeOnScrollListener(it) }
            root.removeCallbacks(addingDaysScrollListenerRunnable)
            root.postDelayed(addingDaysScrollListenerRunnable, 2000)
        }
    }

    private fun onCalendarScrollStateChanged(newState: Int) {
        configureDaysScrollListener()
        with(binding) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                val calendar = binding.viewCalendarDay.calendar
                val startWeekDay = calendar.findFirstVisibleDay()?.date ?: return
                val endWeekDay = calendar.findLastVisibleDay()?.date ?: return
                val visibleWeekRange = startWeekDay.toEpochDay()..endWeekDay.toEpochDay()
                val isLeftDirection = startWeekDay > selectedDateDay
                val newDate = if (isLeftDirection) startWeekDay else endWeekDay
                if (selectedDateDay.toEpochDay() !in visibleWeekRange && selectedDateDay != newDate) {
                    dayAdapter.getPositionByLocalDate(newDate)
                        .takeIf { it in 0 until dayAdapter.itemCount }
                        ?.let { position ->
                            if (selectedDateDay != newDate) {
                                val oldDate = selectedDateDay
                                selectedDateDay = newDate
                                recyclerViewDay.smoothScrollToPosition(position)
                                viewCalendarDay.calendar.notifyDateChanged(oldDate)
                                viewCalendarDay.calendar.notifyDateChanged(newDate)
                            }
                        }
                }
            }
        }
    }

    private fun onDaysScrollStateChanged() {
        val layoutManager = (binding.recyclerViewDay.layoutManager as? LinearLayoutManager)
        val firstVisiblePosition = layoutManager?.findFirstCompletelyVisibleItemPosition() ?: 0
        dayAdapter.getHeaderLocalDateForPosition(firstVisiblePosition)
            ?.let { newDate ->
                if (selectedDateDay != newDate) {
                    val oldDate = selectedDateDay
                    selectedDateDay = newDate
                    binding.viewCalendarDay.calendar.apply {
                        notifyDateChanged(oldDate)
                        notifyDateChanged(newDate)
                        scrollToDate(newDate)
                    }
                }
            }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadData()
    }

    override fun setupToolbar(toolbar: Toolbar) {
        super.setupToolbar(toolbar)
        val isRebook = requireArguments().getBoolean("is_rebook")
        val className = requireArguments().getString("class_name")
        if (isRebook) {
            binding.title.text = getString(R.string.rebook_title, className)
        } else {
            binding.title.text = className
        }
    }

    override fun renderState(state: ClassCalendarState) {
        when (state) {
            is ClassCalendarState.Loading -> showLoading(true)
            is ClassCalendarState.Error -> showError(state.error)
            is ClassCalendarState.CalendarDay -> submitDayList(state)
            is ClassCalendarState.Credits -> setCredits(state)
            is ClassCalendarState.Position -> scrollToPosition(state.position)
        }
    }

    private fun setCredits(state: ClassCalendarState.Credits) {
        with(binding.viewCreditsAndBonusCredits) {
            textCredits.text = getString(R.string.credits, state.credits ?: 0)
            textBonusCredits.text = getString(R.string.credits, state.bonusCredits ?: 0)
            root.isVisible = true
        }
    }

    private fun submitDayList(state: ClassCalendarState.CalendarDay) {
        showLoading(false)
        setupCalendar()
        if (dayAdapter.itemCount == 0) {
            dayAdapter.submitList(state.dayItems)
            binding.recyclerViewDay.scrollToPosition(state.position)
        } else {
            dayAdapter.submitList(state.dayItems)
        }
    }

    private fun setupCalendar() {
        with(binding.viewCalendarDay) {
            if (calendar.adapter == null) {
                calendar.dayBinder = CalendarDayBinder()
                val currentMonth = todayLocal()
                calendar.setup(
                    currentMonth.minusMonths(1),
                    currentMonth.plusMonths(1),
                    daysOfWeek().first()
                )
                calendar.scrollToDate(selectedDateDay)
            }
        }
    }

    private fun selectDate(date: LocalDate) {
        if (selectedDateDay != date) {
            val oldDate = selectedDateDay
            selectedDateDay = date
            binding.viewCalendarDay.calendar.notifyDateChanged(oldDate)
            binding.viewCalendarDay.calendar.notifyDateChanged(date)
        }
        dayAdapter.getPositionByLocalDate(date)
            .takeIf { it in 0 until dayAdapter.itemCount }
            ?.let { binding.recyclerViewDay.smoothScrollToPosition(it) }
    }

    private fun scrollToPosition(position: Int) {
        binding.recyclerViewDay.smoothScrollToPosition(position)
    }

    override fun onBookClassClick(bookedClass: ClassCalendarDayViewItem.Item) {
        viewModel.onBookClassClick(bookedClass)
    }

    override fun onCancelClassClick(bookedClass: ClassCalendarDayViewItem.Item) {
        viewModel.onCancelBookedClassClick(bookedClass)
    }

    private inner class CalendarDayBinder : WeekDayBinder<DayViewContainer> {

        private val dayFormatter = DateTimeFormatter.ofPattern("EEE")
        private val dateFormatter = DateTimeFormatter.ofPattern("dd")

        private val blueColor = requireContext().getColorCompat(common.color.blue)
        private val greyColor = requireContext().getColorCompat(common.color.grey)
        private val blackColor = requireContext().getColorCompat(common.color.black)
        private val grayLightColor = requireContext().getColorCompat(common.color.gray_light)

        override fun create(view: View) = DayViewContainer(view)

        override fun bind(container: DayViewContainer, day: WeekDay) {
            container.day = day
            with(container.binding) {
                textCalendarDay.text = dateFormatter.format(day.date)
                textCalendarDayOfWeek.text = dayFormatter.format(day.date)
                viewIndicator.isVisible = day.date == selectedDateDay
                setupColorDate(day)
            }
        }

        private fun ViewFitnessCalendarHorizontalDayBinding.setupColorDate(day: WeekDay) {
            when {
                day.date == LocalDate.now() -> {
                    textCalendarDayOfWeek.setTextColor(blueColor)
                    textCalendarDay.setTextColor(blueColor)
                }
                viewModel.isEventExist(day.date) -> {
                    textCalendarDayOfWeek.setTextColor(greyColor)
                    textCalendarDay.setTextColor(blackColor)
                }
                else -> {
                    textCalendarDayOfWeek.setTextColor(grayLightColor)
                    textCalendarDay.setTextColor(grayLightColor)
                }
            }
        }
    }

    private inner class DayViewContainer(view: View) : ViewContainer(view) {

        val binding = ViewFitnessCalendarHorizontalDayBinding.bind(view)
        lateinit var day: WeekDay

        init {
            view.setOnClickListener { selectDate(day.date) }
        }
    }

    companion object {
        const val REQUEST_KEY_BOOKING_CONFIRMATION = "request_key_booking_confirmation"
        const val REQUEST_KEY_RESCHEDULE_CONFIRMATION = "request_key_reschedule_confirmation"
        const val REQUEST_KEY_CREDITS_PURCHASED = "request_key_credits_purchased"
    }
}