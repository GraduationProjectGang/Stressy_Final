package com.android.stressy.etc

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import au.com.bytecode.opencsv.CSVWriter
import com.android.stressy.R
import kotlinx.coroutines.coroutineScope
import okhttp3.MediaType
import okhttp3.RequestBody
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.util.ModelSerializer
import org.json.JSONObject
import java.io.*
import java.nio.charset.StandardCharsets


class SendWeightWorker(appContext: Context, workerParams: WorkerParameters)
    : CoroutineWorker(appContext, workerParams) {
    val context = appContext
    val dataTimestamp = mutableListOf<Long>()
    val resultArray = mutableListOf<Int>()
    val mPref = "my_pref"
    val prefs = context.getSharedPreferences(mPref, Context.MODE_PRIVATE)

    override suspend fun doWork(): Result = coroutineScope {
        val inputStream = context.resources.openRawResource(R.raw.stressy_final_model_nokeras)
        val model = ModelSerializer.restoreMultiLayerNetwork(inputStream, false)

        Log.d("swsw", model.summary())

        val fileArray = getWeight(model)
        sendTable(fileArray)


        Result.success()
    }

    fun getWeight(model:MultiLayerNetwork): Array<File>{
        val paramTable = model.paramTable()
        val fileArr = arrayListOf<File>()
        val keys = paramTable.keys
        val it = keys.iterator()
        val jsonParam = JSONObject()
        var fileIndex= 0
        while (it.hasNext()) {
            val key = it.next()
            Log.d("model_key", key);//print keys

            val values = paramTable.get(key)!!
            val file = File(context.filesDir.path +"weight_"+fileIndex.toString()+".csv")
            fileArr.add(file)
            FileOutputStream(file).use { fos ->
                OutputStreamWriter(
                    fos,
                    StandardCharsets.UTF_8
                ).use { osw ->
                    CSVWriter(osw).use { writer ->
                        for (i in 0 until values.rows()) {
                            val temp = values.getRow(i.toLong()).toDoubleVector().contentToString()
                            writer.writeNext(arrayOf(temp))

                        }
                    }
                }
            }


            fileIndex++
        }
        return fileArr.toTypedArray()
    }

    fun sendTable(fileArr:Array<File>) {
        for (file in fileArr){
            doInBackground(file)
        }
    }

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