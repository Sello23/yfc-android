package com.yourfitness.coach.data.dao

import android.database.Cursor
import androidx.room.*
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.data.entity.FacilityEntity.Companion.FACILITIES_TABLE

@Dao
interface FacilityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveFacilities(facilities: List<FacilityEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun writeFacility(facility: FacilityEntity)

    @Query("SELECT * FROM $FACILITIES_TABLE")
    suspend fun readAllFacilities(): List<FacilityEntity>

    @Query("SELECT id, name, personalTrainersIDs, address, icon," +
            " classification, latitude, longitude, schedule FROM $FACILITIES_TABLE")
    fun readAllFacilitiesCursor(): Cursor?

    @Query("SELECT name, icon, address FROM $FACILITIES_TABLE WHERE id = :id")
    fun readFacilityByIdCursor(id: String): Cursor?

    @Query("SELECT * FROM $FACILITIES_TABLE WHERE isVisit == 1")
    suspend fun readVisitedFacility(): List<FacilityEntity>

    @Query("SELECT * FROM $FACILITIES_TABLE WHERE name LIKE :searchQuery AND isVisit == 1")
    suspend fun readVisitedFacility(searchQuery: String): List<FacilityEntity>

    @Query("DELETE FROM $FACILITIES_TABLE WHERE id IN (:facilityIds)")
    suspend fun deleteFacilities(facilityIds: kotlin.collections.List<kotlin.String?>)
}