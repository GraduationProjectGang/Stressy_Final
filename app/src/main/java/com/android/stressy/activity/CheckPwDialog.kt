package com.android.stressy.activity

import android.content.DialogInterface
import android.content.Intent
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

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

    }

    fun init() {
        button_check_pw.setOnClickListener {
            Log.d("cheche","button Clicked")
            val inputPassword = editText_check_pw.text.toString()
//            checkPassword(inputPassword)

            val intent = Intent(requireActivity(),ChangePwActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            requireActivity().startActivity(intent)
        }
    }

    fun checkPassword(input:String) :Boolean{
        val url = "http://114.70.23.77:8002/v1/user/account/checkpw"
        val queue = Volley.newRequestQueue(requireActivity().applicationContext)

        val stringRequest = object : StringRequest(
            Request.Method.POST,url,
            Response.Listener<String> { res ->
                Log.d("cheche", res)
                if (res == "200") {
                    Toast.makeText(requireActivity(),"확인되었습니다.",Toast.LENGTH_SHORT).show()
                    Log.d("cheche","good")
                    val intent = Intent(requireActivity().applicationContext,ChangePwActivity::class.java)
                    startActivity(intent)
                }else{
                    Toast.makeText(requireActivity(),"비밀번호가 틀렸습니다.",Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->  Log.d("cheche", error.toString()) }
        ){
            override fun getParams(): MutableMap<String, String>? {
                val params = hashMapOf<String,String>()
                params.put("user_pw",input)
                return params
            }
        }
        queue.add(stringRequest)
        return true
    }
}