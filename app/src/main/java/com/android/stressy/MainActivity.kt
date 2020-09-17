package com.android.stressy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel
import com.opencsv.CSVReader
import kotlinx.android.synthetic.main.activity_main.*
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.FileReader
import java.io.InputStreamReader
import java.lang.reflect.Type
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : AppCompatActivity() {

    lateinit var interpreter: Interpreter
    lateinit var inputList: ArrayList<String>
    lateinit var graph: Graph

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()

    }

    fun init() {
        val remoteModel = FirebaseCustomRemoteModel.Builder("stressy_model").build()
        val conditions = FirebaseModelDownloadConditions.Builder()
            //.requireWifi()
            .build()
        FirebaseModelManager.getInstance().download(remoteModel, conditions)
            .addOnCompleteListener {
                // Download complete. Depending on your app, you could enable the ML
                // feature, or switch from the local model to the remote model, etc.
                Toast.makeText(this, "Download Complete", Toast.LENGTH_SHORT).show()
                Hello.text = "completed"
            }

        FirebaseModelManager.getInstance().getLatestModelFile(remoteModel)
            .addOnCompleteListener { task ->
                val modelFile = task.result
                if (modelFile != null) {
                    interpreter = Interpreter(modelFile)
                }
            }
    }

}