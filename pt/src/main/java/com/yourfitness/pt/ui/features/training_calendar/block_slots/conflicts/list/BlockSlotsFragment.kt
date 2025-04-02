package com.yourfitness.pt.ui.features.training_calendar.block_slots.conflicts.list

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yourfitness.common.domain.models.CalendarView
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.pt.R
import com.yourfitness.pt.databinding.FragmentBlockSlotsBinding
import com.yourfitness.pt.domain.values.SUCCESS
import com.yourfitness.pt.ui.features.training_calendar.actions.UserActionResultDialog
import com.yourfitness.pt.ui.features.training_calendar.actions.UserConfirmActionDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class BlockSlotsFragment : MviFragment<BlockSlotsIntent, BlockSlotsState, BlockSlotsViewModel>() {

    override val binding: FragmentBlockSlotsBinding by viewBinding()
    override val viewModel: BlockSlotsViewModel by viewModels()

    private var adapter = BlockSlotAdapter(::onSlotClick, ::onCheckboxClick)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.toolbar.title = getString(R.string.blocked_time_slots)
        setupToolbar(binding.toolbar.root)
        setupFragmentListener()
        setupSuccessDeleteFragmentListener()

        binding.toolbar.action.setOnClickListener {
            viewModel.intent.postValue(BlockSlotsIntent.ToolbarActionClicked)
        }

        binding.removeAll.setOnClickListener {
            viewModel.intent.value = BlockSlotsIntent.RemoveClicked(true)
        }
        binding.removeSelected.setOnClickListener {
            viewModel.intent.value = BlockSlotsIntent.RemoveClicked(false)
        }
        binding.listItems.adapter = adapter
    }

    override fun renderState(state: BlockSlotsState) {
        when (state) {
            is BlockSlotsState.Loading -> showLoading(true)
            is BlockSlotsState.SlotsLoaded -> setupMultiselectState(state.slots)
            is BlockSlotsState.Error -> showError(state.error)
            is BlockSlotsState.ActionStateUpdated -> setupMultiselectState(state.slots)
            is BlockSlotsState.ListItemUpdated -> {
                setupActionArea(state.slots)
                adapter.setData(state.slots)
                adapter.notifyItemChanged(state.pos)
            }
            is BlockSlotsState.CloseScreen -> {
                findNavController().navigateUp()
                findNavController().navigateUp()
            }
        }
    }

    private fun setupActionArea(slots: List<Pair<Boolean, CalendarView.CalendarItem>>) {
        val selectedSlotsCount = slots.count { it.first }
        binding.removeSelected.isEnabled = selectedSlotsCount > 0
        binding.infoLabel.text = resources.getQuantityString(
            R.plurals.selected_slots,
            selectedSlotsCount,
            selectedSlotsCount
        )
    }

    private fun setupMultiselectState(slots: List<Pair<Boolean, CalendarView.CalendarItem>>) {
        setupActionArea(slots)
        binding.toolbar.action.text = getString(
            if (viewModel.selectionMode) R.string.cancel
            else R.string.select
        )
        binding.actionContainer.isVisible = viewModel.selectionMode
        adapter.setData(slots)
        adapter.updateMultiselectMode(viewModel.selectionMode)
        adapter.notifyDataSetChanged()
    }

    private fun onSlotClick(id: String, status: String) {
        viewModel.intent.value = BlockSlotsIntent.SlotClicked(id)
    }

    private fun onCheckboxClick(sessionId: String, selected: Boolean, pos: Int) {
        viewModel.intent.value = BlockSlotsIntent.ListItemChecked(sessionId, selected, pos)
    }

    private fun setupFragmentListener() {
        setFragmentResultListener(UserConfirmActionDialog.RESULT) { _, bundle ->
            if (bundle.getBoolean(SUCCESS)) viewModel.intent.value = BlockSlotsIntent.SlotRemoved
            clearFragmentResultListener(UserConfirmActionDialog.RESULT)
            setupFragmentListener()
        }
    }

    private fun setupSuccessDeleteFragmentListener() {
        setFragmentResultListener(UserActionResultDialog.RESULT) { _, _ ->
            viewModel.intent.postValue(BlockSlotsIntent.SuccessDialogClosed)
            clearFragmentResultListener(UserActionResultDialog.RESULT)
            setupSuccessDeleteFragmentListener()
        }
    }

    override fun onDestroy() {
        setFragmentResult(RESULT, bundleOf())
        super.onDestroy()
    }

    companion object {
        const val RESULT = "block_slots_list_result"
    }
}
