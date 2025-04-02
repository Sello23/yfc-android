package com.yourfitness.pt.ui.features.training_calendar

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.clearFragmentResult
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.yourfitness.common.ui.features.fitness_calendar.BaseFitnessCalendarState
import com.yourfitness.common.ui.features.fitness_calendar.SnappingLinearLayoutManager
import com.yourfitness.pt.R
import com.yourfitness.pt.databinding.ItemStatusChipBinding
import com.yourfitness.pt.domain.models.StatusFilter
import com.yourfitness.pt.ui.features.calendar.CalendarFragmentPt
import com.yourfitness.pt.ui.features.training_calendar.adapters.PtScheduleAdapter
import com.yourfitness.pt.ui.features.training_calendar.block_slots.BlockTimeSlotDialog
import com.yourfitness.pt.ui.features.training_calendar.block_slots.conflicts.list.BlockSlotsFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrainingCalendarPtFragment : TrainingCalendarFragment(), PopupMenu.OnMenuItemClickListener {

    override val viewModel: TrainingCalendarPtViewModel by viewModels()

    private val scheduleAdapter by lazy { PtScheduleAdapter(::onItemClick) }
    private var currentPositionSchedule = 0
    private var startPositionSchedule = 0

    private val adapterObserver by lazy { AdapterUpdateObserver(::scrollToStartPosition) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            recyclerViewSchedule.apply {
                adapter = scheduleAdapter
                layoutManager = SnappingLinearLayoutManager(requireContext())
            }
        }
        setupBlockSlotListener()
        setupBlockSlotListUpdateListener()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        scheduleAdapter.registerAdapterDataObserver(adapterObserver)
        super.onResume()
    }

    override fun onPause() {
        scheduleAdapter.unregisterAdapterDataObserver(adapterObserver)
        super.onPause()
    }

    override fun setupToolbar(toolbar: Toolbar) {
        if (!viewModel.isMainScreen) {
            super.setupToolbar(toolbar)
        } else {
            setupOptionsMenu()
        }
    }

    override fun configureTabBar() {
        binding.tabLayout.getTabAt(0)?.view?.isVisible = true
    }


    override fun getTabIndex(position: Int): Int {
        return if (position == 0) SCHEDULE_TAB
        else position - 1
    }

    override fun renderState(state: BaseFitnessCalendarState) {
        when (state) {
            is TrainingCalendarPtState.ScheduleTabLoaded -> showScheduleTabData(state)
            is BaseFitnessCalendarState.MonthTabDataLoaded,
            is BaseFitnessCalendarState.WeekDataLoaded,
            is BaseFitnessCalendarState.DayDataLoaded -> showFilters(viewModel.statusFilter)
        }
        super.renderState(state)
    }

    override fun moveToCurrentDay() {
        with(binding) {
            when (viewModel.tabPosition) {
                SCHEDULE_TAB -> {
                    recyclerViewSchedule.smoothScrollToPosition(startPositionSchedule)
                }
                else -> super.moveToCurrentDay()

            }
        }
    }

    override fun onItemClick (sessionId: String, status: String) {
        onItemClick()
        viewModel.intent.value = TrainingCalendarIntent.OnSessionActionClicked(sessionId, status)
    }

    override fun setupOptionsMenu() {
        setupOptionsMenu(binding.toolbar.toolbarSolid, R.menu.fitness_calendar_pt) {
            onMenuItemSelected(it)
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.fitness_calendar_pt -> {
                if (viewModel.blockedSlotsNumber == 0) {
                    manageTimeSlots()
                } else {
                    showPopup()
                }
            }
            else -> super.onMenuItemSelected(menuItem)
        }
    }

    private fun showPopup() {
        val view = requireActivity().findViewById<View>(R.id.fitness_calendar_pt)
        PopupMenu(requireContext(), view).apply {
            setOnMenuItemClickListener(this@TrainingCalendarPtFragment)
            inflate(R.menu.block_slots_actions)
            show()
        }
    }

    private fun showScheduleTabData(state: TrainingCalendarPtState.ScheduleTabLoaded) {
        showLoading(false)
        showFilters(viewModel.statusFilter)
        currentPositionSchedule = state.scrollToPosition ?: 0
        scheduleAdapter.submitList(state.scheduleItems)
        startPositionSchedule = state.position
        binding.apply {
            recyclerViewSchedule.isVisible = true
            recyclerViewWeek.isVisible = false
            nestedScroll.isVisible = false
            groupCalendarDay.isVisible = false
        }
    }

    private fun scrollToStartPosition() {
        if (!needScrollToState) return
        binding.recyclerViewSchedule.post {
            if (currentPositionSchedule != 0) {
                binding.recyclerViewSchedule.smoothScrollToPosition(currentPositionSchedule)
            } else {
                binding.recyclerViewSchedule.smoothScrollToPosition(startPositionSchedule)
            }
            needScrollToState = false
        }
    }

    private fun showFilters(filter: StatusFilter) {
        binding.chipContainer.isVisible = true
        val parent = binding.filterChipGroup
        parent.removeAllViews()
        filter.allStatuses.forEach { item ->
            val binding = ItemStatusChipBinding.inflate(layoutInflater, parent, false)
            binding.apply {
                check.text = getString(item.second)
                amount.isVisible = item.first != StatusFilter.EMPTY_STATE
                amount.text = (filter.statusAmounts[item.first] ?: 0).toString()
                root.apply {
                    val selected = filter.selectedStatuses.contains(item.first)
                    isSelected = selected
                    check.isSelected = selected
                    amount.isSelected = selected

                    setOnClickListener {
                        viewModel.intent.value = TrainingCalendarPtIntent.FilterItemTapped(item.first, !selected)
                    }
                    parent.addView(this)
                }
            }
        }
    }

    private fun manageTimeSlots() {
        setFragmentResultListener(CalendarFragmentPt.RESULT) { _, bundle ->
            val openBlockDialog = bundle.getBoolean("openBlockDialog")
            if (openBlockDialog) {
                manageTimeSlots()
            }
            clearFragmentResult(CalendarFragmentPt.RESULT)
        }
        openBlockDialog()
    }

    private fun setupBlockSlotListener() {
        setFragmentResultListener(BlockTimeSlotDialog.RESULT) { _, _ ->
            refresh()
            clearFragmentResultListener(BlockTimeSlotDialog.RESULT)
            setupBlockSlotListener()
            setupFragmentListener()
        }
    }

    private fun setupBlockSlotListUpdateListener() {
        setFragmentResultListener(BlockSlotsFragment.RESULT) { _, _ ->
            refresh()
            clearFragmentResultListener(BlockSlotsFragment.RESULT)
            setupBlockSlotListUpdateListener()
        }
    }

    fun refresh() {
        viewModel.intent.value = TrainingCalendarIntent.Refresh
    }

    fun openBlockDialog() {
        viewModel.intent.value = TrainingCalendarPtIntent.ManageTimeSlots
    }

    private fun openBlockList() {
        viewModel.intent.value = TrainingCalendarPtIntent.ShowBlockedTimeSlots
    }

    override fun registerSessionConfirmListener() {}

    companion object {
        const val SCHEDULE_TAB = 3
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.block_new_slot -> openBlockDialog()
            R.id.mange_blocked_slots -> openBlockList()
        }
        return true
    }
}
