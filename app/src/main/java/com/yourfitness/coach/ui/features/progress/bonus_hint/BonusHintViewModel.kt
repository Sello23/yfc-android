package com.yourfitness.coach.ui.features.progress.bonus_hint

import androidx.lifecycle.viewModelScope
import com.yourfitness.coach.domain.models.BonusCredits
import com.yourfitness.coach.domain.settings.SettingsManager
import com.yourfitness.coach.network.YFCRestApi
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.coach.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BonusHintViewModel @Inject constructor(
    private val settingsManager: SettingsManager
) : MviViewModel<Any, Any>() {

    fun getBonusCredits(visits: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                state.postValue(BonusHintState.Loading)
                val settings = settingsManager.getSettings()
                val bonusCredits = mutableListOf<BonusCredits>()
                settings?.bonuses?.forEachIndexed { index, bonus ->
                    if (index == 0) {
                        bonusCredits.add(
                            BonusCredits(
                                amount = bonus.amount ?: "",
                                color = bonus.color ?: "",
                                credits = bonus.credits ?: "",
                                name = bonus.name ?: "",
                                maxVisits = visits.toString(),
                                isFirst = true,
                            )
                        )
                    } else {
                        bonusCredits.add(
                            BonusCredits(
                                amount = bonus.amount ?: "",
                                color = bonus.color ?: "",
                                credits = bonus.credits ?: "",
                                name = bonus.name ?: "",
                                maxVisits = visits.toString(),
                                isFirst = false,
                            )
                        )
                    }
                }
                state.postValue(BonusHintState.Success(bonusCredits.reversed()))
            } catch (error: Exception) {
                Timber.e(error)
                state.postValue(BonusHintState.Error(error))
            }
        }
    }
}

open class BonusHintState {
    object Loading : BonusHintState()
    data class Success(val bonusCredits: List<BonusCredits>) : BonusHintState()
    data class Error(val error: Exception) : BonusHintState()
}