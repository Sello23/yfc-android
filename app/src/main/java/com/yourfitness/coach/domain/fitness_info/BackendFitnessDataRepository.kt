package com.yourfitness.coach.domain.fitness_info

import android.database.Cursor
import com.yourfitness.coach.data.dao.FitnessBackendDataDao
import com.yourfitness.coach.data.entity.FitnessDataBackendEntity
import com.yourfitness.coach.data.mapper.toEntity
import com.yourfitness.coach.domain.date.setDayEndTime
import com.yourfitness.coach.network.YFCRestApi
import com.yourfitness.common.domain.date.atDayEnd
import com.yourfitness.common.domain.date.atDayStart
import com.yourfitness.common.domain.date.milliseconds
import com.yourfitness.common.domain.date.setDayStartTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Date
import javax.inject.Inject

class BackendFitnessDataRepository @Inject constructor(
    private val fitnessBackendDao: FitnessBackendDataDao,
    private val restApi: YFCRestApi
) {

    suspend fun hasData(): Boolean = (fitnessBackendDao.getEntryAmount() ?: 0) > 0

    suspend fun loadFromServer(startDate: String, offset: ZoneOffset): List<FitnessDataBackendEntity> {
        return restApi.getAchievementsTimeZone(startDate).map { it.toEntity(offset) }
    }

    suspend fun saveToDb(data: List<FitnessDataBackendEntity>) = fitnessBackendDao.saveFitnessData(data)

    suspend fun saveToDb(data: FitnessDataBackendEntity) = fitnessBackendDao.saveFitnessData(data)

    suspend fun lastEntryDate(): Long? = fitnessBackendDao.readLastRecord()?.entryDate

    suspend fun getAllData(): List<FitnessDataBackendEntity> = fitnessBackendDao.readAllData().orEmpty()

    suspend fun getPointsSum(): Double = fitnessBackendDao.getPointsSum() ?: 0.0

    suspend fun getRecordForDay(date: ZonedDateTime): FitnessDataBackendEntity? {
        val dateStart = date.atDayStart().milliseconds
        val dateEnd = date.atDayEnd().milliseconds
        return fitnessBackendDao.readDataForDay(dateStart, dateEnd)
    }

    fun getRecordForDayCursor(dateStart: Long, dateEnd: Long): Cursor {
        return fitnessBackendDao.readDataForDayCursor(dateStart, dateEnd)
    }

    suspend fun markAsNotSynced(entryDate: Long) = fitnessBackendDao.markAsNotSynced(entryDate)

    suspend fun hasNotSyncedRecords() = fitnessBackendDao.getNotSyncedRecords() > 0
}
