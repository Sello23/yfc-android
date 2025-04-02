package com.yourfitness.coach.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yourfitness.coach.data.entity.DayLimits
import com.yourfitness.coach.data.entity.SettingsEntity
import com.yourfitness.coach.data.entity.SettingsEntity.Companion.SETTINGS_TABLE

@Dao
interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(schedule: SettingsEntity)

    @Query("SELECT * FROM $SETTINGS_TABLE LIMIT 1")
    suspend fun read(): SettingsEntity?

    @Query("SELECT caloriesDayLimit, stepsDayLimit FROM $SETTINGS_TABLE LIMIT 1")
    suspend fun readDayLimits(): DayLimits?

    @Query("DELETE FROM $SETTINGS_TABLE")
    suspend fun delete()
}
