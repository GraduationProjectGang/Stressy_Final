package com.android.stressy.etc

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import au.com.bytecode.opencsv.CSVWriter
import com.android.stressy.R
import com.android.stressy.dataclass.BaseUrl
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import kotlinx.coroutines.coroutineScope
import okhttp3.MediaType
import okhttp3.RequestBody
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.util.ModelSerializer
import org.json.JSONObject
import org.nd4j.linalg.api.ndarray.INDArray
import java.io.*
import java.nio.charset.StandardCharsets


class SendWeightWorker(appContext: Context, workerParams: WorkerParameters)
    : CoroutineWorker(appContext, workerParams) {
    val mContext = appContext
    lateinit var paramTable : Map<String,INDArray>

    override suspend fun doWork(): Result = coroutineScope {
        val inputStream = mContext.resources.openRawResource(R.raw.stressy_final_model_nokeras)
        val model = ModelSerializer.restoreMultiLayerNetwork(inputStream, false)

        Log.d("swsw", model.summary())

        val fileArray = getWeight(model)
        paramTable = model.paramTable()
        getFile()


        Result.success()
    }

    fun getFile(){

        Log.d("params",paramTable.keys.toString())

        val jsonObject = JSONObject()

        val paramArr = paramTable.get("0_W")
        Log.d("everykey", paramArr!!.shapeInfoToString())

        val dataBuffer = paramArr.data()

        val jsonString = Gson().toJson(dataBuffer.asDouble())

        jsonObject.put("W_0",jsonString)
        Log.d("params.json", jsonObject.toString())
        withVolley("W_0",jsonObject)


    }

    // 6,128      69120         W:{6,512}, RW:{128,512}, b:{1,512}
    //    dropout (DropoutLayer)     -,-        0             -
    //    dense (DenseLayer)         128,4      516           W:{128,4}, b:{1,4}

    fun withVolley(keyString:String,jsonObject:JSONObject){
        val prefs = mContext.getSharedPreferences("my_pref",Context.MODE_PRIVATE)
        val fcm_token = prefs.getString("pref_fcm_token",null).toString()
        val jwt_token = prefs.getString("jwt",null).toString()
        jsonObject.put("fcm_token",fcm_token)

        var keyUrl = ""
        if (keyString == "W_0") keyUrl = "w0"
//        else if (keyString == "RW_0") keyUrl = "rw0"
//        else if (keyString == "b_0") keyUrl = "b0"
//        else if (keyString == "W_2") keyUrl = "w2"
//        else if (keyString == "b_2") keyUrl = "b2"

            val url = BaseUrl.url_aggregate + "/send_" + keyUrl
            val queue = Volley.newRequestQueue(mContext)
//        jsonObject.put("weight_key",key)

            val jsonRequest = object : JsonObjectRequest(
                Request.Method.POST,url,jsonObject,
                Response.Listener<JSONObject> { res ->
                    Log.d("sw:res", res.toString())
                },
                Response.ErrorListener { error ->
                    Log.d("sw", error.toString())
                }
            ){
                override fun getHeaders(): MutableMap<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["Authorization"] = "Bearer $jwt_token"
                    return params
                }
            }
            queue.add(jsonRequest)

    }



    fun getWeight(model:MultiLayerNetwork): Array<File>{
        paramTable = model.paramTable()
        val fileArr = arrayListOf<File>()
        val keys = paramTable.keys
        val it = keys.iterator()
        var fileIndex= 0
        while (it.hasNext()) {
            val key = it.next()
            Log.d("model_key", key);//print keys

            val values = paramTable[key]
            val file = File(mContext.filesDir.path +"weight_"+fileIndex.toString()+".csv")
            fileArr.add(file)
            FileOutputStream(file).use { fos ->
                OutputStreamWriter(
                    fos,
                    StandardCharsets.UTF_8
                ).use { osw ->
                    CSVWriter(osw).use { writer ->
                        for (i in 0 until values!!.rows()) {
                            val temp = values.getRow(i.toLong()).toDoubleVector().contentToString()
                            writer.writeNext(arrayOf(temp))

                        }
                    }
                }
            }


            fileIndex++
        }
//        sendTable(fileArr.toTypedArray())
        return fileArr.toTypedArray()
    }

//    fun sendTable(fileArr:Array<File>) {
//        for (file in fileArr){
//            doInBackground(file)
//        }
//    }

    fun doInBackground(input:File){
        val fileInputStream = FileInputStream(input)
        val byteArr = ByteArray(input.length().toInt())
        try {
            fileInputStream.read(byteArr)
            fileInputStream.close()

        } catch (e: FileNotFoundException) {
            println("File Not Found.")
            e.printStackTrace()
        } catch (e1: IOException) {
            println("Error Reading The File.")
            e1.printStackTrace()
        }
        Log.d("bytearr",byteArr.toString())

        try {
            val requestBody = RequestBody.create(MediaType.parse("multipart/form-data"),byteArr)
            Log.d("resres","request")

            val service = FileUploadService.create()
            Log.d("resres","servicecreated")

            val response = service.sendFile(requestBody).execute()

            var resStr = response.body()!!.toString()
            Log.d("resres",resStr)

        }catch (e1:Exception) {
            println("Error Reading The File.")
            e1.printStackTrace()
        }


    }
}