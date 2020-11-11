package com.android.stressy.dataclass.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface StressScoreDataDao {
    @Query(value = "SELECT * FROM stressScore")
    fun getAll(): List<StressScoreData>

    @Insert
    fun insert(data: StressScoreData)

    @Query(value = "SELECT COUNT(*) FROM stressScore")
    fun getCount():Int

    @Query(value = "SELECT * FROM stressScore WHERE timestamp >= :tsFrom AND timestamp < :tsTo")
    fun getFromTo(tsFrom:Long, tsTo:Long): List<StressScoreData>






    //([item['ifMoving'],item['orientation'],item['posture'],item['std_posture'],temp['category'],temp['totalTimeInForeground']])
}