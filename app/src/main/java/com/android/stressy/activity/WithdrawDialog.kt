package com.android.stressy.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.android.stressy.R
import com.android.stressy.etc.Hashing
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.dialog_withdraw.*


class WithdrawDialog : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_withdraw, container, false)
    }

//    override fun onResume() {
//        super.onResume()
//        val height = resources.getDimensionPixelSize(R.dimen.pop_up_height)
//        dialog!!.window!!.setLayout(height)
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    fun init() {
        button_withdraw_dialog.setOnClickListener {
            val user_id = "ksh04023@gmail.com"
            withdraw(user_id)

        }
    }


    fun withdraw(input:String) :Boolean{ //input: user id
        val url = "http://114.70.23.77:8002/v1/user/account/withdraw"
        val hashedInput = Hashing.calculateHash(input)
        val queue = Volley.newRequestQueue(activity!!.applicationContext)
        val stringRequest = object : StringRequest(
            Request.Method.POST,url,
            Response.Listener<String> { res ->
                Log.d("volvol", res)
            },
            Response.ErrorListener { error ->  Log.d("volvol", error.toString()) }
        ){
            override fun getParams(): MutableMap<String, String>? {
                val params = hashMapOf<String,String>()
                params.put("user_id",input)
                return params
            }
        }
        queue.add(stringRequest)
        return true
    }
}