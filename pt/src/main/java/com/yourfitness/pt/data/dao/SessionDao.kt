package com.yourfitness.pt.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yourfitness.pt.data.entity.SessionEntity
import com.yourfitness.pt.data.entity.SessionEntity.Companion.SESSION_TABLE

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSessions(sessions: List<SessionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun writeSession(session: SessionEntity)

    @Query("SELECT * FROM $SESSION_TABLE")
    suspend fun readAllSessions(): List<SessionEntity>

    @Query("SELECT * FROM $SESSION_TABLE WHERE id = :id LIMIT 1")
    suspend fun readSessionById(id: String): SessionEntity?

    @Query("SELECT * FROM $SESSION_TABLE WHERE status = :status")
    suspend fun getSessionsByStatus(status: String): List<SessionEntity>?

    @Query("SELECT COUNT(*) FROM $SESSION_TABLE WHERE status = :status")
    suspend fun countSessionsByStatus(status: String): Int

    @Query("DELETE FROM $SESSION_TABLE WHERE status = :status")
    suspend fun clearSessionsByStatus(status: String)

    @Query("DELETE FROM $SESSION_TABLE WHERE status = :status AND id = :id")
    suspend fun clearSessionsById(status: String, id: String)

    @Query("DELETE FROM $SESSION_TABLE WHERE deleted = :deleted")
    suspend fun clearDeletedSessions(deleted: Boolean)

    @Query("DELETE FROM $SESSION_TABLE WHERE id = :id")
    suspend fun clearSessionsById(id: String)

    @Query("DELETE FROM $SESSION_TABLE WHERE chainId = :id")
    suspend fun clearSessionsByChainId(id: String)

    @Query("SELECT * FROM $SESSION_TABLE WHERE status = :status AND dateFrom >= :start AND dateTo <= :end LIMIT 1")
    suspend fun readSessionByDateAndStatus(status: String, start: Long, end: Long): SessionEntity?

    @Query("SELECT * FROM $SESSION_TABLE WHERE personalTrainerId = :ptId AND status != :status AND dateFrom >= :start AND dateTo <= :end")
    suspend fun readUserSessionsByDate(status: String, ptId: String, start: Long, end: Long): List<SessionEntity>?

    @Query("SELECT * FROM $SESSION_TABLE WHERE personalTrainerId = :ptId AND dateFrom >= :start AND dateTo <= :end")
    suspend fun readSessionsByDatePt(ptId: String, start: Long, end: Long): List<SessionEntity>?

    @Query("SELECT * FROM $SESSION_TABLE WHERE status != :status " +
            "AND ((personalTrainerId = :ptId AND profileId != :userId) " +
            "OR (profileId = :userId AND personalTrainerId != :ptId)) " +
            "AND dateFrom >= :start AND dateTo <= :end")
    suspend fun readPtSessionsByDate(status: String, userId: String, ptId: String, start: Long, end: Long): List<SessionEntity>?

    @Query("SELECT * FROM $SESSION_TABLE WHERE status != :status " +
            "AND (personalTrainerId = :ptId " +
            "OR (profileId = :userId AND personalTrainerId != :ptId)) " +
            "AND dateFrom = :start LIMIT 1")
    suspend fun readSessionByStartDate(status: String, userId: String, ptId: String, start: Long): SessionEntity?

    @Query("DELETE FROM $SESSION_TABLE WHERE status = :status AND dateFrom = :start")
    suspend fun deleteConflictingTempSlot(status: String, start: Long)

    @Query("SELECT * FROM $SESSION_TABLE WHERE status = :status AND profileId = :userId AND dateTo <= :time")
    suspend fun readCompletedSessions(status: String, userId: String, time: Long): List<SessionEntity>

    @Query("SELECT * FROM $SESSION_TABLE WHERE profileId = :profileId AND dateFrom >= :start AND dateTo <= :end")
    suspend fun readSessionsBetween(profileId: String, start: Long, end: Long): List<SessionEntity>

    @Query("SELECT * FROM $SESSION_TABLE WHERE profileId = :profileId")
    suspend fun readSessions(profileId: String): List<SessionEntity>

    @Query("SELECT * FROM $SESSION_TABLE WHERE personalTrainerId = :ptId AND dateFrom >= :start AND dateTo <= :end")
    suspend fun readSessionsBetweenPt(ptId: String, start: Long, end: Long): List<SessionEntity>

    @Query("SELECT * FROM $SESSION_TABLE WHERE personalTrainerId = :ptId")
    suspend fun readSessionsPt(ptId: String): List<SessionEntity>

    @Query("DELETE FROM $SESSION_TABLE")
    suspend fun clearTable()

    @Query("DELETE FROM $SESSION_TABLE WHERE (personalTrainerId = :ptId AND status = 'pt') " +
            "OR (profileId = :userId AND personalTrainerId = :ptId)")
    suspend fun deletePtSessions(userId: String, ptId: String)
}
