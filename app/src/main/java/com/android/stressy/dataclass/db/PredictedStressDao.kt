package com.android.stressy.dataclass.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PredictedStressDao {
    @Query(value = "SELECT * FROM stressPredicted")
    fun getAll(): List<PredictedStressData>

    @Query(value = "SELECT * FROM stressPredicted WHERE timeStamp <= :tsFrom AND timeStamp < :tsTo")
    fun getFromTo(tsFrom:Long, tsTo:Long): List<PredictedStressData>

    @Insert
    fun insert(data: PredictedStressData)

    //([item['ifMoving'],item['orientation'],item['posture'],item['std_posture'],temp['category'],temp['totalTimeInForeground']])
}