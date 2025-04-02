package com.yourfitness.coach.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteTable
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yourfitness.coach.data.dao.FacilityDao
import com.yourfitness.coach.data.dao.FitnessBackendDataDao
import com.yourfitness.coach.data.dao.FitnessDataDao
import com.yourfitness.coach.data.dao.LeaderboardDao
import com.yourfitness.coach.data.dao.ReferralCodeDao
import com.yourfitness.coach.data.dao.ScheduleDao
import com.yourfitness.coach.data.dao.SettingsDao
import com.yourfitness.coach.data.dao.SubscriptionDao
import com.yourfitness.coach.data.dao.WorkoutDao
import com.yourfitness.coach.data.entity.FacilityEntity
import com.yourfitness.coach.data.entity.FitnessDataBackendEntity
import com.yourfitness.coach.data.entity.FitnessDataEntity
import com.yourfitness.coach.data.entity.LeaderboardEntity
import com.yourfitness.coach.data.entity.ReferralCodeEntity
import com.yourfitness.coach.data.entity.ScheduleEntity
import com.yourfitness.coach.data.entity.SettingsEntity
import com.yourfitness.coach.data.entity.SubscriptionEntity
import com.yourfitness.coach.data.entity.WorkoutEntity
import com.yourfitness.common.data.CommonConverters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Database(
    entities = [
        FacilityEntity::class,
        FitnessDataEntity::class,
        FitnessDataBackendEntity::class,
        ScheduleEntity::class,
        ReferralCodeEntity::class,
        SubscriptionEntity::class,
        SettingsEntity::class,
        WorkoutEntity::class,
        LeaderboardEntity::class,
    ],
    version = 13,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(
            from = 9,
            to = 10,
            spec = ProgressLevelDeleteMigration::class
        ),
    ],
)
@TypeConverters(
    Converters::class, CommonConverters::class
)
abstract class YFCDatabase : RoomDatabase() {
    abstract fun facilityDao(): FacilityDao
    abstract fun fitnessDataDao(): FitnessDataDao
    abstract fun fitnessBackendDataDao(): FitnessBackendDataDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun referralCodeDao(): ReferralCodeDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun settingsDao(): SettingsDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun leaderboardDao(): LeaderboardDao

    suspend fun clearDb() = withContext(Dispatchers.IO) { clearAllTables() }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE facility ADD COLUMN schedule TEXT")
        database.execSQL("ALTER TABLE facility ADD COLUMN accessLimitationMessage TEXT")
        database.execSQL("ALTER TABLE facility ADD COLUMN displayAccessLimitationMessage INTEGER NOT NULL DEFAULT FALSE")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE settings_table ADD COLUMN corporateLeaderboardStartDate INTEGER")
        database.execSQL("ALTER TABLE settings_table ADD COLUMN corporateLeaderboardEndDate INTEGER")

        database.execSQL(
            "CREATE TABLE IF NOT EXISTS leaderboard_table (" +
                    "_id INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "corporationId TEXT NOT NULL," +
                    "profileId TEXT NOT NULL," +
                    "mediaId TEXT NOT NULL," +
                    "name TEXT NOT NULL," +
                    "surname TEXT NOT NULL," +
                    "rank INTEGER NOT NULL," +
                    "score INTEGER NOT NULL," +
                    "boardId INTEGER NOT NULL," +
                    "syncedAt INTEGER NOT NULL" +
                    ")"
        )
    }
}


val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS google_fit_last_entry_date_data")
        database.execSQL("DROP TABLE IF EXISTS google_fit_data")

        database.execSQL("ALTER TABLE settings_table ADD COLUMN dubai3030ChallengeStartDate INTEGER")
        database.execSQL("ALTER TABLE settings_table ADD COLUMN dubai3030ChallengeEndDate INTEGER")

        database.execSQL("ALTER TABLE subscription_table ADD COLUMN isOneTime INTEGER NOT NULL DEFAULT FALSE")
        database.execSQL("ALTER TABLE subscription_table ADD COLUMN duration INTEGER")
        database.execSQL("ALTER TABLE subscription_table ADD COLUMN corporationName TEXT")

        database.execSQL(
            "CREATE TABLE IF NOT EXISTS workout_data (" +
                    "id TEXT NOT NULL PRIMARY KEY," +
                    " createdAt INTEGER NOT NULL," +
                    " trackedAt INTEGER NOT NULL," +
                    " userId TEXT," +
                    " manual INTEGER DEFAULT 0 NOT NULL" +
                    ")"
        )

        database.execSQL("ALTER TABLE facility ADD COLUMN personalTrainersIDs TEXT")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_facility_id ON facility (id)")
    }
}


val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE settings_table ADD COLUMN caloriesDayLimit INTEGER")
        database.execSQL("ALTER TABLE settings_table ADD COLUMN stepsDayLimit INTEGER")

        database.execSQL(
            "CREATE TABLE IF NOT EXISTS progress_levels (" +
                    "_id INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "coinRewards INTEGER DEFAULT 0 NOT NULL," +
                    "createdAt TEXT," +
                    "id TEXT," +
                    "mediaId TEXT," +
                    "name TEXT," +
                    "pointLevel INTEGER DEFAULT 0 NOT NULL," +
                    "updatedAt TEXT" +
                    ")"
        )
    }
}

@DeleteTable(tableName = "progress_levels")
class ProgressLevelDeleteMigration : AutoMigrationSpec {
    @Override
    override fun onPostMigrate(db: SupportSQLiteDatabase) {
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE fitness_data ADD COLUMN allStepsSum FLOAT NOT NULL DEFAULT 0.0")
        database.execSQL("ALTER TABLE fitness_data ADD COLUMN manualSteps FLOAT NOT NULL DEFAULT 0.0")
    }
}
