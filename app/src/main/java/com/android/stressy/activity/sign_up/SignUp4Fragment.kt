package com.android.stressy.activity.sign_up

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.stressy.R
import com.android.stressy.activity.UserMainActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_sign_up4.*
import java.util.*


class SignUp4Fragment : androidx.fragment.app.Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_sign_up4, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        init(view)
    }

    private fun init(rootView: View) {
        val c = Calendar.getInstance()
        c.set(Calendar.YEAR,1995)
        var y = c.get(Calendar.YEAR)
        var m = c.get(Calendar.MONTH)
        var d = c.get(Calendar.DAY_OF_MONTH)

        nextButton4.setOnClickListener {
            val df = SimpleDateFormat("yyyy-MM-dd")
//            val dateString = df.format(c)
            addUserToDB()
        }

        button_birthday.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireActivity(),
                DatePickerDialog.OnDateSetListener { datePicker, i, i2, i3 ->
                    y = i
                    m = i2+1
                    d = i3
                    textView_birth.text = "$y 년 $m 월 $d 일"
                    c.set(y,m,d)
                },
                y, m, d
            )
            datePickerDialog.window!!.setBackgroundDrawableResource(android.R.color.white)
            datePickerDialog.show()
        }
    }
    fun addUserToDB(){
        val bundle = requireArguments()
        if (!bundle.isEmpty()){ //TODO
            val userName = bundle.get("userName").toString()
            val userEmail = bundle.get("userEmail").toString()
            val userPassword = bundle.get("userPassword").toString()
            val userGender = bundle.get("userGender").toString().toInt()

            val url = "http://114.70.23.77:8002/v1/user/account/signup"
            val queue = Volley.newRequestQueue(requireActivity().applicationContext)
            val stringRequest = object : StringRequest(
                Request.Method.POST,url,
                Response.Listener<String> { response ->
                    Log.d("volvol", response) },
                Response.ErrorListener { error ->  Log.d("volvol", error.toString()) }
            ){
                override fun getParams(): MutableMap<String, String>? {
                    val params = hashMapOf<String,String>()
                    params.put("user_name",userName)
                    params.put("user_pw",userPassword)
                    params.put("user_email",userEmail)
                    params.put("user_gender",userGender.toString())
                    return params
                }
            }
            queue.add(stringRequest)

            //이거 맞나 회원가입 안했으면 회원가입하라고..,?ㅎ?ㅇ?ㅇㅎ/
            val prefs = requireActivity().getPreferences(Context.MODE_PRIVATE)
            prefs.edit().putBoolean(getString(R.string.pref_previously_started),true).apply()
        }





        val intent = Intent(requireActivity(), UserMainActivity::class.java)
        startActivity(intent)
    }
}