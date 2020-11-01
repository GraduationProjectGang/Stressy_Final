package com.android.stressy.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.stressy.R
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_my_page.*

class MyPageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)
        init()
        makeGraphFragment()
    }

    fun makeGraphFragment(){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val graphFragment = StressGraphFragment()
        fragmentTransaction.add(R.id.graphFragment, graphFragment).commit()
    }

    fun init(){

        account_settings.setOnClickListener {
            val modalBottomSheet = BottomSheetFragment()
            modalBottomSheet.show(supportFragmentManager, BottomSheetFragment.TAG)
        }
    }

    fun checkOriginalPassword(pw:String):Boolean{
        return false
    }

    fun changePassword(user_email:String) :Boolean{
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
                params.put("user_email",user_email)
                return params
            }
        }
        queue.add(stringRequest)
        return true
    }
}