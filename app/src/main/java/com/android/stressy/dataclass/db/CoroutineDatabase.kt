package com.android.stressy.dataclass.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(CoroutineData::class),version = 1)
abstract class CoroutineDatabase : RoomDatabase() {
    abstract fun coroutineDataDao(): CoroutineDataDao
//    companion object{
//        @JvmField
//        val MIGRATION1_2 = DataCollectWorker.Migration1_2()
//    }
}