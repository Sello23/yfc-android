package com.yourfitness.pt.ui.features.calendar.confirm

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.yourfitness.common.domain.date.dayOfWeekFormatted
import com.yourfitness.common.domain.date.timeFormatted
import com.yourfitness.common.ui.mvi.MviBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setCompoundDrawables
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.pt.R
import com.yourfitness.pt.databinding.DialogProcessSessionConfirmBinding
import com.yourfitness.pt.ui.navigation.PtNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProcessSessionConfirmDialog :
    MviBottomSheetDialogFragment<ConfirmSessionIntent, ConfirmSessionState, ProcessSessionConfirmViewModel>() {

    override val viewModel: ProcessSessionConfirmViewModel by viewModels()
    override val binding: DialogProcessSessionConfirmBinding by viewBinding()

    @Inject
    lateinit var navigator: PtNavigator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar(
            binding.toolbar.root,
            getString(R.string.process_confirm_booking_title),
            dismissId = com.yourfitness.common.R.id.close
        )
        binding.apply {
            setupBottomInsets(root)
            card.viewSeparator.isVisible = false
            card.actionLabel.isVisible = false
            actionBtn.setOnClickListener {
                viewModel.intent.value = ConfirmSessionIntent.ActionButtonClicked
            }
        }
    }

    override fun renderState(state: ConfirmSessionState) {
        when (state) {
            is ConfirmSessionState.Loading -> {
                binding.actionBtn.isClickable = false
                showLoading(true)
            }
            is ConfirmSessionState.Error -> {
                binding.actionBtn.isClickable = true
                showLoading(false)
            }
            is ConfirmSessionState.DataLoaded -> setupViews(state)
            is ConfirmSessionState.Confirmed -> {
                showLoading(false)
                setFragmentResult(RESULT, bundleOf())
                dismiss()
            }
        }
    }

    override fun showLoading(isLoading: Boolean) {
        binding.card.progress.isVisible = isLoading
    }

    private fun setupViews(state: ConfirmSessionState.DataLoaded) {
        binding.apply {
            card.apply {
                title.text = state.ptName
                subtitle.text = getString(R.string.personal_trainer)
                time.text = "${state.startDate.timeFormatted()} - ${state.endDate.timeFormatted()}"
                date.text = state.startDate.dayOfWeekFormatted()
                address.text = state.address
                info.text = state.facilityName
                Glide.with(requireContext()).load(state.logo.toImageUri()).into(imageIcon)
                actionLabel.text = getString(R.string.debited_info)
                actionLabel.setCompoundDrawables(start = R.drawable.ic_barbell)
            }
        }
    }

    companion object {
        const val RESULT = "confirm_session_completed_dialog_result"
    }
}
