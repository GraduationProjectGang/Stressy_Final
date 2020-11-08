package com.android.stressy.etc

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.android.stressy.dataclass.RotateVectorStress
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.LocalDateTime

class RVecWorker(appContext: Context, workerParams: WorkerParameters)
    : CoroutineWorker(appContext, workerParams), SensorEventListener {
    val TAG_LOCATION = "LocationTest"
    val TAG_ROTATE = "rotateVectorTest"
    val TAG_COROUTINE = "coroutineWorkerTest"
    val TAG_USAGE = "usageTest"

    //rotate vector variable
    private lateinit var sensorManager: SensorManager
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private val mutableListOrientationAngles = mutableListOf<String>()

    //firebase reference
    var mTimestamp:Long = 0
    val dateFormat = SimpleDateFormat("yyyyMMdd.HH:mm:ss")



    private fun printCallStack() {
        val sb = StringBuilder()
        sb.append("==================================\n  CALL STACK\n==================================\n");

        val e = Exception();
        val steArr = e.stackTrace;
        for (ste in steArr) {
            sb.append("  ");
            sb.append(ste.className);
            sb.append(".");
            sb.append(ste.methodName);
            sb.append(" #");
            sb.append(ste.lineNumber);
            sb.append("\n");
        }

        Log.d("test", sb.toString());
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result = coroutineScope {
        val iterationRange = 60
        mTimestamp = System.currentTimeMillis()//공통으로 쓰일 timestamp
        val index =  inputData?.getInt("index",0)
        val time =  inputData?.getLong("time",System.currentTimeMillis())

        Log.d("stressRotation","stressRotation")
        //debug
        printCallStack()
        val jobs =
            async {
                for (i in 1 .. iterationRange){
                    //Repeat every 1s
                    delay(1000L)
                    startMeasureRotateVector()
                    Log.d(TAG_COROUTINE, LocalDateTime.now().toString())
                }


                var rVector =
                    RotateVectorStress(
                        mutableListOf(),
                        dateFormat.format(mTimestamp),
                        index,
                        time
                    )
                rVector.angleList = mutableListOrientationAngles


                Log.d("rotationVecStress","pushed!!")

            }
        Result.success()
    }



    private fun startMeasureRotateVector() {

        sensorManager = applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            sensorManager.registerListener(
                this,
                magneticField,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )
        SensorManager.getOrientation(rotationMatrix, orientationAngles)
        //print roll, pitch, yaw
        //note that all three orientation angles are expressed in !!RADIANS!.
        Log.d(TAG_ROTATE, orientationAngles.contentToString())
        mutableListOrientationAngles.add(orientationAngles.contentToString())

    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event == null) return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }
    }
}



