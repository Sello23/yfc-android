package com.yourfitness.coach.domain.progress.reward

import com.yourfitness.coach.domain.entity.CoinReward
import com.yourfitness.coach.domain.entity.LevelUpReward
import com.yourfitness.common.network.dto.ProgressLevel
import java.lang.Long.max
import javax.inject.Inject

class RewardManager @Inject constructor() {

    fun fetchSubReward(
        initPoints: Long,
        finalPoints: Long,
        pointLevel: Long,
        rewardLevel: Long
    ): CoinReward? {
        val startPoints = initPoints - initPoints % pointLevel
        val rewardCoins = max(((finalPoints - startPoints) / pointLevel) * rewardLevel, 0L)
        return if (rewardCoins != 0L) {
            CoinReward(
                rewardCoins,
                pointLevel,
                false,
                isReward = true,
                isCoin = true
            )
        } else {
            null
        }
    }

    fun fetchReward(
        initPoints: Long,
        finalPoints: Long, levels: List<ProgressLevel>
    ): LevelUpReward? {
        val levelUp = levels.findLast { it.pointLevel in (initPoints + 1)..finalPoints }
        return if (levelUp != null) {
            LevelUpReward(
                levelUp.coinRewards,
                levelUp.pointLevel,
                levelUp.name.orEmpty(),
                levelUp.mediaId.orEmpty()
            )
        } else {
            null
        }
    }
}