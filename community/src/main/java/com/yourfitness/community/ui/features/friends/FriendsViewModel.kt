package com.yourfitness.community.ui.features.friends

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.yourfitness.common.network.dto.ProgressLevel
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.community.data.entity.FriendsEntity
import com.yourfitness.community.domain.FriendsProfileRepository
import com.yourfitness.community.ui.navigation.CommunityNavigation
import com.yourfitness.community.ui.navigation.CommunityNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    val navigator: CommunityNavigator,
    private val friendsProfileRepository: FriendsProfileRepository,
    savedState: SavedStateHandle
) : MviViewModel<FriendsIntent, FriendsState>() {

    private var tabPosition = savedState["tab_position"] ?: 0
    var flow: Flow<PagingData<FriendsEntity>>? = null
        private set

    private val friends = mutableMapOf<String, FriendsEntity>()
    private var searchText: String? = null

    private var progressLevels: List<ProgressLevel>? = null

    init {
        loadData()
    }

    override fun handleIntent(intent: FriendsIntent) {
        when (intent) {
            is FriendsIntent.TabChanged -> {
                tabPosition = intent.tab
            }
            is FriendsIntent.Search -> {
                val search = intent.query?.trim()
                searchText = search
                if (search == null) {
                    prepareUiData()
                } else if (search.isNotBlank()) {
                    flow = createPager()
                    state.value = FriendsState.SearchPrepared
                } else {
                    flow = null
                    state.value = FriendsState.SearchCleared
                }
            }
            is FriendsIntent.AcceptInvite -> acceptRequest(intent.item, intent.position)
            is FriendsIntent.DeclineInvite -> declineRequest(intent.item, intent.position)
            is FriendsIntent.RequestFriend -> requestFriend(intent.item, intent.position)
            is FriendsIntent.Details -> navigator.navigate(CommunityNavigation.FriendDetails(intent.id))
            is FriendsIntent.FriendDeleted -> processDeleteFriend(intent.id)
            is FriendsIntent.ShowSavedData -> prepareFriendsLists()
        }
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                state.postValue(FriendsState.Loading())
                progressLevels = friendsProfileRepository.fetchLevels()
                friendsProfileRepository.updateFriends(progressLevels.orEmpty())
                friends.clear()
                friendsProfileRepository.getFriends()
                    .filter { !it.requestOut }
                    .map {
                        friends[it.id] = it
                    }
                prepareUiData()
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(FriendsState.Error)
            }
        }
    }

    private fun prepareUiData() {
        val data = friends.values.groupBy { it.isFriend }
        state.postValue(
            FriendsState.FriendsLoaded(
                data[true]?.toList().orEmpty(),
                data[false]?.toList().orEmpty(),
                tabPosition,
            )
        )
    }

    private fun createPager() = Pager(
        PagingConfig(
            pageSize = 20,
            prefetchDistance = 4,
            initialLoadSize = 20,
            enablePlaceholders = false
        )
    ) {
        SearchFriendsPagingSource { offset ->
            friendsProfileRepository.searchProfiles(
                progressLevels.orEmpty(),
                offset = offset,
                searchText = searchText ?: ""
            )
        }
    }.flow.cachedIn(viewModelScope)

    private fun acceptRequest(item: FriendsEntity, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val friend = friendsProfileRepository.acceptRequest(item)
            if (friend != null) {
                friends[friend.id] = friend
                prepareFriendsLists(position, item, accepted = true)
            }
        }
    }

    private fun declineRequest(item: FriendsEntity, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val friend = friendsProfileRepository.declineRequest(item)
            if (friend != null) {
                friends.remove(friend.id)
                prepareFriendsLists(position, item, declined = true)
            }
        }
    }

    private fun requestFriend(item: FriendsEntity, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val friend = friendsProfileRepository.requestFriend(item)
            if (friend != null) {
                prepareFriendsLists(position, item, requested = true)
            }
        }
    }

    private fun processDeleteFriend(id: String) {
        val friend = friends.remove(id)
        friend?.let { prepareFriendsLists(updatedItem = it) }
    }

    private fun prepareFriendsLists(
        position: Int = -1,
        updatedItem: FriendsEntity? = null,
        accepted: Boolean = false,
        requested: Boolean = false,
        declined: Boolean = false
    ) {
        val data = friends.values.groupBy { it.isFriend }
        state.postValue(FriendsState.FriendsSilentlyUpdated(
            data[true]?.toList().orEmpty(),
            data[false]?.toList().orEmpty(),
            updatedItem?.copy(
                isFriend = accepted,
                requestOut = requested,
                requestIn = false
            ),
            position,
            requested,
            accepted,
        ))
    }
}

sealed class FriendsState {
    data class Loading(val active: Boolean = true) : FriendsState()
    object Error: FriendsState()
    object SomethingWentWrong: FriendsState()
    data class FriendsLoaded(
        val friends: List<FriendsEntity>,
        val requests: List<FriendsEntity>,
        val selectedTab: Int,
    ) : FriendsState()
    data class FriendsSilentlyUpdated(
        val friends: List<FriendsEntity>,
        val requests: List<FriendsEntity>,
        val updatedItem: FriendsEntity?,
        val updatedPosition: Int,
        val requested: Boolean = false,
        val accepted: Boolean = false
        ) : FriendsState()
    object SearchPrepared : FriendsState()
    object SearchCleared : FriendsState()
}

sealed class FriendsIntent {
    data class TabChanged(val tab: Int) : FriendsIntent()
    data class Search(val query: String?) : FriendsIntent()
    data class AcceptInvite(val item: FriendsEntity, val position: Int) : FriendsIntent()
    data class DeclineInvite(val item: FriendsEntity, val position: Int) : FriendsIntent()
    data class RequestFriend(val item: FriendsEntity, val position: Int) : FriendsIntent()
    data class Details(val id: String) : FriendsIntent()
    data class FriendDeleted(val id: String) : FriendsIntent()
    object ShowSavedData : FriendsIntent()
}
