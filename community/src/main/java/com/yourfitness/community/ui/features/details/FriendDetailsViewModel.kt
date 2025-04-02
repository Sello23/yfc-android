package com.yourfitness.community.ui.features.details

import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.yourfitness.common.network.dto.Challenge
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.community.data.entity.FriendsEntity
import com.yourfitness.community.data.mappers.toEntity
import com.yourfitness.community.domain.FriendsProfileRepository
import com.yourfitness.community.network.dto.TotalScoreDto
import com.yourfitness.community.network.dto.WorkoutInfoDto
import com.yourfitness.community.ui.navigation.CommunityNavigation
import com.yourfitness.community.ui.navigation.CommunityNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FriendDetailsViewModel @Inject constructor(
    private val navigator: CommunityNavigator,
    private val friendsProfileRepository: FriendsProfileRepository,
    savedState: SavedStateHandle
) : MviViewModel<FriendDetailsIntent, FriendDetailsState>() {

    private val userId = savedState.get<String>("friend_id").orEmpty()
    var tabPosition = 0
        private set
    private val myLikes = mutableMapOf<String, Pair<Boolean, Boolean>>()

    var resultBundle = bundleOf()

    private var workouts: MutableList<WorkoutInfoDto> = mutableListOf()
    private var challenges: List<Challenge> = listOf()
    private var totalScore: TotalScoreDto? = null
    private var profile: FriendsEntity? = null

    init {
        loadData()
    }

    override fun handleIntent(intent: FriendDetailsIntent) {
        when (intent) {
            is FriendDetailsIntent.TabChanged -> {
                tabPosition = intent.tab
            }
            is FriendDetailsIntent.TryUnfriend -> navigator.navigate(CommunityNavigation.Unfriend(userId))
            is FriendDetailsIntent.LikeAction -> {
                val originalLikeState = myLikes[intent.id]?.second ?: false
                myLikes[intent.id] = intent.isLiked to originalLikeState
                workouts.replaceAll {
                    if (it.id == intent.id) {
                        it.copy(
                            likesCount = if (intent.isLiked) (it.likesCount?.plus(1))
                            else it.likesCount?.minus(1),
                            isLiked = intent.isLiked,
                        )
                    } else it
                }
            }
            is FriendDetailsIntent.UploadLikes -> uploadLikes()
            is FriendDetailsIntent.OpenLeaderboard -> openLeaderBoard(intent.challenge, intent.intent)
            is FriendDetailsIntent.ScreenOpened -> {
                if (totalScore != null && profile != null) loadData()
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (totalScore == null || profile == null) {
                    state.postValue(FriendDetailsState.Loading())
                    val progressLevels = friendsProfileRepository.fetchLevels()
                    val data = friendsProfileRepository.getFriendInfo(userId)
                    workouts = data.workouts.orEmpty().toMutableList()
                    challenges = data.challenges.orEmpty()
                    totalScore = data.totalScore
                    profile = data.profileInfo?.toEntity(progressLevels)
                    workouts.map {
                        if (it.isLiked != null && it.id != null) {
                            myLikes[it.id] = it.isLiked to it.isLiked
                        }
                    }
                }
                state.postValue(
                    FriendDetailsState.DataLoaded(challenges, workouts, totalScore, profile)
                )
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(FriendDetailsState.Error)
            }
        }
    }

    private fun openLeaderBoard(challenge: Challenge, intent: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val data = Gson().toJson(challenge)
                val destination = if (intent == "Details") {
                    CommunityNavigation.ChallengeDetails(data)
                } else {
                    CommunityNavigation.Leaderboard(userId, data)
                }
                navigator.navigate(destination)
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(FriendDetailsState.Error)
            }
        }
    }

    private fun uploadLikes() {
        GlobalScope.launch {
            val filteredLikes = myLikes.filter {
                it.value.first != it.value.second
            }
            if (filteredLikes.isNotEmpty()) {
                friendsProfileRepository.uploadLikes(userId, filteredLikes)
                try {
                    filteredLikes.forEach { pair ->
                        val likeState = myLikes[pair.key]?.first ?: false
                        myLikes[pair.key] = likeState to pair.value.first
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }
    }
}

sealed class FriendDetailsState {
    data class Loading(val active: Boolean = true) : FriendDetailsState()
    object Error : FriendDetailsState()
    object SomethingWentWrong : FriendDetailsState()
    data class DataLoaded(
        val challenges: List<Challenge>,
        val workouts: List<WorkoutInfoDto>,
        val totalScore: TotalScoreDto?,
        val profile: FriendsEntity?
    ) : FriendDetailsState()
}

sealed class FriendDetailsIntent {
    data class TabChanged(val tab: Int) : FriendDetailsIntent()
    object TryUnfriend : FriendDetailsIntent()
    data class LikeAction(val id: String, val isLiked: Boolean) : FriendDetailsIntent()
    object UploadLikes : FriendDetailsIntent()
    object ScreenOpened : FriendDetailsIntent()
    data class OpenLeaderboard(val challenge: Challenge, val intent: String) : FriendDetailsIntent()
}
