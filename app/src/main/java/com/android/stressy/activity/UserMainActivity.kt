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
import androidx.room.Room
import com.android.stressy.R
import com.android.stressy.dataclass.BaseUrl
import com.android.stressy.dataclass.db.CoroutineData
import com.android.stressy.dataclass.db.CoroutineDatabase
import com.android.stressy.etc.StressCollectAlarmReceiver
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_user_main.*
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class UserMainActivity() : AppCompatActivity(), PopupMenu.OnMenuItemClickListener {
    val MULTIPLE_REQUEST = 1234
    val stressCollectRequest = 111
    val mPref = "my_pref"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_main)

        var toolbar = getSupportActionBar()?.apply {
            setDisplayShowCustomEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)

        }
        getData()
        getFcmToken()
        getRequestCode()
        init()


    }

    fun getData() {
        //csv data 넣기
        val dbObject = Room.databaseBuilder(
            applicationContext,
            CoroutineDatabase::class.java, "coroutine"
        ).allowMainThreadQueries().fallbackToDestructiveMigration().build().coroutineDataDao()

//        dbObject.deleteAll()
//        Log.d("ecec",dbObject.countCoroutine().toString())
//        val file = resources.openRawResource(R.raw.coroutine)
//        val br = BufferedReader(InputStreamReader(file))
//        for (line in br.lines()){
//            val arr = line.split(",")
//            val data1 = arr[1].toLong()
//            val data2 = arr[2].toDouble()
//            val data3 = arr[3].toDouble()
//            val data4 = arr[4].toDouble()
//            var data5 = arr[5].toDoubleOrNull()
//            if (data5 == null){
//                data5 = 0.0
//            }
//            var data6 = arr[6].toDouble()
//            val data7 = arr[7].toDouble()
//            val tempData = CoroutineData(timestamp = data1,ifMoving = data2,orientation = data3,posture = data4,std_posture = data5,category = data6,totalTimeInForeground = data7)
//            dbObject.insert(tempData)
//        }


        val data = dbObject.getAll()
        Log.d("ecec",data.size.toString())
        Log.d("ecec",data.get(0).toString())
        val arr = mutableListOf<MutableList<CoroutineData>>()
//        val temp_arr = listOf<List<Double>>()[5]
        val timestampArr = mutableListOf<CoroutineData>()
//        val tempArr = mutableListOf<CoroutineData>()
        //timestamp별로 모아서 timestampArr에 넣기 -> 코루틴 개수만큼 생기겠지

        var timestamp_this = 0.toLong()
        var idx = 0
        for (eachCoroutine in data){
            if (eachCoroutine.timestamp == timestamp_this){
                arr[idx].add(eachCoroutine)
            }else{
                arr.add(mutableListOf(eachCoroutine))
                timestamp_this = eachCoroutine.timestamp
            }
        }
        Log.d("ecec",arr.get(0).toString())

        val doubleList = mutableListOf<List<Double>>()
        for (eachCoroutine in arr){
//            val coroutineData = doubleArray2D(5,6, )
//            for (index in eachCoroutine.indices){
//                val ed = eachCoroutine[index]
//                coroutineData[index] = doubleArrayOf(ed.ifMoving,ed.orientation,ed.posture,ed.std_posture,ed.category,ed.totalTimeInForeground)
//            }
//            doubleList.add(coroutineData)
        }

    }
    fun doubleArray2D(rows: Int, cols: Int, block: (i: Int, j: Int) -> Double): Array<DoubleArray>{
        return Array(rows){ i ->
            DoubleArray(cols){ j ->
                block(i,j)
            }
        }
    }
    fun getRequestCode(){
        if (intent.extras != null){ //알림타고 들어온거면
            val notificationCode = intent.extras!!.getString("notification_code")?.toIntOrNull()
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
    fun init() {
        checkPermission()
        addWhiteList()
        initButtonAndText()
        setAlarm()
        makeGraphFragment()
        val prefs = getSharedPreferences(mPref,Context.MODE_PRIVATE)
//        usercode.text =
//            "Usercode: " + prefs.getString(getString(R.string.pref_previously_logined), "null")
        val u_key = prefs.getString(getString(R.string.pref_previously_logined), "null")!!


        Log.w(
            "UMA_worker",
            prefs.getBoolean(getString(R.string.pref_previously_started), false).toString()
        )

        if (!prefs.getBoolean(getString(R.string.pref_previously_started), false)) {
            var edit = prefs.edit() as SharedPreferences.Editor
            edit.putBoolean(getString(R.string.pref_previously_started), true)
            edit.commit()
        }
        val mystring = "프로젝트 가이드 다시보기"
        val content = SpannableString(mystring)
        content.setSpan(UnderlineSpan(), 0, mystring.length, 0)
        tutorialAgain.setText(content)
        tutorialAgain.setOnClickListener {
            val intent = Intent(this, Tutorial1Activity::class.java)
            startActivity(intent)
        }
    }

    fun makeGraphFragment(){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val graphFragment = MainStressGraphFragment()
        fragmentTransaction.add(R.id.mainStressGraph, graphFragment).commit()
    }
    private fun setAlarm() {
        Log.d("setalarm","onusermain")
        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()+10
//            set(Calendar.HOUR_OF_DAY, 20)

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
            val pendingIntent = PendingIntent.getBroadcast(this, stressCollectRequest, alarmIntent, PendingIntent.FLAG_NO_CREATE)
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
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

    private fun setOnMenuItemClickListener(item: MenuItem) {

    }
    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.mypage ->{
                val intent = Intent(this, MyPageActivity::class.java)
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

        val mystring = "회원가입 하기"
        val content = SpannableString(mystring)
        content.setSpan(UnderlineSpan(), 0, mystring.length, 0)


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