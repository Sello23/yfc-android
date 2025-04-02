package com.yourfitness.pt.ui.features.training_calendar.actions

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yourfitness.common.R
import com.yourfitness.common.domain.models.CalendarView
import com.yourfitness.common.ui.mvi.MviBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.pt.databinding.DialogActionDecisionBinding
import com.yourfitness.pt.domain.values.SUCCESS
import com.yourfitness.pt.ui.features.training_calendar.adapters.CalendarCardBuilder
import com.yourfitness.pt.ui.features.training_calendar.block_slots.conflicts.list.BlockSlotAdapter
import com.yourfitness.pt.ui.utils.SessionStatus
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserConfirmActionDialog :
    MviBottomSheetDialogFragment<UserConfirmActionIntent, UserConfirmActionState, UserConfirmActionViewModel>(),
    CalendarCardBuilder {

    override val binding: DialogActionDecisionBinding by viewBinding()
    override val viewModel: UserConfirmActionViewModel by viewModels()

    private val adapter: BlockSlotAdapter by lazy { BlockSlotAdapter(hideAction = true) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar(
            binding.toolbar.root,
            getString(viewModel.flowData.tittle),
            dismissId = R.id.close
        )

        binding.allChainAction.isChecked = viewModel.flow == ConfirmFlow.CANCEL_BLOCK
        binding.actionMain.apply {
            background = ContextCompat.getDrawable(requireContext(), viewModel.flowData.btnBackground)
            text = getString(viewModel.flowData.actionMain)
            setOnClickListener {
                viewModel.intent.value = UserConfirmActionIntent.MainActionButtonClicked(binding.allChainAction.isChecked)
                isClickable = false
            }
        }

        binding.infoMsg.apply {
            isVisible = viewModel.flowData.info != null
            text = viewModel.flowData.info?.let { getString(it) }
        }
        dialog?.setCancelable(false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.configureDialogView()
        return dialog
    }

    override fun renderState(state: UserConfirmActionState) {
        when (state) {
            is UserConfirmActionState.Loading -> showLoading(true)
            is UserConfirmActionState.Error -> {
                binding.actionMain.isClickable = true
                showError(state.error)
                dismiss()
            }
            is UserConfirmActionState.DataLoaded -> setupViews(state.sessions)
            is UserConfirmActionState.ShouldDismiss -> {
                setResult(true)
                if (state.navObj != null) {
                    viewModel.navigator.navigate(state.navObj)
                } else {
                    findNavController().navigateUp()
                }
            }
        }
    }

    override fun dismissDialog() {
        setResult(false)
        dismiss()
    }

    private fun setupViews(sessions: List<CalendarView.CalendarItem>) {
        val firstSession = sessions.firstOrNull()
        val isBlock = firstSession?.status == SessionStatus.BLOCKED_SLOT.value || sessions.size > 1
        binding.card.root.isVisible = !isBlock
        binding.blockSlotsRecyclerView.isVisible = isBlock
        if (firstSession == null) return
        if (!isBlock) {
            binding.card.buildCard(firstSession)
        } else {
            adapter.setData(sessions.map { false to it })
            binding.blockSlotsRecyclerView.adapter = adapter
            binding.groupChain.isVisible = sessions.count { it.repeats > 1 } > 0
        }
    }

    override fun showLoading(isLoading: Boolean) {
       binding.progressContainer.isVisible = isLoading
    }

    private fun setResult(success: Boolean) {
        setFragmentResult(RESULT, bundleOf(SUCCESS to success))
    }

    companion object {
        const val RESULT = "user_action_dialog_result"
    }
}
