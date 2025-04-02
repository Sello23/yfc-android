package com.yourfitness.coach.ui.features.more.challenges.dialogs.join_private

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogChallengeJoinPrivateBinding
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.common.network.dto.Challenge
import com.yourfitness.common.ui.mvi.MviDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setupBottomInsets
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChallengePrivateJoinDialogFragment : MviDialogFragment<Any, Any, ChallengePrivateJoinViewModel>() {

    override val binding: DialogChallengeJoinPrivateBinding by viewBinding()
    override val viewModel: ChallengePrivateJoinViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomInsets(binding.root)
        binding.toolbar.toolbar.setOnMenuItemClickListener { onMenuClicked(it) }
        val challenge = requireArguments().get("challenge") as Challenge
        binding.toolbar.toolbar.title = getString(R.string.challenges_screen_join_want_private_challenge_text)
        binding.textDontHaveCode.setOnClickListener { dismiss() }
        binding.buttonYesHaveCode.setOnClickListener {
            dismiss()
            viewModel.navigator.navigate(Navigation.ChallengeJoinPrivateCode(challenge))
        }
    }

    private fun onMenuClicked(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.close -> dismiss()
        }
        return true
    }
}