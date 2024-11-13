package com.hritik.appreminder.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppsDAO {
    @Query("SELECT packageName FROM appdata")
    fun getAllPackages(): Flow<List<String>>

    @Query("SELECT * FROM appdata WHERE packageName=:packageName")
    suspend fun getAppdata(packageName:String): AppData?

    @Insert
    suspend fun insertAppData(vararg appData:AppData)

    @Delete
    suspend fun deleteAppData(appData: AppData)
}