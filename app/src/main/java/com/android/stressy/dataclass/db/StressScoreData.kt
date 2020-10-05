package com.android.stressy.dataclass.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "stress")
data class StressScoreData(
    @PrimaryKey val timestamp:Long,
    @ColumnInfo(name = "stressScore") val stressScore: Int
) {

    constructor() : this(0, 2) {
    }
}