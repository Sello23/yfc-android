package com.yourfitness.pt.domain.calendar

import com.kizitonwose.calendar.core.daysOfWeek
import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.domain.date.*
import com.yourfitness.common.domain.models.CalendarView
import com.yourfitness.pt.data.PtStorage
import com.yourfitness.pt.data.dao.SessionDao
import com.yourfitness.pt.data.entity.ProfileInfo
import com.yourfitness.pt.data.entity.SessionEntity
import com.yourfitness.pt.data.entity.fullName
import com.yourfitness.pt.data.mappers.toEntity
import com.yourfitness.pt.domain.calendar.UserCalendarRepository.Companion.minSlotsInSession
import com.yourfitness.pt.domain.calendar.UserCalendarRepository.Companion.timeSlotDurationMs
import com.yourfitness.pt.domain.calendar.UserCalendarRepository.Companion.workDayEndMs
import com.yourfitness.pt.domain.models.TimeSlot
import com.yourfitness.pt.domain.pt.PtRepository
import com.yourfitness.pt.network.PtRestApi
import com.yourfitness.pt.network.dto.BookSessionDto
import com.yourfitness.pt.network.dto.DeleteBlockSlotListDto
import com.yourfitness.pt.network.dto.StatusDto
import com.yourfitness.pt.ui.utils.*
import timber.log.Timber
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

