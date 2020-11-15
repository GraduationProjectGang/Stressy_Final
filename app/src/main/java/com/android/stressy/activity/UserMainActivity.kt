package com.android.stressy.activity

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.AppOpsManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.room.Room
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.android.stressy.R
import com.android.stressy.dataclass.BaseUrl
import com.android.stressy.dataclass.db.StressPredictedDatabase
import com.android.stressy.etc.*
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_user_main.*
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class UserMainActivity() : AppCompatActivity(), PopupMenu.OnMenuItemClickListener {
    val MULTIPLE_REQUEST = 1234
    val mPref = "my_pref"
    lateinit var user_email:String
    lateinit var user_pw:String

    val graphFragment = MainTimeStressGraphFragment()
    val graphFragment2 = MainStressGraphFragment()
    val graphFragment3 = MainHighAppGraphFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_main)

        var toolbar = getSupportActionBar()?.apply {
            setDisplayShowCustomEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
        }
        getFcmToken()
        getRequestCode()
        init()
    }

    fun init() = runBlocking {
        val prefs = getSharedPreferences(mPref,Context.MODE_PRIVATE)

        user_email = intent.getStringExtra("user_email").toString()
        user_pw = intent.getStringExtra("user_pw").toString()
        checkPermission()
        addWhiteList()
        initButtonAndText()
        setAlarm()


        setFragments(getGraphData(),graphFragment,graphFragment2,graphFragment3)




        Log.w(
            "UMA_worker",
            prefs.getBoolean(getString(R.string.pref_previously_started), false).toString())

        if (!prefs.getBoolean(getString(R.string.pref_previously_started), false)) {
            var edit = prefs.edit() as SharedPreferences.Editor
            edit.putBoolean(getString(R.string.pref_previously_started), true)
            edit.commit()
        }
    }

    fun getRequestCode(){
        if (intent.extras != null){ //알림타고 들어온거면
            val notificationCode = intent.extras!!.getInt("notificationCode")
            if (notificationCode == 111)
                startStressCollectDialog(notificationCode)
        }
    }

    /////////TEMP//////////

    fun getFcmToken():String{
        var token = ""
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("logman", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                token = task.result?.token.toString()
                val prefs = getSharedPreferences(mPref,Context.MODE_PRIVATE)
                Log.d("logman:token",token)
                Log.d("logman:original token",prefs.getString("pref_fcm_token",getString(R.string.pref_fcm_token)).toString())

                if (prefs.getString("pref_fcm_token",getString(R.string.pref_fcm_token)) != token) {
                    Log.d("logman", "new token")
                    //add to db
                    val url = BaseUrl.url + "/user/fcm/newtoken"
                    val queue = Volley.newRequestQueue(this)
                    val stringRequest = object : StringRequest(
                        Method.POST,url,
                        Response.Listener<String> { response ->
                            val jsonObject = JSONObject(response)
                            val tokenId = jsonObject.getString("id")
                            Log.d("su4:gettoken",response.toString())
                        },
                        Response.ErrorListener { error ->  Log.d("logman:error", error.toString()) }
                    ){
                        override fun getParams(): MutableMap<String, String>? {
                            val params = hashMapOf<String,String>()
                            params["user_email"] = "TEMP"
                            params["fcm_token"] = token
                            return params
                        }
                    }
                    queue.add(stringRequest)

                    //add to sharedpreference
                    prefs.edit().putString("pref_fcm_token", token).apply()
                }
            })
        return token
    }

    private fun setAlarm(){
        val stressCollectRequest = 111

        Log.d("setalarm","onusermain")
        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()+6000
//            set(Calendar.HOUR_OF_DAY, 1)
        }

        Log.d("setalarm",calendar.toString())
        Log.d("setalarm",(calendar.timeInMillis-System.currentTimeMillis()).toString())

        val alarmIntent = Intent(this, StressCollectAlarmReceiver::class.java)
        alarmIntent.putExtra("notificationCode",stressCollectRequest)
        val alarmUp = PendingIntent.getBroadcast(this, stressCollectRequest,alarmIntent,
            PendingIntent.FLAG_NO_CREATE) != null

        if (alarmUp)
            Log.d("setalarm","alarm is already active")
        else{
            Log.d("setalarm","alarm setting")
            val pendingIntent = PendingIntent.getBroadcast(this, stressCollectRequest, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT )
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis, pendingIntent
            )
        }
    }

    private fun checkPermission() {
        var permissionArr = arrayOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION

        )
        val rejectedPermissionList = ArrayList<String>()

        for (permission in permissionArr) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                rejectedPermissionList.add(permission)
            }
        }
        if(rejectedPermissionList.isNotEmpty()) {
            val array = arrayOfNulls<String>(rejectedPermissionList.size)
            ActivityCompat.requestPermissions(this, rejectedPermissionList.toArray(array), MULTIPLE_REQUEST)
        }

        //앱 사용기록 Permission Check
        if (!ifStatsPermitted()){
            val fragmentManager = supportFragmentManager
            val statsPermissionFragment = StatsPermissionFragment()

            statsPermissionFragment.show(fragmentManager,"permission")
        }
    }

    fun ifStatsPermitted(): Boolean{
        var granted = false
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,android.os.Process.myUid(), getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
            granted = (checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED)
            Log.d("frafraif",granted.toString() + "1")
        } else {
            granted = (mode == AppOpsManager.MODE_ALLOWED)
            Log.d("frafraif",granted.toString() + "2")
        }
        return granted
    }

    fun addWhiteList() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        var isWhite = false
        isWhite = pm.isIgnoringBatteryOptimizations(packageName)

        if (!isWhite) {
            val setdialog = AlertDialog.Builder(this)
            setdialog.setTitle("추가 설정이 필요합니다.")
                .setMessage("어플을 문제없이 사용하기 위해서는 해당 어플을 \"배터리 사용량 최적화\" 목록에서 \"제외\"해야 합니다. 설정화면으로 이동하시겠습니까?")
                .setPositiveButton("네") { _, _ -> startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) }
                .setNegativeButton("아니오") { _, _ -> Toast.makeText(this, "설정을 취소했습니다.", Toast.LENGTH_SHORT).show() }
                .create()
                .show()
        }
    }

    private fun getStatsPermission() {
        // 권한이 없을 경우 권한 요구 페이지 이동
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MULTIPLE_REQUEST -> {

            }
        }
    }
    fun showPopup(v: View?) {
        PopupMenu(this, v).apply {
            // MainActivity implements OnMenuItemClickListener
            setOnMenuItemClickListener(this@UserMainActivity)
            inflate(R.menu.menu)
            show()
        }
    }

