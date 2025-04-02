package com.yourfitness.coach.data

import android.content.Context
import androidx.room.Room
import com.yourfitness.coach.data.dao.FacilityDao
import com.yourfitness.coach.data.dao.FitnessBackendDataDao
import com.yourfitness.coach.data.dao.FitnessDataDao
import com.yourfitness.coach.data.dao.LeaderboardDao
import com.yourfitness.coach.data.dao.ReferralCodeDao
import com.yourfitness.coach.data.dao.ScheduleDao
import com.yourfitness.coach.data.dao.SettingsDao
import com.yourfitness.coach.data.dao.SubscriptionDao
import com.yourfitness.coach.data.dao.WorkoutDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): YFCDatabase {
        return Room.databaseBuilder(context, YFCDatabase::class.java, "yfc-database")
            .fallbackToDestructiveMigration()
            .addMigrations(MIGRATION_4_5, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
            .build()
    }

    @Singleton
    @Provides
    fun provideFacilityDao(database: YFCDatabase): FacilityDao {
        return database.facilityDao()
    }

    @Singleton
    @Provides
    fun provideFitnessDataDao(database: YFCDatabase): FitnessDataDao {
        return database.fitnessDataDao()
    }

    @Singleton
    @Provides
    fun provideFitnessBackendDataDao(database: YFCDatabase): FitnessBackendDataDao {
        return database.fitnessBackendDataDao()
    }

    @Singleton
    @Provides
    fun provideScheduleDao(database: YFCDatabase): ScheduleDao {
        return database.scheduleDao()
    }

    @Singleton
    @Provides
    fun provideReferralCodeDao(database: YFCDatabase): ReferralCodeDao {
        return database.referralCodeDao()
    }

    @Singleton
    @Provides
    fun provideSubscriptionDao(database: YFCDatabase): SubscriptionDao {
        return database.subscriptionDao()
    }

    @Singleton
    @Provides
    fun settingsDao(database: YFCDatabase): SettingsDao {
        return database.settingsDao()
    }

    @Singleton
    @Provides
    fun workoutDao(database: YFCDatabase): WorkoutDao {
        return database.workoutDao()
    }

    @Singleton
    @Provides
    fun leaderboardDao(database: YFCDatabase): LeaderboardDao {
        return database.leaderboardDao()
    }
}
