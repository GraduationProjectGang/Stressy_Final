package com.android.stressy.dataclass.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(HighAppData::class),version = 1)
abstract class HighAppDatabase : RoomDatabase() {
    abstract fun highAppDataDao(): HighAppDataDao
//    companion object{
//        @JvmField
//        val MIGRATION1_2 = DataCollectWorker.Migration1_2()
//    }
}