package com.yourfitness.coach.ui.features.progress.fitness_info

import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekDayBinder
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.FragmentFitnessDataBinding
import com.yourfitness.coach.databinding.ViewCalendarHorizontalDayBinding
import com.yourfitness.coach.domain.date.daysOfWeek
import com.yourfitness.coach.ui.features.progress.ProgressFragment
import com.yourfitness.coach.ui.features.progress.ProgressState
import com.yourfitness.coach.ui.features.progress.points.FitnessDataUpdateContract
import com.yourfitness.coach.ui.features.progress.points.PointsFragment
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.common.domain.date.atDayStart
import com.yourfitness.common.domain.date.todayLocal
import com.yourfitness.common.domain.date.todayZonedNull
import com.yourfitness.common.domain.date.utcToZoneNull
import com.yourfitness.common.ui.utils.doubleToStringNoDecimal
import com.yourfitness.common.ui.utils.formatCoins
import com.yourfitness.common.ui.utils.getColorCompat
import com.yourfitness.community.ui.features.likes.LikesListDialog
import com.yourfitness.community.ui.features.likes.WorkoutLikesFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import kotlin.math.min
import com.yourfitness.common.R as common

@AndroidEntryPoint
class FitnessInfoFragment : ProgressFragment(), FitnessDataUpdateContract {

    override val viewModel: FitnessInfoViewModel by viewModels()
    private lateinit var bindingAppBar: FragmentFitnessDataBinding

    private val dayFormatter = DateTimeFormatter.ofPattern("EEE")
    private val dateFormatter = DateTimeFormatter.ofPattern("dd")
    private val dateFormatterRing = DateTimeFormatter.ofPattern("MMM dd")
    private val likesFragment: WorkoutLikesFragment? by lazy {
        childFragmentManager.findFragmentById(R.id.fragment_likes) as WorkoutLikesFragment?
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindingAppBar = binding.fitnessInfo
        setupUserBalance()
        checkGoogleFitConnection(viewModel.isProviderConnected)
        setupCalendar()
        setupListeners()
        setupLikesDetailsResultListener()
//        bindingAppBar.viewProgressScreenDataChart.viewCoinsAndCredits.textCredits.setOnClickListener {
//            viewModel.navigator.navigate(Navigation.BuyCredits(flow = PaymentFlow.BUY_CREDITS_FROM_PROGRESS))
//        }
    }

    override fun renderState(state: ProgressState) {
        when (state) {
            is FitnessInfoState.Success -> setupUserBalance(state.coins, state.credits)
            is FitnessInfoState.GoogleFitData -> {
                state.isProviderConnected?.let { checkGoogleFitConnection(it)}
                showDailyGoogleFitData(state)
            }
            is FitnessInfoState.LoadCalendar -> {
                state.isProviderConnected?.let { checkGoogleFitConnection(it)}
                setupCalendar()
            }
            else -> super.renderState(state)
        }
    }

    private fun setupListeners() {
        bindingAppBar.viewProgressScreenDataChart.viewBlur.buttonConnectDevice.setOnClickListener {
            viewModel.navigator.navigate(Navigation.ConnectedDevices)
        }

        val pointsFragment = childFragmentManager.findFragmentById(R.id.fragment_points) as PointsFragment
        pointsFragment.registerFitnessDataUpdateListener(this)
    }

    private fun setupUserBalance(coins: Long = 0, credits: Long = 0) {
        bindingAppBar.viewProgressScreenDataChart.viewCoinsAndCredits.textCoins.text =
            resources.getQuantityString(
                common.plurals.profile_screen_format_coins,
                coins.toInt(),
                coins.formatCoins()
            )
//        bindingAppBar.viewProgressScreenDataChart.viewCoinsAndCredits.textCredits.text =
//            getString(R.string.profile_screen_format_credits, credits)
    }

    private fun showDailyGoogleFitData(state: FitnessInfoState.GoogleFitData) {
        if (state.isFirstLoad) {
            setupCalendar()
        }
        bindingAppBar.viewProgressScreenDataChart.viewStepsKcalPoints.apply {
            textStepsAmount.text = doubleToStringNoDecimal(state.dailyGoogleFitData.steps)
            setRingCharPosition(state.dailyGoogleFitData.steps, state.settingsGlobal?.maxStepsPerDay, Companion.RingType.STEPS_RING)
            textKcalAmount.text = doubleToStringNoDecimal(state.dailyGoogleFitData.calories)
            setRingCharPosition(state.dailyGoogleFitData.calories, state.settingsGlobal?.maxCaloriesPerDay, Companion.RingType.CALORIES_RING)
            textPointsAmount.text = doubleToStringNoDecimal(state.dailyGoogleFitData.points)
            setRingCharPosition(state.dailyGoogleFitData.points, state.settingsGlobal?.maxPointsPerDay, Companion.RingType.POINTS_RING)
        }
    }

