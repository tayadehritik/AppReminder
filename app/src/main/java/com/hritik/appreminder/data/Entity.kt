package com.hritik.appreminder.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AppData (
    @PrimaryKey val packageName:String,
    @ColumnInfo(name = "time_limit") val timeLimit: Long?,
    @ColumnInfo(name = "time_spent") val timeSpent: Long?,
    @ColumnInfo(name = "extended_time") val extendedTime: Long?,
)