package com.android.stressy.etc

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.android.stressy.paillier.KeyPairBuilder
import com.android.stressy.R
import com.android.stressy.dataclass.db.CoroutineData
import com.android.stressy.dataclass.db.CoroutineDatabase
import kotlinx.coroutines.coroutineScope
import org.deeplearning4j.util.ModelSerializer
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j

class TrainingWorker(appContext: Context, workerParams: WorkerParameters)
    : CoroutineWorker(appContext, workerParams) {
    val context = appContext
    override suspend fun doWork(): Result = coroutineScope {
        val inputStream = context.resources.openRawResource(R.raw.stressy_final_model_2mall)
        val model = ModelSerializer.restoreMultiLayerNetwork(inputStream, false)


        val trainData = getData()
//            model.output()


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

    private fun getData(): INDArray {
        val dbObject = Room.databaseBuilder(
            applicationContext,
            CoroutineDatabase::class.java, "coroutine"
        ).fallbackToDestructiveMigration().build().coroutineDataDao()

        val data = dbObject.getAll()
        Log.d("data", data.size.toString())
        val arr = mutableListOf<MutableList<CoroutineData>>()
//        val temp_arr = listOf<List<Double>>()[5]
        val timestampArr = mutableListOf<CoroutineData>()
//        val tempArr = mutableListOf<CoroutineData>()
        //timestamp별로 모아서 timestampArr에 넣기 -> 코루틴 개수만큼 생기겠지

        var timestamp_this = 0.toLong()
        var idx = 0
        for (eachCoroutine in data) {
            if (eachCoroutine.timestamp == timestamp_this) {
                arr[idx].add(eachCoroutine)
            } else {
                arr.add(mutableListOf(eachCoroutine))
                timestamp_this = eachCoroutine.timestamp
            }
        }
        Log.d("ecec", arr.toString())


        //timestampArr size만큼 모아서 Double[][][]로 만들기


        for (cd in data) {
            val coroutineData = listOf<Double>(
                cd.ifMoving,
                cd.orientation,
                cd.posture,
                cd.std_posture,
                cd.category,
                cd.totalTimeInForeground
            )

        }
        return Nd4j.create(0)
    }
}

