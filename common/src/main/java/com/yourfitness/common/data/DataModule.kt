package com.yourfitness.common.data

import android.content.Context
import androidx.room.Room
import com.yourfitness.common.data.dao.ProfileDao
import com.yourfitness.common.data.dao.ProgressLevelDao
import com.yourfitness.common.data.dao.RegionSettingsDao
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
    fun provideDatabase(@ApplicationContext context: Context): CommonDatabase {
        return Room.databaseBuilder(context, CommonDatabase::class.java, "common-database")
            .fallbackToDestructiveMigration()
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
    }

    @Singleton
    @Provides
    fun provideProfileDao(database: CommonDatabase): ProfileDao {
        return database.profileDao()
    }

    @Singleton
    @Provides
    fun provideRegionSettingsDao(database: CommonDatabase): RegionSettingsDao {
        return database.regionSettingsDao()
    }

    @Singleton
    @Provides
    fun progressLevelDao(database: CommonDatabase): ProgressLevelDao {
        return database.progressLevelDao()
    }
}
