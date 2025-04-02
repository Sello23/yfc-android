package com.yourfitness.coach.domain.fitness_info

import android.database.Cursor
import com.yourfitness.coach.data.dao.FitnessDataDao
import com.yourfitness.coach.data.entity.FitnessDataEntity
import javax.inject.Inject

class ProviderFitnessDataRepository @Inject constructor(
    private val fitnessDao: FitnessDataDao,
) {

    suspend fun saveToDb(data: List<FitnessDataEntity>) = fitnessDao.saveFitnessData(data)

    suspend fun getLastSyncDate(): Long? = fitnessDao.readLastSyncDate()?.syncDate

    suspend fun getEntriesToUpload(): List<FitnessDataEntity> = fitnessDao.readNotSyncedRecords().orEmpty()

    suspend fun getAllEntries(): List<FitnessDataEntity> = fitnessDao.readAllData().orEmpty()

    suspend fun getNotSyncedDataForDay(date: Long, endDate: Long): List<FitnessDataEntity> {
        return fitnessDao.readNotSyncedDataForDay(date, endDate).orEmpty()
    }

    fun getNotSyncedDataForDayCursor(date: Long, endDate: Long): Cursor {
        return fitnessDao.readNotSyncedDataForDayCursor(date, endDate)
    }

    suspend fun getSyncedDataForDay(date: Long, endDate: Long): List<FitnessDataEntity> {
        return fitnessDao.readSyncedDataForDay(date, endDate).orEmpty()
    }
}