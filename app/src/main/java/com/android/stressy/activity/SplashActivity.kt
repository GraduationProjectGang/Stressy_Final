package com.android.stressy.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.stressy.R
import com.android.stressy.etc.LoginManager

//lateinit var u_key: String

class SplashActivity : AppCompatActivity() {
    private val SPLASH_TIME_OUT:Long = 2000 // 일단짧게
    val pref_auto_email = "mEmail"
    val pref_auto_password = "mPassword"
    val pref_auto_login = "autoLoginFlag"
    val mPref = "my_pref"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler(Looper.getMainLooper()).postDelayed({
            val prefs = getSharedPreferences(mPref,Context.MODE_PRIVATE)
            val user_email = prefs.getString(pref_auto_email,"null").toString()
            val user_pw = prefs.getString(pref_auto_password,"null").toString()
//            prefs.edit().remove(pref_auto_login).apply()
            val if_auto_login = prefs.getString(pref_auto_login,false.toString())
            //자동 로그인 정보 확인
            Log.d("splash login",user_email +"  "+ user_pw)

            if (if_auto_login!!.toBoolean()){
//                val jwtToken = jsonObject.getString("jwtToken")
//                editor.putString("user_jwt",jwtToken).apply()
//                Log.d("jwtjwt",jwtToken)
                //TODO
                Toast.makeText(this,"자동 로그인 되었습니다.",Toast.LENGTH_SHORT).show()
                Log.d("autolog",user_email+"  "+user_pw)

                val intent = Intent(this, UserMainActivity::class.java)
                startActivity(intent)
            }else{
                LoginManager(applicationContext).login(user_email,user_pw)

                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }


            // close this activity
            finish()
        }, SPLASH_TIME_OUT)

    }

}