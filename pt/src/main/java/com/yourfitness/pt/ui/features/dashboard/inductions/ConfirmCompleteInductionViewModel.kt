package com.yourfitness.pt.ui.features.dashboard.inductions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.common.ui.navigation.CommonNavigation
import com.yourfitness.common.ui.navigation.CommonNavigator
import com.yourfitness.pt.domain.dashboard.DashboardRepository
import com.yourfitness.pt.domain.models.InductionInfo
import com.yourfitness.pt.domain.values.INDUCTION
import com.yourfitness.pt.domain.values.INDUCTION_STRING
import com.yourfitness.pt.domain.values.NOTE
import com.yourfitness.pt.ui.navigation.PtNavigation
import com.yourfitness.pt.ui.navigation.PtNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ConfirmCompleteInductionViewModel @Inject constructor(
    private val navigator: PtNavigator,
    private val commonNavigator: CommonNavigator,
    private val dashboardRepository: DashboardRepository,
    savedState: SavedStateHandle
) : MviViewModel<ConfirmCompleteInductionIntent, ConfirmCompleteInductionState>() {

    var isConfirmed = false
        private set

    private var inductionInfo = savedState.get<InductionInfo?>(INDUCTION)
    private val inductionInfoString = savedState.get<String?>(INDUCTION_STRING)
    private val note = savedState.get<String?>(NOTE).orEmpty()

    init {
        loadData()
    }

    override fun handleIntent(intent: ConfirmCompleteInductionIntent) {
        when (intent) {
            is ConfirmCompleteInductionIntent.OnConfirmClick -> confirmInduction()
        }
    }

    private fun loadData() {
        if (inductionInfo == null) {
            try {
                val type = object : TypeToken<InductionInfo?>() {}.type
                inductionInfo = Gson().fromJson(inductionInfoString, type)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        state.value = inductionInfo?.let { ConfirmCompleteInductionState.Loaded(it) }
    }

    private fun confirmInduction() {
        viewModelScope.launch {
            try {
                inductionInfo?.induction?.id?.let { dashboardRepository.completeInduction(it, note) }
                isConfirmed = true
                navigator.navigate(PtNavigation.ConfirmInductionSuccess)
            } catch (e: Exception) {
                Timber.e(e)
                state.postValue(ConfirmCompleteInductionState.Error(e))
                commonNavigator.navigate(CommonNavigation.CommonError)
            }
        }
    }
}

open class ConfirmCompleteInductionState {
    object Loading : ConfirmCompleteInductionState()
    data class Error(val error: Exception) : ConfirmCompleteInductionState()
    data class Loaded(val induction: InductionInfo) : ConfirmCompleteInductionState()
}

open class ConfirmCompleteInductionIntent {
    object OnConfirmClick : ConfirmCompleteInductionIntent()
}
