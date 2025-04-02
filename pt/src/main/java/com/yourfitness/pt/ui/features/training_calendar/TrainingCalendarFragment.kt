package com.yourfitness.pt.ui.features.training_calendar

import android.os.Bundle
import android.view.View
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.yourfitness.common.ui.features.fitness_calendar.AbstractMonthViewHolder
import com.yourfitness.common.ui.features.fitness_calendar.BaseDayViewHolder
import com.yourfitness.common.ui.features.fitness_calendar.BaseFitnessCalendarFragment
import com.yourfitness.common.ui.features.fitness_calendar.BaseWeekViewHolder
import com.yourfitness.pt.domain.values.SUCCESS
import com.yourfitness.pt.ui.features.calendar.confirm.ProcessSessionConfirmDialog
import com.yourfitness.pt.ui.features.training_calendar.actions.UserConfirmActionDialog
import com.yourfitness.pt.ui.features.training_calendar.adapters.PtDayAdapter
import com.yourfitness.pt.ui.features.training_calendar.adapters.PtMonthAdapter
import com.yourfitness.pt.ui.features.training_calendar.adapters.PtWeekAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class TrainingCalendarFragment :
    BaseFitnessCalendarFragment<TrainingCalendarViewModel, AbstractMonthViewHolder, BaseWeekViewHolder, BaseDayViewHolder>() {

    override val viewModel: TrainingCalendarViewModel by viewModels()
    override val baseMonthAdapter by lazy { PtMonthAdapter(::onItemClick) }
    override val weekAdapter by lazy { PtWeekAdapter(::onItemClick) }
    override val dayAdapter by lazy { PtDayAdapter(::onItemClick) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerSessionConfirmListener()
    }

    override fun onResume() {
        super.onResume()
        setupFragmentListener()
    }

    protected fun setupFragmentListener() {
        setFragmentResultListener(UserConfirmActionDialog.RESULT) { _, bundle ->
            if (bundle.getBoolean(SUCCESS)) viewModel.intent.value = TrainingCalendarIntent.Refresh
            clearFragmentResultListener(UserConfirmActionDialog.RESULT)
            setupFragmentListener()
        }
    }

    protected open fun onItemClick(sessionId: String, status: String) {
        onItemClick()
        viewModel.intent.value = TrainingCalendarIntent.OnSessionActionClicked(sessionId, status)
    }

    protected open fun registerSessionConfirmListener() {
        setFragmentResultListener(ProcessSessionConfirmDialog.RESULT) { _, _ ->
            viewModel.intent.value = TrainingCalendarIntent.Refresh
            clearFragmentResultListener(ProcessSessionConfirmDialog.RESULT)
            registerSessionConfirmListener()
        }
    }
}
