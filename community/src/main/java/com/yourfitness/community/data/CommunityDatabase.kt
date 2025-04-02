package com.yourfitness.community.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.yourfitness.common.data.CommonConverters
import com.yourfitness.community.data.dao.FriendsDao
import com.yourfitness.community.data.entity.FriendsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@Database(
    entities = [
        FriendsEntity::class,
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(CommonConverters::class)
abstract class CommunityDatabase : RoomDatabase() {
    abstract fun friendsDao(): FriendsDao

    suspend fun clearDb() = withContext(Dispatchers.IO) { clearAllTables() }
}
