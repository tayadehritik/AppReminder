package com.hritik.appreminder.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AppData::class], version = 1)
abstract class AppsDatabase:RoomDatabase() {
    abstract fun appsDAO(): AppsDAO
}