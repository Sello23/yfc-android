package com.yourfitness.common.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yourfitness.common.data.dao.ProfileDao
import com.yourfitness.common.data.dao.ProgressLevelDao
import com.yourfitness.common.data.dao.RegionSettingsDao
import com.yourfitness.common.data.entity.ProfileEntity
import com.yourfitness.common.data.entity.RegionSettingsEntity
import com.yourfitness.common.network.dto.ProgressLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Database(
    entities = [
        ProfileEntity::class,
        RegionSettingsEntity::class,
        ProgressLevel::class
    ],
    version = 7,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(
            from = 4,
            to = 5
        ),
    ],
)
@TypeConverters(
    CommonConverters::class
)
abstract class CommonDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun regionSettingsDao(): RegionSettingsDao
    abstract fun progressLevelDao(): ProgressLevelDao

    suspend fun clearDb() = withContext(Dispatchers.IO) {
        clearAllTables()
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE profile ADD COLUMN corporationId TEXT")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE profile ADD COLUMN personalTrainer INTEGER")
        db.execSQL("ALTER TABLE profile ADD COLUMN complimentaryAccess INTEGER")

        db.execSQL("ALTER TABLE region_settings ADD COLUMN timeZoneOffset INTEGER NOT NULL DEFAULT '0'")
        db.execSQL("ALTER TABLE region_settings ADD COLUMN region TEXT NOT NULL DEFAULT 'UAE'")
        db.execSQL("ALTER TABLE region_settings ADD COLUMN country TEXT NOT NULL DEFAULT 'UAE'")
    }
}
