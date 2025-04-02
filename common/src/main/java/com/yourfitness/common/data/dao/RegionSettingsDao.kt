package com.yourfitness.common.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yourfitness.common.data.entity.RegionSettingsEntity

@Dao
interface RegionSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(schedule: RegionSettingsEntity)

    @Query("SELECT * FROM ${RegionSettingsEntity.REGION_SETTINGS_TABLE} LIMIT 1")
    suspend fun read(): RegionSettingsEntity?

    @Query("DELETE FROM ${RegionSettingsEntity.REGION_SETTINGS_TABLE}")
    suspend fun delete()
}