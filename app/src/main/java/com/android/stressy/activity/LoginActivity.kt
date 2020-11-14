package com.android.stressy.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.stressy.R
import com.android.stressy.dataclass.BaseUrl
import com.android.stressy.etc.LoginManager
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    val pref_auto_email = "mEmail"
    val pref_auto_password = "mPassword"
    var pref_auto_login = "autoLoginFlag"
    val mPref = "my_pref"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        init()
    }

    private fun init() {
        val prefsEditor = getSharedPreferences(mPref,Context.MODE_PRIVATE).edit()

        button_login.setOnClickListener {
            val email = login_email.text.toString()
            val password = login_password.text.toString()
            val autoLogin = switch_autologin.isChecked

            LoginManager(applicationContext).login(email,password)


            if(autoLogin){
                prefsEditor.putString(pref_auto_login,autoLogin.toString())
                Log.d("loglog","switch On")

                prefsEditor.putString(pref_auto_email,email)
                prefsEditor.putString(pref_auto_password,password)
            }else{
                prefsEditor.putString(pref_auto_login,autoLogin.toString())
            }
            prefsEditor.apply()
        }

        val mystring = "회원가입"
        val content = SpannableString(mystring)
        content.setSpan(UnderlineSpan(), 0, mystring.length, 0)
        button_signup_onlogin.setText(content)
        button_signup_onlogin.setOnClickListener {
            val intent = Intent(this,SignUpActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

    }
    fun initFcmToken(){
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("fcm", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token.toString()
                val prefs = getSharedPreferences(mPref,Context.MODE_PRIVATE)
                if (prefs.getString("pref_fcm_token",getString(R.string.pref_fcm_token)) != token) {
                    Log.d("fcm:", "new token")

                    //add to db
                    val url = BaseUrl.url + "user/fcm/newtoken"
                    val queue = Volley.newRequestQueue(applicationContext)
                    val stringRequest = object : StringRequest(
                        Method.POST,url,
                        Response.Listener<String> { response ->
                            Log.d("volvol", response)
                        },
                        Response.ErrorListener { error ->  Log.d("volvol", error.toString()) }
                    ){
                        override fun getParams(): MutableMap<String, String>? {
                            val params = hashMapOf<String,String>()
                            params.put("fcm_token",token)
                            return params
                        }
                    }
                    queue.add(stringRequest)

                    //add to sharedpreference
                }

            })
    }
}