package com.yourfitness.pt.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yourfitness.pt.data.entity.PtBalanceEntity
import com.yourfitness.pt.data.entity.PtBalanceEntity.Companion.PT_BALANCE_TABLE

@Dao
interface PtBalanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePtBalance(ptBalance: List<PtBalanceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun writePtBalance(ptBalance: PtBalanceEntity)

    @Query("SELECT * FROM $PT_BALANCE_TABLE")
    suspend fun readPtBalanceList(): List<PtBalanceEntity>

    @Query("SELECT * FROM $PT_BALANCE_TABLE WHERE personalTrainerId = :id LIMIT 1")
    suspend fun readPtBalanceById(id: String): PtBalanceEntity?

    @Query("DELETE FROM $PT_BALANCE_TABLE")
    fun clearTable()
}
