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
                }else{
                    Toast.makeText(mContext,"틀림", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->  Log.d("logman", error.toString()) }
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

    fun getFcmToken():String{
        var token = ""
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("logman", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                token = task.result?.token.toString()
                val prefs = mContext.getSharedPreferences(mPref,Context.MODE_PRIVATE)
                Log.d("logman:token",token)
                Log.d("logman:original token",prefs.getString("pref_fcm_token",mContext.getString(R.string.pref_fcm_token)).toString())

                if (prefs.getString("pref_fcm_token",mContext.getString(R.string.pref_fcm_token)) != token) {
                    Log.d("logman", "new token")
                    //add to db
                    val url = BaseUrl.url + "/user/fcm/newtoken"
                    val queue = Volley.newRequestQueue(mContext)
                    val stringRequest = object : StringRequest(
                        Method.POST,url,
                        Response.Listener<String> { response ->
                            val jsonObject = JSONObject(response)

                            val jsonWebToken = jsonObject.getString("jwtToken")
                            prefs.edit().putString("jsonWebToken",jsonWebToken).apply()
                            Log.d("logman:jwt",jsonWebToken)
                        },
                        Response.ErrorListener { error ->  Log.d("logman:error", error.toString()) }
                    ){
                        override fun getParams(): MutableMap<String, String>? {
                            val params = hashMapOf<String,String>()
                            params["fcm_token"] = token
                            return params
                        }
                    }
                    queue.add(stringRequest)

                    //add to sharedpreference
                    prefs.edit().putString("pref_fcm_token", token).apply()
                }
            })
        return token
    }

}