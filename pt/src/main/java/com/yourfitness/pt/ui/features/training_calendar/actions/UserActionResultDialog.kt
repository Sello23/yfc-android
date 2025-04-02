package com.yourfitness.pt.ui.features.training_calendar.actions

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.yourfitness.common.R
import com.yourfitness.common.databinding.DialogOneActionBinding
import com.yourfitness.common.ui.base.BaseBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.pt.domain.values.ACTION_FLOW
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserActionResultDialog : BaseBottomSheetDialogFragment() {

    override val binding: DialogOneActionBinding by viewBinding()

    private val flowDataMap = mapOf(
        ResultFlow.CONFIRM_SUCCESS to ResultFlowData(
            R.drawable.ic_success,
            com.yourfitness.pt.R.string.confirm_session_result_title,
            com.yourfitness.pt.R.string.confirm_session_result_msg,
        ),
        ResultFlow.DECLINE_SUCCESS to ResultFlowData(
            R.drawable.ic_cancel,
            com.yourfitness.pt.R.string.decline_session_result_title,
            com.yourfitness.pt.R.string.decline_session_result_msg,
        ),
        ResultFlow.CANCEl_SUCCESS to ResultFlowData(
            R.drawable.ic_success,
            com.yourfitness.pt.R.string.confirm_cancel_session_result_title,
            com.yourfitness.pt.R.string.confirm_cancel_session_result_msg,
        ),
        ResultFlow.REMOVE_BLOCK_SUCCESS to ResultFlowData(
            R.drawable.ic_success,
            com.yourfitness.pt.R.string.block_remove_result_title,
            com.yourfitness.pt.R.string.block_remove_result_msg,
        ),
        ResultFlow.REMOVE_MULTI_BLOCK_SUCCESS to ResultFlowData(
            R.drawable.ic_success,
            com.yourfitness.pt.R.string.block_multi_remove_result_title,
            com.yourfitness.pt.R.string.block_multi_remove_result_msg,
        )
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val flow = (requireArguments().get(ACTION_FLOW) as Int?) ?: ResultFlow.CONFIRM_SUCCESS
        val flowData: ResultFlowData = flowDataMap[flow] ?: flowDataMap.values.first()

        setupBottomInsets(binding.root)
        setupToolbar(
            binding.toolbar.root,
            getString(flowData.tittle),
            dismissId = R.id.close
        )
        binding.buttonConfirm.setOnClickListener { dismiss() }
        binding.message.text = getString(flowData.message)
        binding.image.setImageDrawable(ContextCompat.getDrawable(requireContext(), flowData.image))
    }

    override fun onDismiss(dialog: DialogInterface) {
        setFragmentResult(RESULT, bundleOf())
        super.onDismiss(dialog)
    }

    companion object {
        const val RESULT = "success"
    }
}

sealed class ResultFlow {
    companion object {
        const val CONFIRM_SUCCESS = 0
        const val DECLINE_SUCCESS = 1
        const val CANCEl_SUCCESS = 2
        const val REMOVE_BLOCK_SUCCESS = 3
        const val REMOVE_MULTI_BLOCK_SUCCESS = 4
    }
}

data class ResultFlowData(
    @DrawableRes val image: Int,
    @StringRes val tittle: Int,
    @StringRes val message: Int,
)

