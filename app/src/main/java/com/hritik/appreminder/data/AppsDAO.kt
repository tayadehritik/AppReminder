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

    @Update
    suspend fun updateAppData(vararg appData: AppData)

    @Query("SELECT * FROM appdata")
    fun getAllAppData(): Flow<List<AppData>>

    @Query("SELECT * FROM appdata WHERE packageName=:packageName")
    suspend fun getAppdata(packageName:String?): AppData?

}