package com.android.stressy.dataclass.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.android.stressy.dataclass.PredictedStress

@Dao
interface PredictedStressDao {
    @Query(value = "SELECT * FROM stressPredicted")
    fun getAll(): List<PredictedStress>

    @Query(value = "SELECT * FROM stressPredicted WHERE startTimestamp <=  AND endTimestamp < 51515151512121")
    fun getFromTo(tsFrom:Long, tsTo:Long): List<PredictedStress>

    @Insert
    fun insert(data: PredictedStress)

    //([item['ifMoving'],item['orientation'],item['posture'],item['std_posture'],temp['category'],temp['totalTimeInForeground']])
}