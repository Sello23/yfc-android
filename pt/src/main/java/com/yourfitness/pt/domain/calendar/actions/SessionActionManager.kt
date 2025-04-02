package com.yourfitness.pt.domain.calendar.actions

import com.yourfitness.common.domain.ProfileRepository
import com.yourfitness.pt.domain.calendar.SessionsRepository
import com.yourfitness.pt.ui.features.training_calendar.actions.ActionFlow
import com.yourfitness.pt.ui.features.training_calendar.actions.ConfirmFlow
import com.yourfitness.pt.ui.navigation.PtNavigation
import com.yourfitness.pt.ui.navigation.PtNavigator
import com.yourfitness.pt.ui.utils.SessionStatus
import javax.inject.Inject


class SessionActionManager @Inject constructor(
    private val navigator: PtNavigator,
    private val sessionsRepository: SessionsRepository,
    private val profileRepository: ProfileRepository
) {

    suspend fun startSessionAction(sessionId: String, status: String) {
        val ptRole = profileRepository.isPtRole()
        when (status) {
            SessionStatus.REQUESTED.value -> {
                if (ptRole) navigator.navigate(
                    PtNavigation.StartCalendarActionFlow(
                        sessionId,
                        ActionFlow.CONFIRM
                    )
                )
            }
            SessionStatus.COMPLETED.value -> {
                if (ptRole) navigator.navigate(
                    PtNavigation.ConfirmCalendarActionFlow(
                        sessionId,
                        ConfirmFlow.CANCEL,
                        false
                    )
                )
                else {
                    navigator.navigate(PtNavigation.ConfirmSession(sessionId))
                }
            }
            SessionStatus.BLOCKED_SLOT.value -> {
                navigator.navigate(
                    PtNavigation.ConfirmCalendarActionFlow(
                        sessionId,
                        ConfirmFlow.CANCEL_BLOCK,
                        false
                    )
                )
            }
            SessionStatus.BOOKED.value -> {
                if (ptRole) navigator.navigate(
                    PtNavigation.ConfirmCalendarActionFlow(
                        sessionId,
                        ConfirmFlow.CANCEL_BOOKED,
                        false
                    )
                )
            }
        }
    }

    private suspend fun confirmSession(id: String) {
        sessionsRepository.confirmSessionCompleted(id)
    }
}
