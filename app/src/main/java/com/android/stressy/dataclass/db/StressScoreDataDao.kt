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






    //([item['ifMoving'],item['orientation'],item['posture'],item['std_posture'],temp['category'],temp['totalTimeInForeground']])
}