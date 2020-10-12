package com.android.stressy.dataclass.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "stress")
data class PredictedStressData(
    @PrimaryKey val timestamp:Long,
    @ColumnInfo(name = "startTimestamp") val startTimestamp:Long,
    @ColumnInfo(name = "EndTimestamp") val EndTimestamp:Long,
    @ColumnInfo(name = "stressPredicted") val stressPredicted: Int
) {
    constructor() : this(0,0,0,2) {
    }
}