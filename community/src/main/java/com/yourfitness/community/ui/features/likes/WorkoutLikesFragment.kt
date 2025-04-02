package com.yourfitness.community.ui.features.likes

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.community.network.dto.LikesInfo
import com.yourfitness.comunity.R
import com.yourfitness.comunity.databinding.FragmentWorkoutLikesBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.ZoneOffset
import java.time.ZonedDateTime

@AndroidEntryPoint
class WorkoutLikesFragment :
    MviFragment<WorkoutLikesIntent, WorkoutLikesState, WorkoutLikesViewModel>() {

    override val binding: FragmentWorkoutLikesBinding by viewBinding()
    override val viewModel: WorkoutLikesViewModel by viewModels()

    override fun renderState(state: WorkoutLikesState) {
        when (state) {
            is WorkoutLikesState.DayDataLoaded -> showDayData(state.data, state.isToday)
        }
    }

    private fun showDayData(data: LikesInfo?, isToday: Boolean) {
        binding.apply {
            val images = data?.images ?: emptyList()
            val likesCount = data?.likesCount ?: 0
            val newLikesCount = data?.newLikesCount ?: 0

            root.setOnClickListener {
                viewModel.intent.value = if (images.isEmpty() && newLikesCount == 0) {
                    WorkoutLikesIntent.NoLikesClicked
                } else {
                    WorkoutLikesIntent.LikesClicked(newLikesCount)

                }
            }

            shortcut1.apply {
                root.isVisible = images.isNotEmpty()
                Glide.with(requireContext()).load(images.getOrNull(0)?.toImageUri()).into(imageProfile)
            }
            shortcut2.apply {
                root.isVisible = images.size > 1
                Glide.with(requireContext()).load(images.getOrNull(1)?.toImageUri()).into(imageProfile)
            }
            shortcut3.apply {
                root.isVisible = likesCount > 2
                amount.isVisible = likesCount > 2
                amount.text = "+${(likesCount - images.size).coerceAtLeast(0)}"
            }

            binding.info.text = if (likesCount > 0) {
                resources.getQuantityString(
                    R.plurals.people_liked_this_workout,
                    likesCount,
                    likesCount
                )
            } else {
                getString(if (newLikesCount > 0) R.string.you_have_new_likes
                else R.string.workout_not_liked)
            }

            binding.newLikes.apply {
                isVisible = newLikesCount > 0
                text = if (newLikesCount > 99) getString(R.string.max_new_likes_label)
                else getString(R.string.new_likes_label, newLikesCount)
            }
        }
    }

    fun updateLikes(date: ZonedDateTime?, zoneOffset: ZoneOffset?) {
        if (date == null || zoneOffset == null) return
        viewModel.intent.value = WorkoutLikesIntent.DateChanged(date, zoneOffset)
    }
}
