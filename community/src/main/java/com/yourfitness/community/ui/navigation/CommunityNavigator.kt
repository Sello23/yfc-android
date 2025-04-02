package com.yourfitness.community.ui.navigation

import androidx.lifecycle.MutableLiveData
import com.yourfitness.common.network.dto.Challenge
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunityNavigator @Inject constructor() {

    val navigation = MutableLiveData<CommunityNavigation?>()

    fun navigate(node: CommunityNavigation) {
        navigation.postValue(node)
    }

    suspend fun navigateDelayed(node: CommunityNavigation) {
        delay(500)
        navigate(node)
    }
}

open class CommunityNavigation {
    object FriendsList : CommunityNavigation()
    data class FriendDetails(val id: String) : CommunityNavigation()
    data class Unfriend(val id: String) : CommunityNavigation()
    data class PopScreen(val times: Int) : CommunityNavigation()
    data class Leaderboard(
        val friendId: String,
        val challenge: String? = null,
    ) : CommunityNavigation()

    data class ChallengeDetails(
        val challenge: String? = null,
        val hideActions: Boolean = true,
    ) : CommunityNavigation()

    data class LikesList(
        val workoutDate: Long,
        val minAmount: Int,
        val newLikes: Int,
    ) : CommunityNavigation()
    object HowGetLikes : CommunityNavigation()
}
