package com.yourfitness.coach.ui.features.more.challenges.dialogs.join_private_code

import android.os.Bundle
import android.text.InputFilter
import android.view.MenuItem
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogChallengeJoinPrivateCodeBinding
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.common.network.dto.Challenge
import com.yourfitness.common.ui.mvi.MviDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setupBottomInsets
import dagger.hilt.android.AndroidEntryPoint
import com.yourfitness.common.R as common

@AndroidEntryPoint
class ChallengeJoinPrivateCodeDialogFragment : MviDialogFragment<Any, Any, ChallengeJoinPrivateCodeViewModel>() {

    override val binding: DialogChallengeJoinPrivateCodeBinding by viewBinding()
    override val viewModel: ChallengeJoinPrivateCodeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        binding.toolbar.toolbar.setOnMenuItemClickListener { onMenuClicked(it) }
        val challenge = requireArguments().get("challenge") as Challenge
        binding.buttonJoin.isEnabled = false
        binding.toolbar.toolbar.title = getString(R.string.challenges_screen_join_private_challenge_text)
        binding.editCode.apply {
            filters += InputFilter.AllCaps()
            doOnTextChanged { text, _, _, _ -> onCodeChanged(text ?: "", challenge) }
        }
    }

    override fun renderState(state: Any) {
        super.renderState(state)
        when (state) {
            is ChallengeJoinPrivateCodeState.Success -> {
                setResult(state.challenge)
                dismiss()
                viewModel.navigator.navigate(Navigation.ChallengeJoined(state.challenge))
            }
            is ChallengeJoinPrivateCodeState.Error -> {
                showCodeError(false)
                binding.buttonJoin.isClickable = true
            }
        }
    }

    private fun onCodeChanged(code: CharSequence, challenge: Challenge) {
        showCodeError(true)
        binding.buttonJoin.isEnabled = true
        binding.buttonJoin.setOnClickListener {
            viewModel.joinChallenge(challenge, code.toString().trim().uppercase())
            binding.buttonJoin.isClickable = false
        }
    }

    private fun showCodeError(isClean: Boolean) {
        binding.buttonJoin.isClickable = true
        if (isClean) {
            binding.textError.isVisible = false
            binding.editCode.setBackgroundResource(R.drawable.edit_text_search_line_background_selected)
        } else {
            binding.textError.isVisible = true
            binding.editCode.setBackgroundResource(common.drawable.edit_text_background_error)
            binding.buttonJoin.isEnabled = false
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
        const val RESULT = "challenge_join_private_result"
    }
}