package com.android.stressy.etc

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.android.stressy.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MessagingService() : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // ...
        val TAG = "fcm onmessage"
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: ${remoteMessage.from}")


        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()){
            Log.d(TAG,"onMessageReceived: Data Size:"+remoteMessage.data.size)
            for(key in remoteMessage.data.keys){
                Log.d(TAG, "onMessageReceived: Key:"+key +" Data: " + remoteMessage.data.get(key))
            }
            Log.d(TAG,"onMessageReceived: Data:"+remoteMessage.data.toString())
            createWorker()

        }

            Log.d(TAG, "Message data payload: ${remoteMessage.data}")


        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    fun createWorker(){//init Periodic work

        val uniqueWorkName = "DataCollectWorker"

        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .build()

        //20분 마다 반복
        val collectRequest =
            OneTimeWorkRequestBuilder<DataCollectWorker>()
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

        val workManager = WorkManager.getInstance(this)
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

    override fun onNewToken(token: String) {//if new token created
        super.onNewToken(token)
        Log.i("fcm", "new token: " + token)
        //save token on db
        val requestBody = RequestBody.create(MediaType.parse("text/plain"),token)
        val response = TokenService.create().submit(requestBody)
        Log.d("fcm", "enqueued")

        response.enqueue(object: Callback<ResponseBody> {

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                //TODO
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                //SharedPreference에도 저장
                val pref = getSharedPreferences("pref", Context.MODE_PRIVATE)
                val editor = pref.edit()
                editor.putString("token", token)
                editor.commit()
            }
        })
    }

    fun sendNotification() {
        val CHANNEL_ID = "$applicationContext.packageName-${R.string.app_name}"
        val title = "사용자 데이터 수집"
        // This PendingIntent can be used to cancel the worker

        Log.d("fcm","send notification started")
        // Create a Notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(CHANNEL_ID, title, NotificationManager.IMPORTANCE_LOW)
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText("데이터 전송 중")
            .setSmallIcon(R.drawable.empty_swipe)
            .setOngoing(true)
            .build()
        Log.d("fcm","build")
    }

    fun createChannel(CHANNEL_ID:String, name:String, importance:Int) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as
                    NotificationManager
        val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
        mChannel.description = "데이터 전송 중"
        notificationManager.createNotificationChannel(mChannel)
        Log.d("setForeground","created")
    }
}
