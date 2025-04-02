package com.yourfitness.pt.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yourfitness.pt.data.entity.PersonalTrainerEntity
import com.yourfitness.pt.data.entity.PersonalTrainerEntity.Companion.PERSONAL_TRAINERS_TABLE
import com.yourfitness.pt.data.entity.PtCombinedEntity

@Dao
interface PtDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePersonalTrainers(facilities: List<PersonalTrainerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun writePersonalTrainer(facility: PersonalTrainerEntity)

    @Query("SELECT * FROM $PERSONAL_TRAINERS_TABLE")
    suspend fun readAllPersonalTrainers(): List<PersonalTrainerEntity>

    @Query("SELECT * FROM $PERSONAL_TRAINERS_TABLE")
    suspend fun readAllPtsCombined(): List<PtCombinedEntity>

    @Query("SELECT * FROM $PERSONAL_TRAINERS_TABLE WHERE id = :id LIMIT 1")
    suspend fun readPersonalTrainerById(id: String): PersonalTrainerEntity?

    @Query("SELECT * FROM $PERSONAL_TRAINERS_TABLE WHERE id = :id LIMIT 1")
    suspend fun readPtByIdCombined(id: String): PtCombinedEntity?
}
