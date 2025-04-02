package com.yourfitness.pt.domain.dashboard

import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.common.domain.date.getNowUtcFormatted
import com.yourfitness.common.domain.date.today
import com.yourfitness.pt.data.entity.SessionEntity
import com.yourfitness.pt.data.mappers.toEntity
import com.yourfitness.pt.network.PtRestApi
import com.yourfitness.pt.network.dto.DashboardDto
import com.yourfitness.pt.network.dto.InductionNote
import com.yourfitness.pt.network.dto.PtClientDto
import com.yourfitness.pt.network.dto.PtInductionDto
import com.yourfitness.pt.network.dto.SessionDto
import com.yourfitness.pt.ui.utils.SessionStatus
import javax.inject.Inject

class DashboardRepository @Inject constructor(
    private val ptRestApi: PtRestApi,
    private val profileRepository: ProfileRepository
) {

    suspend fun loadDashboardSummary(): DashboardDto? {
        return ptRestApi.getPtDashboardSummary()
    }

    suspend fun loadPtClients(): List<PtClientDto> {
        return ptRestApi.getPtClients()
    }

    suspend fun loadPtInductions(): List<PtInductionDto> {
        return ptRestApi.getPtInductions()
    }

    suspend fun getLatestRequests(): List<SessionEntity> {
        val profileId = profileRepository.profileId()
        return ptRestApi.getUpcomingTraining(
            personalTrainerId = profileId,
            status = SessionStatus.REQUESTED.value
        )?.sessions.mapToEntity().reversed()
    }

    suspend fun getNextAppointments(): List<SessionEntity> {
        val profileId = profileRepository.profileId()
        val startDate = today().getNowUtcFormatted()
        return ptRestApi.getUpcomingTraining(
            personalTrainerId = profileId,
            from = startDate,
            status = SessionStatus.BOOKED.value
        )?.sessions.mapToEntity()
    }

    private fun List<SessionDto>?.mapToEntity(): List<SessionEntity> {
        return this.orEmpty().sortedBy { it.from }.map { it.toEntity() }
    }

    suspend fun completeInduction(id: String, note: String) {
        return ptRestApi.completeInduction(id, InductionNote(note))
    }
}
