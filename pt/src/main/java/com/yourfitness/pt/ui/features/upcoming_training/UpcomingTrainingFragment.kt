package com.yourfitness.pt.ui.features.upcoming_training

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.yourfitness.common.domain.date.dayOfWeekFormatted
import com.yourfitness.common.domain.date.timeFormatted
import com.yourfitness.common.domain.date.toDate
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.pt.R
import com.yourfitness.pt.databinding.FragmentTrainingBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UpcomingTrainingFragment : MviFragment<UpcomingTrainingIntent, UpcomingTrainingState, UpcomingTrainingViewModel>() {

    override val binding: FragmentTrainingBinding by viewBinding()
    override val viewModel: UpcomingTrainingViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            root.isVisible = false
            card.status.text = getString(R.string.status_booked).uppercase()
            textGoToFitnessCalendar.setOnClickListener {
                viewModel.intent.value = UpcomingTrainingIntent.OpenFitnessCalendarClicked
            }
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.intent.value = UpcomingTrainingIntent.UpdateUpcomingState
    }

    override fun renderState(state: UpcomingTrainingState) {
        when (state) {
            is UpcomingTrainingState.UpcomingTrainingLoaded -> showUpcomingClass(state)
        }
    }

    private fun showUpcomingClass(state: UpcomingTrainingState.UpcomingTrainingLoaded) {
        binding.root.isVisible = true
        binding.card.apply {
            status.isVisible = true
            val training = state.training
            val startDate = training.from.toDate()
            title.text = state.ptName
            subtitle.text = getString(R.string.personal_trainer)
            time.text = "${startDate.timeFormatted()}-${training.to.toDate().timeFormatted()}"
            date.text = startDate.dayOfWeekFormatted()
            address.text = state.address
            info.text = state.facilityName
            Glide.with(requireContext()).load(state.logo.toImageUri()).into(imageIcon)
            actionLabel.isVisible = false
            viewSeparator.visibility = View.INVISIBLE
        }
    }
}
