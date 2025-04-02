package com.yourfitness.coach.ui.features.more.challenges

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.clearFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.yourfitness.coach.R
import com.yourfitness.coach.databinding.FragmentChallengesBinding
import com.yourfitness.coach.domain.leaderboard.LeaderboardId
import com.yourfitness.coach.ui.features.more.challenges.challenge_details.ChallengeDetailsFragment
import com.yourfitness.coach.ui.features.more.challenges.dialogs.join.ChallengeJoinDialogFragment
import com.yourfitness.coach.ui.features.more.challenges.dialogs.join_private_code.ChallengeJoinPrivateCodeDialogFragment
import com.yourfitness.coach.ui.features.more.challenges.dialogs.leave.ChallengeLeaveDialogFragment
import com.yourfitness.coach.ui.features.more.challenges.leaderboard.LeaderboardFragment
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.common.network.dto.Challenge
import com.yourfitness.common.ui.adapters.DETAILS
import com.yourfitness.common.ui.adapters.MyChallengesAdapter
import com.yourfitness.common.ui.mvi.MviFragment
import com.yourfitness.common.ui.mvi.viewBinding
import com.yourfitness.common.ui.utils.setCompoundDrawables
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import reactivecircus.flowbinding.android.widget.textChanges

enum class ChallengeFlow {
    JOIN,
    DETAILS,
    JOIN_PRIVATE,
    GLOBAL
}

@AndroidEntryPoint
class ChallengesFragment : MviFragment<ChallengesIntent, ChallengesState, ChallengesViewModel>() {

    override val binding: FragmentChallengesBinding by viewBinding()
    override val viewModel: ChallengesViewModel by viewModels()

    private val myChallengesAdapter by lazy { MyChallengesAdapter(::onItemClicked) }
    private val activeChallengesAdapter by lazy { ActiveChallengesAdapter(::onItemClicked) }
    private val searchChallengesAdapter by lazy { ActiveChallengesAdapter(::onItemClicked) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar(binding.toolbar.root)
        binding.groupContent.isVisible = false
        binding.toolbar.toolbar.title = getString(R.string.challenges_screen_challenges_text)
        binding.toolbarSearch.buttonCancel.isVisible = false
        binding.toolbar.root.elevation = 0f

        binding.searchResultList.adapter = searchChallengesAdapter
        binding.searchResultList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        lifecycleScope.launch {
            binding.toolbarSearch.editSearch.textChanges().debounce(100).collect { onQueryChanged(it) }
        }

        setupDubaiContentVisibility()

        binding.globalLeaderboard.apply {
            viewDubaiInfo.infoTitle.setCompoundDrawables(start = null)
            trackProgress.text = getString(R.string.global_leaderboard)
            viewDubaiInfo.infoTitle.text = getString(R.string.global_dubai_30x30)
            viewDubaiInfo.infoMsg.text = getString(R.string.global_dubai_30x30_msg)
            viewDubaiInfo.dubaiImage.setImageResource(R.drawable.dubai_logo)
            trackProgress.setOnClickListener {
                viewModel.navigator.navigate(Navigation.Leaderboard(leaderboardId = LeaderboardId.GLOBAL))
            }
        }

        binding.intercorporateLeaderboardTest.apply {
            root.isVisible = viewModel.isIntercorporateStarted && !viewModel.isIntercorporateEnded
            root.background = ContextCompat.getDrawable(requireContext(), R.drawable.shape_rounded_4_blue)
            viewDubaiInfo.infoTitle.setCompoundDrawables(start = null)
            trackProgress.text = getString(R.string.intercorporate_leaderboard)
            viewDubaiInfo.infoTitle.text = getString(R.string.intercorporate_competition)
            viewDubaiInfo.infoMsg.text = getString(R.string.intercorporate_competition_msg2)
            trackProgress.setOnClickListener {
                viewModel.navigator.navigate(Navigation.Leaderboard(leaderboardId = LeaderboardId.CORPORATE_PRIVATE_TEST))
            }

        }

        binding.intercorporateLeaderboard.apply {
            root.background = ContextCompat.getDrawable(requireContext(), R.drawable.shape_rounded_4_blue)
            viewDubaiInfo.infoTitle.setCompoundDrawables(start = null)
            trackProgress.text = getString(R.string.intercorporate_leaderboard)
            viewDubaiInfo.infoTitle.text = getString(R.string.intercorporate_competition)
            viewDubaiInfo.infoMsg.text = getString(R.string.intercorporate_competition_msg)
            trackProgress.setOnClickListener {
                viewModel.navigator.navigate(Navigation.Leaderboard(leaderboardId = LeaderboardId.CORPORATE_PRIVATE))
            }
        }

        binding.dubaiPreview.apply {
            viewDubaiInfo.infoTitle.setCompoundDrawables(start = null)
            viewDubaiInfo.dubaiImage.setImageResource(R.drawable.dubai_logo)
            trackProgress.text = getString(com.yourfitness.common.R.string.challenges_screen_details_text)
            viewDubaiInfo.infoTitle.text = getString(R.string.dubai_30x30)
            viewDubaiInfo.infoMsg.text = getString(R.string.global_dubai_30x30_preview_msg)
            trackProgress.setOnClickListener {
                viewModel.intent.value = ChallengesIntent.OpenDubaiDetails
            }
        }

        setupDetailsFragmentListener()
        setupLeaderboardFragmentListener()
        setupJoinListener()
        setupJoinPrivateListener()
        setupLeaveListener()
    }

