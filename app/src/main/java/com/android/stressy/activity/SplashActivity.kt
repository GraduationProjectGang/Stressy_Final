package com.android.stressy.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.stressy.R

//lateinit var u_key: String

class SplashActivity : AppCompatActivity() {
    private val SPLASH_TIME_OUT:Long = 2000 // 일단짧게
    val pref_auto_email = "mEmail"
    val pref_auto_password = "mPassword"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler().postDelayed({
            val prefs = getPreferences(Context.MODE_PRIVATE)
            val user_email = prefs.getString(pref_auto_email,"null")
            val user_pw = prefs.getString(pref_auto_password,"null")
            //자동 로그인 정보 확인
            if (prefs.contains(pref_auto_email) && prefs.contains(pref_auto_password)){
                Toast.makeText(this,"자동 로그인 되었습니다.",Toast.LENGTH_SHORT).show()
                Log.d("autolog",user_email+"  "+user_pw)
                val intent = Intent(this, UserMainActivity::class.java)
                startActivity(intent)
            }else{
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }

            // close this activity
            finish()
        }, SPLASH_TIME_OUT)

    }

}