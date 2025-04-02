package com.yourfitness.coach.ui.features.more.challenges.leaderboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yourfitness.coach.data.entity.LeaderboardEntity
import com.yourfitness.coach.domain.leaderboard.LeaderboardId
import com.yourfitness.coach.domain.leaderboard.LeaderboardManager
import com.yourfitness.coach.domain.settings.SettingsManager
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.coach.ui.utils.openDubai30x30Details
import com.yourfitness.common.CommonPreferencesStorage
import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.network.dto.Challenge
import com.yourfitness.common.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    val navigator: Navigator,
    private val profileRepository: ProfileRepository,
    private val leaderboardManager: LeaderboardManager,
    private val settingsManager: SettingsManager,
    private val commonStorage: CommonPreferencesStorage,
    private val savedState: SavedStateHandle
) : MviViewModel<LeaderboardIntent, LeaderboardState>() {

    var hideDetailsActions = false
        private set
    var challenge = savedState.get<Challenge?>("challenge")
        private set
    var leaderboardId = savedState["leaderboard_id"] ?: LeaderboardId.OTHER
        private set
    private val friendId = savedState.get<String?>("friend_id").orEmpty()

    var flow: Flow<PagingData<LeaderboardEntity>>? = null
        private set

    private var profileId: String? = null
    private var corporationId: String? = null
    val checkId: String
        get() = (when (leaderboardId) {
            LeaderboardId.CORPORATE_PRIVATE, LeaderboardId.CORPORATE_GOV, LeaderboardId.CORPORATE_GOV_TEST, LeaderboardId.CORPORATE_PRIVATE_TEST -> corporationId
            LeaderboardId.FRIEND -> friendId
            else -> profileId
        }).orEmpty()

    val measurement get() = when (leaderboardId) {
        LeaderboardId.OTHER, LeaderboardId.FRIEND -> challenge?.measurement.orEmpty()
        else -> LeaderboardAdapter2.STEPS
    }

    private var tabPosition = 0
    private var myRank: LeaderboardEntity? = null

    init {
        loadData()
    }

    override fun handleIntent(intent: LeaderboardIntent) {
        when (intent) {
            is LeaderboardIntent.TabChanged -> {
                flow = null
                tabPosition = intent.tab
                flow = createPager()
                state.value = LeaderboardState.DataUpdated()
            }
            is LeaderboardIntent.OpenDubai30x30Details -> openDubaiDetails()
            is LeaderboardIntent.Update -> loadData()
        }
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            state.postValue(LeaderboardState.ShowLoading)
            delay(150)
            try {
                try {
                    savedState.get<String?>("challenge_string")?.let {
                        val challengeString = it.replace("~", "/")
                        val type = object : TypeToken<Challenge>() {}.type
                        challenge = Gson().fromJson<Challenge>(challengeString, type)
                        hideDetailsActions = true
                    }
                    val leaderboard = savedState.get<Int?>("leaderboard")
                    LeaderboardId.values().firstOrNull { it.id == leaderboard }?.let {id ->
                        leaderboardId = id
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                }

                if (leaderboardId == LeaderboardId.GLOBAL) {
                    myRank = leaderboardManager.getSavedMyGlobalRank()
                    state.postValue(LeaderboardState.Loading(true, myRank))
                }

                val profile = profileRepository.getProfile()
                profileId = profile.id
                corporationId = profile.corporationId

                flow = createPager()
                state.postValue(LeaderboardState.DataUpdated(myRank))
            } catch (error: Exception) {
                Timber.e(error)
            }
        }
    }

    private fun createPager() = Pager(
        PagingConfig(
            pageSize = 20,
            prefetchDistance = 4,
            initialLoadSize = 20,
            enablePlaceholders = false
        )
    ) {
        LeaderBoardPagingSource { offset ->
            val data = when (leaderboardId) {
                LeaderboardId.GLOBAL -> {
                    val dataList = leaderboardManager.fetchGlobalLeaderboard(offset)
                    myRank = dataList?.removeLast()
                    dataList
                }

                LeaderboardId.CORPORATE_PRIVATE, LeaderboardId.CORPORATE_GOV -> {
                    if (tabPosition == 0) leaderboardManager.fetchPrivateLeaderboard(offset)
                    else leaderboardManager.fetchGovLeaderboard(offset)
                }
                LeaderboardId.CORPORATE_PRIVATE_TEST, LeaderboardId.CORPORATE_GOV_TEST -> {
                    if (tabPosition == 0) leaderboardManager.fetchPrivateLeaderboardTest(offset)
                    else leaderboardManager.fetchGovLeaderboardTest(offset)
                }
                LeaderboardId.OTHER -> {
                    val dataList = leaderboardManager.getLeaderboardById(
                        challenge?.id.orEmpty(),
                        getPeriod(),
                        offset
                    )
                    myRank = dataList.removeLast()
                    if (profileId != myRank?.profileId) myRank = null
                    dataList
                }
                LeaderboardId.FRIEND -> {
                    val dataList = leaderboardManager.getFriendLeaderboardById(
                        friendId,
                        challenge?.id.orEmpty(),
                        getPeriod(),
                        offset
                    )
                    myRank = dataList.removeLast()
                    if (friendId != myRank?.profileId) myRank = null
                    dataList
                }

                else -> emptyList()
            }
            state.postValue(LeaderboardState.Loading(false, myRank))
            data
        }
    }.flow.cachedIn(viewModelScope)

    private fun getPeriod(): String {
        return when (tabPosition) {
            0 -> ALL_TIME
            1 -> MONTH
            2 -> WEEK
            else -> WEEK
        }
    }

    private fun openDubaiDetails() =
        viewModelScope.launch { navigator.openDubai30x30Details(settingsManager, commonStorage) }

    companion object {
        private const val WEEK = "week"
        private const val MONTH = "month"
        private const val ALL_TIME = "all-time"
    }
}

sealed class LeaderboardState {
    object ShowLoading: LeaderboardState()
    data class Loading(val active: Boolean, val rank: LeaderboardEntity?) : LeaderboardState()
    data class DataUpdated(val rank: LeaderboardEntity? = null) : LeaderboardState()
}

sealed class LeaderboardIntent {
    data class TabChanged(val tab: Int) : LeaderboardIntent()
    object OpenDubai30x30Details : LeaderboardIntent()
    object Update : LeaderboardIntent()
}
