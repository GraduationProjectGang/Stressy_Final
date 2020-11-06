package com.android.stressy.dataclass.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "stressPredicted")
data class StressPredictedData(
    @PrimaryKey val timestamp:Long,
    @ColumnInfo(name = "stressPredicted") val stressPredicted: Int
) {
    constructor() : this(0,2) {
    }
}