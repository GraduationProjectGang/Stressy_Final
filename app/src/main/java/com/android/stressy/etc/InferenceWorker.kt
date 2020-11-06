package com.android.stressy.etc

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.android.stressy.dataclass.db.StressPredictedData
import com.android.stressy.dataclass.db.StressPredictedDatabase
import kotlinx.coroutines.coroutineScope

class InferenceWorker(appContext: Context, workerParams: WorkerParameters)
    : CoroutineWorker(appContext, workerParams) {

        override suspend fun doWork(): Result = coroutineScope {












            
            val timestamp = 1111111111.toLong() //temp
            val stressPredicted = 3 //temp
            val stressData = StressPredictedData(timestamp,stressPredicted)
            saveData(stressData)
            Result.success()

        }
        fun saveData(stressPredictedData: StressPredictedData){
            val dbObject = Room.databaseBuilder(
                applicationContext,
                StressPredictedDatabase::class.java, "stressPredicted"
            ).fallbackToDestructiveMigration().build().stressPredictedDao()

            dbObject.insert(stressPredictedData)

            val data = dbObject.getAll()
            Log.d("stressData",data.size.toString())
        }
    }