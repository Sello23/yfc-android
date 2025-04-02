package com.yourfitness.community.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yourfitness.community.data.entity.FriendsEntity
import com.yourfitness.community.data.entity.FriendsEntity.Companion.FRIENDS_TABLE

@Dao
interface FriendsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFriends(friends: List<FriendsEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFriend(friend: FriendsEntity)

    @Query("SELECT * FROM $FRIENDS_TABLE")
    suspend fun readFriends(): List<FriendsEntity>

    @Query("SELECT * FROM $FRIENDS_TABLE WHERE id = :id LIMIT 1")
    suspend fun readFriendById(id: String): FriendsEntity?


    @Query("DELETE FROM $FRIENDS_TABLE WHERE id = :id")
    suspend fun deleteFriend(id: String)

    @Query("DELETE FROM $FRIENDS_TABLE")
    suspend fun clearTable()
}
