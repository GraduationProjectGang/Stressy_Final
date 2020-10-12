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
import com.android.stressy.dataclass.CategoryForJson
import com.android.stressy.dataclass.LocationData
import com.android.stressy.dataclass.RotateVectorData
import com.android.stressy.dataclass.UsageAppData
import com.android.stressy.dataclass.db.CoroutineData
import com.android.stressy.dataclass.db.CoroutineDatabase
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import java.io.BufferedInputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.Comparator

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
    private val rotateVecList = mutableListOf<String>()

    //    lateinit var fbDatabase: FirebaseDatabase
//    lateinit var dbReference: DatabaseReference
    var mTimestamp: Long = 0
    val dateFormat = SimpleDateFormat("yyyyMMdd.HH:mm:ss")
    lateinit var mChannel: NotificationChannel

    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager


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


    override suspend fun doWork(): Result = coroutineScope {
//        readFileLineByLineUsingForEachLine("com/android/stressy/etc/categories.json")
        val progress = "데이터 전송 중"

        setForeground(createForegroundInfo(progress))

        mTimestamp = System.currentTimeMillis()//공통으로 쓰일 timestamp
        val iterationRange = 10

        //debug
        printCallStack()

        val usageStatsData = showAppUsageStats(getAppUsageStats(mTimestamp - 900000))

        initLocationParams()
        startLocationUpdates()

        async {
            for (i in 1..iterationRange) {
                //Repeat every 1s
                delay(1000L)
                startMeasureRotateVector()
                Log.d("dcworker", LocalDateTime.now().toString())
            }
            //stop location request when iteration was ended
            stopLocationUpdates()

            val loc = LocationData(mutableListOf(), dateFormat.format(mTimestamp)) //얘가 파베에서 location임
            loc.locationList = locationList

            val rVector = RotateVectorData(mutableListOf(), dateFormat.format(mTimestamp))
            rVector.angleList = rotateVecList


            saveData(mTimestamp,rotateVecList, usageStatsData, locationList)

        }
        //TODO: add to local db



        Result.success()

    }

    fun saveData(mTimestamp:Long, rVector:MutableList<String>, usageStats:MutableList<UsageAppData>, loc:MutableList<Location>){
        //TODO
        //([item["ifMoving"],item["orientation"],item["posture"],item["std_posture"],temp["category"],temp["totalTimeInForeground"]])

        //loc
        val ifMovingList = mutableListOf<Float>()
        for (item in loc){
            ifMovingList.add(item.speed)
        }

        val speedMax = ifMovingList.max()!!
        Log.d("savedata",speedMax.toString())
        Log.d("savedata",rVector.toString())
        Log.d("savadatasize",rVector.size.toString())

        val ifMoving = if (speedMax > 1) 1 else 0
        Log.d("savadatamov",ifMoving.toString())


        //rVec관련
        val x_list = mutableListOf<Double>()
        val y_list = mutableListOf<Double>()

        for (value in rVector){
            val i = value.split(",","[","]").map { it.trim() }
            Log.d("savedata",i.toString())
            x_list.add(i[1].toDouble()) //x
            y_list.add(i[2].toDouble()) //y
        }

        val posture = getPosture(x_list)
        val std_posture = calculate_std(x_list)
        val orientation = getOrientation(y_list)

        val categorizedList = appToIndex(usageStats)

        //save data
        for (i in 0..4){
            save(
                CoroutineData(
                    mTimestamp,
                    ifMoving,
                    orientation,
                    posture,
                    std_posture,
                    categorizedList[i].category,
                    categorizedList[i].totalTimeInForeground
                )
            )
        }

        val dbObject = Room.databaseBuilder(
            applicationContext,
            CoroutineDatabase::class.java, "coroutine"
        ).fallbackToDestructiveMigration().build().coroutineDataDao()

        val obs = dbObject.getAll()
        for (each in obs)
            Log.d("catedb",each.toString())
    }
