package com.yourfitness.coach.ui.features.more.challenges.dialogs.joined

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.MenuItem
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogChallengeJoinedBinding
import com.yourfitness.coach.domain.leaderboard.LeaderboardId
import com.yourfitness.coach.ui.features.more.challenges.ChallengeFlow
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.common.domain.date.toDateDMmmYyyy
import com.yourfitness.common.domain.date.toMilliseconds
import com.yourfitness.common.network.dto.Challenge
import com.yourfitness.common.ui.mvi.MviDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setupBottomInsets
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import com.yourfitness.common.R as common

@AndroidEntryPoint
class ChallengeJoinedDialogFragment : MviDialogFragment<Any, Any, ChallengeJoinedViewModel>() {

    override val binding: DialogChallengeJoinedBinding by viewBinding()
    override val viewModel: ChallengeJoinedViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        binding.toolbar.toolbar.setOnMenuItemClickListener { onMenuClicked(it) }
        val challenge = requireArguments().get("challenge") as Challenge
        if (challenge.private == true) {
            binding.toolbar.toolbar.title = getString(R.string.challenge_details_screen_you_joined_private_text)
            binding.textCompeteWithOther.text = getString(R.string.challenge_details_screen_compete_top_spot_text)
        } else {
            binding.toolbar.toolbar.title = getString(R.string.challenge_details_screen_you_joined_text)
        }
        val date = Calendar.getInstance().timeInMillis
        if (date < (challenge.startDate?.toMilliseconds() ?: 0)) {
            showStartDate(challenge)
        } else {
            showChallengeLeaderboard(challenge)
        }
    }

    private fun showStartDate(challenge: Challenge) {
        val date = challenge.startDate?.toDateDMmmYyyy()
        val foregroundSpan = ForegroundColorSpan(ResourcesCompat.getColor(resources, common.color.blue, null))
        val spannableString = SpannableString(getString(R.string.challenge_details_screen_challenge_will_begin_text, challenge.name, date))
        spannableString.setSpan(
            foregroundSpan,
            spannableString.indexOf(date ?: ""),
            spannableString.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.textChallengeBeginOn.text = spannableString
        binding.buttonSee.text = getString(R.string.challenge_details_screen_see_challenge_details_text)
        binding.buttonSee.setOnClickListener { viewModel.navigator.navigate(Navigation.ChallengeDetails(challenge, ChallengeFlow.DETAILS)) }
    }

    private fun showChallengeLeaderboard(challenge: Challenge) {
        val foregroundSpan = ForegroundColorSpan(ResourcesCompat.getColor(resources, common.color.blue, null))
        val spannableString = SpannableString(getString(R.string.challenge_details_screen_challenge_began_text))
        spannableString.setSpan(
            foregroundSpan,
            14,
            spannableString.indexOf(","),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.textChallengeBeginOn.text = spannableString
        binding.buttonSee.text = getString(R.string.challenge_details_screen_see_leaderboard_text)
        binding.buttonSee.setOnClickListener {
            viewModel.navigator.navigate(Navigation.Leaderboard(challenge, LeaderboardId.OTHER))
        }
    }

    private fun onMenuClicked(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.close -> dismiss()
        }
        return true
    }
}