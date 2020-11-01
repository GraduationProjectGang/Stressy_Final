package com.android.stressy.activity

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.android.stressy.R
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_sign_up1.*
import java.util.regex.Pattern

class ChangePwActivity : AppCompatActivity() {
    var validFlag = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        init()
        return super.onCreateView(name, context, attrs)
    }

    fun init() {
        button_change_password.setOnClickListener {
            val passwordInput = editText_password.text.toString()
            val passwordInput2 = editText_password2.text.toString()
            guide_password.text = "" //초기화
            Log.d("valval",passwordInput.length.toString())
            if (!isValidPassword(passwordInput) or (passwordInput.length <= 8)){
                validFlag = false
                guide_password.text = getString(R.string.guide_password)
            }else if (passwordInput != passwordInput2){
                validFlag = false
                guide_password.text = getString(R.string.guide_password2)
            }
            Log.d("valval validflag",validFlag.toString())
            if(validFlag) changePassword()
        }
    }

    fun isValidPassword(input:String):Boolean{
        val PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[\$@\$!%*#?&]).{8,15}.\$"
        val pattern = Pattern.compile(PASSWORD_PATTERN)
        val matcher = pattern.matcher(input)
        Log.d("valval: matcher",matcher.matches().toString())

        return matcher.matches()
    }

    fun changePassword() :Boolean {
        val url = "http://114.70.23.77:8002/v1/user/account/changepw"
        val queue = Volley.newRequestQueue(applicationContext)
        val stringRequest = object : StringRequest(
            Request.Method.POST,url,
            Response.Listener<String> { response ->
                Log.d("volvol", response) },
            Response.ErrorListener { error ->  Log.d("volvol", error.toString()) }
        ){
            override fun getParams(): MutableMap<String, String>? {
                val params = hashMapOf<String,String>()
//                params.put("user_email",user_email)
                return params
            }
        }
        queue.add(stringRequest)
        return true
    }
}