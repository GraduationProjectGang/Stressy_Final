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

//            withVolley()

            Result.success()
        }

    fun withVolley(jsonObject: JSONObject){
        val prefs = context.getSharedPreferences("my_pref",Context.MODE_PRIVATE)
        val fcm_token = prefs.getString("pref_fcm_token",null).toString()
        val jwt_token = prefs.getString("jwt",null).toString()
        jsonObject.put("fcm_token",fcm_token)

        val url = BaseUrl.url_aggregate + "/receive_weights"
        val queue = Volley.newRequestQueue(context)

        val jsonRequest = object : JsonObjectRequest(
            Method.POST,url,jsonObject,
            Response.Listener<JSONObject> { res ->
                val jsonObject = res

                Log.d("rw:res", res.toString())
            },
            Response.ErrorListener { error ->
                Log.d("rw.error", error.toString())
            }
        ){

        }
        queue.add(jsonRequest)

    }

}

