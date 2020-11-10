package com.android.stressy.dataclass.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CoroutineDataDao {
    @Query(value = "SELECT * FROM coroutine")
    fun getAll(): List<CoroutineData>


    @Query(value = "SELECT * FROM coroutine WHERE timestamp > :tsFrom")
    fun getFrom(tsFrom:Long): List<CoroutineData>


    @Query(value = "SELECT * FROM coroutine WHERE timestamp > :tsFrom and timestamp <= :tsTo")
    fun getFromTo(tsFrom:Long, tsTo:Long): List<CoroutineData>

    @Insert
    fun insert(data: CoroutineData)

    @Query(value= "DELETE FROM coroutine")
    fun deleteAll()

    @Query(value="SELECT COUNT(*) FROM coroutine")
    fun countCoroutine(): Int

    @Query(value = "DELETE FROM coroutine WHERE timestamp = :timestamp")
    fun deleteAt(timestamp:Long)



    //([item['ifMoving'],item['orientation'],item['posture'],item['std_posture'],temp['category'],temp['totalTimeInForeground']])
}