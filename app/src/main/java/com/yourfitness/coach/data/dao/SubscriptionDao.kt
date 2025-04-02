package com.yourfitness.coach.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yourfitness.coach.data.entity.SubscriptionEntity
import com.yourfitness.coach.data.entity.SubscriptionEntity.Companion.SUBSCRIPTION_TABLE

@Dao
interface SubscriptionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSubscription(schedule: SubscriptionEntity)

    @Query("SELECT * FROM $SUBSCRIPTION_TABLE LIMIT 1")
    suspend fun read(): SubscriptionEntity?

    @Query("SELECT * FROM $SUBSCRIPTION_TABLE")
    suspend fun readAll(): List<SubscriptionEntity>

    @Query("DELETE FROM $SUBSCRIPTION_TABLE")
    suspend fun delete()
}
