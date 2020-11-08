package com.android.stressy.dataclass.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coroutine")
data class CoroutineData (
    //([item["ifMoving"],item["orientation"],item["posture"],item["std_posture"],temp["category"],temp["totalTimeInForeground"]])
    @PrimaryKey(autoGenerate = true) val id:Int,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "ifMoving") val ifMoving: Double,
    @ColumnInfo(name = "orientation") val orientation: Double,
    @ColumnInfo(name = "posture") val posture: Double,
    @ColumnInfo(name = "std_posture") val std_posture: Double,
    @ColumnInfo(name = "category") val category: Double,
    @ColumnInfo(name = "totalTimeInForeground") val totalTimeInForeground: Double

){
    constructor(
        timestamp: Long,
        ifMoving: Double,
        orientation: Double,
        posture: Double,
        std_posture: Double,
        category: Double,
        totalTimeInForeground: Double):this(0,timestamp,ifMoving,orientation,posture,std_posture,category,totalTimeInForeground)
}