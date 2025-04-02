package com.yourfitness.coach.domain.leaderboard

import com.yourfitness.coach.data.dao.LeaderboardDao
import com.yourfitness.coach.data.entity.LeaderboardEntity
import com.yourfitness.coach.data.mapper.toEntity
import com.yourfitness.coach.network.YFCRestApi
import com.yourfitness.coach.network.dto.ChallengeLeaderboard
import com.yourfitness.common.domain.date.today
import timber.log.Timber
import javax.inject.Inject

class LeaderboardManager @Inject constructor(
    private val leaderboardDao: LeaderboardDao,
    private val restApi: YFCRestApi
) {

    suspend fun getSavedMyGlobalRank() = leaderboardDao.readByBoardId(LeaderboardId.MY_RANK.id)?.firstOrNull()

    suspend fun getSavedGlobalLeaderboard() = getSavedLeaderboard(LeaderboardId.GLOBAL.id)

    suspend fun getSavedPrivateLeaderboard() = getSavedLeaderboard(LeaderboardId.CORPORATE_PRIVATE.id)

    suspend fun getSavedGovLeaderboard() = getSavedLeaderboard(LeaderboardId.CORPORATE_GOV.id)

    private suspend fun getSavedLeaderboard(boardId: Int, offset: Int = 1000, limit: Int = 20): List<LeaderboardEntity> {
        return leaderboardDao.readByBoardIdPaging(boardId, offset, offset + limit).orEmpty()
    }

    suspend fun fetchGlobalLeaderboard(offset: Int = 1000, limit: Int = 20): MutableList<LeaderboardEntity>? {
        return fetchLeaderboard(LeaderboardId.GLOBAL.id) {
            restApi.getGlobalLeaderboard(limit, offset)
        }
    }

    suspend fun fetchPrivateLeaderboard(offset: Int = 1000, limit: Int = 20): List<LeaderboardEntity>? {
        return fetchLeaderboard(LeaderboardId.CORPORATE_PRIVATE.id) {
            restApi.getCorporateLeaderboard(limit, offset, PRIVATE)
        }
    }

    suspend fun fetchPrivateLeaderboardTest(offset: Int = 1000, limit: Int = 20): List<LeaderboardEntity>? {
        return fetchLeaderboard(LeaderboardId.CORPORATE_PRIVATE_TEST.id) {
            restApi.getCorporateLeaderboardTest(limit, offset, PRIVATE)
        }
    }

    suspend fun fetchGovLeaderboard(offset: Int = 1000, limit: Int = 20): List<LeaderboardEntity>? {
        return fetchLeaderboard(LeaderboardId.CORPORATE_GOV.id) {
            restApi.getCorporateLeaderboard(limit, offset, GOVERNMENT)
        }
    }

    suspend fun fetchGovLeaderboardTest(offset: Int = 1000, limit: Int = 20): List<LeaderboardEntity>? {
        return fetchLeaderboard(LeaderboardId.CORPORATE_GOV_TEST.id) {
            restApi.getCorporateLeaderboardTest(limit, offset, GOVERNMENT)
        }
    }

    private suspend fun fetchLeaderboard(
        boardId: Int,
        fetchBoard: suspend () -> ChallengeLeaderboard
    ): MutableList<LeaderboardEntity> {
        val today = today().time
        var boardEntries: MutableList<LeaderboardEntity> = mutableListOf()

        try {
            val boardData = fetchBoard()
            boardEntries = boardData.entries?.map { it.toEntity(boardId, today) }.orEmpty().toMutableList()
            boardData.rank?.let {
                val rankEntry = it.toEntity(LeaderboardId.MY_RANK.id, today)
                boardEntries.add(rankEntry)
                leaderboardDao.apply {
                    deleteByBoardId(LeaderboardId.MY_RANK.id)
                    writeEntry(rankEntry)
                }
            }
        } catch (error: Exception) {
            Timber.e(error)
        }

        return boardEntries
    }

    suspend fun getLeaderboardById(challengeId: String, period: String, offset: Int = 1000, limit: Int = 20): MutableList<LeaderboardEntity> {
        val today = today().time
        val result = restApi.getChallengeLeaderboard(
            challengeId,
            limit,
            offset,
            period
        )
        val data = result.entries
            ?.map { it.toEntity(LeaderboardId.OTHER.id, today) }
            .orEmpty()
            .toMutableList()
        result.rank?.toEntity(LeaderboardId.MY_RANK.id, today)?.let { data.add(it) }
        return data
    }

    suspend fun getFriendLeaderboardById(
        friendId: String,
        challengeId: String,
        period: String,
        offset: Int = 1000,
        limit: Int = 20
    ): MutableList<LeaderboardEntity> {
        val today = today().time
        val result = restApi.getFriendChallengeLeaderboard(
            friendId,
            challengeId,
            limit,
            offset,
            period
        )
        val data = result.entries
            ?.map { it.toEntity(LeaderboardId.FRIEND.id, today) }
            .orEmpty()
            .toMutableList()
        result.rank?.toEntity(LeaderboardId.MY_RANK.id, today)?.let { data.add(it) }
        return data
    }

    companion object {
        private const val UPDATE_INTERVAL = 10 * 60 * 1000 // 10 min

        private const val PRIVATE = "private"
        private const val GOVERNMENT = "government"
    }
}

enum class LeaderboardId(val id: Int) {
    MY_RANK(-1),
    OTHER(0),
    GLOBAL(1),
    CORPORATE_PRIVATE(2),
    CORPORATE_GOV(3),
    CORPORATE_PRIVATE_TEST(4),
    CORPORATE_GOV_TEST(5),
    FRIEND(6),
}
