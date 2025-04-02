package com.yourfitness.coach.ui.features.profile.deletion_request.success

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogDeletionRequestSuccessBinding
import com.yourfitness.common.ui.mvi.MviDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeletionRequestSuccessDialogFragment : MviDialogFragment<Any, Any, DeletionRequestSuccessViewModel>() {

    override val binding: DialogDeletionRequestSuccessBinding by viewBinding()
    override val viewModel: DeletionRequestSuccessViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.toolbar.setOnMenuItemClickListener { onMenuClicked(it) }
        binding.toolbar.toolbar.title = getString(R.string.deletion_request_screen_success_text)
        binding.buttonGotIt.setOnClickListener { dismiss() }
    }

    private fun onMenuClicked(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.close -> dismiss()
        }
        return true
    }
}