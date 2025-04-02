package com.yourfitness.community.ui.features.details

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.yourfitness.common.domain.date.secToMinutes
import com.yourfitness.common.network.dto.Challenge
import com.yourfitness.common.ui.adapters.MyChallengesAdapter
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.addOnTabSelectionListener
import com.yourfitness.common.ui.utils.selectTab
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.common.ui.utils.stringCommaSeparated
import com.yourfitness.community.data.entity.FriendsEntity
import com.yourfitness.community.data.entity.fullName
import com.yourfitness.community.network.dto.TotalScoreDto
import com.yourfitness.community.ui.features.details.adapters.WorkoutsAdapter
import com.yourfitness.comunity.R
import com.yourfitness.comunity.databinding.FragmentFriendDetailsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

@AndroidEntryPoint
class FriendDetailsFragment : MviFragment<FriendDetailsIntent, FriendDetailsState, FriendDetailsViewModel>() {

    override val binding: FragmentFriendDetailsBinding by viewBinding()
    override val viewModel: FriendDetailsViewModel by viewModels()

    private val contentAdapter by lazy { WorkoutsAdapter(::onActionClick) }
    private val requestsAdapter by lazy { MyChallengesAdapter(::onChallengeClick) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.intent.value = FriendDetailsIntent.ScreenOpened

        setupUnfriendListener()

        binding.workoutsList.adapter = contentAdapter
        binding.campaignsList.adapter = requestsAdapter

        binding.workoutsList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.campaignsList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        binding.tabLayout.addOnTabSelectionListener { onTabSelected(it.position) }
        binding.tabLayout.selectTab(binding.tabLayout.getTabAt(viewModel.tabPosition))

        binding.arrowBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.arrowBack2.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.header.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (abs(verticalOffset) == appBarLayout.totalScrollRange) {
                binding.arrowBack2.isVisible = true
            } else {
             if (binding.arrowBack2.isVisible) binding.arrowBack2.isVisible = false
            }
        }

        binding.unfriend.setOnClickListener {
            viewModel.intent.value = FriendDetailsIntent.TryUnfriend
        }
    }

    override fun onPause() {
        super.onPause()

        viewModel.intent.value = FriendDetailsIntent.UploadLikes
    }

    override fun renderState(state: FriendDetailsState) {
        when (state) {
            is FriendDetailsState.Loading -> {
                showLoading(state.active)
            }
            is FriendDetailsState.Error -> {
                showLoading(false)
            }
            is FriendDetailsState.DataLoaded -> {
                setupProfileArea(state.profile, state.totalScore)
                contentAdapter.setData(state.workouts)
                requestsAdapter.setData(state.challenges)
                contentAdapter.notifyDataSetChanged()
                requestsAdapter.notifyDataSetChanged()
                showLoading(false)
            }
            is FriendDetailsState.SomethingWentWrong -> showError(Throwable(getString(com.yourfitness.common.R.string.error_something_went_wrong)))
        }
    }

    private fun onTabSelected(position: Int) {
        binding.workoutsList.isVisible = position == 0
        binding.campaignsList.isVisible = position == 1
        viewModel.intent.postValue(FriendDetailsIntent.TabChanged(position))
    }

    private fun setupProfileArea(profile: FriendsEntity?, total: TotalScoreDto?) {
        val minutes = total?.duration?.secToMinutes() ?: 0
        binding.totalInfo.durationValue.text = minutes.stringCommaSeparated
        binding.totalInfo.durationLabel.text =
            binding.root.context.resources.getQuantityString(R.plurals.minutes, minutes)
        binding.totalInfo.stepsValue.text = (total?.steps ?: 0).stringCommaSeparated
        binding.totalInfo.caloriesValue.text = (total?.calories ?: 0).stringCommaSeparated

        Glide.with(requireContext()).load(profile?.mediaId?.toImageUri()).into(binding.photo)

        binding.textName.text = profile?.fullName
        binding.description.text =
            "${getString(com.yourfitness.common.R.string.level_number, profile?.levelNumber ?: 1)}:" +
                    " ${profile?.progressLevelName.orEmpty()}"
    }

    private fun onActionClick(id: String, liked: Boolean) {
        viewModel.intent.value = FriendDetailsIntent.LikeAction(id, liked)
    }

    private fun onChallengeClick(intent: String, challenge: Challenge) {
        viewModel.intent.value = FriendDetailsIntent.OpenLeaderboard(challenge, intent)
    }

    private fun setupUnfriendListener() {
        setFragmentResultListener(UnfriendDialog.RESULT) { _, bundle ->
            viewModel.resultBundle = bundle
            setupUnfriendListener()
        }
    }

    override fun onDestroy() {
        setFragmentResult(RESULT, viewModel.resultBundle)
        super.onDestroy()
    }

    companion object {
        const val RESULT = "friend_details_result"
    }
}
