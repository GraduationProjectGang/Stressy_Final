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
import com.android.stressy.etc.Hashing
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_login.*

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
            if (autoLogin){
                prefs_editor.putString(pref_auto_email,email)
                prefs_editor.putString(pref_auto_password,password)
                prefs_editor.apply()
            }
            login(email,password)
        }

        val mystring = "프로젝트 가이드 다시보기"
        val content = SpannableString(mystring)
        content.setSpan(UnderlineSpan(), 0, mystring.length, 0)
        button_signup_onlogin.setText(content)
        button_signup_onlogin.setOnClickListener {
            val intent = Intent(this,SignUpActivity::class.java)
            startActivity(intent)
        }

    }

    fun login(userEmail:String, userPassword:String) :Boolean{
        val url = "http://114.70.23.77:8002/v1/user/account/login"
        val hashedPassword = Hashing.calculateHash(userPassword)
        val queue = Volley.newRequestQueue(applicationContext)
        val stringRequest = object : StringRequest(
            Request.Method.POST,url,
            Response.Listener<String> { res ->
                Log.d("volvol", res)
                if (res == "200") {
                    Toast.makeText(applicationContext,"환영합니다.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(applicationContext,UserMainActivity::class.java)
                    startActivity(intent)
                }else{
                    Toast.makeText(applicationContext,"틀림", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->  Log.d("volvol", error.toString()) }
        ){
            override fun getParams(): MutableMap<String, String>? {
                val params = hashMapOf<String,String>()
                params.put("user_email",userEmail)
                params.put("user_pw",hashedPassword)
                return params
            }
        }
        queue.add(stringRequest)
        return true
    }


}