//    fun runInference() = runBlocking{
//        Log.d("trtr", "fcmMes: training worker Created")
//        val constraints = Constraints.Builder()
//            .setRequiresCharging(false)
//            .build()
//        val collectRequest =
//            OneTimeWorkRequestBuilder<InferenceWorker>()
//                .setConstraints(constraints)
//                .addTag("training")
//                .build()
//
//        val workManager = WorkManager.getInstance(applicationContext)
//        workManager?.let {
//            it.enqueue(collectRequest)
//        }
//    }
    private fun getGraphData(): DoubleArray = runBlocking{
        val dbObject = Room.databaseBuilder(
            applicationContext,
            StressPredictedDatabase::class.java, "stressPredicted"
        ).fallbackToDestructiveMigration().allowMainThreadQueries().build().stressPredictedDao()


//        for (i in 0 until 10){
//            val timestamp_rand = (1603173028..1603605028).random().toLong()
//            val predictedData_rand = (2..4).random()
//            dbObject.insert(PredictedStressData(timestamp_rand,predictedData_rand))
//        }

        val timeStampArr = arrayListOf<Long>()
        val midNightCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY,0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        var zero = 0.0
        var one = 0.0
        var two = 0.0
        var three = 0.0

        //지난 자정부터 데이터 받아오기
        val getData = dbObject.getFrom(midNightCal.timeInMillis)
        Log.d("mainfrag",getData.size.toString())
        if (getData.isNotEmpty()){
            for (data in getData){
                val predicted = data.stressPredicted
                if (predicted == 0) zero++
                else if (predicted == 1) one++
                else if (predicted == 2) two++
                else if (predicted == 3) three++
            }
        }
        var dataArr = doubleArrayOf(zero, one, two, three)
        var avg = 0.0
        var size = 0
        if (getData.isNotEmpty())
            size = getData.size
        for (scoreCount in dataArr.indices){
            avg += scoreCount*dataArr[scoreCount]
        }
        avg /= size
        setImageAndDescription(avg)
        Log.d("mainfrag.dataarr",dataArr.contentToString())

        return@runBlocking dataArr
    }
    fun setFragments(dataArr: DoubleArray, graphFragment: Fragment, graphFragment2: Fragment, graphFragment3: Fragment ){
        runInferenceWorker()
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        val bundle = Bundle()
        bundle.putDoubleArray("data",dataArr)
        graphFragment2.arguments = bundle
        fragmentTransaction.add(R.id.mainTimeGraph, graphFragment)
        fragmentTransaction.add(R.id.mainStressGraph, graphFragment2)
        fragmentTransaction.add(R.id.mainHighAppGraph, graphFragment3).commit()
    }

    fun refreshFragments(dataArr: DoubleArray, graphFragment: Fragment, graphFragment2: Fragment, graphFragment3: Fragment ){
        runInferenceWorker()

        val fragmentTransaction = supportFragmentManager.beginTransaction()

        fragmentTransaction.detach(graphFragment)
        fragmentTransaction.attach(graphFragment)

        fragmentTransaction.detach(graphFragment2)
        val newBundle = Bundle()
        newBundle.putDoubleArray("data",dataArr)
        graphFragment2.arguments = newBundle
        fragmentTransaction.attach(graphFragment2)

        fragmentTransaction.detach(graphFragment3)
        fragmentTransaction.attach(graphFragment3).commit()
    }

    private fun runInferenceWorker() = runBlocking{
        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .build()
        val collectRequest =
            OneTimeWorkRequestBuilder<InferenceWorker>()
                .setConstraints(constraints)
                .addTag("inference")
                .build()

        val workManager = WorkManager.getInstance(applicationContext)
        workManager.enqueue(collectRequest)

    }

    private fun setImageAndDescription(avg:Double) {
        if (avg < 1.0){
            stressImage.setImageResource(R.drawable.stressicon_1)
            stressDescription.text = "낮음"
        }
        else if (avg < 2.0 && avg>= 1.0){
            stressImage.setImageResource(R.drawable.stressicon_2)
            stressDescription.text = "보통"

        }
        else if (avg < 3.0 && avg>= 2.0){
            stressImage.setImageResource(R.drawable.stressicon_3)
            stressDescription.text = "높음"

        }
        else if (avg < 4.0 && avg>= 3.0){
            stressImage.setImageResource(R.drawable.stressicon_4)
            stressDescription.text = "매우\n높음"
            stressDescription.textSize = 25f

        }


    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.mypage ->{
                val intent = Intent(this, MyPageActivity::class.java)
                intent.putExtra("user_email",user_email)
                intent.putExtra("user_pw",user_pw)
                startActivity(intent)
                true
            }
            else -> false
        }
    }

    private fun initButtonAndText() {
        //설문 버튼
        button_menu.setOnClickListener {
            showPopup(it)
        }

        button_survey.setOnClickListener {
            startStressCollectDialog(0)
        }

        button_refresh.setOnClickListener {
            refreshFragments(getGraphData(),graphFragment,graphFragment2,graphFragment3)
        }

        val mystring = "회원가입 하기"
        val content = SpannableString(mystring)
        content.setSpan(UnderlineSpan(), 0, mystring.length, 0)

        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .build()

        inferenceWorker.setOnClickListener {
            val collectRequest =
                OneTimeWorkRequestBuilder<InferenceWorker>()
                    .setConstraints(constraints)
                    .addTag("inferring")
                    .build()
            val workManager = WorkManager.getInstance(this)
            workManager?.let {
                it.enqueue(collectRequest)
            }
            Log.d("trtr", "InferenceWorker enqueued")
        }

        trainingWorker.setOnClickListener {
            val collectRequest =
                OneTimeWorkRequestBuilder<TrainingWorker>()
                    .setConstraints(constraints)
                    .addTag("inferring")
                    .build()
            val workManager = WorkManager.getInstance(this)
            workManager?.let {
                it.enqueue(collectRequest)
            }
            Log.d("trtr", "TrainingWorker enqueued")
        }

        sendweight.setOnClickListener {
            val collectRequest =
                OneTimeWorkRequestBuilder<SendWeightWorker>()
                    .setConstraints(constraints)
                    .addTag("inferring")
                    .build()
            val workManager = WorkManager.getInstance(this)
            workManager?.let {
                it.enqueue(collectRequest)
            }
            Log.d("trtr", "SendWeightWorker enqueued")
        }
        datacollectworker.setOnClickListener {
            val collectRequest =
                OneTimeWorkRequestBuilder<DataCollectWorker>()
                    .setConstraints(constraints)
                    .addTag("inferring")
                    .build()
            val workManager = WorkManager.getInstance(this)
            workManager?.let {
                it.enqueue(collectRequest)
            }
            Log.d("trtr", "SendWeightWorker enqueued")
        }
    }

    fun startStressCollectDialog(code:Int?){
//        val fragmentManager = supportFragmentManager
//        val fragmentTransaction = fragmentManager.beginTransaction()
//        fragmentTransaction.addToBackStack()
        val bundle = bundleOf("notificationCode" to code)
        val  dialog = StressCollectDialog()
        dialog.arguments = bundle
        dialog.show(supportFragmentManager, "dialog");
    }
}