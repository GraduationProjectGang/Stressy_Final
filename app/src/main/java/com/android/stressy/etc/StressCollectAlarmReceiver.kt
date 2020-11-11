package com.android.stressy.etc


import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity.NOTIFICATION_SERVICE
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android.stressy.R
import com.android.stressy.activity.UserMainActivity
import java.util.*

class StressCollectAlarmReceiver: BroadcastReceiver() {
    val stressCollectRequest = 111
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("setalarm","received")
        val CHANNEL_ID = "$context.packageName-${R.string.app_name}"

        val name = R.string.channel_name
        val mChannel = NotificationChannel(CHANNEL_ID, name.toString(), NotificationManager.IMPORTANCE_HIGH)
        mChannel.description = R.string.channel_description.toString()
        val notificationManager = context?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)

        val clickIntent = Intent(context, UserMainActivity::class.java)
        clickIntent.putExtra("notificationCode",stressCollectRequest)

        clickIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, stressCollectRequest,
            clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)

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

        val cal = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        var addtime = 0
        if (cal>21 || cal < 8) {
            Log.d("alarmset","10")
            addtime = 10
        }else{
            addtime = 2
            Log.d("alarmset","2")

        }
        setAlarm(context,addtime)
    }
    fun setAlarm(context:Context, addtime: Int) {
        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.HOUR_OF_DAY, addtime)
        }
        val alarmIntent = Intent(context, StressCollectAlarmReceiver::class.java)

        alarmIntent.putExtra("notificationCode",stressCollectRequest)

        val pendingIntent = PendingIntent.getBroadcast(context, stressCollectRequest, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis, pendingIntent
        )
    }
}