package com.yourfitness.community.ui.features.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.community.domain.FriendsProfileRepository
import com.yourfitness.community.ui.navigation.CommunityNavigation
import com.yourfitness.community.ui.navigation.CommunityNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class UnfriendViewModel @Inject constructor(
    private val navigator: CommunityNavigator,
    private val friendsProfileRepository: FriendsProfileRepository,
    savedState: SavedStateHandle
) : MviViewModel<UnfriendIntent, UnfriendState>() {

    val friendId = savedState.get<String>("friend_id").orEmpty()
    var isDeleted = false
        private set

    override fun handleIntent(intent: UnfriendIntent) {
        when (intent) {
            is UnfriendIntent.Unfriend -> unfriend()
        }
    }

    private fun unfriend() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                friendsProfileRepository.unfriend(friendId)
                isDeleted = true
                navigator.navigate(CommunityNavigation.PopScreen(2))
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(UnfriendState.Error)
            }
        }
    }
}

sealed class UnfriendState {
    object Error: UnfriendState()
}

sealed class UnfriendIntent {
    object Unfriend : UnfriendIntent()
}
