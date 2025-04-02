package com.yourfitness.community.ui.features.likes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yourfitness.common.CommonPreferencesStorage
import com.yourfitness.common.domain.date.utcToZone
import com.yourfitness.common.domain.models.CalendarView
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.community.data.entity.FriendsEntity
import com.yourfitness.community.domain.FriendsProfileRepository
import com.yourfitness.community.domain.StepsCaloriesRepository
import com.yourfitness.community.network.dto.FriendsDto
import com.yourfitness.community.ui.features.friends.FriendsIntent
import com.yourfitness.community.ui.navigation.CommunityNavigation
import com.yourfitness.community.ui.navigation.CommunityNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

typealias FitnessData = MutableMap<Long, Pair<Long, Long>>

@HiltViewModel
class LikesListViewModel @Inject constructor(
    private val friendsProfileRepository: FriendsProfileRepository,
    private val stepsCaloriesRepository: StepsCaloriesRepository,
    private val navigator: CommunityNavigator,
    commonStorage: CommonPreferencesStorage,
    savedState: SavedStateHandle
) : MviViewModel<LikesListIntent, LikesListState>() {

    val workoutDate = savedState.get<Int>("workout_date") ?: 0
    val minAmount = savedState.get<Int>("min_amount") ?: 0
    val newLikes = savedState.get<Int>("new_likes") ?: 0

    private val zoneOffset: ZoneOffset? = commonStorage.zoneOffset
    val workoutDateUtc = workoutDate + (zoneOffset?.totalSeconds ?: 0)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val data = friendsProfileRepository.getWorkoutLikes(workoutDate)

                val fitnessDataMap: FitnessData = mutableMapOf()
                data.filter { it.viewed == false }
                    .map {
                        val date = it.workoutDate
                        if (date != null && fitnessDataMap[date] == null) {
                            val zonedDate = date.minus(zoneOffset?.totalSeconds ?: 0)
                            stepsCaloriesRepository.getFitnessInfo(zonedDate)?.let { info ->
                                fitnessDataMap[date] = info
                            }
                        }
                    }

                state.postValue(LikesListState.LikesLoaded(data, fitnessDataMap))
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(LikesListState.Error)
            }
        }
    }

    override fun handleIntent(intent: LikesListIntent) {
        when (intent) {
            is LikesListIntent.Details -> navigator.navigate(CommunityNavigation.FriendDetails(intent.friendId))
        }
    }
}

sealed class LikesListState {
    data object Error : LikesListState()
    data class LikesLoaded(
        val data: List<FriendsDto>,
        val fitnessData: FitnessData,
    ) : LikesListState()
}

sealed class LikesListIntent {
    data class Details(
        val friendId: String,
    ) : LikesListIntent()
}