    private fun setupDubaiContentVisibility() {
        binding.textDubai30x30.isVisible = viewModel.isDubai30x30Active &&
                (viewModel.isDubai30x30Started || viewModel.isDubai30x30Preview)
        binding.dubaiPreview.root.isVisible = viewModel.isDubai30x30Active &&
                !viewModel.isDubai30x30Started && viewModel.isDubai30x30Preview
        binding.dubaiContent.isVisible = viewModel.isDubai30x30Active && viewModel.isDubai30x30Started
    }

    override fun onStart() {
        super.onStart()
        val navController = findNavController()
        val prevId = navController.previousBackStackEntry?.destination?.id
        if (prevId == R.id.fragment_challenge_details) {
            navController.popBackStack(prevId, true)
        }
    }

    private fun setupDetailsFragmentListener() {
        setFragmentResultListener(ChallengeDetailsFragment.RESULT) { _, bundle ->
            parseResultBundle(bundle)
            clearFragmentResult(ChallengeDetailsFragment.RESULT)
            setupDetailsFragmentListener()
        }
    }

    private fun setupLeaderboardFragmentListener() {
        setFragmentResultListener(LeaderboardFragment.RESULT) { _, bundle ->
            parseResultBundle(bundle)
            clearFragmentResult(LeaderboardFragment.RESULT)
            setupLeaderboardFragmentListener()
        }
    }

    private fun parseResultBundle(bundle: Bundle) {
        val challenge = bundle.get("challenge") as Challenge?
        if (challenge != null) {
            viewModel.getChallenges()
//            if (bundle.getBoolean("joined")) viewModel.moveToMyChallenges(challenge)
//            else viewModel.removeFromMyChallenges(challenge)
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
//            viewModel.moveToMyChallenges(challenge)
            viewModel.getChallenges()
        }
    }

    private fun setupLeaveListener() {
        setFragmentResultListener(ChallengeLeaveDialogFragment.RESULT) { _, bundle ->
            val challenge = bundle.get("challenge") as Challenge?
            if (challenge != null) {
                viewModel.removeFromMyChallenges(challenge)
            }
            clearFragmentResult(ChallengeLeaveDialogFragment.RESULT)
            setupLeaveListener()
        }
    }

    override fun renderState(state: ChallengesState) {
        when (state) {
            is ChallengesState.Loading -> showLoading(true)
            is ChallengesState.Success -> {
                if (state.pinnedChallengesUpdated) {
                    setupDubaiContentVisibility()
                }
                showChallenges(state)
            }
            is ChallengesState.More -> showMore(state)
            is ChallengesState.Less -> showLess(state)
            is ChallengesState.Error -> showError()
            is ChallengesState.SearchResult -> showSearchResults(state)
        }
    }

    private fun onQueryChanged(query: CharSequence) {
        viewModel.intent.postValue(ChallengesIntent.Search(query.toString().trim()))
    }

