package com.android.stressy.dataclass

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(CoroutineData::class),version = 1)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun coroutineDataDao(): CoroutineDataDao
}