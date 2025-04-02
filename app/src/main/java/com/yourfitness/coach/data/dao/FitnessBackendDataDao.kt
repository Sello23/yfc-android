package com.yourfitness.coach.data.dao

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yourfitness.coach.data.entity.FitnessDataBackendEntity
import com.yourfitness.coach.data.entity.FitnessDataBackendEntity.Companion.BACKEND_FIT_DATA_TABLE

@Dao
interface FitnessBackendDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFitnessData(data: FitnessDataBackendEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFitnessData(data: List<FitnessDataBackendEntity>)

    @Query("SELECT * FROM $BACKEND_FIT_DATA_TABLE ORDER BY entryDate DESC LIMIT 1")
    suspend fun readLastRecord(): FitnessDataBackendEntity?

    @Query("SELECT * FROM $BACKEND_FIT_DATA_TABLE")
    suspend fun readAllData(): List<FitnessDataBackendEntity>?

    @Query("DELETE FROM $BACKEND_FIT_DATA_TABLE")
    suspend fun deleteBackendData()

    @Query("SELECT count(*) FROM $BACKEND_FIT_DATA_TABLE")
    suspend fun getEntryAmount(): Int?

    @Query("SELECT SUM(points) FROM $BACKEND_FIT_DATA_TABLE")
    suspend fun getPointsSum(): Double?

    @Query("SELECT * FROM $BACKEND_FIT_DATA_TABLE WHERE entryDate >= :date AND entryDate <= :endDate LIMIT 1")
    suspend fun readDataForDay(date: Long, endDate: Long): FitnessDataBackendEntity?

    @Query("SELECT steps, calories FROM $BACKEND_FIT_DATA_TABLE WHERE entryDate >= :date AND entryDate <= :endDate LIMIT 1")
    fun readDataForDayCursor(date: Long, endDate: Long): Cursor

    @Query("UPDATE $BACKEND_FIT_DATA_TABLE SET isSynced = 0")
    suspend fun markAsNotSynced()

    @Query("SELECT COUNT(*) FROM $BACKEND_FIT_DATA_TABLE WHERE isSynced != 1")
    suspend fun getNotSyncedRecords(): Int

    @Query("UPDATE $BACKEND_FIT_DATA_TABLE SET isSynced = 0 WHERE entryDate = :entryDate")
    suspend fun markAsNotSynced(entryDate: Long)
}