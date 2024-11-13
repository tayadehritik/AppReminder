package com.hritik.appreminder.di


import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.room.Room
import com.hritik.appreminder.data.AppsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providesAppsDatabase(app:Application): AppsDatabase {
        return Room.databaseBuilder(
            app,
            AppsDatabase::class.java, "apps-database"
        ).build()
    }

    @Provides
    @Singleton
    fun providesUsageStatsManager(app:Application): UsageStatsManager {
        return app.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }

}