    private fun showChallenges(state: ChallengesState.Success) {
        binding.textResults.isVisible = false
        binding.searchResultList.isVisible = false
        binding.emptySearchState.isVisible = false
        binding.nestedScroll.isVisible = true
        binding.groupNewChallenges.isVisible = false

        showLoading(false)
        binding.groupContent.isVisible = true
        if (state.myChallenges.isNotEmpty()) {
            binding.groupMyChallenges.isVisible = true
            if (state.myChallengesAmount <= 3) {
                binding.textShowMore.isVisible = false
            } else {
                binding.textShowMore.isVisible = true
                binding.textShowMore.text = getString(R.string.challenges_screen_show_more_text)
                binding.textShowMore.setOnClickListener { viewModel.getMore() }
            }
            myChallengesAdapter.setData(state.myChallenges)
            binding.myChallengesList.adapter = myChallengesAdapter
            binding.myChallengesList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        } else {
            binding.groupMyChallenges.isVisible = false
            binding.textShowMore.isVisible = false
        }
        activeChallengesAdapter.setData(state.activeChallenges)
        binding.activeChallengesList.adapter = activeChallengesAdapter
        binding.activeChallengesList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        if (state.myChallenges.isEmpty() && state.activeChallenges.isEmpty()) {
            showError()
        }
    }

    private fun showMore(state: ChallengesState.More) {
        showLoading(false)
        binding.textShowMore.text = getString(R.string.challenges_screen_show_less_text)
        binding.textShowMore.setOnClickListener { viewModel.getLess() }
        myChallengesAdapter.setData(state.challenges)
        binding.myChallengesList.adapter = myChallengesAdapter
        binding.myChallengesList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    private fun showLess(state: ChallengesState.Less) {
        showLoading(false)
        binding.textShowMore.text = getString(R.string.challenges_screen_show_more_text)
        binding.textShowMore.setOnClickListener { viewModel.getMore() }
        myChallengesAdapter.setData(state.challenges)
        binding.myChallengesList.adapter = myChallengesAdapter
        binding.myChallengesList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    fun showError() {
        showLoading(false)
        binding.groupContent.isVisible = false
        binding.groupNewChallenges.isVisible = true
    }

    private fun onItemClicked(intent: String, challenge: Challenge) {
        when (intent) {
            DETAILS -> {
                viewModel.navigator.navigate(Navigation.ChallengeDetails(challenge, ChallengeFlow.DETAILS))
            }
            LEADERBOARD -> {
                viewModel.navigator.navigate(Navigation.Leaderboard(challenge, LeaderboardId.OTHER))
            }
            JOIN -> {
                viewModel.navigator.navigate(Navigation.ChallengeJoin(challenge))
            }
            JOIN_DETAILS -> {
                viewModel.navigator.navigate(Navigation.ChallengeDetails(challenge, ChallengeFlow.JOIN))
            }
            JOIN_PRIVATE -> {
                viewModel.navigator.navigate(Navigation.ChallengeJoinPrivate(challenge))
            }
            JOIN_DETAILS_PRIVATE -> {
                viewModel.navigator.navigate(Navigation.ChallengeDetails(challenge, ChallengeFlow.JOIN_PRIVATE))
            }
            SHOW_MORE -> {
                viewModel.getMore()
            }
            SHOW_LESS -> {
                viewModel.getLess()
            }
        }
    }

    private fun showSearchResults(state: ChallengesState.SearchResult) {
        binding.textResults.isVisible = state.challenges.isNotEmpty()
        binding.emptySearchState.isVisible = state.challenges.isEmpty()
        binding.searchResultList.isVisible = state.challenges.isNotEmpty()
        binding.nestedScroll.isVisible = false
        binding.groupNewChallenges.isVisible = false

        binding.textResults.text = getString(
            com.yourfitness.common.R.string.map_screen_results_format,
            state.challenges.size
        )
        searchChallengesAdapter.setData(state.challenges)
        searchChallengesAdapter.notifyDataSetChanged()
    }

    companion object {
        private const val LEADERBOARD = "Leaderboard"
        private const val JOIN = "Join"
        const val JOIN_DETAILS = "Join details"
        const val JOIN_PRIVATE = "Join private"
        const val JOIN_DETAILS_PRIVATE = "Join details private"
        private const val SHOW_MORE = "Show more"
        private const val SHOW_LESS = "Show less"
    }
}