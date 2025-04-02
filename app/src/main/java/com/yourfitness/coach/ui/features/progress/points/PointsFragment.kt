package com.yourfitness.coach.ui.features.progress.points

import android.os.Bundle
import android.text.SpannableString
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.spikeapi.SpikeConnection
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.FragmentPointsBinding
import com.yourfitness.coach.domain.progress.points.ProgressInfo
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.doubleToStringNoDecimal
import com.yourfitness.common.ui.utils.formatProgressAmount
import com.yourfitness.common.ui.utils.setCustomFont
import com.yourfitness.common.ui.utils.toImageUri
import dagger.hilt.android.AndroidEntryPoint
import com.yourfitness.common.R as common

@AndroidEntryPoint
class PointsFragment : MviFragment<PointsIntent, PointsState, PointsViewModel>() {

    override val binding: FragmentPointsBinding by viewBinding()
    override val viewModel: PointsViewModel by viewModels()

    private var fitnessDataUpdateListener : FitnessDataUpdateContract? = null

    private var permissionLauncher: ActivityResultLauncher<Set<String>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        permissionLauncher = requireActivity().registerForActivityResult<Set<String>, Set<String>>(SpikeConnection.rerequestReadAuthorization()) {}
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonPointsHint.setOnClickListener { viewModel.intent.value = PointsIntent.InfoIconTapped }
        binding.content.setOnClickListener { viewModel.intent.value = PointsIntent.ProgressLevelCardTapped}
    }

    override fun showLoading(isLoading: Boolean) {
        binding.progress.isVisible = isLoading
        binding.content.isVisible = !isLoading
    }

    override fun renderState(state: PointsState) {
        when (state) {
            is PointsState.Loading -> showLoading(true)
            is PointsState.Success -> {
                fitnessDataUpdateListener?.onFitnessDataFetched()
                showProgressPoints(state.progress)
            }
            is PointsState.AskPermissions -> permissionLauncher?.launch(state.permissions)
            is PointsState.Error -> showLoading(false)
        }
    }

    fun registerFitnessDataUpdateListener(listener: FitnessDataUpdateContract) {
        fitnessDataUpdateListener = listener
    }

    private fun showProgressPoints(progress: ProgressInfo) {
        Glide.with(this).load(progress.levelLogoId.toImageUri()).into(binding.levelLogo)
        binding.sliderPrefix.isVisible = !progress.isFirstLevel
        binding.levelLabel.text = progress.currentLevel
        binding.nextLevelMsg.text = if (!progress.isLastLevel) {
            val coinsReward = resources.getQuantityString(
                R.plurals.coins_plural_format,
                progress.nextLevelReward?.toInt() ?: 0,
                progress.nextLevelReward
            )
            getString(
                R.string.next_level_msg, coinsReward
            ).buildSpannedText(coinsReward)
        } else {
            val suffix = getString(R.string.global_leaderboard)
            getString(R.string.last_level_msg, suffix).buildSpannedText(suffix)
        }
        binding.currentPoints.text = doubleToStringNoDecimal(progress.totalPoints.toDouble())

        if (progress.nextLevelPoints != null && progress.nextLevelReward != null) {
            binding.nextPoints.text = getString(
                R.string.amount_formatted,
                progress.nextLevelPoints.formatProgressAmount()
            )

            binding.sliderLevels.progress = progress.progress

            binding.layoutStart.newtLevelName.text = progress.currentLevel
            binding.layoutNextGoal.nextLevelName.text = progress.nextLevel
            binding.layoutNextGoal.textPoints.text = resources.getQuantityString(
                R.plurals.coins_plural_format,
                progress.nextLevelReward.toInt(),
                progress.nextLevelReward.toInt()
            )
        } else {
            binding.sliderLevels.isVisible = false
            binding.sliderPrefix.isVisible = false
            binding.sliderSuffix.isVisible = false
            binding.layoutStart.root.isVisible = false
            binding.layoutNextGoal.root.isVisible = false
            binding.nextPoints.isVisible = false
        }
        showLoading(false)
    }

    private fun String.buildSpannedText(span: String): SpannableString {
        return SpannableString(this).apply {
            val spanStart = indexOf(span)
            setCustomFont(requireContext(), spanStart, spanStart + span.length, common.font.work_sans_bold)
        }
    }
}

interface FitnessDataUpdateContract {
    fun onFitnessDataFetched()
}
