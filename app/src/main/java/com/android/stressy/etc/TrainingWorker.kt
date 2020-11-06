package com.android.stressy.etc

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.coroutineScope

class TrainingWorker(appContext: Context, workerParams: WorkerParameters)
    : CoroutineWorker(appContext, workerParams) {

        override suspend fun doWork(): Result = coroutineScope {


            Log.d("trainingWorker","working")
            Result.success()
        }
    }