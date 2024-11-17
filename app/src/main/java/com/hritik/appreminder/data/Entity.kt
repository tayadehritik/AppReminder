package com.hritik.appreminder.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AppData (
    @PrimaryKey val packageName:String,
    @ColumnInfo(name = "time_limit") var timeLimit: Long = 0,
    @ColumnInfo(name = "time_spent") var timeSpent: Long = 0,
    @ColumnInfo(name = "extended_time") var extendedTime: Long = 0,
    @ColumnInfo(name = "state") var state: Int = 2
)