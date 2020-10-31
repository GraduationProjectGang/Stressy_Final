package com.android.stressy.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.stressy.R
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    val pref_auto_email = "mEmail"
    val pref_auto_password = "mPassword"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        init()
    }

    private fun init() {
        val prefs_editor = getPreferences(Context.MODE_PRIVATE).edit() as SharedPreferences.Editor
        val email = login_email.text.toString()
        val password = login_password.text.toString()
        val autoLogin = switch_autologin.isChecked
        button_login.setOnClickListener {
            Log.d("loglog","switch On")
            if(switch_autologin.isChecked){
                prefs_editor.putString(pref_auto_email,email)
                prefs_editor.putString(pref_auto_password,password)
                prefs_editor.apply()
            }
            login(email,password)
        }

        val mystring = "회원가입"
        val content = SpannableString(mystring)
        content.setSpan(UnderlineSpan(), 0, mystring.length, 0)
        button_signup_onlogin.setText(content)
        button_signup_onlogin.setOnClickListener {
            val intent = Intent(this,SignUpActivity::class.java)
            startActivity(intent)
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
                val prefs = getPreferences(Context.MODE_PRIVATE)
                if (prefs.getString("pref_fcm_token",getString(R.string.pref_fcm_token)) != token) {
                    Log.d("fcm:", "new token")

                    //add to db
                    val url = "http://114.70.23.77:8002/v1/user/fcm/newtoken"
                    val queue = Volley.newRequestQueue(applicationContext)
                    val stringRequest = object : StringRequest(
                        Method.POST,url,
                        Response.Listener<String> { response ->
                            Log.d("volvol", response) },
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
                    val edit = prefs.edit() as SharedPreferences.Editor
                    edit.putString("pref_fcm_token", token)
                    edit.commit()
                }

            })

    }

    fun login(userEmail:String, userPassword:String) :Boolean{
        Log.d("loglog",userEmail+userPassword)
        val url = "http://114.70.23.77:8002/v1/user/account/auth"
//        val hashedPassword = Hashing.calculateHash(userPassword)
        val queue = Volley.newRequestQueue(applicationContext)
        val jsonObject = JSONObject()
        jsonObject.put("user_email",userEmail)
        jsonObject.put("user_pw",userPassword)
        val stringRequest = object : JsonObjectRequest(
            Request.Method.POST,url,jsonObject,
            Response.Listener<JSONObject> { res ->
                Log.d("loglog", res.toString())
                if (res.getString("code") == "200") {
                    Toast.makeText(applicationContext,"환영합니다.", Toast.LENGTH_SHORT).show()
                    initFcmToken()
                    val intent = Intent(applicationContext,UserMainActivity::class.java)
                    startActivity(intent)
                }else{
                    Toast.makeText(applicationContext,"틀림", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->  Log.d("loglog", error.toString()) }
        ){
            override fun getParams(): MutableMap<String, String>? {
                val params = mutableMapOf<String,String>()
                params["user_email"] = userEmail
                params["user_pw"] = userPassword
                return params
            }
        }
        queue.add(stringRequest)
        return true
    }


}