class SessionsRepository @Inject constructor(
    private val sessionDao: SessionDao,
    private val ptStorage: PtStorage,
    private val ptRestApi: PtRestApi,
    private val ptRepository: PtRepository,
    private val profileRepository: ProfileRepository
) {

    suspend fun getBlockSlots(): List<CalendarView.CalendarItem> {
        return sessionDao.getSessionsByStatus(SessionStatus.BLOCKED_SLOT.value)
            ?.sortedBy { it.from }
            ?.mapNotNull { it.getCalendarItemDataObject(false) }
            .orEmpty()
    }

    suspend fun getBlockSlotsCount(): Int {
        return sessionDao.countSessionsByStatus(SessionStatus.BLOCKED_SLOT.value)
    }

    suspend fun downloadUserSessions() {
        val now = getNowUtcFormatted()
        val userSessions =
            if (profileRepository.isPtRole()) ptRestApi.getSessionsForTrainer(ptStorage.userSessionsSyncedAt)
            else ptRestApi.getUserSessions(ptStorage.userSessionsSyncedAt)
        val sessionEntities = userSessions?.map { it.toEntity() }
            ?.filter { !it.deleted }
        saveSessions(sessionEntities)
        ptStorage.userSessionsSyncedAt = now
        clearCanceledSessions()
    }

    private suspend fun clearCanceledSessions() {
        sessionDao.clearSessionsByStatus(SessionStatus.CANCEL.value)
        sessionDao.clearDeletedSessions(true)
    }

    suspend fun downloadPtSessions(ptId: String) {
        val userId = profileRepository.profileId()
        val ptSessions = ptRestApi.getPtSessions(ptId)?.sessions
        sessionDao.deletePtSessions(userId, ptId)
        val sessionEntities = ptSessions
            ?.filter {
                val isValid = it.status != SessionStatus.CANCEL.value && it.deleted == false
                if (!isValid) it.id?.let { sessionId -> sessionDao.clearSessionsById(sessionId) }
                isValid
            }
            ?.map {
                it.toEntity(if (userId == it.profileId) null else SessionStatus.PT.value)
            }
        saveSessions(sessionEntities)
        clearCanceledSessions()
    }

    suspend fun saveSessions(sessionEntities: List<SessionEntity>?) {
        if (sessionEntities != null) sessionDao.saveSessions(sessionEntities)
    }

    suspend fun saveTemporarySession(slot: TimeSlot, ptId: String, uniqueId: Boolean) {
        val nextSlotTime = slot.time.time + timeSlotDurationMs
        val dayEndTime = slot.time.setDayStart().addMs(workDayEndMs).time
        val nextSlot = sessionDao.readSessionByStartDate(
            SessionStatus.TEMPORARY.value,
            profileRepository.profileId(),
            ptId,
            nextSlotTime
        )
        sessionDao.deleteConflictingTempSlot(SessionStatus.TEMPORARY.value, nextSlotTime)
        val timeFrom =
            if (nextSlot == null && nextSlotTime != dayEndTime) slot.time.time
            else {
                sessionDao.deleteConflictingTempSlot(SessionStatus.TEMPORARY.value, slot.time.time - 2 * timeSlotDurationMs)
                slot.time.time - timeSlotDurationMs
            }
        val timeTo = timeFrom + timeSlotDurationMs * minSlotsInSession
        val tempSession = SessionEntity(
            if (uniqueId) UUID.randomUUID().toString() else slot.id, "", ptId, "", timeFrom, timeTo,
            SessionStatus.TEMPORARY.value, "", false, 0, ProfileInfo.empty()
        )
        sessionDao.writeSession(tempSession)
    }

    suspend fun clearTemporarySessions() {
        sessionDao.clearSessionsByStatus(SessionStatus.TEMPORARY.value)
    }

    suspend fun clearTemporarySessionById(id: String) {
        sessionDao.clearSessionsById(SessionStatus.TEMPORARY.value, id)
    }

    suspend fun updateStatusesIfNeeded() {
        val now = today().time
        updateStatusesIfNeeded(now, SessionStatus.BOOKED, SessionStatus.COMPLETED)
    }

    suspend fun confirmRequestedSession(sessionId: String) {
        val sessionsToComplete = sessionDao.readSessionById(sessionId) ?: return
        val updatedSession = sessionsToComplete.copy(status = SessionStatus.BOOKED.value)
        ptRestApi.updateSessionStatus(
            updatedSession.id,
            StatusDto(SessionStatus.BOOKED.value)
        )
        sessionDao.writeSession(updatedSession)
    }

    suspend fun declineRequestedSession(sessionId: String) {
        ptRestApi.updateSessionStatus(
            sessionId,
            StatusDto(SessionStatus.CANCEL.value)
        )
        sessionDao.clearSessionsById(sessionId)
    }

    suspend fun cancelBlockSlot(sessionId: String) {
        ptRestApi.deleteBlockSlot(sessionId)
        sessionDao.clearSessionsById(sessionId)
    }

    suspend fun cancelBlockSlotList(sessionIds: List<String>, chainIds: List<String>) {
        ptRestApi.deleteBlockSlotList(DeleteBlockSlotListDto(sessionIds, chainIds))
        sessionIds.forEach { sessionDao.clearSessionsById(it) }
        chainIds.forEach { sessionDao.clearSessionsByChainId(it) }
    }

    suspend fun cancelChainBlockSlot(sessionId: String) {
        val chainId = sessionDao.readSessionById(sessionId)?.chainId ?: throw Exception("Chain not found")
        ptRestApi.deleteChainBlock(chainId)
        sessionDao.clearSessionsByChainId(chainId)
    }

    suspend fun getChainId(sessionId: String): String? {
        return sessionDao.readSessionById(sessionId)?.chainId
    }

    private suspend fun updateStatusesIfNeeded(
        now: Long,
        checkedStatus: SessionStatus,
        newStatus: SessionStatus,
    ) {
        val sessionsToComplete = sessionDao.readCompletedSessions(
            checkedStatus.value,
            profileRepository.profileId(),
            now + 1000
        )
        sessionsToComplete.forEach {
            val updatedSession = it.copy(status = newStatus.value)
            try {
                ptRestApi.updateSessionStatus(
                    updatedSession.id,
                    StatusDto(newStatus.value)
                )
                sessionDao.writeSession(updatedSession)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    suspend fun confirmSessionCompleted(sessionId: String) {
        val confirmedSession =
            sessionDao.readSessionById(sessionId)?.copy(status = SessionStatus.CONFIRMED.value)
        ptRestApi.updateSessionStatus(
            sessionId,
            StatusDto(SessionStatus.CONFIRMED.value)
        )
        confirmedSession?.let { sessionDao.writeSession(it) }
    }

    suspend fun createBooking(
        ptId: String,
        facilityId: String,
        startDate: Date,
        endDate: Date,
    ) {
        val requestBody = BookSessionDto(
            facilityId = facilityId,
            personalTrainerId = ptId,
            from = startDate.getNowUtcFormatted(),
            to = endDate.getNowUtcFormatted(),
        )
        val session = ptRestApi.bookSessions(requestBody)
        if (session != null) {
            sessionDao.writeSession(session.toEntity())
        }
    }

    suspend fun getSessionStatus(id: String): String {
        return sessionDao.readSessionById(id)?.status.orEmpty()
    }

    suspend fun getSession(sessionId: String): SessionEntity? =
        sessionDao.readSessionById(sessionId)

    suspend fun getUpcomingTraining(): SessionEntity? {
        val profileId = profileRepository.profileId()
        val startDate = today().getNowUtcFormatted()
        return ptRestApi.getUpcomingTraining(profileId, startDate, SessionStatus.BOOKED.value)
            ?.sessions
            .orEmpty()
            .sortedBy { it.from }
            .firstOrNull()
            ?.toEntity()
    }

    suspend fun getStatusesAmountInfo(): Map<String, Int> {
        return getAllSessions()
            .groupBy { it.status }
            .mapValues { it.value.size }
    }

    private suspend fun getAllSessions(): List<SessionEntity> {
        val profileId = profileRepository.profileId()
        return if (profileRepository.isPtRole()) sessionDao.readSessionsPt(profileId)
        else sessionDao.readSessions(profileId)
    }

    private suspend fun getSessionsForPeriod(start: LocalDate, end: LocalDate): List<SessionEntity> {
        val profileId = profileRepository.profileId()
        val startTime = start.toDate().setDayStartTime().time
        val endTime = end.toDate().setDayEndTime().time
        return if (profileRepository.isPtRole()) sessionDao.readSessionsBetweenPt(profileId, startTime.time, endTime.time + 1000)
        else sessionDao.readSessionsBetween(profileId, startTime.time, endTime.time + 1000)
    }

    private suspend fun getSessionsForMonth(date: LocalDate): List<SessionEntity> {
        val profileId = profileRepository.profileId()
        val startTime = date.setMonthStart()
        val endTime = date.setMonthEnd()
        return if (profileRepository.isPtRole()) sessionDao.readSessionsBetweenPt(
            profileId,
            startTime.time,
            endTime.time + 1000
        )
        else sessionDao.readSessionsBetween(profileId, startTime.time, endTime.time + 1000)
    }

    private suspend fun getSessionsForWeek(date: LocalDate): List<SessionEntity> {
        val profileId = profileRepository.profileId()
        val startTime = date.toDate().setDayStart()
        val endTime = date.plusDays(6).toDate().setDayEnd()
        return if (profileRepository.isPtRole()) sessionDao.readSessionsBetweenPt(profileId, startTime.time, endTime.time + 1000)
        else sessionDao.readSessionsBetween(profileId, startTime.time, endTime.time + 1000)
    }

    private suspend fun getSessionsForDay(date: LocalDate): List<SessionEntity> {
        val profileId = profileRepository.profileId()
        val startTime = date.toDate().setDayStart()
        val endTime = date.toDate().setDayEnd()
        return if (profileRepository.isPtRole()) sessionDao.readSessionsBetweenPt(profileId, startTime.time, endTime.time + 1000)
        else sessionDao.readSessionsBetween(profileId, startTime.time, endTime.time + 1000)
    }


    suspend fun getSessionsDataForMonthCalendar(
        date: LocalDate,
        statuses: List<String>,
        sessionId: String?
    ): Pair<List<CalendarView.CalendarItem>, LocalDate?> {
        var dateToScroll: LocalDate? = null
        val uiData = mutableListOf<CalendarView.CalendarItem>()
        val sessions = getSessionsForMonth(date)
            .filter { statuses.isEmpty() || statuses.contains(it.status) }
        sessions.forEach { training ->
            val itemToAdd = training.getCalendarItemDataObject(true)
            if (itemToAdd != null) {
                if (itemToAdd.objectId == sessionId) {
                    dateToScroll = itemToAdd.date.toDate().toLocalDate()
                }
                uiData.add(itemToAdd)
            }
        }
        return uiData.sortedBy { it.time } to dateToScroll
    }

    suspend fun getSessionsDataForWeekCalendar(statuses: List<String>): Pair<List<CalendarView>, Int> {
        var position = 0
        val weeksList = mutableListOf<CalendarView>()

        val nowDayStart = today().setDayStart()
        val today = todayLocal()
        val daysOfWeek = daysOfWeek()

        var firstWeekDayDistance = today.dayOfWeek.value
        if (daysOfWeek.first() == DayOfWeek.MONDAY) firstWeekDayDistance -= 1
        val intervalStart = today.minusDays(firstWeekDayDistance.toLong()).minusWeeks(5)
        val intervalEnd = intervalStart.plusWeeks(14L)

        var weekStart = intervalStart
        while (weekStart.isBefore(intervalEnd)) {

            weeksList.addWeekHeaderItem(weekStart)

            val weekDays = getSessionsForWeek(weekStart)
                .filter { statuses.isEmpty() || statuses.contains(it.status) }
                .sortedBy { it.from }
                .groupBy {
                    it.from.toDate().setDayStart().time
                }.toMutableMap()

            var weekDay = weekStart.toDate().setDayStart()
            for (i in 0..6) {
                if (weekDays[weekDay.time] == null) {
                    weekDays[weekDay.time] = emptyList()
                }
                weekDay = weekDay.addDays(1)
            }

            weekDays.toSortedMap { o1, o2 -> o1.compareTo(o2) }.map { weekDayData ->

                if (Date(weekDayData.key).setDayStart() == nowDayStart) {
                    position = weeksList.size
                }

                if (weekDayData.value.isEmpty()) {
                    weeksList.add(CalendarView.CalendarItem.empty(weekDayData.key))
                }

                var isFirst = true
                weekDayData.value.map { training ->

                    val itemToAdd = training.getCalendarItemDataObject(isFirst)
                    if (itemToAdd != null) {
                        isFirst = false
                        weeksList.add(itemToAdd)
                    }

                }

            }

            weekStart = weekStart.plusDays(7)
        }

        return weeksList to position
    }


    suspend fun getSessionsDataForDayCalendar(
        date: LocalDate,
        statuses: List<String>
    ): Pair<List<CalendarView>, Int> {
        var position = 0
        val weeksList = mutableListOf<CalendarView>()

        val nowDayStart = today().setDayStart()
        val daysOfWeek = daysOfWeek()

        var firstWeekDayDistance = date.dayOfWeek.value
        /*if (daysOfWeek.first() == DayOfWeek.MONDAY) */firstWeekDayDistance -= 1
//        if (daysOfWeek.first() == DayOfWeek.SUNDAY && firstWeekDayDistance == 7) firstWeekDayDistance = 0
        val intervalStart = date.minusDays(firstWeekDayDistance.toLong())
        val intervalEnd = intervalStart.plusDays(7L)

        var dayStart = intervalStart
        while (dayStart.isBefore(intervalEnd)) {

            weeksList.addWeekHeaderItem(dayStart)

            val dayData = getSessionsForDay(dayStart)
                .filter { statuses.isEmpty() || statuses.contains(it.status) }

            if (dayData.isEmpty()) {
                weeksList.add(CalendarView.CalendarItem.empty(dayStart.toDate().time))
            }

            var isFirst = true
            dayData.sortedBy { it.from }.map { training ->

                if (isFirst && Date(training.from).setDayStart() == nowDayStart) {
                    position = weeksList.size
                }

                val itemToAdd = training.getCalendarItemDataObject(isFirst)
                if (itemToAdd != null) {
                    isFirst = false
                    weeksList.add(itemToAdd)
                }

            }

            dayStart = dayStart.plusDays(1)
        }

        return weeksList to position
    }

    suspend fun getSessionsDataForScheduleCalendar(
        sessionId: String?,
        statuses: List<String>
    ): Triple<List<CalendarView>, Int, Int?> {
        var position = 0
        var positionToScroll: Int? = null
        val scheduleList = mutableListOf<CalendarView>()

        val nowDayStart = today().setDayStart().time
        val today = todayLocal()

        var monthStart = today.minusMonths(1).withDayOfMonth(1)
        var intervalEnd = today.plusMonths(2)
        intervalEnd = intervalEnd.withDayOfMonth(intervalEnd.month.length(intervalEnd.isLeapYear))

        var todayPosFound = false
        while (monthStart.isBefore(intervalEnd)) {

            val monthDays = getSessionsForMonth(monthStart)
                .filter { statuses.isEmpty() || statuses.contains(it.status) }
                .sortedBy { it.from }
                .groupBy {
                    it.from.toDate().setDayStart().time
                }
                .toSortedMap { o1, o2 -> o1.compareTo(o2) }
                .toMutableMap()

            if (scheduleList.isNotEmpty() && monthDays.isNotEmpty()) {
                scheduleList.addIntervalHeaderItemIfNeeded(
                    (scheduleList.last() as CalendarView.CalendarItem).time,
                    monthDays.values.first().first().from
                )
            }
            scheduleList.addScheduleHeaderItem(monthStart)
            val initMonthItemsCount = scheduleList.size

            val monthIterator = monthDays.iterator()
            var prevItem: MutableMap.MutableEntry<Long, List<SessionEntity>>? = null
            while (monthIterator.hasNext()) {
                val curItem = monthIterator.next()

                if (prevItem != null) {
                    scheduleList.addIntervalHeaderItemIfNeeded(prevItem.key, curItem.key)
                }

                var isFirst = true
                curItem.value.forEach { training ->

                    val itemToAdd = training.getCalendarItemDataObject(isFirst)

                    if (itemToAdd != null) {
                        if (itemToAdd.objectId == sessionId) {
                            positionToScroll = scheduleList.size
                        }
                        isFirst = false
                        scheduleList.add(itemToAdd)
                        if (!todayPosFound && training.from > nowDayStart) {
                            position = scheduleList.size - 1
                            todayPosFound = true
                        }
                    }
                }

                prevItem = curItem
            }

            if (initMonthItemsCount == scheduleList.size) scheduleList.removeLast()
            monthStart = monthStart.plusMonths(1)
        }

        return Triple(scheduleList, position, positionToScroll)
    }

    private fun MutableList<CalendarView>.addWeekHeaderItem(weekStart: LocalDate) {
        add(CalendarView.Header(
            weekStart.toDate().time,
            weekStart.plusDays(6L).toDate().time
        ))
    }

    private fun MutableList<CalendarView>.addIntervalHeaderItemIfNeeded(start: Long, end: Long) {
        val emptyIntervalStart = start + DAY_DURATION_MS
        if (emptyIntervalStart != end) {
            val emptyIntervalEnd = end - DAY_DURATION_MS
            add(CalendarView.Header(emptyIntervalStart, emptyIntervalEnd))
        }
    }

    private fun MutableList<CalendarView>.addScheduleHeaderItem(weekStart: LocalDate) {
        add(CalendarView.Header(weekStart.toDate().time, null))
    }

    suspend fun getCalendarItemDataObject(
        session: SessionEntity?
    ): CalendarView.CalendarItem?  {
        return session?.getCalendarItemDataObject(false)
    }

    private suspend fun SessionEntity.getCalendarItemDataObject(
        isFirst: Boolean
    ): CalendarView.CalendarItem? {
        val isPtRole = profileRepository.isPtRole()
        val facilityData = ptRepository.getFacilityById(facilityId)
        val ptData = if (isPtRole) null else ptRepository.getPtById(personalTrainerId)

        return if ((facilityData != null || this.status == SessionStatus.BLOCKED_SLOT.value) &&
            (ptData != null || isPtRole)) {
            CalendarView.CalendarItem(
                day = if (isFirst) from else null,
                facilityName = facilityData?.first.orEmpty(),
                objectName = ptData?.fullName ?: profileInfo.fullName,
                time = from,
                timeTo = to,
                date = from.toDate().setDayStart().time,
                address = facilityData?.third.orEmpty(),
                icon = facilityData?.second.orEmpty(),
                objectId = id,
                status = status,
                statusBg = status.getStatusBg(),
                statusBuilder = ::getLocalisedStatus,
                labelBuilder = if (isPtRole) null else ::getPtLocalisedLabel,
                actionLabel = status.getLocalisedAction(isPtRole),
                repeats = repeats
            )
        } else null
    }

    suspend fun getTempSlots(): List<SessionEntity> {
        return sessionDao.getSessionsByStatus(SessionStatus.TEMPORARY.value)
            ?.sortedBy { it.from }
            .orEmpty()
    }
}