    private fun checkGoogleFitConnection(isAllowed: Boolean) {
        lifecycleScope.launch(Dispatchers.Main) {
            if (isAllowed) {
                bindingAppBar.viewProgressScreenDataChart.groupViewBlur.isVisible = false
                bindingAppBar.viewProgressScreenDataChart.textHeader.text = getString(R.string.progress_screen_keep_moving_text)
            } else {
                bindingAppBar.viewProgressScreenDataChart.viewStepsKcalPoints.textKcalAmount.text =
                    ZERO_DATA
                bindingAppBar.viewProgressScreenDataChart.viewStepsKcalPoints.textStepsAmount.text =
                    ZERO_DATA
                bindingAppBar.viewProgressScreenDataChart.viewStepsKcalPoints.textPointsAmount.text =
                    ZERO_DATA
                bindingAppBar.viewProgressScreenDataChart.groupViewBlur.isVisible = true
                bindingAppBar.viewProgressScreenDataChart.textHeader.text = getString(R.string.progress_screen_ready_lets_go_text)
            }
        }
    }

    private fun setRingCharPosition(param: Double, maxParam: Double?, ringType: RingType) {
        val view = bindingAppBar.viewProgressScreenDataChart.viewRingChart
        val ringView = when (ringType) {
            RingType.STEPS_RING -> view.viewRingChartSteps
            RingType.CALORIES_RING -> view.viewRingChartCalories
            RingType.POINTS_RING -> view.viewRingChartPoints
        }
        val max = maxParam ?: MAX_PARAM_DEFAULT
        val current = min(param, max)
        ringView.setProgress(current, max)
    }

    private fun setupCalendar() {
        likesFragment?.updateLikes(todayZonedNull(viewModel.zoneOffset)?.atDayStart(), viewModel.zoneOffset)
        class DayViewContainer(view: View) : ViewContainer(view) {
            val bind = ViewCalendarHorizontalDayBinding.bind(view)
            lateinit var day: WeekDay

            init {
                view.setOnClickListener {
                    val selectedDate = day.date.utcToZoneNull(viewModel.zoneOffset)
                    if (viewModel.selectedDate == selectedDate) return@setOnClickListener
                    val oldDate = viewModel.selectedDate
                    if (selectedDate != null) {
                        viewModel.intent.value = FitnessInfoIntent.SelectedDateChanged(selectedDate)
                        likesFragment?.updateLikes(selectedDate, viewModel.zoneOffset)
                    }
                    bindingAppBar.viewProgressScreenDataChart.viewRingChart.textToday.text =
                        if (selectedDate != todayZonedNull(viewModel.zoneOffset)) {
                            dateFormatterRing.format(selectedDate)
                        } else {
                            getString(R.string.progress_screen_today_text)
                        }
                    viewModel.selectedDate = selectedDate
                    bindingAppBar.viewCalendarHorizontal.calendar.notifyDateChanged(day.date)
                    oldDate?.let { bindingAppBar.viewCalendarHorizontal.calendar.notifyDateChanged(it.toLocalDate()) }
                }
            }

            fun bind(day: WeekDay) {
                this.day = day
                val zonedDay = day.date.utcToZoneNull(viewModel.zoneOffset)
                bind.textCalendarDay.text = dateFormatter.format(day.date)
                bind.textCalendarDayOfWeek.text = dayFormatter.format(day.date)

                bind.textCalendarDay.setTextColor(view.context.getColorCompat(common.color.white))
                bind.textCalendarDayOfWeek.setTextColor(view.context.getColorCompat(
                    if (zonedDay == viewModel.selectedDate) common.color.white
                    else common.color.gray_light
                ))
                if (zonedDay == viewModel.selectedDate) {
                    val typeface = ResourcesCompat.getFont(
                        requireContext(),
                        com.yourfitness.common.R.font.work_sans_bold
                    )
                    bind.textCalendarDayOfWeek.typeface = typeface
                }
                bind.viewIndicator.isVisible = zonedDay == viewModel.selectedDate
            }
        }

        bindingAppBar.viewCalendarHorizontal.calendar.dayBinder = object : WeekDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: WeekDay) = container.bind(day)
        }

        viewModel.zoneOffset?.let {
            val currentMonth = todayLocal(it)
            val daysOfWeek = daysOfWeek()
            bindingAppBar.viewCalendarHorizontal.calendar.setup(currentMonth.minusMonths(1), currentMonth.plusMonths(1), daysOfWeek.first())
            bindingAppBar.viewCalendarHorizontal.calendar.scrollToDate(currentMonth)
        }
    }

    private fun setupLikesDetailsResultListener() {
        setFragmentResultListener(LikesListDialog.RESULT) { key, bundle ->
            likesFragment?.updateLikes(viewModel.selectedDate, viewModel.zoneOffset)
            clearFragmentResultListener(LikesListDialog.RESULT)
            setupLikesDetailsResultListener()
        }
    }

    override fun onFitnessDataFetched() = viewModel.intent.postValue(FitnessInfoIntent.UpdatePointsData)

    companion object {
        private enum class RingType {
            STEPS_RING, CALORIES_RING, POINTS_RING
        }
        private const val MAX_PARAM_DEFAULT = 20000.0
        private const val ZERO_DATA = "0"
        private const val ETC_UTC = "Etc/UTC"
    }
}
