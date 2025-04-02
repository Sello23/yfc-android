package com.yourfitness.pt.ui.features.training_calendar.block_slots.conflicts

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.pt.data.entity.SessionEntity
import com.yourfitness.pt.domain.values.CONFLICTS
import com.yourfitness.pt.ui.features.training_calendar.actions.ConfirmFlow
import com.yourfitness.pt.ui.navigation.PtNavigation
import com.yourfitness.pt.ui.navigation.PtNavigator
import com.yourfitness.pt.ui.utils.SessionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class ResolveConflictViewModel @Inject constructor(
    private val navigator: PtNavigator,
    savedState: SavedStateHandle
) : MviViewModel<ResolveConflictIntent, ResolveConflictState>() {

    private val conflict = savedState.get<SessionEntity>(CONFLICTS)

    init {
        loadData()
    }

    override fun handleIntent(intent: ResolveConflictIntent) {
        when (intent) {
            is ResolveConflictIntent.CancelConfirmed -> showCancelConfirmation()
        }
    }

    private fun loadData() {
        if (conflict == null) {
            navigator.navigate(PtNavigation.PopScreen)
            return
        }
        state.value = ResolveConflictState.Loaded(
            conflict.from,
            conflict.to,
            conflict.profileInfo.fullName,
            conflict.status != SessionStatus.BLOCKED_SLOT.value
        )
    }

    private fun showCancelConfirmation() {
        if (conflict == null) {
            navigator.navigate(PtNavigation.PopScreen)
            return
        }
        navigator.navigate(
            PtNavigation.ConfirmCalendarActionFlow(
                conflict.id,
                if (conflict.status != SessionStatus.BLOCKED_SLOT.value) ConfirmFlow.CANCEL_CONFLICT
                else ConfirmFlow.CANCEL_BLOCK_CONFLICT,
            )
        )
    }
}

sealed class ResolveConflictState {
    data class Loading(val active: Boolean = true) : ResolveConflictState()
    data class Error(val error: Exception) : ResolveConflictState()
    data class Loaded(
        val from: Long,
        val to: Long,
        val traineeName: String,
        val isSession: Boolean
    ) : ResolveConflictState()
}

sealed class ResolveConflictIntent {
    object CancelConfirmed : ResolveConflictIntent()
}

@Parcelize
data class SessionConflicts(
    val conflicts: MutableList<SessionEntity>
) : Parcelable
