package com.android.stressy.dataclass

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CoroutineData (
    @PrimaryKey val timestamp: Long,
    @ColumnInfo(name = "rotateVector") val rotateVector: RotateVectorData?,
    @ColumnInfo(name = "usageStats") val usageStats: UsageStatsList?,
    @ColumnInfo(name = "location") val locate: LocationData?

){
}