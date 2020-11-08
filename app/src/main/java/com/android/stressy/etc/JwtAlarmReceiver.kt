package com.android.stressy.etc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.auth0.android.jwt.JWT

class JwtAlarmReceiver: BroadcastReceiver() {
    val mPref = "my_pref"

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("jwtAlarm","received")
        val loginManager = context?.let { LoginManager(it) } as LoginManager
        val expiresIn = intent?.getStringExtra("expiresIn").toString().toInt()
//        loginManager.login()

        val prefs = context.getSharedPreferences(mPref,Context.MODE_PRIVATE)
        val old_jwt = JWT(prefs.getString("jwt","null").toString())
        val user_email = old_jwt.signature

    }

}