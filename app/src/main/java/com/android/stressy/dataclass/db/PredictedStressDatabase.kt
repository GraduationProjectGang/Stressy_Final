package com.android.stressy.dataclass.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(PredictedStressData::class),version = 1)
abstract class PredictedStressDatabase : RoomDatabase() {
    abstract fun predictedStressDao(): PredictedStressDao
//    companion object{
//        @JvmField
//        val MIGRATION1_2 = DataCollectWorker.Migration1_2()
//    }
}