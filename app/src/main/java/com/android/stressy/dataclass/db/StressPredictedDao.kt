package com.android.stressy.dataclass.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface StressPredictedDao {
    @Query(value = "SELECT * FROM stressPredicted")
    fun getAll(): List<StressPredictedData>

    @Query(value = "SELECT * FROM stressPredicted WHERE timestamp >= :tsFrom AND timestamp < :tsTo")
    fun getFromTo(tsFrom:Long, tsTo:Long): List<StressPredictedData>

    @Query(value = "SELECT * FROM stressPredicted WHERE timestamp >= :tsFrom")
    fun getFrom(tsFrom:Long): List<StressPredictedData>

    @Insert
    fun insert(data:StressPredictedData)

    @Query(value = "SELECT COUNT(*) FROM stressPredicted")
    fun countResult(): Int

    @Query(value = "SELECT * FROM stressPredicted WHERE timestamp = :timestamp")
    fun getDataFromTimestamp(timestamp:Long): StressPredictedData
    //([item['ifMoving'],item['orientation'],item['posture'],item['std_posture'],temp['category'],temp['totalTimeInForeground']])
}