package com.yourfitness.community.ui.navigation

import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import com.yourfitness.comunity.R

class CommunityNavigationHandler constructor(
    private val navController: NavController,
    private val navigator: CommunityNavigator
) {

    fun observeNavigation(owner: LifecycleOwner) {
        navigator.navigation.observe(owner) {
            if (it != null) {
                navigator.navigation.value = null
                navigate(it)
            }
        }
    }

    private fun navigate(node: CommunityNavigation) {
        when (node) {
            is CommunityNavigation.FriendsList -> {
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://com.yourfitness.community/fragment_friends/0".toUri())
                    .build()

                navController.navigate(request)
            }
            is CommunityNavigation.FriendDetails -> {
                val args = bundleOf("friend_id" to node.id)
                navController.navigate(R.id.fragment_friend_details, args)
            }
            is CommunityNavigation.Unfriend -> {
                val args = bundleOf("friend_id" to node.id)
                navController.navigate(R.id.dialog_unfriend, args)
            }
            is CommunityNavigation.LikesList -> {
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://com.yourfitness.community/dialog_likes_list/${node.workoutDate}/${node.minAmount}/${node.newLikes}".toUri())
                    .build()

                navController.navigate(request)
            }
            is CommunityNavigation.Leaderboard -> {
                val challenge = node.challenge?.replace("/", "~")
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://com.yourfitness.coach/fragment_leaderboard/6/$challenge/${node.friendId}".toUri())
                    .build()

                navController.navigate(request)
            }
            is CommunityNavigation.ChallengeDetails -> {
                val challenge = node.challenge?.replace("/", "~")
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://com.yourfitness.coach/fragment_challenge_details/$challenge/${node.hideActions}".toUri())
                    .build()

                navController.navigate(request)
            }
            is CommunityNavigation.HowGetLikes -> {
                val request = NavDeepLinkRequest.Builder
                    .fromUri("android-app://com.yourfitness.community/dialog_how_get_likes".toUri())
                    .build()

                navController.navigate(request)
            }
            is CommunityNavigation.PopScreen -> {
                for (i in 1..node.times) navController.popBackStack()
            }
            else -> Toast.makeText(
                navController.context,
                "Navigation destination not implemented",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
