package com.yourfitness.coach.ui.features.more.challenges.dialogs.leave

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogChallengeLeaveBinding
import com.yourfitness.common.network.dto.Challenge
import com.yourfitness.common.ui.mvi.MviDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setupBottomInsets
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChallengeLeaveDialogFragment : MviDialogFragment<Any, Any, ChallengeLeaveViewModel>() {

    override val binding: DialogChallengeLeaveBinding by viewBinding()
    override val viewModel: ChallengeLeaveViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        val challenge = requireArguments().get("challenge") as Challenge
        if (challenge.private == true) {
            binding.toolbar.toolbar.title = getString(R.string.challenge_details_screen_leave_private_challenge_text)
            binding.textYouCanStillJoin.text = getString(R.string.challenge_details_screen_return_private_challenge_challenge_text)
        } else {
            binding.toolbar.toolbar.title = getString(R.string.challenge_details_screen_want_leave_text)
        }
        binding.toolbar.toolbar.setOnMenuItemClickListener { onMenuClicked(it) }
        binding.buttonLeave.setOnClickListener {
            binding.buttonLeave.isClickable = false
            viewModel.leaveChallenge(challenge)
        }
    }

    override fun renderState(state: Any) {
        super.renderState(state)
        when (state) {
            is ChallengeLeaveState.Success -> {
                setResult(state.challenge)
                dismiss()
            }
            is ChallengeLeaveState.Error -> {
                binding.buttonLeave.isClickable = true
            }
        }
    }

    private fun onMenuClicked(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.close -> dismiss()
        }
        return true
    }

    private fun setResult(challenge: Challenge) {
        setFragmentResult(RESULT, bundleOf("challenge" to challenge))
    }

    companion object {
        const val RESULT = "challenge_leave_result"
    }
}