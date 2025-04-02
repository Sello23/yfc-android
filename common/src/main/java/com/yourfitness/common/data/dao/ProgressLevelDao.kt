package com.yourfitness.common.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yourfitness.common.network.dto.ProgressLevel
import com.yourfitness.common.network.dto.ProgressLevel.Companion.PROGRESS_LEVEL_TABLE

@Dao
interface ProgressLevelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLevels(levels: List<ProgressLevel>)

    @Query("SELECT * FROM $PROGRESS_LEVEL_TABLE")
    suspend fun readLevels(): List<ProgressLevel>

    @Query("DELETE FROM $PROGRESS_LEVEL_TABLE")
    suspend fun delete()
}
