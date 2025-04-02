package com.yourfitness.common.data.dao

import androidx.room.*
import com.yourfitness.common.data.entity.ProfileEntity
import com.yourfitness.common.data.entity.ProfileEntity.Companion.PROFILE_TABLE

@Dao
interface ProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: ProfileEntity)

    @Query("SELECT * FROM $PROFILE_TABLE LIMIT 1")
    suspend fun readProfile(): ProfileEntity?

    @Query("SELECT count(*) FROM (select 0 from $PROFILE_TABLE limit 1)")
    suspend fun countRows(): Int
}
