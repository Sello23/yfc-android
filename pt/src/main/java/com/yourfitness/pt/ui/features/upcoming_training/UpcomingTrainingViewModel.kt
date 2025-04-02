package com.yourfitness.pt.ui.features.upcoming_training

import androidx.lifecycle.viewModelScope
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.pt.data.entity.SessionEntity
import com.yourfitness.pt.data.entity.fullName
import com.yourfitness.pt.domain.calendar.SessionsRepository
import com.yourfitness.pt.domain.pt.PtRepository
import com.yourfitness.pt.ui.navigation.PtNavigation
import com.yourfitness.pt.ui.navigation.PtNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class UpcomingTrainingViewModel @Inject constructor(
    private val navigator: PtNavigator,
    private val sessionsRepository: SessionsRepository,
    private val ptRepository: PtRepository
) : MviViewModel<UpcomingTrainingIntent, UpcomingTrainingState>() {

    private var isInitialized = false

    init {
        loadUpcomingTraining()
    }

    override fun handleIntent(intent: UpcomingTrainingIntent) {
        when (intent) {
            is UpcomingTrainingIntent.UpdateUpcomingState -> if (isInitialized) loadUpcomingTraining()
            is UpcomingTrainingIntent.OpenFitnessCalendarClicked -> {
                navigator.navigate(PtNavigation.TrainingCalendar)
            }
        }
    }

    private fun loadUpcomingTraining() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val training = sessionsRepository.getUpcomingTraining()
                if (training != null) {
                    val facilityData = ptRepository.getFacilityById(training.facilityId)
                    val ptData = ptRepository.getPtById(training.personalTrainerId)
                    if (facilityData != null && ptData != null) {
                        state.postValue(
                            UpcomingTrainingState.UpcomingTrainingLoaded(
                                training,
                                facilityData.first,
                                facilityData.second,
                                facilityData.third,
                                ptData.fullName
                            )
                        )
                    }
                }
                isInitialized = true
            } catch (error: Exception) {
                Timber.e(error)
            }
        }
    }
}

open class UpcomingTrainingState {
    data class UpcomingTrainingLoaded(
        val training: SessionEntity,
        val facilityName: String,
        val logo: String,
        val address: String,
        val ptName: String
    ) : UpcomingTrainingState()
}

open class UpcomingTrainingIntent {
    object UpdateUpcomingState : UpcomingTrainingIntent()
    object OpenFitnessCalendarClicked : UpcomingTrainingIntent()
}
