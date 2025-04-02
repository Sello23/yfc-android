package com.yourfitness.pt.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.yourfitness.common.data.CommonConverters
import com.yourfitness.pt.data.dao.PtBalanceDao
import com.yourfitness.pt.data.dao.PtDao
import com.yourfitness.pt.data.dao.SessionDao
import com.yourfitness.pt.data.entity.PersonalTrainerEntity
import com.yourfitness.pt.data.entity.PtBalanceEntity
import com.yourfitness.pt.data.entity.SessionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Database(
    entities = [
        PersonalTrainerEntity::class,
        PtBalanceEntity::class,
        SessionEntity::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(CommonConverters::class, EducationConverters::class, ProfileInfoConverters::class)
abstract class PtDatabase : RoomDatabase() {
    abstract fun ptDao(): PtDao
    abstract fun ptBalanceDao(): PtBalanceDao
    abstract fun sessionDao(): SessionDao

    suspend fun clearDb() = withContext(Dispatchers.IO) { clearAllTables() }
}
