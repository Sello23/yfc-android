package com.yourfitness.coach.ui.features.more.challenges.leaderboard

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.clearFragmentResult
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.yourfitness.coach.R
import com.yourfitness.coach.data.entity.LeaderboardEntity
import com.yourfitness.coach.data.entity.fullName
import com.yourfitness.coach.databinding.FragmentLeaderboardBinding
import com.yourfitness.coach.databinding.ToolbarIconBinding
import com.yourfitness.coach.domain.leaderboard.LeaderboardId
import com.yourfitness.coach.ui.features.more.challenges.ChallengeFlow
import com.yourfitness.coach.ui.features.more.challenges.challenge_details.ChallengeDetailsFragment
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.utils.LeaderboardUtils
import com.yourfitness.coach.ui.utils.toPx
import com.yourfitness.common.network.dto.Challenge
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.addOnTabSelectionListener
import com.yourfitness.common.ui.utils.setCompoundDrawables
import com.yourfitness.common.ui.utils.toImageUri
import com.yourfitness.common.ui.utils.toStringNoDecimal
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class LeaderboardFragment : MviFragment<LeaderboardIntent, LeaderboardState, LeaderboardViewModel>() {

    override val binding: FragmentLeaderboardBinding by viewBinding()
    override val viewModel: LeaderboardViewModel by viewModels()

    private var pagingAdapter: LeaderboardAdapter2? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.apply {
            setupToolbar(root)
            setupTitle()
            val isGovernment = viewModel.leaderboardId == LeaderboardId.CORPORATE_PRIVATE ||
                    viewModel.leaderboardId == LeaderboardId.CORPORATE_GOV ||
                    viewModel.leaderboardId == LeaderboardId.CORPORATE_GOV_TEST ||
                    viewModel.leaderboardId == LeaderboardId.CORPORATE_PRIVATE_TEST
            tabLayout.isVisible = isGovernment
//                viewModel.leaderboardId != LeaderboardId.GLOBAL || (today().time <= (viewModel.challenge?.endDate?.toMilliseconds()
//                    ?: 0L) && viewModel.leaderboardId == LeaderboardId.OTHER)
            if (isGovernment) {
                tabLayout.removeTabAt(0)
                tabLayout.removeTabAt(0)
                tabLayout.removeTabAt(0)
                textChallengeName.setCompoundDrawables(start = null)
            } else if (viewModel.leaderboardId == LeaderboardId.OTHER ||
                viewModel.leaderboardId == LeaderboardId.FRIEND) {
                tabLayout.removeTabAt(3)
                tabLayout.removeTabAt(3)
            }
            tabLayout.addOnTabSelectionListener { onTabSelected(it.position) }
            textChallengeName.setOnClickListener {
                setupFragmentListener()
                if (viewModel.leaderboardId == LeaderboardId.GLOBAL) {
                    viewModel.intent.value = LeaderboardIntent.OpenDubai30x30Details
                } else {
                    viewModel.challenge?.let {
                        viewModel.navigator.navigate(
                            Navigation.ChallengeDetails(
                                it,
                                ChallengeFlow.DETAILS,
                                hideActions = viewModel.hideDetailsActions,
                            )
                        )
                    }
                }
            }
        }

        binding.globalLeaderboardContainer.imageBg.setBackgroundResource(
            if (viewModel.leaderboardId == LeaderboardId.OTHER ||
                viewModel.leaderboardId == LeaderboardId.FRIEND) R.drawable.leaderboard_rank_bg
            else R.drawable.global_leaderboard_bg
        )
        binding.globalLeaderboardContainer.imageBg.updateLayoutParams<ViewGroup.LayoutParams> {
            height = (if (viewModel.leaderboardId == LeaderboardId.OTHER ||
                viewModel.leaderboardId == LeaderboardId.FRIEND) 214 else 276).toPx()
        }

        binding.leaderboard.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        setupEmptyState()
    }

    private fun ToolbarIconBinding.setupTitle() {
        val uiData = uiDataMap[viewModel.leaderboardId]
        textChallengeName.text =
            if (uiData == null) viewModel.challenge?.name else getString(uiData.toolbarTitle)
    }

    private fun setupFlowListener() {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.flow?.collectLatest { pagingData ->
                pagingAdapter = createPagingAdapter()
                pagingAdapter?.addOnPagesUpdatedListener { showLoading(false) }
                withContext(Dispatchers.Main) {
                    binding.leaderboard.adapter = pagingAdapter
                }
                pagingAdapter?.submitData(pagingData)
            }
        }
    }

    override fun renderState(state: LeaderboardState) {
        when (state) {
            is LeaderboardState.ShowLoading -> showLoading(true)
            is LeaderboardState.Loading -> {
                showLoading(state.active)
                binding.toolbar.setupTitle()
                if (state.rank != null) {
                    setupMyRankCard(state.rank)
                }
                if (state.active) return
                pagingAdapter?.addOnPagesUpdatedListener {
                    val isEmpty = (pagingAdapter?.itemCount ?: 0) <= 0
                    binding.globalLeaderboardContainer.root.isVisible =
                        (viewModel.leaderboardId == LeaderboardId.GLOBAL ||
                                viewModel.leaderboardId == LeaderboardId.OTHER ||
                                viewModel.leaderboardId == LeaderboardId.FRIEND) && !isEmpty
                    binding.leaderboard.isVisible = !isEmpty
                    binding.emptyState.root.isVisible = isEmpty
                }
            }
            is LeaderboardState.DataUpdated -> {
                setupFlowListener()
                if (state.rank != null) {
                    setupMyRankCard(state.rank)
                }
            }
        }
    }

    private fun setupMyRankCard(rank: LeaderboardEntity) {
        binding.globalLeaderboardContainer.apply {
            name.text = rank.fullName
            Glide.with(requireContext()).load(rank.mediaId.toImageUri()).into(imagePhoto)
            points.text = LeaderboardUtils.getScore(requireContext(), rank.score, viewModel.measurement)
            place.text = getString(R.string.place_number, rank.rank.toStringNoDecimal())
        }
    }

    private fun onTabSelected(position: Int) {
        showLoading(true)
        viewModel.intent.postValue(LeaderboardIntent.TabChanged(position))
    }

    private fun createPagingAdapter() = LeaderboardAdapter2(
        viewModel.checkId,
        viewModel.measurement,
        viewModel.leaderboardId,
        viewModel.leaderboardId != LeaderboardId.GLOBAL &&
                viewModel.leaderboardId != LeaderboardId.OTHER &&
                viewModel.leaderboardId != LeaderboardId.FRIEND
    )

    private fun setupFragmentListener() {
        clearFragmentResult(ChallengeDetailsFragment.RESULT)
        setFragmentResultListener(ChallengeDetailsFragment.RESULT) { _, bundle ->
            setFragmentResult(RESULT, bundle)

            val challenge = bundle.get("challenge") as Challenge?
            if (challenge != null && !bundle.getBoolean("joined")) {
                findNavController().navigateUp()
            } else {
                viewModel.intent.postValue(LeaderboardIntent.Update)
            }
            clearFragmentResult(ChallengeDetailsFragment.RESULT)
            clearFragmentResultListener(ChallengeDetailsFragment.RESULT)
        }
    }

    private fun setupEmptyState() {
        binding.emptyState.apply {
            icon.setImageResource(R.drawable.ic_leaderboard_empty_state)
            title.text = getString(R.string.results_are_coming_soon)
            message.text = getString(R.string.the_data_will_be_available_within_24_hours)
        }
    }

    companion object {
        const val RESULT = "leaderboard_action_result"

        private val uiDataMap = mapOf(
            LeaderboardId.GLOBAL to LeaderBoardUiData(R.string.dubai_30x30),
            LeaderboardId.CORPORATE_PRIVATE to LeaderBoardUiData(R.string.dubai_30x30_intercorporate),
            LeaderboardId.CORPORATE_GOV to LeaderBoardUiData(R.string.dubai_30x30_intercorporate),
            LeaderboardId.CORPORATE_PRIVATE_TEST to LeaderBoardUiData(R.string.intercorporate_leaderboard),
            LeaderboardId.CORPORATE_GOV_TEST to LeaderBoardUiData(R.string.intercorporate_leaderboard),
        )
    }
}

private data class LeaderBoardUiData(
    @StringRes val toolbarTitle: Int
)
