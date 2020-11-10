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
import com.android.stressy.dataclass.db.StressPredictedDatabase
import com.android.stressy.paillier.KeyPairBuilder
import com.android.stressy.paillier.PublicKey
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


class TrainingWorker(appContext: Context, workerParams: WorkerParameters)
    : CoroutineWorker(appContext, workerParams) {
    val context = appContext
    val dataTimestamp = mutableListOf<Long>()
    val resultArray = mutableListOf<Int>()
    val mPref = "my_pref"
    val prefs = context.getSharedPreferences(mPref,Context.MODE_PRIVATE)

    override suspend fun doWork(): Result = coroutineScope {
        val inputStream = context.resources.openRawResource(R.raw.stressy_final_model_nokeras)
        val model = ModelSerializer.restoreMultiLayerNetwork(inputStream, false)

//        val last_trained_timestamp = prefs.getLong("last_trained_timestamp",0)
//        val last_inferred_timestamp = prefs.getLong("last_inferred_timestamp",Calendar.getInstance().timeInMillis)
        val last_trained_timestamp = 1604231101275.toLong()
        val last_inferred_timestamp = 1604290504275.toLong()

        val trainData = getDataFromTo(last_trained_timestamp,last_inferred_timestamp)
        val labelData = getLabelFromTo(last_trained_timestamp,last_inferred_timestamp)
        val arrayDataSet = arrayListOf<DataSet>()
        for (idx in trainData.indices){
            val tempTrain = arrayOf(trainData[idx])
            val tempLabel = arrayOf(labelData[idx])
            Log.d("tempdataset",tempTrain.contentDeepToString()+"    "+tempLabel.contentDeepToString())
            val dataSet = DataSet(Nd4j.createFromArray(tempTrain),Nd4j.createFromArray(tempLabel))
            arrayDataSet.add(dataSet)
        }

//        Log.d("twtw.trainingdata",trainData.shapeInfoToString())
//        Log.d("twtw.labeldata",labelData.shapeInfoToString())


        val data_iter = ExistingDataSetIterator(arrayDataSet)

        val nEpochs = 1
        model.setListeners(ScoreIterationListener(10))//Print score every 10 iterations and evaluate on test set every epoch
//        val weights = model
        runModel(model,data_iter,nEpochs)

        val pk = generateKey() // PK를 JSON에 실어서 보내면 됨
        val n = pk.getN()
        val g = pk.getG()
        val nSquared = pk.getnSquared()

        sendData(n,g,nSquared)


        Log.d("trainingWorker", "working")
        Result.success()
    }

    fun sendData(n:BigInteger, g:BigInteger, nSquared:BigInteger){
        //add to db
        val url = BaseUrl.url + "/model/client/acknowledge"
        val queue = Volley.newRequestQueue(context)
        val stringRequest = object : StringRequest(
            Method.POST,url,
            Response.Listener<String> { response ->
                val jsonObject = JSONObject(response)
                val tokenId = jsonObject.getString("id")
                Log.d("twVolley:response",response.toString())
            },
            Response.ErrorListener { error ->  Log.d("twVolley:error", error.toString()) }
        ){
            override fun getParams(): MutableMap<String, String>? {
                val params = hashMapOf<String,String>()
                params["pk_n"] = n.toString()
                params["pk_g"] = g.toString()
                params["pk_nSquared"] = nSquared.toString()
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
            eval.eval(curData.labels, predicted)
        }
        val end_time = System.currentTimeMillis()

        Log.d("model_eval", eval.stats())
        Log.d("model_eval.time",(end_time-start_time).toString())
    }

    fun generateKey() : PublicKey {

        val keygen = KeyPairBuilder()
        val keyPair = keygen.generateKeyPair()

        val publicKey = keyPair.getPublicKey()
        val privateKey = keyPair.getPrivateKey()

        // public str 이거를 JSON에 넣어주면 됨
        val lambda = privateKey.getLambda()
        val mu = privateKey.getPreCalculatedDenominator()

        val prefs = applicationContext.getSharedPreferences("pref", Context.MODE_PRIVATE)
        prefs.edit().putString("prefs_sk_lambda", lambda.toString()).apply()
        prefs.edit().putString("prefs_sk_mu", mu.toString()).apply()

        return publicKey
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
                    Log.d("trtrnormnotyet",cd.contentToString())

                    //normalize [-1,1]
                    for(idx in cd.indices){
                        val rd = (cd[idx] - nMin[idx]) * 2 / (nMax[idx] - nMin[idx]) - 1
                        cd[idx] = rd
                    }
                    Log.d("trtrnormedd",cd.contentToString())

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
    fun getLabelFromTo(last_trained_timestamp:Long,last_inferred_timestamp:Long):Array<DoubleArray>{
        val dbObject = Room.databaseBuilder(
            applicationContext,
            StressPredictedDatabase::class.java, "stressPredicted"
        ).allowMainThreadQueries().fallbackToDestructiveMigration().build().stressPredictedDao()

//        val data = dbObject.getAll()
        val data = dbObject.getFromTo(last_trained_timestamp,last_inferred_timestamp)
        val labelArr = arrayListOf<DoubleArray>()
        for (each in data){
            val zeroLabel = arrayOf(0.0,0.0,0.0,0.0)
            zeroLabel[each.stressPredicted] = 1.0
            labelArr.add(zeroLabel.toDoubleArray())
        }
        return labelArr.toTypedArray()
    }
}

