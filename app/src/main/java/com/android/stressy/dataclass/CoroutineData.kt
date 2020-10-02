package com.android.stressy.dataclass

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coroutine")
data class CoroutineData (
    //([item["ifMoving"],item["orientation"],item["posture"],item["std_posture"],temp["category"],temp["totalTimeInForeground"]])
    @PrimaryKey(autoGenerate = true) val id:Int,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "ifMoving") val ifMoving: Int?,
    @ColumnInfo(name = "orientation") val orientation: Int?,
    @ColumnInfo(name = "posture") val posture: Int?,
    @ColumnInfo(name = "std_posture") val std_posture: Double?,
    @ColumnInfo(name = "category") val category: Int?,
    @ColumnInfo(name = "totalTimeInForeground") val totalTimeInForeground: Long?

){
    constructor(timestamp: Long,ifMoving: Int?,orientation: Int?,posture: Int?,std_posture: Double?,category: Int?,totalTimeInForeground: Long?):this(0,timestamp,ifMoving,orientation,posture,std_posture,category,totalTimeInForeground)
}