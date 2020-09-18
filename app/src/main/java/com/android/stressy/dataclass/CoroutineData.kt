package com.android.stressy.dataclass

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CoroutineData (
    @PrimaryKey val timestamp: Long,
    @ColumnInfo(name = "rotateVector") val rotateVector: RotateVector?,
    @ColumnInfo(name = "usageStats") val usageStats: UsageStatsCollection?,
    @ColumnInfo(name = "location") val locate: Locate?

){
}