package com.android.stressy.etc

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.android.stressy.R
import com.android.stressy.dataclass.db.CoroutineData
import com.android.stressy.dataclass.db.CoroutineDatabase
import com.android.stressy.dataclass.db.StressPredictedData
import com.android.stressy.dataclass.db.StressPredictedDatabase
import kotlinx.coroutines.coroutineScope
import org.deeplearning4j.util.ModelSerializer
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j

class InferenceWorker(appContext: Context, workerParams: WorkerParameters)
    : CoroutineWorker(appContext, workerParams) {
    val context = appContext
    val dataTimestamp = mutableListOf<Long>()
    val resultArray = mutableListOf<Int>()
    override suspend fun doWork(): Result = coroutineScope {
        val inputStream = context.resources.openRawResource(R.raw.stressy_final_model_2mall)
        val model = ModelSerializer.restoreMultiLayerNetwork(inputStream, false)


        val trainData = getData()

        Log.d("trtr",model.summary())

        for (each in trainData){
            val input = each.reshape(1,6,5)
            Log.d("trtrinputshape2",input.toString())
            Log.d("trtrinputshape2",input.shapeInfoToString())
            val result = model.output(input)
            val resultLabel = Nd4j.argMax(result,1).getInt(0)
            Log.d("trtr.resultLable", "$result   $resultLabel")
            resultArray.add(resultLabel)
        }
        Log.d("trtr.resultLabelen",resultArray.size.toString())
        saveData()
        Result.success()
    }

    fun saveData(){
        val dbObject = Room.databaseBuilder(
            context,
            StressPredictedDatabase::class.java, "stressPredicted"
        ).fallbackToDestructiveMigration().build().stressPredictedDao()


        for(idx in dataTimestamp.indices){
            dbObject.insert(StressPredictedData( dataTimestamp.get(idx),resultArray.get(idx)))
        }

        val countResult = dbObject.countResult()
        if (countResult % 100 == 0){
            informServer(countResult / 100)
        }



    }

    private fun informServer(i: Int) {

    }

    fun getData():Array<INDArray> {
        //csv data 넣기
        val dbObject = Room.databaseBuilder(
            applicationContext,
            CoroutineDatabase::class.java, "coroutine"
        ).allowMainThreadQueries().fallbackToDestructiveMigration().build().coroutineDataDao()

        val data = dbObject.getAll()
        val arr = arrayListOf<ArrayList<CoroutineData>>()
        var timestampArr = arrayListOf<CoroutineData>()
        var timestamp_this = 0.toLong()
        for (thisCoroutine in data){
            if (thisCoroutine.timestamp == timestamp_this){
                timestampArr.add(thisCoroutine)
            }else{
                timestamp_this = thisCoroutine.timestamp
                dataTimestamp.add(thisCoroutine.timestamp)
                arr.add(timestampArr)
                Log.d("ecec",timestampArr.size.toString())
                timestampArr = arrayListOf()
                timestampArr.add(thisCoroutine)
            }
        }
        arr.removeAt(0)
        val realData = ArrayList<INDArray>()
        var id = 0

        val nMin = arrayOf(0.0,1.0,0.0,0.0,0.0,0.0)
        val nMax = arrayOf(1.00000000e+00, 2.00000000e+00, 3.00000000e+00, 3.10823229e+00,
            1.40000000e+01, 4.26011840e+07)


        for (eachCoroutine in arr){
            val coroutineData = arrayListOf<DoubleArray>()//5,6
            for (index in eachCoroutine.indices){//5,6, index = 5번
                val ed = eachCoroutine[index]
                var cd = doubleArrayOf(ed.ifMoving,ed.orientation,ed.posture,ed.std_posture,ed.category,ed.totalTimeInForeground)
                Log.d("trtrnormnotyet",cd.contentToString())

                //normalize [-1,1]
                for(idx in cd.indices){
                    val rd = (cd[idx] - nMin[idx]) * 2 / (nMax[idx] - nMin[idx]) - 1
                    cd[idx] = rd
                }
                Log.d("trtrnormedd",cd.contentToString())

                coroutineData.add(cd)
            }



            val arr2d = arrayListOf<Array<Double>>()
            for (i in 0 until 6){
                val temp = arrayListOf<Double>()
                for (j in 0 until 5){
                    temp.add(coroutineData[j][i])
                }

                arr2d.add(temp.toTypedArray())
            }

            val arr2dNd = Nd4j.createFromArray(arr2d.toTypedArray())
            realData.add(arr2dNd)

        }

        return realData.toTypedArray()
    }

    }