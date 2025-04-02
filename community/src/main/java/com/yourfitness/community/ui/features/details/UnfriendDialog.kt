package com.yourfitness.community.ui.features.details

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.yourfitness.common.databinding.DialogTwoActionBinding
import com.yourfitness.common.ui.mvi.MviBottomSheetDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.comunity.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UnfriendDialog : MviBottomSheetDialogFragment<UnfriendIntent, UnfriendState, UnfriendViewModel>() {

    override val binding: DialogTwoActionBinding by viewBinding()
    override val viewModel: UnfriendViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        requireDialog().setCancelable(false)
        requireDialog().setCanceledOnTouchOutside(false)
        setupToolbar(
            binding.toolbar.root,
            getString(R.string.unfriend_title),
            dismissId = com.yourfitness.common.R.id.close
        )
        binding.message.text = getString(R.string.unfriend_desc)
        binding.buttonRetry.text = getString(R.string.unfriend_positive)
        binding.buttonBack.text = getString(R.string.unfriend_negative)
        binding.buttonRetry.setOnClickListener {
            binding.progressContainer.isVisible = true
            viewModel.intent.value = UnfriendIntent.Unfriend
        }
        binding.buttonBack.setOnClickListener { dismiss() }
        binding.image.setImageResource(com.yourfitness.common.R.drawable.ic_error)
    }

    override fun onDismiss(dialog: DialogInterface) {
        setFragmentResult(RESULT, bundleOf(
            RESULT to viewModel.isDeleted,
            ID to viewModel.friendId
            ))
        super.onDismiss(dialog)
    }

    override fun renderState(state: UnfriendState) {
        when (state) {
            is UnfriendState.Error -> {
                binding.progressContainer.isVisible = false
                showError(Throwable(getString(com.yourfitness.common.R.string.error_something_went_wrong)))
            }
        }
    }

    companion object {
        const val RESULT = "unfriend_result"
        const val ID = "unfriend_id"
    }
}
