package com.android.stressy.etc

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import au.com.bytecode.opencsv.CSVWriter
import com.android.stressy.R
import com.android.stressy.dataclass.BaseUrl
import com.android.stressy.paillier.KeyPair
import com.android.stressy.paillier.PrivateKey
import com.android.stressy.paillier.PublicKey
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
import java.math.BigInteger
import java.nio.charset.StandardCharsets


class SendWeightWorker(appContext: Context, workerParams: WorkerParameters)
    : CoroutineWorker(appContext, workerParams) {
    val mContext = appContext
    lateinit var paramTable : Map<String,INDArray>

    val mPref = "my_pref"
    val prefs = mContext.getSharedPreferences(mPref,Context.MODE_PRIVATE)

    override suspend fun doWork(): Result = coroutineScope {
        val inputStream = mContext.resources.openRawResource(R.raw.stressy_final_model_nokeras)
        val model = ModelSerializer.restoreMultiLayerNetwork(inputStream, false)

        val whole_body = inputData.getString("body")!!
        Log.d("sw_getBody", whole_body)

        val bodyToJson = JSONObject(whole_body)

        val maskTableStr = bodyToJson.get("maskTable").toString()
        val index = bodyToJson.get("index").toString()
        val ratio = bodyToJson.get("ratio").toString().toDouble()
        val party = bodyToJson.get("partyId").toString()

        val rows = maskTableStr.split("],")

        var maskTable: Array<DoubleArray> = Array(rows.size) { DoubleArray(rows.size) { 1.0 } }

        val partyThreshold = rows.size

        for (i in 0..rows.size) {
            for (j in 0..rows.size) {
                Log.d("sw_maskTable", maskTable[i][j].toString())
            }
        }

        for (r in 0..rows.size) {
            val dValues = rows[r].replace("[", "").replace("]", "").split(",")
            Log.d("sw_dValueSize", dValues.size.toString())
            for (d in 0..dValues.size) {
                maskTable[r][d] = dValues[d].trim().toDouble()
            }
        }

        for (i in 0..rows.size) {
            for (j in 0..rows.size) {
                Log.d("sw_maskTable", maskTable[i][j].toString())
            }
        }

        val splitABC = index.split(",")
        val A = BigInteger(splitABC[0])
        val B = BigInteger(splitABC[1])
        val C = BigInteger(splitABC[2])

        val pk_n = BigInteger(prefs.getString("pref_pk_n", null)!!)
        val pk_g = BigInteger(prefs.getString("pref_pk_g", null)!!)
        val pk_nSquared = BigInteger(prefs.getString("pref_pk_nSquared", null)!!)
        val sk_lambda = BigInteger(prefs.getString("pref_pk_n", null)!!)
        val sk_mu = BigInteger(prefs.getString("pref_pk_n", null)!!)

        val pk = PublicKey(pk_n, pk_g, pk_nSquared, 1024)
        val sk = PrivateKey(sk_lambda, sk_mu)

        val keypair = KeyPair(sk, pk, null)

        val realA = keypair.decrypt(A).toInt()
        val realB = keypair.decrypt(B).toInt()
        val realC = keypair.decrypt(C).toInt()

        val myIdx: Int = (realC - realB) / realA - 1

        paramTable = model.paramTable()
        getFile(maskTable, partyThreshold, myIdx, ratio, party)

        Result.success()
    }

    fun getFile(maskTable: Array<DoubleArray>, partySize: Int, myIdx: Int, ratio: Double, party: String) {

        Log.d("params",paramTable.keys.toString())

        val jsonObject = JSONObject()

        val paramArr = paramTable["0_W"]
        val ratioMulArr = paramArr!!.mul(ratio)
        var maskSum = 0.0

        for (i in 0..partySize) {
            if (i < myIdx) {
                maskSum += maskTable[i][myIdx]
            }
            else if (i > myIdx) {
                maskSum -= maskTable[i][myIdx]
            }
        }
        Log.d("sw_maskSum", maskSum.toString())

        val resultArr = ratioMulArr.add(maskSum)

        val dataBuffer = resultArr.data()
        val jsonString = Gson().toJson(dataBuffer.asDouble())

        jsonObject.put("W_0",jsonString)
        jsonObject.put("partyId",party)
        Log.d("params.json", jsonObject.toString())
        withVolley("W_0", jsonObject)

    }

    // 6,128      69120         W:{6,512}, RW:{128,512}, b:{1,512}
    //    dropout (DropoutLayer)     -,-        0             -
    //    dense (DenseLayer)         128,4      516           W:{128,4}, b:{1,4}

    fun withVolley(keyString:String,jsonObject:JSONObject) {
        val prefs = mContext.getSharedPreferences("my_pref",Context.MODE_PRIVATE)
        val fcm_token = prefs.getString("pref_fcm_token",null).toString()
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
                val jsonObject = res
//                    val bodyStr = jsonObject.getJSONObject("body")
//                    Log.d("encryption_json", bodyStr.toString())

                Log.d("sw:res", res.toString())
            },
            Response.ErrorListener { error ->
                Log.d("sw", error.toString())
            }
        ){

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