package com.android.stressy.etc

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.android.stressy.activity.UserMainActivity
import com.android.stressy.dataclass.BaseUrl
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.util.*

class LoginManager(val mContext:Context) {
    val mPref = "my_pref"

    fun login(userEmail:String, userPassword:String) = runBlocking{
        Log.d("logman",userEmail+" "+userPassword)
        val url = BaseUrl.url + "/user/account/auth"
        val prefs = mContext.getSharedPreferences(mPref, Context.MODE_PRIVATE)
//        val url = "http://192.168.104.40:8002/v1/user/account/auth"
//        val hashedPassword = Hashing.calculateHash(userPassword)
        val queue = Volley.newRequestQueue(mContext)
        val jsonObject = JSONObject()
        jsonObject.put("user_email",userEmail)
        jsonObject.put("user_pw",userPassword)
        val jsonRequest = object : JsonObjectRequest(
            Request.Method.POST,url,jsonObject,
            Response.Listener<JSONObject> { res ->
                Log.d("logman:login res", res.toString())
                if (res.getString("code") == "200") {
                    Toast.makeText(mContext,"환영합니다.", Toast.LENGTH_SHORT).show()

                    val jwt = res.getString("jwt")
                    Log.d("jwt",jwt)
                    prefs.edit().putString("pref_user_email", userEmail).apply()
                    prefs.edit().putString("jwt",jwt).apply()

                    val expireMin = res.getString("expiresIn").toInt()
                    setJwtAlarmAt(expireMin)

                    val intent = Intent(mContext, UserMainActivity::class.java)
                    intent.putExtra("user_email", userEmail)
                    intent.putExtra("user_pw",userPassword)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    mContext.startActivity(intent)

                }
            },
            Response.ErrorListener { error ->
                Log.d("logman", error.toString())
                Toast.makeText(mContext,"이메일 또는 비밀번호가 잘못되었습니다.",Toast.LENGTH_SHORT).show()
            }
        ){
//            override fun getParams(): MutableMap<String, String> {
//                val params = mutableMapOf<String,String>()
//                params["user_email"] = userEmail
//                params["user_pw"] = userPassword
//                return params
//            }
        }
        queue.add(jsonRequest)
    }
    fun setJwtAlarmAt(expireMin: Int) {
        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.MINUTE, expireMin)
        }

        Log.d("logman","jwt alarm set$expireMin")
        val alarmIntent = Intent(mContext, JwtAlarmReceiver::class.java)
        alarmIntent.putExtra("expiresIn",expireMin)


        val pendingIntent =
            PendingIntent.getBroadcast(mContext, expireMin, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        val alarmManager = mContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,calendar.timeInMillis,pendingIntent)
    }


}