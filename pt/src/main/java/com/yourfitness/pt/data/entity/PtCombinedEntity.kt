package com.yourfitness.pt.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class PtCombinedEntity(
    @Embedded val data: PersonalTrainerEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "personalTrainerId",
    )
    val balance: PtBalanceEntity?,
)
