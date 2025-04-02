package com.yourfitness.pt.data

import android.content.Context
import androidx.room.Room
import com.yourfitness.pt.data.dao.PtBalanceDao
import com.yourfitness.pt.data.dao.PtDao
import com.yourfitness.pt.data.dao.SessionDao
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
    fun provideDatabase(@ApplicationContext context: Context): PtDatabase {
        return Room.databaseBuilder(context, PtDatabase::class.java, "pt-database")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun providePtDao(database: PtDatabase): PtDao {
        return database.ptDao()
    }

    @Provides
    fun providePtBalanceDao(database: PtDatabase): PtBalanceDao {
        return database.ptBalanceDao()
    }

    @Provides
    fun provideSessionDao(database: PtDatabase): SessionDao {
        return database.sessionDao()
    }
}
