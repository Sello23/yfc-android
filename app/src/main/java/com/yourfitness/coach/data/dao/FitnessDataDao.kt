package com.yourfitness.coach.data.dao

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yourfitness.coach.data.entity.FitnessDataEntity
import com.yourfitness.coach.data.entity.FitnessDataEntity.Companion.FITNESS_DATA_TABLE

@Dao
interface FitnessDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun writeDailyGoogleFitData(googleFitData: FitnessDataEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFitnessData(data: List<FitnessDataEntity>)

    @Query("SELECT * FROM $FITNESS_DATA_TABLE ORDER BY CASE WHEN syncDate IS NULL THEN 1 ELSE 0 END, syncDate DESC LIMIT 1")
    suspend fun readLastSyncDate(): FitnessDataEntity?

    @Query("SELECT * FROM $FITNESS_DATA_TABLE")
    suspend fun readAllData(): List<FitnessDataEntity>?

    @Query("SELECT * FROM $FITNESS_DATA_TABLE WHERE syncDate IS NULL")
    suspend fun readNotSyncedRecords(): List<FitnessDataEntity>?

    @Query("DELETE FROM $FITNESS_DATA_TABLE")
    suspend fun deleteGoogleFitData()

    @Query("SELECT * FROM $FITNESS_DATA_TABLE WHERE syncDate IS NULL AND entryDate >= :date AND entryDate <= :endDate")
    suspend fun readNotSyncedDataForDay(date: Long, endDate: Long): List<FitnessDataEntity>?

    @Query("SELECT steps, calories FROM $FITNESS_DATA_TABLE WHERE syncDate IS NULL AND entryDate >= :date AND entryDate <= :endDate")
    fun readNotSyncedDataForDayCursor(date: Long, endDate: Long): Cursor

    @Query("SELECT * FROM $FITNESS_DATA_TABLE WHERE syncDate IS NOT NULL AND entryDate >= :date AND entryDate <= :endDate")
    suspend fun readSyncedDataForDay(date: Long, endDate: Long): List<FitnessDataEntity>?
}