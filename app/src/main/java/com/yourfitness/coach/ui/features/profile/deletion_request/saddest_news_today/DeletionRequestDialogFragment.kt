package com.yourfitness.coach.ui.features.profile.deletion_request.saddest_news_today

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogDeletionRequestBinding
import com.yourfitness.common.ui.mvi.MviDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.coach.ui.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeletionRequestDialogFragment : MviDialogFragment<Any, Any, DeletionRequestViewModel>() {

    override val binding: DialogDeletionRequestBinding by viewBinding()
    override val viewModel: DeletionRequestViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.toolbar.setOnMenuItemClickListener { onMenuClicked(it) }
        binding.toolbar.toolbar.title = getString(R.string.deletion_request_screen_saddest_news_today_text)
        binding.buttonSubmitDelete.setOnClickListener {
            binding.buttonSubmitDelete.isClickable = false
            viewModel.sentDeletionRequest()
        }
    }

    private fun onMenuClicked(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.close -> dismiss()
        }
        return true
    }

    fun showLoading(isLoading: Boolean) {
        binding.progress.root.isVisible = isLoading
        binding.groupContent.isVisible = !isLoading
    }

    override fun renderState(state: Any) {
        when (state) {
            is DeletionRequestState.Loading -> showLoading(true)
            is DeletionRequestState.Success -> onDeletionRequestSuccess()
            is DeletionRequestState.Error -> onDeletionRequestError()
        }
    }

    private fun onDeletionRequestSuccess() {
        showLoading(false)
        dialog?.hide()
        viewModel.navigator.navigate(Navigation.DeletionRequestSuccess)
    }

    private fun onDeletionRequestError() {
        showLoading(false)
        dialog?.hide()
        viewModel.navigator.navigate(Navigation.DeletionRequestError)
    }
}