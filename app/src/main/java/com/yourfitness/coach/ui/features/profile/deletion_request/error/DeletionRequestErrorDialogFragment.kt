package com.yourfitness.coach.ui.features.profile.deletion_request.error

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogDeletionRequestErrorBinding
import com.yourfitness.common.ui.mvi.MviDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeletionRequestErrorDialogFragment : MviDialogFragment<Any, Any, DeletionRequestErrorViewModel>() {

    override val binding: DialogDeletionRequestErrorBinding by viewBinding()
    override val viewModel: DeletionRequestErrorViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.toolbar.setOnMenuItemClickListener { onMenuClicked(it) }
        binding.toolbar.toolbar.title = getString(R.string.deletion_request_screen_we_cant_send_delete_request_text)
        binding.buttonGotIt.setOnClickListener { dismiss() }
    }

    private fun onMenuClicked(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.close -> dismiss()
        }
        return true
    }
}