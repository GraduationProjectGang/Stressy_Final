package com.android.stressy.dataclass

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CoroutineDataDao {
    @Query(value = "SELECT * FROM coroutine")
    fun getAll(): List<CoroutineData>

    @Insert
    fun insert(data: CoroutineData)






    //([item['ifMoving'],item['orientation'],item['posture'],item['std_posture'],temp['category'],temp['totalTimeInForeground']])
}