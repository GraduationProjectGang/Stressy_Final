package com.android.stressy.etc

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.android.stressy.R
import com.android.stressy.dataclass.BaseUrl
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.coroutineScope
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.util.ModelSerializer
import org.json.JSONObject
import org.nd4j.linalg.factory.Nd4j


class ReceiveWeightWorker(appContext: Context, workerParams: WorkerParameters)
    : CoroutineWorker(appContext, workerParams) {
    val context = appContext
    val mPref = "my_pref"
    val prefs = context.getSharedPreferences(mPref,Context.MODE_PRIVATE)
    lateinit var model: MultiLayerNetwork

    override suspend fun doWork(): Result = coroutineScope {
        Log.d("rwrw","received")
        Log.d("SEJIWON", "ReceiveWeightWorker Enqueued")

        val inputStream = context.resources.openRawResource(R.raw.stressy_final_model_nokeras)
        model = ModelSerializer.restoreMultiLayerNetwork(inputStream, false)

        withVolley()

        Result.success()
    }

    fun updateLocalModel(W_0: JSONObject, RW_0: JSONObject, b_0: JSONObject, W_2: JSONObject, b_2: JSONObject) {
        Log.d("SEJIWON", "Local Model Update")

        Log.d("rw_0W", W_0.toString())
        Log.d("rw_0RW", RW_0.toString())
        Log.d("rw_0b", b_0.toString())
        Log.d("rw_2W", W_2.toString())
        Log.d("rw_2b", b_2.toString())

        val value_0W = W_0.getString("data").replace("[", "").replace("]", "").split(",").toTypedArray()
        val value_0RW = RW_0.getString("data").replace("[", "").replace("]", "").split(",").toTypedArray()
        val value_0b = b_0.getString("data").replace("[", "").replace("]", "").split(",").toTypedArray()
        val value_2W = W_2.getString("data").replace("[", "").replace("]", "").split(",").toTypedArray()
        val value_2b = b_2.getString("data").replace("[", "").replace("]", "").split(",").toTypedArray()

        val weights_0W: ArrayList<Double> = ArrayList()
        val weights_0RW: ArrayList<Double> = ArrayList()
        val weights_0b: ArrayList<Double> = ArrayList()
        val weights_2W: ArrayList<Double> = ArrayList()
        val weights_2b: ArrayList<Double> = ArrayList()

        for (v in value_0W) {
            weights_0W.add(v.toDouble())
        }
        for (v in value_0RW) {
            weights_0RW.add(v.toDouble())
        }
        for (v in value_0b) {
            weights_0b.add(v.toDouble())
        }
        for (v in value_2W) {
            weights_2W.add(v.toDouble())
        }
        for (v in value_2b) {
            weights_2b.add(v.toDouble())
        }

        // 6,128      69120         W:{6,512}, RW:{128,512}, b:{1,512}
        //    dense (DenseLayer)         128,4      516           W:{128,4}, b:{1,4}

        val ind_0W = Nd4j.create(weights_0W).reshape(6, 512)
        val ind_0RW = Nd4j.create(weights_0RW).reshape(128, 512)
        val ind_0b = Nd4j.create(weights_0b).reshape(1, 512)
        val ind_2W = Nd4j.create(weights_2W).reshape(128, 4)
        val ind_2b = Nd4j.create(weights_2b).reshape(1, 4)

        model.setParam("0_W", ind_0W)
        model.setParam("0_RW", ind_0RW)
        model.setParam("0_b", ind_0b)
        model.setParam("2_W", ind_2W)
        model.setParam("2_b", ind_2b)
        Log.d("SEJIWON", "2_b $ind_2b")

        // 

    }

    fun withVolley() {

        val url = BaseUrl.url_aggregate + "/receive_weights"
        val queue = Volley.newRequestQueue(context)
        val jsonObject = JSONObject()
        val jsonRequest = object : JsonObjectRequest(
            Method.POST,url,jsonObject,
            Response.Listener<JSONObject> { res ->
                val jsonObject = res
                val W_0 = jsonObject.getJSONObject("finalWeights_0W")
                val RW_0 = jsonObject.getJSONObject("finalWeights_0RW")
                val b_0 = jsonObject.getJSONObject("finalWeights_0b")
                val W_2 = jsonObject.getJSONObject("finalWeights_2W")
                val b_2 = jsonObject.getJSONObject("finalWeights_2b")

                updateLocalModel(W_0, RW_0, b_0, W_2, b_2)

            },
            Response.ErrorListener { error ->
                Log.d("rw.error", error.toString())
            }
        ){

        }
        queue.add(jsonRequest)

    }

}

