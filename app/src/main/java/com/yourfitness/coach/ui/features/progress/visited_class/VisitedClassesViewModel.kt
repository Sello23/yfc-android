package com.yourfitness.coach.ui.features.progress.visited_class

import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.domain.facility.FacilityRepository
import com.yourfitness.coach.domain.models.VisitedClass
import com.yourfitness.coach.domain.progress.points.PointsManager
import com.yourfitness.coach.domain.settings.SettingsManager
import com.yourfitness.coach.network.YFCRestApi
import com.yourfitness.coach.network.dto.Classification
import com.yourfitness.coach.ui.navigation.Navigation
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.coach.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class VisitedClassesViewModel @Inject constructor(
    val navigator: Navigator,
    private val restApi: YFCRestApi,
    private val pointsManager: PointsManager,
    private val settingsManager: SettingsManager
) : MviViewModel<VisitedClassIntent, VisitedClassState>() {

    init {
        getVisitedClasses()
    }

    override fun handleIntent(intent: VisitedClassIntent) {
        when (intent) {
            else -> {}
        }
    }

    private fun getVisitedClasses() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val visits = restApi.getVisits()
                val settings = settingsManager.getSettings()

                val data = pointsManager.getVisitedClasses(visits, settings)
                state.postValue(VisitedClassState.Success(data.first, data.second))

//                val creditRewards = pointsManager.fetchCreditRewards(visits, settings?.bonuses ?: emptyList())
//                if (creditRewards.isNotEmpty()) {
//                    creditRewards.forEach { navigator.navigateDelayed(Navigation.ProgressReward(it)) }
//                }
            } catch (error: Exception) {
                Timber.e(error)
            }
        }
    }
}

sealed class VisitedClassState {
    data class Success(val visitedClass: List<VisitedClass>, val maxVisits: Int) : VisitedClassState()
}

sealed class VisitedClassIntent {
}