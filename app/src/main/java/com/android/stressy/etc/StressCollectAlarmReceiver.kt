package com.android.stressy.etc


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity.NOTIFICATION_SERVICE
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android.stressy.R
import com.android.stressy.activity.UserMainActivity

class StressCollectAlarmReceiver: BroadcastReceiver() {
    val stressCollectRequest = 111
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("setalarm","received")
        val CHANNEL_ID = "$context.packageName-${R.string.app_name}"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = R.string.channel_name
            val mChannel = NotificationChannel(CHANNEL_ID, name.toString(), NotificationManager.IMPORTANCE_HIGH)
            mChannel.description = R.string.channel_description.toString()
            val notificationManager = context?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }

        val intent = Intent(context, UserMainActivity::class.java)
        intent.putExtra("notification_code",stressCollectRequest)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, stressCollectRequest,
            intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(context!!, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.full_swipe)
            setContentTitle("오늘 하루는 어떠셨나요?")
            setContentText("스트레스 설문에 참여해주세요\uD83D\uDD25")
            priority = NotificationCompat.PRIORITY_HIGH
            setAutoCancel(true)
            setContentIntent(pendingIntent)
        }

        with(NotificationManagerCompat.from(context)) {
            notify(stressCollectRequest, builder.build())
            Log.d("alal","notified")
        }
    }
}