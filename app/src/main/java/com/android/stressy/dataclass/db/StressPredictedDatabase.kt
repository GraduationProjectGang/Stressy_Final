package com.android.stressy.dataclass.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(StressPredictedData::class),version = 1)
abstract class StressPredictedDatabase : RoomDatabase() {
    abstract fun stressPredictedDao(): StressPredictedDao
//    companion object{
//        @JvmField
//        val MIGRATION1_2 = DataCollectWorker.Migration1_2()
//    }
}