//    class Migration1_2: Migration(1,2){
//        override fun migrate(database: SupportSQLiteDatabase) {
//
//        }
//
//    }
    fun save(data: CoroutineData){
        val dbObject = Room.databaseBuilder(
            applicationContext,
            CoroutineDatabase::class.java, "coroutine"
        ).fallbackToDestructiveMigration().build().coroutineDataDao()

        dbObject.insert(data)
    }


    private fun getPosture(x_list: MutableList<Double>): Int {
        val bincount_x = intArrayOf(0,0,0,0)
        val posture_list =  mutableListOf<Int>()

        for (item in x_list){
            val value = posture_x(transfer(item))
            posture_list.add(value)
            bincount_x[value] += 1
        }

        var posture = 0
        for (i in 0..3){
            if (bincount_x[i] > bincount_x[posture])
                posture = i
        }
        return posture
    }


    fun getOrientation(y_list: MutableList<Double>):Int{
        val orientation_list =  mutableListOf<Int>()
        val bincount_y = intArrayOf(0,0,0)

        for (item in y_list){
            val value = posture_y(transfer(item))
            orientation_list.add(value)
            bincount_y[value] += 1
        }

        var orientation = 0
        for (i in 0..2){
            if (bincount_y[i] > bincount_y[orientation])
                orientation = i
        }
        return orientation
    }

    fun calculate_std(list: MutableList<Double>): Double {
        var sum = 0.0
        var std = 0.0
        for (num in list)
            sum += num
        val mean = sum/list.size
        for (num in list){
            std += Math.pow(num-mean,2.0)
        }
        return Math.sqrt(std/list.size-1)
    }

    fun posture_x(degree: Double): Int{
        if (degree > 0 && degree < 90) return 1
        else if (degree >90 && degree < 120) return 2
        else if (degree > 120 && degree < 180) return 3
        else return 0
    }
    fun posture_y(degree: Double): Int{
        if ((degree >240 && degree < 300) || (degree >60 && degree < 120) ) return 2
        else if (degree > 270 || degree > 30 || (degree > 180 && degree < 210)) return 1
        else return 0
    }

    fun transfer(radian:Double):Double{
        val ret = (radian*(180/Math.PI))
        if (ret >= 0) return ret
        else return ret + 360
    }

    fun appToIndex(usageStats: MutableList<UsageAppData>): ArrayList<StatsForArray>{
        val CAMERA_STRING = "Photography"
        val UTILITY_ARRAY = listOf("Productivity", "Beauty", "Weather", "News & Magazines", "Dating", "Tools", "Utility")
        val SNS_STRING = "Social"
        val ENTERTAINMENT_ARRAY = listOf("Entertainment", "Books & Reference", "Music & Audio", "House & Home", "Sports", "Video Players & Editors", "Travel & Local", "Lifestyle", "Comics")
        val COMMUNICATION_STRING = "Communication"
        val GAME_ARRAY = listOf("Action", "Racing", "Adventure", "Arcade", "Puzzle", "Simulation", "Strategy", "Role Playing", "Auto & Vehicles", "Casual", "Card", "Music")
        val SYSTEM_STRING = "Personalization"
        val EDUCATION_ARRAY = listOf("Education", "Business")
        val SHOPPING_STRING = "Shopping"
        val MAPS_VEHICLE_ARRAY = listOf("Maps & Navigation", "Auto & Vehicles")
        val HEALTH_STRING = "Health & Fitness"
        val FOOD_STRING = "Food & Drink"
        val FINANCE_STRING = "Finance"
        val BROWSER_STRING = "Browser"

        val inputStream = applicationContext.getResources().openRawResource(R.raw.category_labels2)
        val bufferedReader = BufferedInputStream(inputStream)
        var line = ""
//        val size = inputStream.available()
//        Log.d("fileread",size.toString())
//        val buffer = ByteArray(size)
//        inputStream.read(buffer)
//        inputStream.close()
        val inputAsString = inputStream.bufferedReader().use {
            it.readText()
        }
        Log.d("categ",inputAsString)

        val gson = Gson()
        val listAppType = object : TypeToken<List<CategoryForJson>>() {}.type

        val categoryJson: List<CategoryForJson> = gson.fromJson(inputAsString, listAppType)
        Log.d("categorizing",categoryJson.toString())
        Log.d("categorizing",categoryJson.size.toString())
        val categorizedList = arrayListOf<StatsForArray>()
        Log.d("cate usage",usageStats.toString())
        var idx = 0

        for(i in 0 until usageStats.size){
            val thisApp = usageStats[i]

            for (eachApp in categoryJson){
                if (thisApp.packageName == eachApp.packageName){
                    Log.d("cateAppName",idx.toString()+thisApp.packageName)
                    val temp = eachApp.category
                    val time = thisApp.totalTimeInForeground
                    if (temp in CAMERA_STRING) categorizedList.add(StatsForArray(1,time))
                    else if(temp in UTILITY_ARRAY) categorizedList.add(StatsForArray(2,time))
                    else if(temp in SNS_STRING) categorizedList.add(StatsForArray(3,time))
                    else if(temp in ENTERTAINMENT_ARRAY) categorizedList.add(StatsForArray(4,time))
                    else if(temp in COMMUNICATION_STRING) categorizedList.add(StatsForArray(5,time))
                    else if(temp in GAME_ARRAY) categorizedList.add(StatsForArray(6,time))
                    else if(temp in SYSTEM_STRING) categorizedList.add(StatsForArray(7,time))
                    else if(temp in EDUCATION_ARRAY) categorizedList.add(StatsForArray(8,time))
                    else if(temp in SHOPPING_STRING) categorizedList.add(StatsForArray(9,time))
                    else if(temp in MAPS_VEHICLE_ARRAY) categorizedList.add(StatsForArray(10,time))
                    else if(temp in HEALTH_STRING) categorizedList.add(StatsForArray(11,time))
                    else if(temp in FOOD_STRING) categorizedList.add(StatsForArray(12,time))
                    else if(temp in FINANCE_STRING) categorizedList.add(StatsForArray(13,time))
                    else if(temp in BROWSER_STRING) categorizedList.add(StatsForArray(14,time))
                    else{
                        Log.d("noncate",temp)
                        continue
                    }
                    idx += 1
                }
            }
            for(each in categorizedList)
                Log.d("catelist",each.toString())

            if (idx >= 5) break
        }
        if (idx < 5){ //5개보다 적다면 0,0으로 채워주기
            for (i in idx until 5){
                categorizedList.add(StatsForArray(0,0))
                idx += 1
            }
        }

        if (idx < 5)
            Log.d("catesize","idx is not 5")

        //시계열로 순서 바꾸기
        var temp = categorizedList[0]
        categorizedList[0] = categorizedList[4]
        categorizedList[4] = temp
        temp = categorizedList[1]
        categorizedList[1] = categorizedList[3]
        categorizedList[3] = temp
        for(each in categorizedList)
            Log.d("catelist",each.category.toString()+each.totalTimeInForeground.toString())
        return categorizedList
    }
    class StatsForArray(val category:Int, val totalTimeInForeground:Long ){

    }
    fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
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



    fun initLocationParams() {

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
        cal.add(Calendar.MINUTE, -15)//1분간의 stats 파악
        Log.d("dcworker",cal.toString())

        val usageStatsManager = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val queryUsageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST,time, mTimestamp
        )
        Log.d("dcworker", queryUsageStats.size.toString())
        return queryUsageStats
    }

    fun showAppUsageStats(usageStats: MutableList<UsageStats>) : MutableList<UsageAppData> {

        val dateFormat = SimpleDateFormat("yyyyMMdd.HH:mm:ss")
        Log.d("dcworker", usageStats.size.toString())
        usageStats.sortWith(Comparator { right, left ->
            compareValues(left.lastTimeUsed, right.lastTimeUsed)
        })
        var statsArr = mutableListOf<UsageAppData>()

        usageStats.forEach {
            if(it.totalTimeInForeground>0 && it.lastTimeUsed>mTimestamp-900000){
                statsArr.add(UsageAppData(it.packageName, dateFormat.format(it.lastTimeUsed), it.totalTimeInForeground))
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
        rotateVecList.add(orientationAngles.contentToString())

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