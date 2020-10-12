package com.android.stressy.activity

import android.app.AlarmManager
import android.app.AppOpsManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.android.stressy.R
import com.android.stressy.etc.StressCollectAlarmReceiver
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_user_main.*
import java.util.*
import kotlin.collections.ArrayList

class UserMainActivity : AppCompatActivity() {
    val MULTIPLE_REQUEST = 1234
    lateinit var mFirebaseAnalytics: FirebaseAnalytics
    val stressCollectRequest = 111

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_main)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        getRequestCode()
        init()
    }
    fun getRequestCode(){
        if (intent.extras != null){ //알림타고 들어온거면
            val notificationCode = intent.extras!!.getString("notification_code")?.toIntOrNull()
            startStressCollectDialog(notificationCode)
        }

    }

    fun init() {
        checkPermission()
        initButtonAndText()
        setAlarm()

        val prefs = getPreferences(Context.MODE_PRIVATE)
        usercode.text =
            "Usercode: " + prefs.getString(getString(R.string.pref_previously_logined), "null")
        u_key = prefs.getString(getString(R.string.pref_previously_logined), "null")!!


        Log.w(
            "UMA_worker",
            prefs.getBoolean(getString(R.string.pref_previously_started), false).toString()
        )

        if (!prefs.getBoolean(getString(R.string.pref_previously_started), false)) {
            var edit = prefs.edit() as SharedPreferences.Editor
            edit.putBoolean(getString(R.string.pref_previously_started), true)
            edit.commit()


        }
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

    private fun initButtonAndText() {
        //설문 버튼
        button_survey.setOnClickListener {
            startStressCollectDialog(0)
        }

        //프로젝트 가이드 TextView
        val mystring = "회원가입 하기"
        val content = SpannableString(mystring)
        content.setSpan(UnderlineSpan(), 0, mystring.length, 0)

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("fcm", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token.toString()
                val prefs = getPreferences(Context.MODE_PRIVATE)
                if (prefs.getString("pref_fcm_token",getString(R.string.pref_fcm_token)) != token) {
                    Log.d("fcm:", "new token")

                    //add to db
                    val url = "http://114.70.23.77:8002/v1/user/fcm/newtoken"
                    val queue = Volley.newRequestQueue(applicationContext)
                    val stringRequest = object : StringRequest(
                        Method.POST,url,
                        com.android.volley.Response.Listener<String> { response ->
                            Log.d("volvol", response) },
                        com.android.volley.Response.ErrorListener { error ->  Log.d("volvol", error.toString()) }
                    ){
                        override fun getParams(): MutableMap<String, String>? {
                            val params = hashMapOf<String,String>()
                            params.put("fcm_token",token)
                            return params
                        }
                    }
                    queue.add(stringRequest)

                    //add to sharedpreference
                    val edit = prefs.edit() as SharedPreferences.Editor
                    edit.putString("pref_fcm_token", token)
                    edit.commit()
                }

            })
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

    fun sendEventGoogleAnalytics(id:String, name:String) {
        var bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID,id)
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME,name)
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT,bundle)
    }
}