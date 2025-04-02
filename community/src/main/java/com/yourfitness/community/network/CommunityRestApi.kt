package com.yourfitness.community.network

import com.yourfitness.community.network.dto.FriendInfoDto
import com.yourfitness.community.network.dto.FriendsDto
import com.yourfitness.community.network.dto.LikesInfo
import com.yourfitness.community.network.dto.MyLikesBody
import com.yourfitness.community.network.dto.SearchFriendsDto
import retrofit2.http.*

interface CommunityRestApi {
    @GET("/friends/list")
    suspend fun getMyFriends(): List<FriendsDto>

    @GET("/friends/requests-in")
    suspend fun getIncomeRequests(): List<FriendsDto>

    @GET("/friends/requests-out")
    suspend fun getOutcomeRequests(): List<FriendsDto>

    @PUT("/friends/requests/accept/{id}")
    suspend fun acceptRequest(@Path("id") friendId: String)

    @PUT("/friends/requests/send/{id}")
    suspend fun sendRequest(@Path("id") friendId: String)

    @DELETE("/friends/requests/{id}")
    suspend fun declineRequest(@Path("id") friendId: String)

    @GET("/friends/search")
    suspend fun searchProfiles(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("searchText") searchText: String
    ): SearchFriendsDto

    @GET("/friends/activities/{id}")
    suspend fun getFriendInfo(@Path("id") friendId: String): FriendInfoDto


    @DELETE("/friends/{id}")
    suspend fun unfriend(@Path("id") friendId: String)

    @PUT("/friends/activities/like")
    suspend fun uploadLikes(@Body likesData: MyLikesBody)

    @GET("/friends/workout/likes/{date}")
    suspend fun fetchLikes(@Path("date") workoutDate: Long): LikesInfo

    @GET("/v2/friends/workout/likes-list/{date}")
    suspend fun getWorkoutLikes(@Path("date") workoutDate: Int) : List<FriendsDto>
}