package com.yourfitness.pt.domain.pt

import com.yourfitness.common.domain.date.getNowUtcFormatted
import com.yourfitness.common.domain.date.toDate
import com.yourfitness.pt.data.dao.SessionDao
import com.yourfitness.pt.data.entity.SessionEntity
import com.yourfitness.pt.data.mappers.toEntity
import com.yourfitness.pt.network.PtRestApi
import com.yourfitness.pt.network.dto.BlockTimeSlotDto
import com.yourfitness.pt.network.dto.BlockTimeSlotsWrapper
import java.util.Date
import javax.inject.Inject

class BlockRepository @Inject constructor(
    private val sessionDao: SessionDao,
    private val ptRestApi: PtRestApi,
) {
    suspend fun findConflicts(start: Long, end: Long, repeatWeeks: Int): List<SessionEntity> {
        val datesList = mutableListOf(start to end)
        if (repeatWeeks > 0) {
            for (i: Int in 1..repeatWeeks) {
                datesList.add((start + WEEK_DURATION_MS * i) to (end + WEEK_DURATION_MS * i))
            }
        }

        val sessions = sessionDao.readAllSessions()
        val conflicts = mutableListOf<SessionEntity>()

        for (it: SessionEntity in sessions) {
            if (datesList.first().first > it.to || datesList.last().second < it.from) continue

            datesList.forEach {dates ->
                if ((dates.first in it.from..it.to || dates.second in it.from..it.to) &&
                    dates.first != it.to && dates.second != it.from) {
                    conflicts.add(it)
                    return@forEach
                }
            }
        }

        return conflicts
    }

    suspend fun blockTimeSlots(time: List<Pair<Long, Long>>, repeatWeeks: Int): List<SessionEntity>? {
        val body = List(time.size) {
            BlockTimeSlotDto(
                from = time[it].first.toDate().getNowUtcFormatted(),
                to = time[it].second.toDate().getNowUtcFormatted(),
                repeats = if (repeatWeeks > 0) repeatWeeks else 1
            )
        }
        return ptRestApi.blockTimeSlots(BlockTimeSlotsWrapper(body))?.map { it.toEntity() }
    }

    companion object {
        const val WEEK_DURATION_MS: Long = 7 * 24 * 60 * 60 * 1000
    }
}
