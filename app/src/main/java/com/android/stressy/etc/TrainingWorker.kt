package com.android.stressy.etc

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.android.stressy.R
import com.android.stressy.dataclass.BaseUrl
import com.android.stressy.dataclass.db.CoroutineData
import com.android.stressy.dataclass.db.CoroutineDatabase
import com.android.stressy.dataclass.db.StressScoreDatabase
import com.android.stressy.paillier.KeyPair
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.deeplearning4j.datasets.iterator.ExistingDataSetIterator
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.deeplearning4j.util.ModelSerializer
import org.json.JSONObject
import org.nd4j.evaluation.classification.Evaluation
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.factory.Nd4j
import java.math.BigInteger
import kotlin.properties.Delegates

class TrainingWorker(appContext: Context, workerParams: WorkerParameters)
    : CoroutineWorker(appContext, workerParams) {
    val context = appContext
    val dataTimestamp = mutableListOf<Long>()
    val resultArray = mutableListOf<Int>()
    val mPref = "my_pref"
    val prefs = context.getSharedPreferences(mPref,Context.MODE_PRIVATE)
    var new_last_trained_timestamp by Delegates.notNull<Long>()

    override suspend fun doWork(): Result = coroutineScope {
        val inputStream = context.resources.openRawResource(R.raw.stressy_final_model_nokeras)
        val model = ModelSerializer.restoreMultiLayerNetwork(inputStream, false)
        val jwt = prefs.getString("jwt",null).toString()
        Log.d("trtr.jwt", jwt)

        val last_trained_timestamp = prefs.getLong("last_trained_timestamp",0)
        Log.d("trtr.last_trained",last_trained_timestamp.toString())
//        val last_trained_timestamp = 0.toLong()
        val last_inferred_timestamp = 1604290504275.toLong()

        //stress설문 결과를 받아와서 코루틴이랑 매칭시킴
        val labelData = getLabelFrom(last_trained_timestamp)
        val trainDataMap = mutableMapOf<Long,Array<Array<DoubleArray>>>()
        val timestampArr = labelData.keys.toTypedArray()
        Log.d("trtr.timestampArr",timestampArr.contentDeepToString())

        //지난 설문 timestamp ~ 이번 설문 까지 label을 그 사이의 코루틴 라벨로 지정
        for (idx in 1 until timestampArr.size){
            trainDataMap[timestampArr[idx]] = getDataFromTo(timestampArr[idx-1],timestampArr[idx])
        }

        val arrayDataSet = arrayListOf<DataSet>()
//        for (idx in trainData.indices){
//            val tempTrain = arrayOf(trainData[idx])
//            val tempLabel = arrayOf(labelData[idx])
//            Log.d("tempdataset",tempTrain.contentDeepToString()+"    "+tempLabel.contentDeepToString())
//            val dataSet = DataSet(Nd4j.createFromArray(tempTrain),Nd4j.createFromArray(tempLabel))
//            arrayDataSet.add(dataSet)
//        }

        for (idx in trainDataMap.keys){ //설문데이터 timestamp랑 코루틴이랑 비교,
            for (eachCoroutine in trainDataMap.get(idx)!!.iterator()){
                val trainNd = Nd4j.create(arrayOf(eachCoroutine))
                val labelNd = Nd4j.create(arrayOf(labelData[idx]))
                arrayDataSet.add(DataSet(trainNd,labelNd))
            }
        }

        var count = 0
        if (arrayDataSet.isNotEmpty()){
            count = arrayDataSet.size
            Log.d("twtw.trainingdata", arrayDataSet.size.toString())
            Log.d("twtw.trainingdata",arrayDataSet[0].toString())


            val data_iter = ExistingDataSetIterator(arrayDataSet)

            val nEpochs = 10
            model.setListeners(ScoreIterationListener(10))//Print score every 10 iterations and evaluate on test set every epoch
//        val weights = model
            runModel(model, data_iter, nEpochs)

            val keyGen = KeyPair()
            val n = keyGen.getN()
            val g = keyGen.getG()
            val nSquared = keyGen.getnSquared()
            val lambda = keyGen.getLambda()
            val mu = keyGen.getMu()

            val editor = prefs.edit()

            editor.putString("pref_pk_n", n.toString())
            editor.putString("pref_pk_g", g.toString())
            editor.putString("pref_pk_nSquared", nSquared.toString())
            editor.putString("pref_sk_lambda", lambda.toString())
            editor.putString("pref_sk_mu", mu.toString()).apply()

            sendData(jwt,count, n, g, nSquared)
//        prefs.edit().putLong("last_trained_timestamp",new_last_trained_timestamp).apply()
            prefs.edit().putLong("last_trained_timestamp",new_last_trained_timestamp).apply()
            Log.d("trainingWorker", "working")

            testKeys()

        }else{
            Log.d("trainingworker", "NO DATA")
            testKeys()
        }


        Result.success()
    }

    fun testKeys() {
        val n = BigInteger(prefs.getString("pref_pk_n", null))
        val g = BigInteger(prefs.getString("pref_pk_g", null))
        val lambda = BigInteger(prefs.getString("pref_sk_lambda", null))
        val mu = BigInteger(prefs.getString("pref_sk_mu", null))

        val keypair = KeyPair(n, g, lambda, mu)

        val a = BigInteger.valueOf(10002)
        val a_enc = keypair.encrypt(a)

        val a_dec = keypair.decrypt(a_enc)
        Log.d("tw_prefsTest", a_dec.toString())

    }

    fun sendData(jwt_token:String, count:Int, n:BigInteger, g:BigInteger, nSquared:BigInteger){
        //add to db
        val url = BaseUrl.url + "/model/client/acknowledge"
        val queue = Volley.newRequestQueue(context)

        val stringRequest = object : StringRequest(
            Method.POST,url,
            Response.Listener<String> { response ->
                val jsonObject = JSONObject(response)
//                val tokenId = jsonObject.getString("id")
                Log.d("twVolley:response",response.toString())
            },
            Response.ErrorListener { error ->  Log.d("twVolley:error", error.toString()) }
        ){
            override fun getParams(): MutableMap<String, String>? {
                val params = hashMapOf<String,String>()
                params["user_email"] = prefs.getString("pref_user_email", null).toString()
                params["count"] = count.toString()
                params["pk_n"] = n.toString()
                params["pk_g"] = g.toString()
                params["pk_nSquared"] = nSquared.toString()
                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Authorization"] = "Bearer $jwt_token"
                return params
            }
        }
        queue.add(stringRequest)
    }

    fun runModel(model: MultiLayerNetwork, data_iter:ExistingDataSetIterator,nEpochs:Int) = runBlocking {
        val start_time = System.currentTimeMillis()
        model.fit(data_iter,nEpochs)
        val eval = Evaluation(4)
        data_iter.reset()
        while(data_iter.hasNext()) {
            val curData = data_iter.next()
            val predicted = model.output(curData.features, true)
            Log.d("model_output",predicted.toString())
            eval.eval(curData.labels, predicted)
        }
        val end_time = System.currentTimeMillis()

        Log.d("model_eval", eval.stats())
        Log.d("model_eval.time",(end_time-start_time).toString())
    }

    fun getDataFromTo(last_trained_timestamp:Long,last_inferred_timestamp:Long): Array<Array<DoubleArray>> {
        val dbObject = Room.databaseBuilder(
            applicationContext,
            CoroutineDatabase::class.java, "coroutine"
        ).allowMainThreadQueries().fallbackToDestructiveMigration().build().coroutineDataDao()


        val data = dbObject.getFromTo(last_trained_timestamp,last_inferred_timestamp)
//        val data = dbObject.getAll()
        Log.d("getdata",data.size.toString())
        val realData = ArrayList<Array<DoubleArray>>()

        if (data.isNotEmpty()){
            prefs.edit().putLong("last_trained_timestamp",data.get(data.size-1).timestamp).apply() //update index

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
            arr.add(timestampArr)//마지막 array 저장
            arr.removeAt(0)
            var id = 0

            val nMin = arrayOf(0.0,0.0,0.0,0.0,0.0,0.0)
            val nMax = arrayOf(1.00000000e+00, 2.00000000e+00, 3.00000000e+00, 3.10823229e+00,
                1.40000000e+01, 4.26011840e+07)


            for (eachCoroutine in arr){
                val coroutineData = arrayListOf<DoubleArray>()//5,6
                for (index in eachCoroutine.indices){//5,6, index = 5번
                    val ed = eachCoroutine[index]
                    var cd = doubleArrayOf(ed.ifMoving,ed.orientation,ed.posture,ed.std_posture,ed.category,ed.totalTimeInForeground)

                    //normalize [-1,1]
                    for(idx in cd.indices){
                        val rd = (cd[idx] - nMin[idx]) * 2 / (nMax[idx] - nMin[idx]) - 1
                        cd[idx] = rd
                    }

                    coroutineData.add(cd)
                }

                //reshape (1,6,5) each
                val arr2d = arrayListOf<DoubleArray>()
                for (i in 0 until 6){
                    val temp = arrayListOf<Double>()
                    for (j in 0 until 5){
                        temp.add(coroutineData[j][i])
                    }

                    arr2d.add(temp.toDoubleArray())
                }

                realData.add(arr2d.toTypedArray())

            }
        }
        return realData.toTypedArray()
    }

    fun getLabelFrom(last_trained_timestamp:Long):Map<Long,DoubleArray>{
        val dbObject = Room.databaseBuilder(
            applicationContext,
            StressScoreDatabase::class.java, "stressScore"
        ).allowMainThreadQueries().fallbackToDestructiveMigration().build().stressScoreDataDao()

//        val data = dbObject.getAll()
        val data = dbObject.getFromTo(last_trained_timestamp,System.currentTimeMillis())
        val labelArr = mutableMapOf<Long,DoubleArray>()
        if(data.isNotEmpty()){
            for (each in data){
                val zeroLabel = arrayOf(0.0,0.0,0.0,0.0)
                zeroLabel[each.stressScore] = 1.0
                labelArr[each.timestamp] = zeroLabel.toDoubleArray()
            }
            new_last_trained_timestamp = data.get(data.size-1).timestamp
        }

        return labelArr
    }
}

