package com.android.stressy.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.android.stressy.R
import com.android.stressy.etc.Hashing
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.dialog_check_pw.*


class CheckPwDialog : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_check_pw, container, false)
    }

//    override fun onResume() {
//        super.onResume()
//        val width = resources.getDimensionPixelSize(R.dimen.pop_up_width)
//        val height = resources.getDimensionPixelSize(R.dimen.pop_up_height)
//        dialog!!.window!!.setLayout(width, height)
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    fun init() {
        button_check_pw.setOnClickListener {
            val inputPassword = editText_check_pw.text.toString()
            checkPassword(inputPassword)

        }
    }


    fun checkPassword(input:String) :Boolean{
        val url = "http://114.70.23.77:8002/v1/user/account/checkPw"
        val hashedInput = Hashing.calculateHash(input)
        val queue = Volley.newRequestQueue(activity!!.applicationContext)
        val stringRequest = object : StringRequest(
            Request.Method.POST,url,
            Response.Listener<String> { res ->
                Log.d("volvol", res)
                if (res == "200") {
                    Toast.makeText(activity!!,"확인되었습니다.",Toast.LENGTH_SHORT).show()
                    val intent = Intent(activity!!.applicationContext,ChangePwActivity::class.java)
                    startActivity(intent)
                }else{
                    Toast.makeText(activity!!,"비밀번호가 틀렸습니다.",Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->  Log.d("volvol", error.toString()) }
        ){
            override fun getParams(): MutableMap<String, String>? {
                val params = hashMapOf<String,String>()
                params.put("user_pw",hashedInput)
                return params
            }
        }
        queue.add(stringRequest)
        return true
    }
}