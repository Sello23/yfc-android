package com.yourfitness.coach.ui.features.progress.levels

import android.os.Bundle
import android.text.SpannableString
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.DialogLevelSystemBinding
import com.yourfitness.coach.domain.progress.points.ProgressInfo
import com.yourfitness.common.network.dto.ProgressLevel
import com.yourfitness.common.ui.mvi.MviDialogFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setCustomFont
import com.yourfitness.common.ui.utils.setupBottomInsets
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.common.ui.utils.toStringNoDecimal
import dagger.hilt.android.AndroidEntryPoint
import com.yourfitness.common.R as common

@AndroidEntryPoint
class LevelSystemDialogFragment : MviDialogFragment<Any, LevelSystemDialogState, LevelSystemDialogViewModel>() {

    override val binding: DialogLevelSystemBinding by viewBinding()
    override val viewModel: LevelSystemDialogViewModel by viewModels()
    private val levelSystemAdapter by lazy { LevelSystemAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomInsets(binding.root)
        binding.close.setOnClickListener { findNavController().navigateUp() }
        binding.pointsInfo.setOnClickListener {
            viewModel.intent.value = LevelSystemDialogIntent.HowPointsCalculatedTapped
        }
    }

    override fun renderState(state: LevelSystemDialogState) {
        when (state) {
            is LevelSystemDialogState.DataLoaded -> {
                showProgressPoints(state.progress)
                showPointsHint(state)
                showLevels(state.progress.levels, state.progress.totalPoints)
            }
        }
    }

    private fun showProgressPoints(progress: ProgressInfo) {
        showLoading(false, R.id.content)
        Glide.with(this).load(progress.levelLogoId.toImageUri()).into(binding.levelLogo)
        binding.sliderPrefix.isVisible = !progress.isFirstLevel
        binding.title.text = getString(R.string.your_level_is, progress.currentLevel)
        binding.nextLevelMsg.text = if (progress.pointsToNextLevel != null) {
            val points = resources.getQuantityString(
                R.plurals.points_plural_format,
                progress.pointsToNextLevel.toInt(),
                progress.pointsToNextLevel.toString()
            )
            getString(R.string.next_level_msg_2, points, progress.nextLevel).buildSpannedText(points)
        } else {
            getString(R.string.last_level_msg2)
        }

        if (progress.nextLevelPoints != null && progress.nextLevelReward != null) {
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
        }
    }

    private fun showPointsHint(state: LevelSystemDialogState.DataLoaded) {
        binding.pointLevel.text = resources.getQuantityString(
            R.plurals.points_plural_format,
            state.pointLevel.toInt(), state.pointLevel.toStringNoDecimal()
        )
        binding.rewardLevel.text = resources.getQuantityString(
            R.plurals.extra_coins,
            state.rewardLevel.toInt(), state.rewardLevel.toStringNoDecimal()
        )
    }

    private fun showLevels(levels: List<ProgressLevel>, totalPoints: Long) {
        levelSystemAdapter.setData(levels, totalPoints)
            binding.levelsList.adapter = levelSystemAdapter
            binding.levelsList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    private fun String.buildSpannedText(span: String): SpannableString {
        return SpannableString(this).apply {
            val spanStart = indexOf(span)
            setCustomFont(requireContext(), spanStart, spanStart + span.length, common.font.work_sans_bold)
        }
    }
}
