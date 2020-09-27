package com.android.stressy.activity

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.WorkManager
import com.android.stressy.R
import com.android.stressy.etc.DataCollectWorker
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_user_main.*
import androidx.work.OneTimeWorkRequestBuilder as OneTimeWorkRequestBuilder1

class UserMainActivity : AppCompatActivity() {
    val MULTIPLE_REQUEST = 1234
    lateinit var mFirebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_main)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        init()
    }

    public fun createWorker() {//init Periodic work

        val uniqueWorkName = "DataCollectWorker"

        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .build()

        //20분 마다 반복
        val collectRequest =
            OneTimeWorkRequestBuilder1<DataCollectWorker>()
                .setConstraints(constraints)
                .addTag("DCWorker")
                .build()


        //WorkManager에 enqueue
//        WorkManager.getInstance(applicationContext)
//            .enqueueUniquePeriodicWork(
//                uniqueWorkName,
//                ExistingPeriodicWorkPolicy.REPLACE,
//                collectRequest
//            )
        val workManager = WorkManager.getInstance(applicationContext)
        workManager?.let {
            it.enqueue(collectRequest)
        }

//        workManager?.let {
//            it.enqueueUniquePeriodicWork(uniqueWorkName, ExistingPeriodicWorkPolicy.KEEP, collectRequest)
//            val statusLiveData = it.getWorkInfosForUniqueWorkLiveData(uniqueWorkName)
//            statusLiveData.observe(this, androidx.lifecycle.Observer {
//                Log.w("workstatus", "state: ${it[0].state}")
//                if (it[0].state == WorkInfo.State.BLOCKED || it[0].state == WorkInfo.State.CANCELLED || it[0].state == WorkInfo.State.FAILED) {
//                    val fbDatabase = FirebaseDatabase.getInstance()
//                    val dbReference = fbDatabase.reference
//                    dbReference.child("user").child(u_key).child("isRunning").setValue("false")
//                }
//            })
//        }
        Log.d("fcm", "request enqueued")
    }

    fun cancelWork() {
        val workManager = WorkManager.getInstance(applicationContext)
        workManager.cancelAllWorkByTag("DCWorker")
    }

    fun init() {
        checkPermission()
        initFirebase()
        initButtonAndText()


        val prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext())
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
            sendEventGoogleAnalytics("button_survey","onClick")
            val intent = Intent(this, StressCollectActivity::class.java)
            startActivity(intent)
        }

        //프로젝트 가이드 TextView
        val mystring = "프로젝트 가이드 다시보기"
        val content = SpannableString(mystring)
        content.setSpan(UnderlineSpan(), 0, mystring.length, 0)
        tutorialAgain.setText(content)

        //        tutorialAgain.setOnClickListener {
//            val intent = Intent(this, Tutorial1Activity::class.java)
//            startActivity(intent)
//        }


    }

    fun initFirebase(){
        FirebaseApp.initializeApp(applicationContext)
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("fcm", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token

                // Log and toast
                val msg = "token: " + token
                Log.d("fcm", msg)
            })

    }
    fun sendEventGoogleAnalytics(id:String, name:String) {
        var bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID,id)
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME,name)
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT,bundle)
    }

}