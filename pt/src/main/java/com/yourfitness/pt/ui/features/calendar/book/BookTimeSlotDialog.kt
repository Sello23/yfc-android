package com.yourfitness.pt.ui.features.calendar.book

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.yourfitness.common.domain.date.dayOfWeekFormatted
import com.yourfitness.common.domain.date.timeFormatted
import com.yourfitness.common.ui.mvi.MviBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setCompoundDrawables
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.pt.R
import com.yourfitness.pt.databinding.DialogBookTimeSlotBinding
import com.yourfitness.pt.domain.models.FacilityInfo
import com.yourfitness.pt.ui.features.calendar.SpacesItemDecoration
import com.yourfitness.pt.ui.features.calendar.toPx
import com.yourfitness.pt.ui.navigation.PtNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.min

@AndroidEntryPoint
class BookTimeSlotDialog :
    MviBottomSheetDialogFragment<BookTimeSlotIntent, BookTimeSlotState, BookTimeSlotViewModel>() {

    override val binding: DialogBookTimeSlotBinding by viewBinding()
    override val viewModel: BookTimeSlotViewModel by viewModels()

    private val facilitiesAdapter: FacilityCellAdapter by lazy { FacilityCellAdapter(::onCellClick) }

    @Inject
    lateinit var navigator: PtNavigator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        setupToolbar(
            binding.toolbar.root,
            getString(R.string.select_facility),
            dismissId = com.yourfitness.common.R.id.close
        )
        binding.toolbar.root.background = ContextCompat.getDrawable(
            requireContext(),
            com.yourfitness.common.R.drawable.background_grey_toolbar_bottom_sheet
        )
        setupRecyclerView()
        if (viewModel.facilities.all { it.workTimeData?.isAccessible == false }) {
            showInfoCard()
        }
        updateFacilitiesList(viewModel.facilities)
        binding.actionBtn.apply {
            isEnabled = false
            setOnClickListener {
                viewModel.intent.value = BookTimeSlotIntent.ActionButtonClicked
            }
        }
    }

    override fun renderState(state: BookTimeSlotState) {
        when (state) {
            is BookTimeSlotState.Loading -> {
                binding.actionBtn.isClickable = false
                showLoading(true)
            }
            is BookTimeSlotState.Error -> {
                binding.actionBtn.isClickable = true
                showLoading(false)
            }
            is BookTimeSlotState.SelectedItemUpdated -> {
                updateSelectedFacility(state.id, state.prevPos, state.curPos)
                updateActionButton(state.id != null)
            }
            is BookTimeSlotState.ConfirmStep -> updateViews(state)
        }
    }

    override fun showLoading(isLoading: Boolean) {
        binding.card.progress.isVisible = isLoading
    }

    private fun setupRecyclerView() {
        val visibleSlots = min(3, viewModel.facilities.size)
        binding.facilitiesList.apply {
            layoutParams.height = (visibleSlots * 99 + 12).toPx()
            this.adapter = facilitiesAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(SpacesItemDecoration())
        }
    }

    private fun updateFacilitiesList(list: List<FacilityInfo>) {
        binding.facilitiesList.post {
            facilitiesAdapter.setData(list)
            facilitiesAdapter.notifyDataSetChanged()
        }
    }

    private fun updateSelectedFacility(id: String?, prevPos: Int?, curPos: Int) {
        binding.facilitiesList.post {
            facilitiesAdapter.setSelectedId(id)
            if (prevPos != null) facilitiesAdapter.notifyItemChanged(prevPos)
            facilitiesAdapter.notifyItemChanged(curPos)
        }
    }

    private fun updateActionButton(enabled: Boolean) {
        binding.actionBtn.isEnabled = enabled
    }

    private fun updateViews(state: BookTimeSlotState.ConfirmStep) {
        binding.apply {
            toolbar.root.title = getString(R.string.book_session)
            actionBtn.text = getString(R.string.confirm_booking)
            info.text = getString(R.string.book_time_slot_info_2)
            facilitiesList.isVisible = false
            card.apply {
                root.isVisible = true
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

    private fun showInfoCard() {
        binding.infoMessageContainer.root.isVisible = true
        binding.infoMessageContainer.stubMessage.text = getString(R.string.select_facility_msg)
        binding.infoMessageContainer.infoMessage.background = ContextCompat.getDrawable(requireContext(), com.yourfitness.common.R.drawable.rounded_border_red)
        binding.infoMessageContainer.icon.setImageResource(com.yourfitness.common.R.drawable.ic_info_red_big)
    }

    private fun onCellClick(id: String, pos: Int) {
        viewModel.intent.value = BookTimeSlotIntent.CellClicked(id, pos)
    }
}
