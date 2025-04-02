package com.yourfitness.coach.ui.features.more.challenges.dialogs.join

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogChallengeJoinBinding
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.common.network.dto.Challenge
import com.yourfitness.common.ui.mvi.MviDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setupBottomInsets
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChallengeJoinDialogFragment : MviDialogFragment<Any, Any, ChallengeJoinViewModel>() {

    override val binding: DialogChallengeJoinBinding by viewBinding()
    override val viewModel: ChallengeJoinViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        binding.toolbar.toolbar.title = getString(R.string.challenge_details_screen_want_join_challenge_text)
        binding.toolbar.toolbar.setOnMenuItemClickListener { onMenuClicked(it) }
        val challenge = requireArguments().get("challenge") as Challenge
        binding.textMaybeLater.setOnClickListener { dismiss() }
        binding.buttonYesJoin.setOnClickListener {
            binding.buttonYesJoin.isClickable = false
            viewModel.joinChallenge(challenge)
        }
    }

    override fun renderState(state: Any) {
        super.renderState(state)
        when (state) {
            is ChallengeJoinState.Success -> {
                setResult(state.challenge)
                dismiss()
                viewModel.navigator.navigate(Navigation.ChallengeJoined(state.challenge))
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
        const val RESULT = "challenge_join_result"
    }
}