package com.yourfitness.coach.ui.features.more.challenges

import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.domain.fb_remote_config.FirebaseRemoteConfigRepository
import com.yourfitness.coach.domain.settings.SettingsManager
import com.yourfitness.coach.network.YFCRestApi
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.coach.ui.utils.openDubai30x30Details
import com.yourfitness.common.CommonPreferencesStorage
import com.yourfitness.common.domain.date.addDays
import com.yourfitness.common.domain.date.toDate
import com.yourfitness.common.domain.date.today
import com.yourfitness.common.network.dto.Challenge
import com.yourfitness.common.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ChallengesViewModel @Inject constructor(
    val navigator: Navigator,
    private val restApi: YFCRestApi,
    private val settingsManager: SettingsManager,
    private val commonStorage: CommonPreferencesStorage,
    rmConfig: FirebaseRemoteConfigRepository,
) : MviViewModel<ChallengesIntent, ChallengesState>() {

    private var previewStart = commonStorage.dubaiStart.toDate()?.addDays(-30)
    val isDubai30x30Active = rmConfig.dubai3030Enabled
    var isDubai30x30Started = commonStorage.dubaiStart != null && today().after(commonStorage.dubaiStart.toDate())
        private set
    var isDubai30x30Preview = commonStorage.dubaiStart != null && previewStart != null &&
            today().after(previewStart) && !today().after(commonStorage.dubaiStart.toDate())
        private set
    var isIntercorporateStarted = commonStorage.intercorporateStart != null && today().after(
        commonStorage.intercorporateStart?.toDate() ?: today()
    )
        private set
    var isIntercorporateEnded =
        commonStorage.intercorporateEnd != null && (commonStorage.intercorporateEnd?.toDate()
            ?: today()).before(today())
        private set

    private var myChallenges: MutableList<Challenge>? = null
    private var activeChallenge: MutableList<Challenge>? = null

    private var searchString = ""
    private var showMore = false

    init {
        getChallenges()
    }

    override fun handleIntent(intent: ChallengesIntent) {
        when (intent) {
            is ChallengesIntent.Search -> search(intent.query)
            is ChallengesIntent.OpenDubaiDetails -> {
                openDubaiDetails()
            }
        }
    }

    private fun openDubaiDetails() =
        viewModelScope.launch { navigator.openDubai30x30Details(settingsManager, commonStorage) }

    fun getChallenges() {
        viewModelScope.launch {
            try {
                if (searchString.isNotEmpty()) {
                    search(searchString)
                }
                state.postValue(ChallengesState.Loading)
                myChallenges = restApi.getJoinedChallenge(0, 0).sortedBy { it.startDate }.toMutableList()
                activeChallenge = restApi.getAvailableChallenge(0, 0).sortedBy { it.startDate }.toMutableList()

                val pinnedChallengesUpdated = updatePinnedChallengesData()
                loadChallenges(pinnedChallengesUpdated)
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(ChallengesState.Error(error))
            }
        }
    }

    private suspend fun updatePinnedChallengesData(): Boolean {
        if (commonStorage.intercorporateStart != null && commonStorage.intercorporateEnd != null &&
            commonStorage.dubaiStart != null && commonStorage.dubaiEnd != null) return false

        settingsManager.getSettings()
        previewStart = commonStorage.dubaiStart.toDate()?.addDays(-30)
        isDubai30x30Started = commonStorage.dubaiStart != null && today().after(commonStorage.dubaiStart.toDate())
        isDubai30x30Preview = commonStorage.dubaiStart != null && previewStart != null &&
                today().after(previewStart) && !today().after(commonStorage.dubaiStart.toDate())
        isIntercorporateStarted = commonStorage.intercorporateStart != null && today().after(
            commonStorage.intercorporateStart?.toDate() ?: today()
        )
        isIntercorporateEnded =
            commonStorage.intercorporateEnd != null && (commonStorage.intercorporateEnd?.toDate()
                ?: today()).before(today())

        return true
    }

    fun moveToMyChallenges(challenge: Challenge) {
        myChallenges?.add(challenge)
        myChallenges = myChallenges?.sortedBy { it.startDate }?.toMutableList()

        activeChallenge?.removeIf { it.id == challenge.id }

        if (searchString.isNotEmpty()) {
            search(searchString)
        } else {
            loadChallenges()
        }
    }

    fun removeFromMyChallenges(challenge: Challenge) {
        activeChallenge?.add(challenge)
        activeChallenge = activeChallenge?.sortedBy { it.startDate }?.toMutableList()

        myChallenges?.removeIf { it.id == challenge.id }

        if (searchString.isNotEmpty()) {
            search(searchString)
        } else {
            loadChallenges()
        }
    }

    fun getMore() {
        showMore = true
        viewModelScope.launch {
            try {
                state.postValue(ChallengesState.Loading)
                state.postValue(ChallengesState.More(myChallenges.orEmpty()))
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(ChallengesState.Error(error))
            }
        }
    }

    fun getLess() {
        showMore = false
        viewModelScope.launch {
            try {
                state.postValue(ChallengesState.Loading)
                state.postValue(ChallengesState.Less(myChallenges?.take(3).orEmpty()))
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(ChallengesState.Error(error))
            }
        }
    }

    private fun loadChallenges(pinnedChallengesUpdated: Boolean = false) {
        state.postValue(
            ChallengesState.Success(
                (if (showMore) myChallenges else myChallenges?.take(3)).orEmpty(),
                activeChallenge.orEmpty(),
                myChallenges?.size ?: 0,
                pinnedChallengesUpdated
            )
        )
    }

    private fun search(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val normalizedQuery = query.trim().lowercase()
                if (normalizedQuery.isBlank()) {
                    if (searchString.isNotBlank()) loadChallenges()
                    searchString = ""
                    return@launch
                }
                searchString = normalizedQuery
                val searchResult = activeChallenge?.filter {
                    it.name?.lowercase()?.contains(normalizedQuery) ?: false }
                state.postValue(ChallengesState.SearchResult(searchResult.orEmpty()))
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(ChallengesState.Error(error))
            }
        }
    }
}

sealed class ChallengesIntent {
    data class Search(val query: String) : ChallengesIntent()
    object OpenDubaiDetails : ChallengesIntent()
}

open class ChallengesState {
    object Loading : ChallengesState()
    data class Error(val error: Exception) : ChallengesState()
    data class Success(
        val myChallenges: List<Challenge>,
        val activeChallenges: List<Challenge>,
        val myChallengesAmount: Int,
        val pinnedChallengesUpdated: Boolean
    ) : ChallengesState()
    data class More(val challenges: List<Challenge>) : ChallengesState()
    data class Less(val challenges: List<Challenge>) : ChallengesState()
    data class SearchResult(val challenges: List<Challenge>) : ChallengesState()
}