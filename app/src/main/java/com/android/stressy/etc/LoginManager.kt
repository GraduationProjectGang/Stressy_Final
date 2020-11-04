package com.android.stressy.etc

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.android.stressy.R
import com.android.stressy.activity.UserMainActivity
import com.android.stressy.activity.sign_up.SignUp1Fragment
import com.android.stressy.dataclass.BaseUrl
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import org.json.JSONObject

class LoginManager(val mContext:Context) {
    val mPref = "my_pref"

    fun login(userEmail:String, userPassword:String){
        Log.d("logman",userEmail+" "+userPassword)
        //http://10.0.2.2:8002/
        val url = BaseUrl.url + "/user/account/auth"
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
                    val intent = Intent(mContext, UserMainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
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



}