package com.android.stressy.dataclass.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HighAppDataDao {
    @Query(value = "SELECT * FROM highApp")
    fun getAll(): List<HighAppData>


    @Query(value = "SELECT * FROM highApp WHERE timestamp > :tsFrom")
    fun getFrom(tsFrom:Long): List<HighAppData>

    @Insert
    fun insert(data:Int)


    //([item['ifMoving'],item['orientation'],item['posture'],item['std_posture'],temp['category'],temp['totalTimeInForeground']])
}