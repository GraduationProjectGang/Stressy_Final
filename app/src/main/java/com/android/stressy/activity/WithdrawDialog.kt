package com.android.stressy.activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.android.stressy.R
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.dialog_withdraw.*


class WithdrawDialog : DialogFragment() {
    lateinit var user_email:String
    lateinit var user_pw : String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_withdraw, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    fun init() {
        user_email = requireArguments().getString("user_email",null)
        user_pw = requireArguments().getString("user_pw",null)
        val bundle = Bundle()

        button_withdraw_dialog.setOnClickListener {
            val pwInput = editText_check_pw.text.toString()
            if (user_pw == pwInput){
                withdrawOnDevice()
                withdrawOnServer(user_email)
                Toast.makeText(requireActivity(),"탈퇴되었습니다.", Toast.LENGTH_SHORT).show()
                requireActivity().finish()
            }


        }
    }

    fun withdrawOnDevice(){
        val prefs = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val edit = prefs.edit()
        edit.remove("mEmail")
        edit.remove("mPassword")
        edit.remove("autoLoginFlag").apply()
    }

    fun withdrawOnServer(input:String) :Boolean{ //input: user id, fcm_token
        //get fcm_token from pref and remove
        val prefs = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val edit = prefs.edit()
        val fcmToken = prefs.getString("prefs_fcm_token","null").toString()
        edit.remove("pref_fcm_token").apply()

        val url = "http://114.70.23.77:8002/v1/user/account/withdraw"
        val queue = Volley.newRequestQueue(requireActivity().applicationContext)
        val stringRequest = object : StringRequest(
            Request.Method.POST,url,
            Response.Listener<String> { res ->
                Log.d("volvol", res)
            },
            Response.ErrorListener { error ->  Log.d("volvol", error.toString()) }
        ){
            override fun getParams(): MutableMap<String, String>? {
                val params = hashMapOf<String,String>()
                params.put("user_email",input)
                params.put("fcm_token",fcmToken)
                return params
            }
        }
        queue.add(stringRequest)
        return true
    }
}