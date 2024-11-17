package com.hritik.appreminder.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppsDAO {
    @Insert
    suspend fun insertAppData(vararg appData:AppData)

    @Delete
    suspend fun deleteAppData(vararg appData: AppData)

    @Query("SELECT * FROM appdata")
    fun getAllAppData(): Flow<List<AppData>>

    @Query("SELECT * FROM appdata WHERE packageName=:packageName")
    suspend fun getAppdata(packageName:String?): AppData?

    @Query("SELECT * FROM appdata WHERE state=1")
    suspend fun getActiveApp(): AppData?

    @Query("UPDATE appdata SET extended_time = :extendedTime, time_spent = :timeSpent WHERE packageName = :packageName")
    suspend fun updateAppUsage(packageName: String, extendedTime: Long, timeSpent:Long)

    @Query("UPDATE appdata SET state = :state WHERE packageName = :packageName")
    suspend fun updateState(packageName: String, state: Int)

    @Query("UPDATE appdata SET time_limit = :timeLimit WHERE packageName = :packageName")
    suspend fun updateTimeLimit(packageName: String, timeLimit: Long)

    @Query("UPDATE appdata SET time_spent = 0, extended_time = 0")
    suspend fun dailyReset()
}