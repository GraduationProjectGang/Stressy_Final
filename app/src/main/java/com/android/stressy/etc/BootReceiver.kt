package com.android.stressy.etc


import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.util.Log
import java.util.*


class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("bootReceiver","Bootreceived")
        setAlarmAt(context,10)

    }

    fun setAlarmAt(context:Context, time: Int) {
        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, time)
        }
        Log.d("alarmset","alarmsetat${time}")
        val alarmIntent = Intent(context, StressCollectAlarmReceiver::class.java)
        alarmIntent.putExtra("time",time)

        val alarmUp = PendingIntent.getBroadcast(context, 10, alarmIntent,
            PendingIntent.FLAG_NO_CREATE
        ) != null

        if (alarmUp) {
            Log.d("myTag", "Alarm is already active")
        }else{
            Log.d("myTag", "Alarm doesn't exist")
            val pendingIntent =
                PendingIntent.getBroadcast(context, 10, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,calendar.timeInMillis,pendingIntent)
        }

    }


}

