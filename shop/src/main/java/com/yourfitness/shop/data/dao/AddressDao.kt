package com.yourfitness.shop.data.dao

import androidx.room.*
import com.yourfitness.shop.data.entity.*

@Dao
interface AddressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAddress(address: AddressEntity)

    @Query("SELECT * FROM address LIMIT 1")
    suspend fun getAddress() : AddressEntity?
}
