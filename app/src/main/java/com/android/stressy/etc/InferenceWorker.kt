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
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import kotlinx.coroutines.coroutineScope
import org.deeplearning4j.util.ModelSerializer
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import java.io.BufferedReader
import java.io.InputStreamReader


class InferenceWorker(appContext: Context, workerParams: WorkerParameters)
    : CoroutineWorker(appContext, workerParams) {
    val context = appContext
    val dataTimestamp = mutableListOf<Long>()
    val resultArray = mutableListOf<Int>()
    val mPref = "my_pref"
    val prefs = context.getSharedPreferences(mPref,Context.MODE_PRIVATE)

    override suspend fun doWork(): Result = coroutineScope {
        val inputStream = context.resources.openRawResource(R.raw.stressy_final_model_nokeras)
        val model = ModelSerializer.restoreMultiLayerNetwork(inputStream, false)

        val last_inferred_timestamp = prefs.getLong("last_inferred_timestamp",0)

        putData()
        val data = getDataFrom(last_inferred_timestamp)
        Log.d("trtr",data[0].shapeInfoToString())


        Log.d("trtr",model.summary())

        val highResultApp: MutableSet<Int> = mutableSetOf()

//        val rd = (cd[idx] - nMin[idx]) * 2 / (nMax[idx] - nMin[idx]) - 1
        //

        for (each in data){
            val input = each.reshape(1,6,5)
            val result = model.output(input)
            val resultLabel = Nd4j.argMax(result,1).getInt(0)

            if (resultLabel >= 2) {
                for (i in 0..5) {
                    val appLabel = (each.getDouble(0, i, 4) + 1) * 7
                    if (appLabel != 0.0) {
                        highResultApp.add(appLabel.toInt())
                    }
                }
            }
            
            //TODO : 코루틴 DB에 highResultApp 추가좀 ㅠㅠ 하고 UserMainActivity에 불러와서 텍뷰에띄우면될듯

            resultArray.add(resultLabel)
        }
        saveData()



//        val training_data = getData()
//        val label_data = getLabel()
//
//        for (each in training_data){
//
//            Log.d("trtrinputshape2",each.shapeInfoToString())
//
//            val result = model.output(each.reshape(1,6,5))
//            val resultLabel = Nd4j.argMax(result,1).getInt(0)
//            Log.d("trtr.resultLabel", "$result   $resultLabel")
//            resultArray.add(resultLabel)
//        }
//        Log.d("trtr.resultLabelen",resultArray.size.toString())
//
//        var right = 0
//        var wrong = 0
//        for(idx in resultArray.indices){
//            Log.d("trtr.resres",resultArray[idx].toString() + "   "+label_data[idx].toString())
//            if (resultArray[idx] == label_data[idx]) right++
//            else wrong++
//        }
//        Log.d("trtr.resultresult",right.toString()+ "   "+wrong.toString())

        Result.success()
    }


    fun getData(): ArrayList<INDArray> {
        val data_all = ArrayList<INDArray>()

        val csvReader = CSVReaderBuilder(InputStreamReader(context.resources.openRawResource(R.raw.trainingdata_all)))
            .withCSVParser(CSVParserBuilder().withSeparator(',').build())
            .build()

        val dataArr = arrayListOf<Array<DoubleArray>>()
// Read the rest

        val nMin = arrayOf(0.0,1.0,0.0,0.0,0.0,0.0)
        val nMax = arrayOf(1.00000000e+00, 2.00000000e+00, 3.00000000e+00, 3.10823229e+00,
            1.40000000e+01, 4.26011840e+07)

        var line: Array<String>? = csvReader.readNext()
        while (line != null) {
            // Do something with the data
            val corArr = arrayListOf<DoubleArray>()

            for(each in line){

                val t = each.split("[","]",", ")
                var doubleArr = doubleArrayOf(t[1].toDouble(),t[2].toDouble(),t[3].toDouble(),t[4].toDouble(),t[5].toDouble(),t[6].toDouble())
                for(idx in doubleArr.indices){
                    val rd = (doubleArr[idx] - nMin[idx]) * 2 / (nMax[idx] - nMin[idx]) - 1
                    doubleArr[idx] = rd
                }
                corArr.add(doubleArr)
            }


            val arr2d = arrayListOf<Array<Double>>()
            for (i in 0 until 6){
                val temp = arrayListOf<Double>()
                for (j in 0 until 5){
                    temp.add(corArr[j][i])
                }

                arr2d.add(temp.toTypedArray())
            }

            val arr2dNd = Nd4j.createFromArray(arr2d.toTypedArray())
            data_all.add(arr2dNd)
//            val temp = line.contentToString().split("[","]",",")
//            for (ele in temp){
//            }
//            Log.d("csvReader", doubleArr.toTypedArray().contentToString())
//            dataArr.add(doubleArr.toDoubleArray())
            line = csvReader.readNext()
        }
//        try {
//            println("fileread")
//            val file = context.resources.openRawResource(R.raw.trainingdata_all)
//            val reader = BufferedReader(InputStreamReader(file))
//            for (line in reader.lines()) {
//                val coroutine_array = Array(6) { DoubleArray(5) }
//                val attributes = line.split("[","]",",","\"")
//                Log.d("trtr.split",attributes.toString())
//                for (j in 0 until 6) {
//                    for (k in 0..5) {
//                        coroutine_array[j][k] =
//                            attributes[k].trim { it <= ' ' }.toDouble()
//                    }
//                }
//                data_all[i] = coroutine_array
//            }
//        } catch (var11: IOException) {
//            var11.printStackTrace()
//        }
        return data_all
    }

    fun getLabel(): Array<Int> {
        val labelArr = arrayListOf<Int>()
        val csvReader = CSVReaderBuilder(InputStreamReader(context.resources.openRawResource(R.raw.stressdata_all)))
            .withCSVParser(CSVParserBuilder().withSeparator(',').build())
            .build()
        var line: Array<String>? = csvReader.readNext()
        while (line != null) {
            for (temp in line){
                val t = temp.split("[","]",",").filter { it.isNotEmpty() }
                for (eachInt in t){
                    labelArr.add(eachInt.trim().toInt())
                }
            }

            line = csvReader.readNext()
        }

        Log.d("csvread.label",labelArr.size.toString())
        Log.d("csvread.label",labelArr.toString())
        return labelArr.toTypedArray()
    }

    fun putData(){
        val dbObject = Room.databaseBuilder(
            context,
            CoroutineDatabase::class.java, "coroutine"
        ).fallbackToDestructiveMigration().build().coroutineDataDao()

        dbObject.deleteAll()
        Log.d("ecec",dbObject.countCoroutine().toString())
        val file = context.resources.openRawResource(R.raw.coroutine)
        val br = BufferedReader(InputStreamReader(file))
        for (line in br.lines()){
            val arr = line.split(",")
            val data1 = arr[1].toLong()
            val data2 = arr[2].toDouble()
            val data3 = arr[3].toDouble()
            val data4 = arr[4].toDouble()
            var data5 = arr[5].toDoubleOrNull()
            if (data5 == null){
                data5 = 0.0
            }
            var data6 = arr[6].toDouble()
            val data7 = arr[7].toDouble()
            val tempData = CoroutineData(timestamp = data1,ifMoving = data2,orientation = data3,posture = data4,std_posture = data5,category = data6,totalTimeInForeground = data7)

            dbObject.insert(tempData)
        }
        dbObject.deleteAt(1604291407030)
    }

    fun saveData(){
        val dbObject = Room.databaseBuilder(
            context,
            StressPredictedDatabase::class.java, "stressPredicted"
        ).fallbackToDestructiveMigration().build().stressPredictedDao()

        dbObject.deleteAll()
        var idxmin = dataTimestamp.size
        if (resultArray.size < dataTimestamp.size) idxmin = resultArray.size
        for(idx in 0 until idxmin){
            dbObject.insert(StressPredictedData(dataTimestamp[idx], resultArray[idx]))
        }

        val countResult = dbObject.countResult()
        if (countResult % 100 == 0){
//            informServer(countResult / 100)
        }
    }

    fun getDataFrom(last_inferred_timestamp:Long):Array<INDArray> {
        val dbObject = Room.databaseBuilder(
            applicationContext,
            CoroutineDatabase::class.java, "coroutine"
        ).allowMainThreadQueries().fallbackToDestructiveMigration().build().coroutineDataDao()

//        val data = dbObject.getFrom(last_inferred_timestamp)
        val data = dbObject.getAll()
        prefs.edit().putLong("last_inferred_timestamp",data.get(data.size-1).timestamp).apply() //마지막 데이터 timestamp 받아오기
        Log.d("trtr.datacout",data.size.toString())
        Log.d("trtr.datacoutdata",data[0].toString())
        Log.d("trtr.last_timestamp",data.get(data.size-1).timestamp.toString())

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