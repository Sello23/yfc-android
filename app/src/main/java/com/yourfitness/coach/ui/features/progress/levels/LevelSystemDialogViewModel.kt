package com.yourfitness.coach.ui.features.progress.levels

import androidx.lifecycle.SavedStateHandle
import com.yourfitness.coach.domain.progress.points.ProgressInfo
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LevelSystemDialogViewModel @Inject constructor(
    private val navigator: Navigator,
    savedState: SavedStateHandle
) : MviViewModel<Any, LevelSystemDialogState>() {
    private val progressInfo = savedState.get<ProgressInfo>("progressInfo")
    private val perStep = savedState.get<Double>("pointsPerStep") ?: 0.0
    private val perCalorie = savedState.get<Double>("pointsPerCalorie") ?: 0.0
    private val pointLevel = savedState.get<Long>("pointLevel") ?: 0L
    private val rewardLevel = savedState.get<Long>("rewardLevel") ?: 0L

    init {
        state.value = progressInfo?.let { LevelSystemDialogState.DataLoaded(it, pointLevel, rewardLevel) }
    }

    override fun handleIntent(intent: Any) {
        when (intent) {
            is LevelSystemDialogIntent.HowPointsCalculatedTapped -> navigator.navigate(
                Navigation.PointsHint(
                    perStep,
                    perCalorie
                )
            )
        }
    }
}

open class LevelSystemDialogState {
    data class DataLoaded(
        val progress: ProgressInfo,
        val pointLevel: Long,
        val rewardLevel: Long
    ) : LevelSystemDialogState()
}

open class LevelSystemDialogIntent {
    object HowPointsCalculatedTapped : LevelSystemDialogIntent()
}
