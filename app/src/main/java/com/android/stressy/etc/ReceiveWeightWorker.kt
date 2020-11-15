package com.android.stressy.etc

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.android.stressy.dataclass.BaseUrl
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.coroutineScope
import org.json.JSONObject


class ReceiveWeightWorker(appContext: Context, workerParams: WorkerParameters)
    : CoroutineWorker(appContext, workerParams) {
    val context = appContext
    val mPref = "my_pref"
    val prefs = context.getSharedPreferences(mPref,Context.MODE_PRIVATE)

    override suspend fun doWork(): Result = coroutineScope {
        Log.d("rwrw","received")

        withVolley()

        Result.success()
    }

    fun updateLocalModel(W_0: JSONObject, RW_0: JSONObject, b_0: JSONObject, W_2: JSONObject, b_2: JSONObject) {

        Log.d("rw_0W", W_0.toString())
        Log.d("rw_0RW", RW_0.toString())
        Log.d("rw_0b", b_0.toString())
        Log.d("rw_2W", W_2.toString())
        Log.d("rw_2b", b_2.toString())

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

