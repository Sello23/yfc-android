package com.yourfitness.coach.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yourfitness.coach.data.entity.ScheduleEntity.Companion.SCHEDULE_TABLE

@Entity(tableName = SCHEDULE_TABLE)
data class ScheduleEntity(
    @PrimaryKey
    val createdAt: Long,
    val bookedPlaces: Int,
    val customClassId: String,
    val from: Long,
    val id: String,
    val instructorId: String,
    val licensedClassId: String,
    val to: Long,
    val updatedAt: Long,
) {
    companion object {
        const val SCHEDULE_TABLE = "schedule_table"
    }
}
