package com.android.stressy.dataclass

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CoroutineDataDao {
    @Query(value = "SELECT * FROM coroutineDB")
    fun getAll(): List<CoroutineData>

    @Insert
    fun insert(CoroutineData: CoroutineData)
}