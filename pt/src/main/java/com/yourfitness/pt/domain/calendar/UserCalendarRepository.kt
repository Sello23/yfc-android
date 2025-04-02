package com.yourfitness.pt.domain.calendar

import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.domain.date.addDays
import com.yourfitness.common.domain.date.addMs
import com.yourfitness.common.domain.date.setDayEnd
import com.yourfitness.common.domain.date.setDayStart
import com.yourfitness.common.domain.date.today
import com.yourfitness.pt.data.dao.SessionDao
import com.yourfitness.pt.data.entity.SessionEntity
import com.yourfitness.pt.domain.models.CalendarData
import com.yourfitness.pt.domain.models.DayRow
import com.yourfitness.pt.domain.models.FilledTimeSlot
import com.yourfitness.pt.domain.models.SelectedTimeSlot
import com.yourfitness.pt.domain.models.TimeSlot
import com.yourfitness.pt.domain.pt.PtRepository
import com.yourfitness.pt.ui.utils.SessionStatus
import com.yourfitness.pt.ui.utils.statusDataMap
import java.util.Date
import javax.inject.Inject

class UserCalendarRepository @Inject constructor(
    private val sessionDao: SessionDao,
    private val ptRepository: PtRepository,
    private val profileRepository: ProfileRepository,
) {

    suspend fun generateCalendarData(date: Date, ptId: String): List<CalendarData> {
        val startDate = date.setDayStart()
        val data = mutableListOf<CalendarData>()
        data.add(DayRow(startDate))
        data.addAll(configureDayData(startDate, ptId))
        return data
    }

    private suspend fun configureDayData(date: Date, ptId: String): List<CalendarData> {
        val profile = profileRepository.getProfile()
        val dayTimeSlots = mutableMapOf<Long, CalendarData>()
        fillInBaseSlots(date, dayTimeSlots)
        configureUserSlots(dayTimeSlots) {
            if (profile.personalTrainer == true) {
                sessionDao.readSessionsByDatePt(
                    ptId,
                    date.time,
                    date.setDayEnd().time + 1000
                )
            } else {
                sessionDao.readUserSessionsByDate(
                    SessionStatus.PT.value,
                    ptId,
                    date.time,
                    date.setDayEnd().time + 1000
                )
            }
        }
        if (profile.personalTrainer == false) {
            configurePtSlots(ptId, date, dayTimeSlots)
        }
        updateFreeSlotsEnabledStates(dayTimeSlots)
        return dayTimeSlots.values.toList()
    }

    private fun fillInBaseSlots(
        date: Date,
        dayTimeSlots: MutableMap<Long, CalendarData>
    ) {
        for (i in 0 until daySlotsNumber) {
            val time = date.time + i * timeSlotDurationMs + workDayStartMs
            dayTimeSlots[time] = TimeSlot(Date(time), id = defaultId)
        }
    }

    private suspend fun configureUserSlots(
        dayTimeSlots: MutableMap<Long, CalendarData>,
        fetchSessions: suspend () -> List<SessionEntity>?
    ) {
        val sessions = fetchSessions().orEmpty()
        sessions.forEach {
            val startTime = it.from
            if (dayTimeSlots[startTime] != null) {
                val uiData = statusDataMap[it.status.lowercase()]
                if (uiData != null) {
                    val facilityData = ptRepository.getFacilityById(it.facilityId)
                    dayTimeSlots[startTime] = FilledTimeSlot(
                        uiData,
                        facilityData?.first.orEmpty(),
                        facilityData?.second.orEmpty(),
                        Date(startTime),
                        it.id,
                    )
                    dayTimeSlots.remove(startTime + timeSlotDurationMs)
                } else if (it.status == SessionStatus.TEMPORARY.value) {
                    dayTimeSlots[startTime] = SelectedTimeSlot(
                        Date(startTime),
                        Date(it.to),
                        Date(startTime + timeSlotDurationMs),
                        it.id
                    )
                    dayTimeSlots.remove(startTime + timeSlotDurationMs)
                }
            }
        }
    }

    private suspend fun configurePtSlots(
        ptId: String,
        date: Date,
        dayTimeSlots: MutableMap<Long, CalendarData>
    ) {
        val ptSessions = sessionDao.readPtSessionsByDate(
            SessionStatus.TEMPORARY.value,
            profileRepository.profileId(),
            ptId,
            date.time,
            date.setDayEnd().time + 1000
        ).orEmpty()
        ptSessions.forEach {
            val firstSlotTime = it.from
            val secondSlotTime = firstSlotTime + timeSlotDurationMs
            val firstSlot = dayTimeSlots[firstSlotTime]
            val secondSlot = dayTimeSlots[secondSlotTime]
            if (firstSlot.isFree()) {
                firstSlot as TimeSlot
                dayTimeSlots[firstSlotTime] = TimeSlot(firstSlot.time, false, id = firstSlot.id)
            }
            if (secondSlot.isFree()) {
                secondSlot as TimeSlot
                dayTimeSlots[secondSlotTime] = TimeSlot(secondSlot.time, false, id = secondSlot.id)
            }
        }
    }

    private fun updateFreeSlotsEnabledStates(dayTimeSlots: MutableMap<Long, CalendarData>) {
        val now = today()
        val slotKeys: List<Long> = dayTimeSlots.keys.toList()
        for (index in 0 until dayTimeSlots.size) {
            val curSlot = dayTimeSlots[slotKeys[index]]
            if (curSlot is FilledTimeSlot || curSlot is SelectedTimeSlot || curSlot is DayRow) continue

            curSlot as TimeSlot

            if (curSlot.time.before(now)) {
                dayTimeSlots[slotKeys[index]] = TimeSlot(curSlot.time, false, id = curSlot.id)
                continue
            }

            val prevIndex = index - 1
            val nextIndex = index + 1
            val prevSlot = if (prevIndex >= 0) dayTimeSlots[slotKeys[prevIndex]] else null
            val nextSlot =
                if (nextIndex < dayTimeSlots.size) dayTimeSlots[slotKeys[nextIndex]] else null

            if (prevSlot.isBusy() && nextSlot.isBusy()) {
                dayTimeSlots[slotKeys[index]] = TimeSlot(curSlot.time, false, id = curSlot.id)
            }
        }
    }

    private fun CalendarData?.isBusy(): Boolean {
        return this == null || (this is TimeSlot && !this.enabled) ||
                this is FilledTimeSlot
    }

    private fun CalendarData?.isFree(): Boolean {
        return this != null && this !is FilledTimeSlot && this !is SelectedTimeSlot
    }

    companion object {
        const val minSlotsInSession = 2
        const val dayLimit = 30
        const val timeSlotDurationMs = 30 * 60 * 1000
        const val dayDurationMs = 24 * 60 * 60 * 1000
        const val workDayStartMs = 0 * 60 * 60 * 1000
        const val workDayEndMs = 24 * 60 * 60 * 1000
        private const val workDayDurationMs = workDayEndMs - workDayStartMs
        const val daySlotsNumber = workDayDurationMs / timeSlotDurationMs

        const val hourDuration = 59 * 60 * 1000

        private const val defaultId = "default_temporary_id"
    }
}

