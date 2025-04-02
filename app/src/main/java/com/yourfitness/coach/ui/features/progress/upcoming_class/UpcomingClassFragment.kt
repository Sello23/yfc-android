package com.yourfitness.coach.ui.features.progress.upcoming_class

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.FragmentUpcomingClassBinding
import com.yourfitness.coach.ui.features.facility.class_operations.ClassOperationsFragment
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.utils.ManageClass
import com.yourfitness.common.domain.date.TimeDifference
import com.yourfitness.common.domain.date.dateTimeDifference
import com.yourfitness.common.domain.date.toDateDayOfWeekMonth
import com.yourfitness.common.domain.date.toDateTime
import com.yourfitness.common.domain.date.toMilliseconds
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.toImageUri
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class UpcomingClassFragment : ClassOperationsFragment<UpcomingClassIntent, UpcomingClassState, UpcomingClassViewModel>() {

    override val binding: FragmentUpcomingClassBinding by viewBinding()
    override val viewModel: UpcomingClassViewModel by viewModels()
    private val manageClass = ManageClass(this)

    override fun setFragmentResultListeners(fragment: Fragment?) {
        super.setFragmentResultListeners(requireParentFragment())
    }

    override fun clearFragmentResultListeners(fragment: Fragment?) {
        super.clearFragmentResultListeners(requireParentFragment())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.isVisible = false
        manageClass.init(binding.bookedClass.actionLabel)
    }

    override fun renderState(state: UpcomingClassState) {
        when (state) {
            is UpcomingClassState.Success -> showUpcomingClass(state)
        }
    }

    private fun showUpcomingClass(state: UpcomingClassState.Success) {
        val today = Calendar.getInstance().timeInMillis
        val difference = dateTimeDifference(
            state.upcomingClass?.time?.toMilliseconds() ?: 0L,
            today,
            TimeDifference.MINUTE
        )
        if (state.upcomingClass != null && difference >= (-1) * state.upcomingClass.classEntryLeadTime) {
            binding.root.isVisible = true
            binding.bookedClass.apply {
                title.text = state.upcomingClass.className
                subtitle.text = state.upcomingClass.coachName
                address.text = state.upcomingClass.address
                info.text = state.upcomingClass.facilityName
                Glide.with(requireContext()).load(state.upcomingClass.icon.toImageUri())
                    .into(binding.bookedClass.imageIcon)
                time.text = state.upcomingClass.time.toDateTime()
                date.text = state.upcomingClass.date.toDateDayOfWeekMonth()
                when {
                    difference in (-1) * state.upcomingClass.classEntryLeadTime..state.upcomingClass.classEntryLeadTime -> {
                        actionLabel.text = getString(R.string.fitness_calendar_screen_get_access_text)
                    }
                    difference > state.upcomingClass.classEntryLeadTime ->
                        actionLabel.text = getString(R.string.fitness_calendar_screen_manage_class_text)
                }
                actionLabel.setOnClickListener {
                    when (actionLabel.text) {
                        getString(R.string.fitness_calendar_screen_manage_class_text) -> {
                            manageClass.openMenu(
                                onRebookClick = { viewModel.onRebookClick(state.upcomingClass) },
                                onCancelClick = { viewModel.onCancelClick(state.upcomingClass) }
                            )
                        }
                        getString(R.string.fitness_calendar_screen_get_access_text) -> {
                            viewModel.navigator.navigate(
                                Navigation.YouHaveUpcomingClass(
                                    state.upcomingClass,
                                    state.profile
                                )
                            )
                        }
                    }
                }
            }
            binding.textGoToFitnessCalendar.setOnClickListener {
                viewModel.navigator.navigate(Navigation.FitnessCalendar)
            }
        } else {
            binding.root.isVisible = false
        }
    }
}