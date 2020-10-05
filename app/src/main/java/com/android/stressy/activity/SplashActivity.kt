package com.android.stressy.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.stressy.R
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

lateinit var u_key: String

class SplashActivity : AppCompatActivity() {
    private val SPLASH_TIME_OUT:Long = 1000 // 일단짧게

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler().postDelayed({
            // This method will be executed once the timer is over
            // Start your app main activity

//            val prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext())
//
//            //첫 실행이면 SignInActivity 실행
//            var previouslyStarted = prefs.getBoolean(getString(R.string.pref_previously_started), false)
//            if (!previouslyStarted) {
//                val intent = Intent(this, SignUpActivity::class.java)
//                startActivity(intent)
//            }
//            else {
//                startActivity(
//                    Intent(this,
//                    UserMainActivity::class.java)
//                )
//            }
            startActivity(
                Intent(this,
                    SignUpActivity::class.java)
            )

            // close this activity
            finish()
        }, SPLASH_TIME_OUT)

    }

}