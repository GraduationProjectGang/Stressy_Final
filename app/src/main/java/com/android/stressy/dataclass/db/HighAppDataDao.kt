package com.android.stressy.dataclass.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HighAppDataDao {
    @Query(value = "SELECT * FROM highApp")
    fun getAll(): List<HighAppData>

    @Insert
    fun insert(data: HighAppData)

    @Query(value = "DELETE FROM highApp")
    fun deteleAll()

    //([item['ifMoving'],item['orientation'],item['posture'],item['std_posture'],temp['category'],temp['totalTimeInForeground']])
}