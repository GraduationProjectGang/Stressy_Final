package com.android.stressy.dataclass.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CoroutineDataDao {
    @Query(value = "SELECT * FROM coroutine")
    fun getAll(): List<CoroutineData>


    @Insert
    fun insert(data: CoroutineData)

    @Query(value= "DELETE FROM coroutine")
    fun deleteAll()

    @Query(value="SELECT COUNT(*) FROM coroutine")
    fun countCoroutine(): Int




    //([item['ifMoving'],item['orientation'],item['posture'],item['std_posture'],temp['category'],temp['totalTimeInForeground']])
}