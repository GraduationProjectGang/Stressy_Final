package com.android.stressy.etc

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.android.stressy.R
import com.android.stressy.activity.UserMainActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import org.json.JSONObject

class LoginManager(val mContext:Context, val email:String, val pw:String) {

    fun login(userEmail:String, userPassword:String) :Boolean{
        Log.d("loglog",userEmail+userPassword)
        val url = "http://114.70.23.77:8002/v1/user/account/auth"
//        val hashedPassword = Hashing.calculateHash(userPassword)
        val queue = Volley.newRequestQueue(mContext)
        val jsonObject = JSONObject()
        jsonObject.put("user_email",userEmail)
        jsonObject.put("user_pw",userPassword)
        val stringRequest = object : JsonObjectRequest(
            Request.Method.POST,url,jsonObject,
            Response.Listener<JSONObject> { res ->
                Log.d("loglog", res.toString())
                if (res.getString("code") == "200") {
                    Toast.makeText(mContext,"환영합니다.", Toast.LENGTH_SHORT).show()
                    getFcmToken()
                    val intent = Intent(mContext, UserMainActivity::class.java)
                    startActivity(intent)
                }else{
                    Toast.makeText(mContext,"틀림", Toast.LENGTH_SHORT).show()
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

    fun getFcmToken(){
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("fcm", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token.toString()


                val prefs = requireActivity().getPreferences(Context.MODE_PRIVATE)
                prefs.getString("pref_fcm_token",getString(R.string.pref_fcm_token))
                if (prefs.getString("pref_fcm_token",getString(R.string.pref_fcm_token)) != token) {
                    Log.d("su4:", "new token")
                    //add to db
                    val url = "http://114.70.23.77:8002/v1/user/fcm/newtoken"
                    val queue = Volley.newRequestQueue(requireContext())
                    val stringRequest = object : StringRequest(
                        Method.POST,url,
                        Response.Listener<String> { response ->
                            val jsonObject = JSONObject(response)
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
                    val edit = prefs.edit()
                    edit.putString("pref_fcm_token", token)
                    edit.commit()
                }
            })
    }
}