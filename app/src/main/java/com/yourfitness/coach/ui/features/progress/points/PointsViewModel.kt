package com.yourfitness.coach.ui.features.progress.points

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.domain.fitness_info.FitnessInfoService
import com.yourfitness.coach.domain.progress.points.PointsManager
import com.yourfitness.coach.domain.progress.points.ProgressInfo
import com.yourfitness.coach.domain.progress.reward.RewardManager
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.coach.workers.syncAchievements
import com.yourfitness.common.domain.analytics.CrashlyticsManager
import com.yourfitness.common.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PointsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pointsManager: PointsManager,
    private val fitnessInfoService: FitnessInfoService,
    private val rewardManager: RewardManager,
    private val navigator: Navigator
) : MviViewModel<PointsIntent, PointsState>() {

    private var progressInfo: ProgressInfo? = null
    private var pointsPerStep: Double = 0.0
    private var pointsPerCalorie: Double = 0.0
    private var pointLevel: Long = 0L
    private var rewardLevel: Long = 0L

    init {
        loadProgress()
        loadRewards()
    }

    override fun handleIntent(intent: PointsIntent) {
        when (intent) {
            is PointsIntent.ProgressLevelCardTapped -> {
                val info = progressInfo ?: return
                navigator.navigate(
                    Navigation.LevelSystemDialog(
                        info,
                        pointsPerStep,
                        pointsPerCalorie,
                        pointLevel,
                        rewardLevel
                    )
                )
            }

            is PointsIntent.InfoIconTapped -> navigator.navigate(
                Navigation.PointsHint(
                    pointsPerStep,
                    pointsPerCalorie
                )
            )
        }
    }

    private fun loadProgress() {
        viewModelScope.launch {
            try {
                state.postValue(PointsState.Loading)
                context.syncAchievements {
                    progressInfo = pointsManager.fetchProgress()
                    val info = progressInfo ?: return@syncAchievements
                    state.postValue(PointsState.Success(info))
                }
            } catch (error: Exception) {
                Timber.e(error)
                CrashlyticsManager.trackException(error)
                state.postValue(PointsState.Error)
            }
        }
    }

    private fun loadRewards() {
        viewModelScope.launch {
            try {
                val initPoints = pointsManager.fetchPoints()
                val data = pointsManager.fetchPointsCountData()
                pointsPerStep = data.first.first
                pointsPerCalorie = data.first.second
                pointLevel = data.second.first
                rewardLevel = data.second.second

                var attempts = 6
                while (progressInfo == null && attempts > 0) {
                    delay(200)
                    attempts--
                }

                val levelSubReward = rewardManager.fetchSubReward(
                    initPoints,
                    progressInfo?.totalPoints ?: 0L,
                    pointLevel,
                    rewardLevel
                )
                val levelReward = rewardManager.fetchReward(
                    initPoints,
                    progressInfo?.totalPoints ?: 0L,
                    progressInfo?.levels.orEmpty()
                )

                if (levelReward != null) {
                    val coins = levelReward.coins + (levelSubReward?.coins ?: 0L)
                    if (coins > 0) {
                        navigator.navigateDelayed(
                            Navigation.ProgressReward(levelReward.copy(coins = coins))
                        )
                    }
                } else if (levelSubReward != null) {
                    navigator.navigateDelayed(Navigation.ProgressReward(levelSubReward))
                }

//                val creditRewards = pointsManager.fetchCreditRewards() // TODO temporary removed Studios
//                if (creditRewards.isNotEmpty()) {
//                    creditRewards.forEach { navigator.navigateDelayed(Navigation.ProgressReward(it)) }
//                }
            } catch (error: Exception) {
                Timber.e(error)
            }
        }
    }
}

open class PointsState {
    object Loading : PointsState()
    data class Success(val progress: ProgressInfo) : PointsState()
    data class AskPermissions(val permissions: Set<String>) : PointsState()
    object Error : PointsState()
}

open class PointsIntent {
    object ProgressLevelCardTapped : PointsIntent()
    object InfoIconTapped : PointsIntent()
}
