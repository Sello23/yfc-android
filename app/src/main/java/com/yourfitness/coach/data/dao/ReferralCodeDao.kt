package com.yourfitness.coach.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yourfitness.coach.data.entity.ReferralCodeEntity

@Dao
interface ReferralCodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun write(referralCode: ReferralCodeEntity)

    @Query("SELECT * FROM referral_code")
    suspend fun read(): ReferralCodeEntity?

    @Query("DELETE FROM referral_code")
    suspend fun delete()
}