package com.yourfitness.pt.domain.pt

import android.content.Context
import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yourfitness.common.R
import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.domain.date.DAY_END_MINUTES
import com.yourfitness.common.domain.date.addMs
import com.yourfitness.common.domain.date.dateTimeValues
import com.yourfitness.common.domain.date.minutesOfDay
import com.yourfitness.common.domain.date.toDateTimeUtc0
import com.yourfitness.common.domain.date.weekDays
import com.yourfitness.common.domain.models.CalendarView
import com.yourfitness.common.domain.models.WorkTimeData
import com.yourfitness.common.domain.models.WorkTimeDto
import com.yourfitness.pt.data.dao.PtBalanceDao
import com.yourfitness.pt.data.dao.PtDao
import com.yourfitness.pt.data.entity.PersonalTrainerEntity
import com.yourfitness.pt.data.entity.PtBalanceEntity
import com.yourfitness.pt.data.entity.PtCombinedEntity
import com.yourfitness.pt.data.entity.SessionEntity
import com.yourfitness.pt.data.entity.fullName
import com.yourfitness.pt.data.mappers.toEntity
import com.yourfitness.pt.domain.models.FacilityInfo
import com.yourfitness.pt.domain.models.InductionInfo
import com.yourfitness.pt.domain.models.TrainerCard
import com.yourfitness.pt.domain.models.distance
import com.yourfitness.pt.network.PtRestApi
import com.yourfitness.pt.network.dto.PersonalTrainerDto
import com.yourfitness.pt.network.dto.PtInductionDto
import com.yourfitness.pt.ui.utils.getLocalisedAction
import com.yourfitness.pt.ui.utils.getLocalisedStatus
import com.yourfitness.pt.ui.utils.getStatusBg
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

class PtRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ptDao: PtDao,
    private val ptBalanceDao: PtBalanceDao,
    private val ptRestApi: PtRestApi,
    private val profileRepository: ProfileRepository
) {

    suspend fun getPtFacilities(ptId: String): List<FacilityInfo> = withContext(Dispatchers.IO) {
        val cursor = context.contentResolver.query(
            Uri.parse("content://com.yourfitness.facilities/facilities"),
            null, null, null, null
        )

        val facilities: MutableList<FacilityInfo> = mutableListOf()
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {
                if (cursor.getString(5) == "Gym") {
                    val ptIds: List<String> = cursor.getString(2).toList()
                    if (ptIds.contains(ptId)) {
                        val workTimeList = fromWorkTimeString(cursor.getString(8))
                        facilities.add(
                            FacilityInfo(
                                cursor.getString(4),
                                cursor.getString(1),
                                cursor.getString(3),
                                cursor.getDouble(6),
                                cursor.getDouble(7),
                                id = cursor.getString(0),
                                timetable = workTimeList,
//                                workTimeData = getWorkTimeData(workTimeList),
                            )
                        )
                    }
                }
                cursor.moveToNext()
            }
        }
        cursor?.close()
        return@withContext facilities
    }

    private fun fromWorkTimeString(value: String?): List<WorkTimeDto>? {
        if (value.isNullOrBlank()) return null
        val listType = object : TypeToken<List<WorkTimeDto>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    fun getWorkTimeData(
        schedule: List<WorkTimeDto>?,
        date: Date
    ): WorkTimeData? { // String id, string arguments, background id
        if (schedule == null) return null
        val start = date.dateTimeValues()
        val end = date.addMs(60 * 60 * 1000).dateTimeValues()
        val workTime = schedule.firstOrNull { it.weekDay == weekDays[start.second] }
            ?: return WorkTimeData(
                R.string.no_access_for_today,
                null,
                R.drawable.rounded_border_no_access,
                R.drawable.ic_info_red,
                false
            )

        val workTimeStart = workTime.from?.minutesOfDay() ?: -1
        var workTimeEnd = workTime.to?.minutesOfDay() ?: -1
        if (workTimeEnd == 0) workTimeEnd = DAY_END_MINUTES

        val iconResId: Int
        val bgResId: Int
        val isAccessible: Boolean
        if (start.first in (workTimeStart..workTimeEnd) && end.first in (workTimeStart..workTimeEnd)) {
            bgResId = R.drawable.rounded_border_access_hours
            iconResId = R.drawable.ic_info_yellow
            isAccessible = true
        } else {
            bgResId = R.drawable.rounded_border_no_access
            iconResId = R.drawable.ic_info_red
            isAccessible = false
        }
        val from = workTime.from
        val to = workTime.to
        val timeInterval =
            if (from != null && to != null) {
                "${from.toDateTimeUtc0()} - ${to.toDateTimeUtc0()}"
            } else {
                null
            }
        return WorkTimeData(R.string.access_hours, timeInterval, bgResId, iconResId, isAccessible)
    }

    suspend fun getFacilityById(facilityId: String): Triple<String, String, String>? = withContext(Dispatchers.IO) {
        val cursor = context.contentResolver.query(
            Uri.parse("content://com.yourfitness.facilities/facility"),
            null, facilityId, null, null
        )

        if (cursor != null && cursor.moveToFirst()) {
            return@withContext Triple(
                cursor.getString(0),
                cursor.getString(1),
                cursor.getString(2)
            )
        }
        cursor?.close()
        return@withContext null
    }

    private fun String.toList(): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(this, type)
    }

    suspend fun savePersonalTrainers(trainers: List<PersonalTrainerDto>) {
        try {
            ptDao.savePersonalTrainers(trainers.map { it.toEntity() })
        } catch (e: java.lang.Exception) {
            Timber.e(e)
        }
    }

    suspend fun searchPersonalTrainers(query: String): List<PersonalTrainerEntity> {
        val normalizedQuery = query.trim().lowercase()
        return ptDao.readAllPersonalTrainers()
            .filter { it.fullName.lowercase().contains(normalizedQuery) }.sortedBy { it.fullName }
    }

    suspend fun getAllPersonalTrainers(): List<PersonalTrainerEntity> {
        return ptDao.readAllPersonalTrainers()
    }

    suspend fun getAllPtsCombined(): List<PtCombinedEntity> {
        return ptDao.readAllPtsCombined()
    }

    suspend fun getPtById(id: String): PersonalTrainerEntity? {
        return ptDao.readPersonalTrainerById(id)
    }

    suspend fun getPtByIdCombined(id: String): PtCombinedEntity? {
        return ptDao.readPtByIdCombined(id)
    }

    suspend fun getFocusAreas(): List<String> {
        return ptDao.readAllPersonalTrainers().mapNotNull { it.focusAreas }.flatten().distinct()
    }

    suspend fun downloadPtBalanceList() {
        // TODO Coming soon
        try {
            if (profileRepository.isPtRole()) return
            val balanceList = ptRestApi.getPtBalanceList()
            ptBalanceDao.clearTable()
            ptBalanceDao.savePtBalance(balanceList.mapNotNull { it.toEntity() })
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    suspend fun updatePtBalance(id: String, amount: Int) {
        try {
            val balance = ptBalanceDao.readPtBalanceById(id) ?: PtBalanceEntity(id, 0, "")
            val balanceEntity = balance.copy(amount = balance.amount + amount)
            ptBalanceDao.writePtBalance(balanceEntity)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    suspend fun getPtBalance(id: String): Int {
        return ptBalanceDao.readPtBalanceById(id)?.amount ?: 0
    }

    suspend fun getFilteredTrainers(
        focusAreas: List<String>,
        ptIdsByLocation: List<String>
    ): List<PtCombinedEntity> {
        val types = focusAreas.map { it.lowercase() }
        return getAllPtsCombined()
            .filter { item ->
                types.isEmpty() ||
                        item.data.focusAreas.orEmpty()
                            .map { it.lowercase() }
                            .any { it in types }
            }
            .filter { ptIdsByLocation.contains(it.data.id) }
    }

    suspend fun getBalanceList(): List<PtBalanceEntity> {
        return try {
            ptBalanceDao.readPtBalanceList()
        } catch (e: Exception) {
            Timber.e(e)
            listOf()
        }
    }

    suspend fun getUserTrainers(lastLocation: LatLng): List<TrainerCard> {
        val trainerCards = mutableListOf<TrainerCard>()
        try {
            val userTrainers = getBalanceList()
            userTrainers.forEach {
                val ptInfo = ptDao.readPersonalTrainerById(it.ptId)
                val ptFacilities = getPtFacilities(it.ptId)
                if (ptInfo != null && ptFacilities.isNotEmpty()) {
                    trainerCards.add(TrainerCard(
                        ptInfo.id.orEmpty(),
                        ptInfo.fullName,
                        ptInfo.mediaId.orEmpty(),
                        ptInfo.focusAreas.orEmpty(),
                        ptFacilities.map { item ->
                            Pair(item.name, item.distance(lastLocation))
                        }.sortedBy {dist -> dist.second },
                        it.amount,
                        false
                    ))
                }
            }
            ptBalanceDao.readPtBalanceList()
        } catch (e: Exception) {
            Timber.e(e)
        }
        return trainerCards.sortedByDescending { it.availableSessions }
    }

    suspend fun mapSessionsToCompleteDataObject(sessions: List<SessionEntity>): List<CalendarView.CalendarItem> {
        return sessions.mapNotNull {
            val facilityData = getFacilityById(it.facilityId)
            if (facilityData != null) {
                CalendarView.CalendarItem(
                    facilityName = facilityData.first,
                    objectName = it.profileInfo.fullName,
                    time = it.from,
                    timeTo = it.to,
                    date = it.from,
                    address = facilityData.third,
                    status = it.status,
                    statusBg = it.status.getStatusBg(),
                    statusBuilder = ::getLocalisedStatus,
                    icon = facilityData.second,
                    objectId = it.id,
                    actionLabel = it.status.getLocalisedAction(true),
                    repeats = it.repeats
                )
            } else null
        }
    }

    suspend fun mapInductionsToFacilities(inductions: List<PtInductionDto>): List<InductionInfo> {
        return inductions.mapNotNull {
            val facilityData = it.facilityId?.let { id -> getFacilityById(id) }
            if (facilityData != null) {
                InductionInfo(
                    it,
                    facilityData.first,
                    facilityData.second,
                    facilityData.third
                )
            } else null
        }
    }
}
