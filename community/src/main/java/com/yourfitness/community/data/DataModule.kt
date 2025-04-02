package com.yourfitness.community.data

import android.content.Context
import androidx.room.Room
import com.yourfitness.community.data.dao.FriendsDao
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
    fun provideDatabase(@ApplicationContext context: Context): CommunityDatabase {
        return Room.databaseBuilder(context, CommunityDatabase::class.java, "community-database")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun providePtDao(database: CommunityDatabase): FriendsDao {
        return database.friendsDao()
    }
}
