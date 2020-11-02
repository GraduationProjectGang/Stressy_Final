package com.android.stressy.activity.sign_up

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.stressy.R
import com.android.stressy.activity.UserMainActivity
import com.android.stressy.etc.LoginManager
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_sign_up4.*
import java.text.SimpleDateFormat
import java.util.*


class SignUp4Fragment : androidx.fragment.app.Fragment() {
    val mPref = "my_pref"

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

        val bundle = requireArguments()
        val name = bundle.get("userName").toString()
        val email = bundle.get("userEmail").toString()
        val pw = bundle.get("userPassword").toString()
        val gender = bundle.get("userGender").toString()

        nextButton4.setOnClickListener {
            val df = SimpleDateFormat("yyyyMMdd")
            val prefs = requireActivity().getSharedPreferences(mPref,Context.MODE_PRIVATE)
            if (!textView_birth.text.toString().contains("-")){ //if user edited
                val bd = df.format(c.time)
                Handler(Looper.getMainLooper()).postDelayed({
                    LoginManager(requireActivity(),email,pw).login()
                },2000)
                addUserToDB(name, pw,email,gender,bd, prefs.getString("pref_fcm_token","null").toString())
            }
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

    fun addUserToDB(name:String, pw:String,email:String,gender:String,bd:String, token:String) {
//        val url = "http://114.70.23.77:8002/v1/user/account/signup"
        val url = "http://192.168.104.40:8002/v1/user/account/signup"

        val queue = Volley.newRequestQueue(requireActivity().applicationContext)
        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                Log.d("su4: add res", response)
            },
            Response.ErrorListener { error -> Log.d("su4: error", error.toString()) }
        ) {
            override fun getParams(): MutableMap<String, String>? {
                val params = hashMapOf<String, String>()
                params["user_name"] = name
                params["user_pw"] = pw
                params["user_email"] = email
                params["user_gender"] = gender
                params["user_bd"] = bd
                params["user_token"] = token
                return params
            }
        }
        queue.add(stringRequest)
        val prefs = requireActivity().getPreferences(Context.MODE_PRIVATE)
        prefs.edit().putBoolean(getString(R.string.pref_previously_started), true).apply()
//          }

        val intent = Intent(requireActivity(), UserMainActivity::class.java)
        startActivity(intent)
    }
}
