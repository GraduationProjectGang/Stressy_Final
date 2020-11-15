package com.android.stressy.etc

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.android.stressy.R
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService() : FirebaseMessagingService() {

    val TAG = "fcm onmessage"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        var message = ""
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: ${remoteMessage.from}")

        val dataBuilder = Data.Builder()

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()){
            for(key in remoteMessage.data.keys){
                Log.d(TAG, "fcmMes: Key:"+key +" Data: " + remoteMessage.data[key])
                dataBuilder.putString(key, remoteMessage.data[key])
            }
            message = remoteMessage.data["title"]!! //payload 중 첫번째 value
            Log.d(TAG, "fcmMes: Data:$message")
            when (message) {
                "dataCollect" -> {
                    createDataCollectWorker()
                }
                "startTraining" -> {
                    startTraining()
                }
                "weightRequest" -> {
                    sendWeight(dataBuilder)
                }
                "receiveWeights" -> {
                    receiveWeight()
                }
            }

        }
        Log.d(TAG, "Message data payload: ${remoteMessage.data}")


        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }

        sendNotification()
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private fun receiveWeight() {
        Log.d(TAG, "fcmMes: receive worker Created")
        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .build()
        val collectRequest =
            OneTimeWorkRequestBuilder<ReceiveWeightWorker>()
                .setConstraints(constraints)
                .addTag("receiveweight")
                .build()

        val workManager = WorkManager.getInstance(this)
        workManager?.let {
            it.enqueue(collectRequest)
        }

        Log.d(TAG, "receive worker enqueued")

    }


    private fun sendWeight(dataBuilder: Data.Builder) {
        Log.d(TAG, "fcmMes: training worker Created")
        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .build()
        val collectRequest =
            OneTimeWorkRequestBuilder<SendWeightWorker>()
                .setInputData(dataBuilder.build())
                .setConstraints(constraints)
                .addTag("training")
                .build()

        val workManager = WorkManager.getInstance(this)
        workManager?.let {
            it.enqueue(collectRequest)
        }

        Log.d(TAG, "sendWeight worker enqueued")

    }

    override fun onNewToken(token: String) {//if new token created
        Log.d(TAG, "new token: $token")
        //save token on db

        val url = "http://114.70.23.77:8002/v1/user/fcm/newtoken"
        val queue = Volley.newRequestQueue(applicationContext)
        val stringRequest = object : StringRequest(
            Request.Method.POST,url,
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

    }

    private fun startTraining() {
        Log.d(TAG, "fcmMes: training worker Created")
        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .build()
        val collectRequest =
            OneTimeWorkRequestBuilder<TrainingWorker>()
                .setConstraints(constraints)
                .addTag("training")
                .build()

        val workManager = WorkManager.getInstance(this)
        workManager?.let {
            it.enqueue(collectRequest)
        }

        Log.d(TAG, "request enqueued")

    }

    fun createDataCollectWorker(){//init Periodic work
        Log.d(TAG, "fcmMes: datacollectWorker Created")

        val uniqueWorkName = "DataCollectWorker"

        val constraints = Constraints.Builder()
            .setRequiresCharging(false)
            .build()

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
        Log.d(TAG, "request enqueued")
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
