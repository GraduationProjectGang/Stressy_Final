package com.android.stressy.etc

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.android.stressy.R
import com.android.stressy.activity.u_key
import com.android.stressy.dataclass.*
import com.google.android.gms.location.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class DataCollectWorker(appContext: Context, workerParams: WorkerParameters)
    : CoroutineWorker(appContext, workerParams), SensorEventListener {
    //reference doc link
    //https://developer.android.com/topic/libraries/architecture/workmanager/advanced/coroutineworker
    //https://developer.android.com/training/location/request-updates

    val TAG_LOCATION = "LocationTest"
    val TAG_ROTATE = "rotateVectorTest"
    val TAG_COROUTINE = "coroutineWorkerTest"
    val TAG_USAGE = "usageTest"
    val userKey = u_key

    //location variable
    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationList: MutableList<Location>
    private lateinit var locationCallback: LocationCallback

    //rotate vector variable
    private lateinit var sensorManager: SensorManager
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private val mutableListOrientationAngles = mutableListOf<String>()

    //    lateinit var fbDatabase: FirebaseDatabase
//    lateinit var dbReference: DatabaseReference
    var mTimestamp: Long = 0
    val dateFormat = SimpleDateFormat("yyyyMMdd.HH:mm:ss")
    lateinit var mChannel: NotificationChannel

    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager

    companion object

    var flag = false

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

        val progress = "데이터 전송 중"
        setForeground(createForegroundInfo(progress))

        mTimestamp = System.currentTimeMillis()//공통으로 쓰일 timestamp
        val iterationRange = 60

        //debug
        printCallStack()

        var stats = showAppUsageStats(getAppUsageStats(mTimestamp - 900000))

        initLocationParms()
        startLocationUpdates()

        val jobs =
            async {
                for (i in 1..iterationRange) {
                    //Repeat every 1s
                    delay(1000L)
                    startMeasureRotateVector()
                    Log.d("dcworker", LocalDateTime.now().toString())
                }
                //stop location request when iteration was ended
                stopLocationUpdates()

                var loc = LocationData(mutableListOf(), dateFormat.format(mTimestamp))
                loc.locationList = locationList
                var usage = UsageStatsCollection(
                    ArrayList(),
                    "coroutine",
                    mTimestamp,
                    dateFormat.format(mTimestamp)
                )
                usage.statsList = stats
                var rVector = RotateVectorData(mutableListOf(), dateFormat.format(mTimestamp))
                rVector.angleList = mutableListOrientationAngles


                val coroutineData = CoroutineData(mTimestamp, rVector, usage, loc)
                val dbObject = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java, "coroutineDB"
                ).build().coroutineDataDao()

                dbObject.insert(coroutineData)
            }
        Result.success()

    }

    fun dataMatch(mTimestamp:Long, rVector:RotateVectorData, usage:ArrayList<UsageStat>, loc: com.android.stressy.dataclass.LocationData):String{
        


        return ""
    }


    private fun createForegroundInfo(progress:String):ForegroundInfo{
        val CHANNEL_ID = "$applicationContext.packageName-${R.string.app_name}"
        val title = "사용자 데이터 수집"
        // This PendingIntent can be used to cancel the worker

        Log.d("dcworker","foreground")
        // Create a Notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(CHANNEL_ID, title, NotificationManager.IMPORTANCE_LOW)
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(progress)
            .setSmallIcon(R.drawable.empty_swipe)
            .setOngoing(true)
            .build()
        Log.d("dcworker","foreground build")


        return ForegroundInfo(100,notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createChannel(CHANNEL_ID:String, name:String, importance:Int) {
        mChannel = NotificationChannel(CHANNEL_ID, name, importance)
        mChannel.description = "데이터 전송 중"
        notificationManager.createNotificationChannel(mChannel)
        Log.d("dcworker","channel created")
    }



    fun initLocationParms() {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        locationList = mutableListOf()
        locationRequest = LocationRequest.create().apply {
            interval = 20 * 1000
            fastestInterval = 5 * 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    locationList.add(location)
                    Log.d(TAG_LOCATION,"(${location.latitude}, ${location.longitude})")
                }
            }
        }
    }

    fun getAppUsageStats(time:Long): MutableList<UsageStats> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MINUTE, -1)//1분간의 stats 파악
        Log.d("dcworker",cal.toString())

        val usageStatsManager = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val queryUsageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST,time, mTimestamp
        )
        Log.d("dcworker", queryUsageStats.size.toString())
        return queryUsageStats
    }

    fun showAppUsageStats(usageStats: MutableList<UsageStats>) : ArrayList<UsageStat> {

        val dateFormat = SimpleDateFormat("yyyyMMdd.HH:mm:ss")
        Log.d("dcworker", usageStats.size.toString())
        usageStats.sortWith(Comparator { right, left ->
            compareValues(left.lastTimeUsed, right.lastTimeUsed)
        })
        var statsArr = ArrayList<UsageStat>()

        usageStats.forEach {
            if(it.totalTimeInForeground>0 && it.lastTimeUsed>mTimestamp-900000){
                statsArr.add(UsageStat(it.packageName, dateFormat.format(it.lastTimeUsed), it.totalTimeInForeground))
                Log.d("appusing",statsArr.last().toString())
            }
        }
        Log.d("dcworker","statsArrLen: ${statsArr.size}")

        return statsArr
    }

    private fun startMeasureRotateVector(){

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

    private fun getLocation() {
        var ret = mutableMapOf<String, Double>()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG_LOCATION, "permission get failed")
            return
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper())
    }
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
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