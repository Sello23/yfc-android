package com.yourfitness.coach.ui.features.progress.coin_reward

import androidx.lifecycle.SavedStateHandle
import com.yourfitness.coach.domain.entity.CoinReward
import com.yourfitness.coach.domain.entity.Reward
import com.yourfitness.coach.ui.navigation.Navigator
import com.yourfitness.common.ui.mvi.MviViewModel
import com.yourfitness.shop.ui.constants.Constants.Companion.REWARD
import com.yourfitness.shop.ui.constants.Constants.Companion.STARTUP
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RewardViewModel @Inject constructor(
    val navigator: Navigator,
    savedState: SavedStateHandle
) : MviViewModel<Any, Any>() {

    val reward = savedState.get<Reward?>(REWARD) ?: CoinReward()
    val isStartup = savedState[STARTUP] ?: false

    init {
        loadReward()
    }

    private fun loadReward() {
        state.postValue(CoinRewardState.Success(reward))
    }
}

open class CoinRewardState {
    data class Success(val reward: Reward) : CoinRewardState()
}
