package com.yourfitness.coach.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yourfitness.coach.data.entity.ScheduleEntity

@Dao
interface ScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun writeSchedule(schedule: List<ScheduleEntity>)

    @Query("SELECT * FROM schedule_table")
    suspend fun readAll(): List<ScheduleEntity>

    @Query("DELETE FROM schedule_table")
    suspend fun delete()
}