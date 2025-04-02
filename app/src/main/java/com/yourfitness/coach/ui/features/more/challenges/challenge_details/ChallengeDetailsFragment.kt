package com.yourfitness.coach.ui.features.more.challenges.challenge_details

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.clearFragmentResult
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.FragmentChallengeDetailsBinding
import com.yourfitness.coach.ui.features.more.challenges.ChallengeFlow
import com.yourfitness.coach.ui.features.more.challenges.dialogs.join.ChallengeJoinDialogFragment
import com.yourfitness.coach.ui.features.more.challenges.dialogs.join_private_code.ChallengeJoinPrivateCodeDialogFragment
import com.yourfitness.coach.ui.features.more.challenges.dialogs.leave.ChallengeLeaveDialogFragment
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.utils.DubaiDetailsData
import com.yourfitness.common.domain.date.toDateDMmmYyyy
import com.yourfitness.common.domain.date.toMilliseconds
import com.yourfitness.common.network.dto.Challenge
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.toImageUri
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.Calendar

@AndroidEntryPoint
class ChallengeDetailsFragment : MviFragment<Any, Any, ChallengeDetailsViewModel>() {

    override val binding: FragmentChallengeDetailsBinding by viewBinding()
    override val viewModel: ChallengeDetailsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.toolbar.root)
        binding.toolbar.toolbar.title = getString(R.string.challenges_screen_challenges_text)
        showChallenge()
        setupJoinListener()
        setupJoinPrivateListener()
        setupLeaveListener()
    }

    private fun showChallenge() {
        val challenge =
            requireArguments().get("challenge") as Challenge? ?: parseChallenge() ?: return
        val flow = requireArguments().get("flow") as ChallengeFlow? ?: ChallengeFlow.DETAILS
        val dubaiInfo = requireArguments().get("dubai_info") as DubaiDetailsData?
        if (challenge.imageID == null) {
            binding.imageChallenge.setImageResource(R.drawable.dubai_30x30_logo)
        } else {
            Glide.with(binding.root).load(challenge.imageID?.toImageUri()).into(binding.imageChallenge)
        }
        binding.textChallengeName.text = if (dubaiInfo != null) getString(dubaiInfo.nameId) else challenge.name
        binding.textStartDate.text = challenge.startDate?.toDateDMmmYyyy()
        binding.textFinishDate.text = challenge.endDate?.toDateDMmmYyyy()
        binding.textDescriptionContent.text = if (dubaiInfo != null) getString(dubaiInfo.descriptionId) else challenge.description
        binding.textRulesContent.text = if (dubaiInfo != null) getString(dubaiInfo.rulesId) else challenge.rules
        when (flow) {
            ChallengeFlow.DETAILS -> {
                val date = Calendar.getInstance().timeInMillis
                if (date > (challenge.endDate?.toMilliseconds() ?: 0L)) {
                    binding.bottomBar.isVisible = false
                } else {
                    binding.buttonLeaveChallenge.isVisible = true
                    binding.buttonJoinChallenge.isVisible = false
                    binding.buttonLeaveChallenge.setOnClickListener {
                        viewModel.navigator.navigate(Navigation.ChallengeLeave(challenge))
                    }
                }
            }
            ChallengeFlow.JOIN -> {
                binding.buttonJoinChallenge.isVisible = true
                binding.buttonJoinChallenge.setOnClickListener {
                    viewModel.navigator.navigate(Navigation.ChallengeJoin(challenge))
                }
            }
            ChallengeFlow.JOIN_PRIVATE -> {
                binding.buttonJoinChallenge.isVisible = true
                binding.buttonJoinChallenge.setOnClickListener {
                    viewModel.navigator.navigate(Navigation.ChallengeJoinPrivate(challenge))
                }
            }
            ChallengeFlow.GLOBAL -> {
                binding.bottomBar.isVisible = false
            }
        }

        if (requireArguments().getBoolean("hide_actions") || requireArguments().get("challenge") == null) {
            binding.buttonLeaveChallenge.isVisible = false
            binding.buttonJoinChallenge.isVisible = false
        }
    }

    private fun parseChallenge(): Challenge? {
        return try {
            val challengeString = requireArguments()
                .getString("challenge_string")
                ?.replace("~", "/")
            val type = object : TypeToken<Challenge>() {}.type
            return Gson().fromJson(challengeString, type)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    private fun setupJoinListener() {
        setFragmentResultListener(ChallengeJoinDialogFragment.RESULT) { _, bundle ->
            getJoinedChallenge(bundle)
            clearFragmentResult(ChallengeJoinDialogFragment.RESULT)
            setupJoinListener()
        }
    }

    private fun setupJoinPrivateListener() {
        setFragmentResultListener(ChallengeJoinPrivateCodeDialogFragment.RESULT) { _, bundle ->
            getJoinedChallenge(bundle)
            clearFragmentResult(ChallengeJoinPrivateCodeDialogFragment.RESULT)
            setupJoinPrivateListener()
        }
    }

    private fun getJoinedChallenge(bundle: Bundle) {
        val challenge = bundle.get("challenge") as Challenge?
        if (challenge != null) {
            setFragmentResult(challenge)
            binding.buttonLeaveChallenge.isVisible = true
            binding.buttonJoinChallenge.isVisible = false
            binding.buttonLeaveChallenge.setOnClickListener {
                viewModel.navigator.navigate(Navigation.ChallengeLeave(challenge))
            }
        }
    }

    private fun setupLeaveListener() {
        setFragmentResultListener(ChallengeLeaveDialogFragment.RESULT) { _, bundle ->
            val challenge = bundle.get("challenge") as Challenge?
            if (challenge != null) {
                setFragmentResult(challenge, false)
            }
            clearFragmentResult(ChallengeLeaveDialogFragment.RESULT)
            setupLeaveListener()
            findNavController().navigateUp()
            findNavController().navigateUp()
        }
    }

    private fun setFragmentResult(challenge: Challenge, joined: Boolean = true) {
        viewModel.resultBundle = bundleOf("challenge" to challenge, "joined" to joined)
    }

    override fun onDestroy() {
        setFragmentResult(RESULT, viewModel.resultBundle)
        super.onDestroy()
    }

    companion object {
        const val RESULT = "details_challenge_action_result"
    }
}
