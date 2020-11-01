package com.android.stressy.activity.sign_up

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.stressy.R
import com.android.stressy.activity.UserMainActivity
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.fragment_sign_up4.*
import org.json.JSONObject
import java.util.*
import kotlin.properties.Delegates


class SignUp4Fragment : androidx.fragment.app.Fragment() {
    var endTime by Delegates.notNull<Long>()
    var startTime by Delegates.notNull<Long>()
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
//            val df = SimpleDateFormat("yyyy-MM-dd")
            if (!textView_birth.text.toString().contains("-")){
                Log.d("su4 buttonclick",y.toString()+m.toString()+d.toString())
                val bdString = y.toString()+m.toString()+d.toString()
                Log.d("su4 bdstring",bdString)
                startTime = Calendar.getInstance().timeInMillis
                getFcmToken(bdString)
                val end = Calendar.getInstance().timeInMillis
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

    fun addUserToDB(bd:String, token:String) {
        val bundle = requireArguments()
        if (!bundle.isEmpty()) { //TODO
            val userName = bundle.get("userName").toString()
            val userEmail = bundle.get("userEmail").toString()
            val userPassword = bundle.get("userPassword").toString()
            val userGender = bundle.get("userGender").toString()


            Log.d("su4 token in add", token)


            val url = "http://114.70.23.77:8002/v1/user/account/signup"
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
                    params["user_name"] = userName
                    params["user_pw"] = userPassword
                    params["user_email"] = userEmail
                    params["user_gender"] = userGender
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

}