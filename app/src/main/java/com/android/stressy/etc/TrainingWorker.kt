package com.android.stressy.etc

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.android.stressy.paillier.KeyPairBuilder
import kotlinx.coroutines.coroutineScope

class TrainingWorker(appContext: Context, workerParams: WorkerParameters): CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {

        generateKey()

        Log.d("trainingWorker", "working")
        Result.success()
    }

    fun generateKey() {

        val keygen = KeyPairBuilder()
        val keyPair = keygen.generateKeyPair()

        val publicKey = keyPair.getPublicKey()
        val privateKey = keyPair.getPrivateKey()

        val publicKey_str = publicKey.toString()
        val privateKey_str = privateKey.toString()

    }

}