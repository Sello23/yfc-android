package com.yourfitness.community.ui.features.likes

import androidx.lifecycle.viewModelScope
import com.yourfitness.common.domain.date.atDayStart
import com.yourfitness.common.domain.date.milliseconds
import com.yourfitness.common.domain.date.toSeconds
import com.yourfitness.common.domain.date.todayZoned
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.community.domain.FriendsProfileRepository
import com.yourfitness.community.network.dto.LikesInfo
import com.yourfitness.community.ui.navigation.CommunityNavigation
import com.yourfitness.community.ui.navigation.CommunityNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZoneOffset
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class WorkoutLikesViewModel @Inject constructor(
    val navigator: CommunityNavigator,
    private val friendsProfileRepository: FriendsProfileRepository
) : MviViewModel<WorkoutLikesIntent, WorkoutLikesState>() {

    private val likesData = mutableMapOf<Long, LikesInfo?>()
    private var selectedData: Long = 0L

    override fun handleIntent(intent: WorkoutLikesIntent) {
        when (intent) {
            is WorkoutLikesIntent.DateChanged -> loadLikesData(intent.date, intent.zoneOffset)
            is WorkoutLikesIntent.LikesClicked -> {
                navigator.navigate(
                    CommunityNavigation.LikesList(
                        selectedData.toSeconds(),
                        likesData[selectedData]?.likesCount ?: 1,
                        intent.newLikes
                    )
                )
            }

            is WorkoutLikesIntent.NoLikesClicked -> navigator.navigate(CommunityNavigation.HowGetLikes)
        }
    }

    private fun loadLikesData(date: ZonedDateTime, offset: ZoneOffset) {
        viewModelScope.launch(Dispatchers.IO) {
            val isoDate = date.milliseconds
            selectedData = isoDate
            val info = friendsProfileRepository.fetchLikes(isoDate.toSeconds())
            likesData[isoDate] = info

            val todayZoned = todayZoned(offset)
            val isToday = todayZoned.atDayStart() == date
            state.postValue(WorkoutLikesState.DayDataLoaded(info, isToday))
        }
    }
}

sealed class WorkoutLikesState {
    data class DayDataLoaded(
        val data: LikesInfo?,
        val isToday: Boolean,
    ) : WorkoutLikesState()
}

sealed class WorkoutLikesIntent {
    data class DateChanged(
        val date: ZonedDateTime,
        val zoneOffset: ZoneOffset
    ) : WorkoutLikesIntent()
    data class LikesClicked(val newLikes: Int) : WorkoutLikesIntent()
    data object NoLikesClicked : WorkoutLikesIntent()
}
