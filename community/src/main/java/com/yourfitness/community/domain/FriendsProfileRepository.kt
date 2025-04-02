package com.yourfitness.community.domain

import com.yourfitness.common.data.dao.ProgressLevelDao
import com.yourfitness.common.domain.date.toCustomFormat
import com.yourfitness.common.domain.date.toMilliseconds
import com.yourfitness.common.domain.date.toTimeZone
import com.yourfitness.common.network.CommonRestApi
import com.yourfitness.common.network.dto.ProgressLevel
import com.yourfitness.community.data.dao.FriendsDao
import com.yourfitness.community.data.entity.FriendsEntity
import com.yourfitness.community.data.mappers.toEntity
import com.yourfitness.community.network.CommunityRestApi
import com.yourfitness.community.network.dto.FriendInfoDto
import com.yourfitness.community.network.dto.FriendsDto
import com.yourfitness.community.network.dto.LikesInfo
import com.yourfitness.community.network.dto.MyLikesBody
import timber.log.Timber
import java.time.ZoneOffset
import javax.inject.Inject

class FriendsProfileRepository @Inject constructor(
    private val restApi: CommunityRestApi,
    private val friendsDao: FriendsDao,
    private val progressLevelDao: ProgressLevelDao,
    private val commonRestApi: CommonRestApi
) {

    suspend fun searchProfiles(levels: List<ProgressLevel>, limit: Int = 20, offset: Int, searchText: String): List<FriendsEntity> {
        val result = restApi.searchProfiles(limit, offset, searchText)
        return result.profiles?.map { it.toEntity(levels) }.orEmpty()
    }

    suspend fun updateFriends(levels: List<ProgressLevel>) {
        clearFriends()

        restApi.getMyFriends()
            .map { it.toEntity(levels, isFriend = true, requestIn = false, requestOut = false) }
            .saveFriends()

        restApi.getIncomeRequests()
            .map { it.toEntity(levels, isFriend = false, requestIn = true, requestOut = false) }
            .saveFriends()

//        restApi.getOutcomeRequests()
//            .map { it.toEntity(levels, isFriend = false, requestIn = false, requestOut = true) }
//            .saveFriends()
    }

    suspend fun getFriends(): List<FriendsEntity> = friendsDao.readFriends()

    suspend fun acceptRequest(item: FriendsEntity): FriendsEntity? {
        return try {
            restApi.acceptRequest(item.id)
            val updatedFriend = item.copy(isFriend = true, requestIn = false)
            friendsDao.saveFriend(updatedFriend)
            updatedFriend
        } catch (e: Exception) {
            null
        }
    }

    suspend fun declineRequest(item: FriendsEntity): FriendsEntity? {
        return try {
            restApi.declineRequest(item.id)
            val updatedFriend = item.copy(requestIn = false)
            friendsDao.saveFriend(updatedFriend)
            updatedFriend
        } catch (e: Exception) {
            null
        }
    }

    suspend fun requestFriend(item: FriendsEntity): FriendsEntity? {
        return try {
            restApi.sendRequest(item.id)
            val updatedFriend = item.copy(requestOut = true)
            friendsDao.saveFriend(updatedFriend)
            updatedFriend
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getFriendInfo(id: String): FriendInfoDto = restApi.getFriendInfo(id)

    private suspend fun clearFriends() = friendsDao.clearTable()

    private suspend fun List<FriendsEntity>.saveFriends() = friendsDao.saveFriends(this)

    suspend fun fetchLevels(): List<ProgressLevel> {
        var levels = progressLevelDao.readLevels()
        if (levels.isEmpty()) {
            levels = commonRestApi.getProgressLevels()
            progressLevelDao.saveLevels(levels)
        }
        return levels
    }

    suspend fun unfriend(id: String){
        restApi.unfriend(id)
        friendsDao.deleteFriend(id)
    }

    suspend fun uploadLikes(id: String, likesData: Map<String, Pair<Boolean, Boolean>>){
        try {
            restApi.uploadLikes(MyLikesBody(
                id,
                likesData.entries.filter { it.value.first }.map { it.key },
                likesData.entries.filter { !it.value.first }.map { it.key },
            ))
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    suspend fun fetchLikes(workoutDate: Long): LikesInfo? {
        return try {
            restApi.fetchLikes(workoutDate)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    suspend fun getWorkoutLikes(workoutDate: Int): List<FriendsDto> {
        return restApi.getWorkoutLikes(workoutDate)
    }
}
