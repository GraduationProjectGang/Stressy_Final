package com.android.stressy.activity.sign_up

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.fragment_sign_up4.*
import org.json.JSONObject
import java.util.*


class SignUp4Fragment : androidx.fragment.app.Fragment() {
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
            val bdString = df.format(c)
            Log.d("volvol bd",bdString)
            getFcmToken()
            addUserToDB(bdString)
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
                val editor = prefs.edit()
                if (prefs.getString("pref_fcm_token",getString(R.string.pref_fcm_token)) != token) {
                    Log.d("fcm:", "new token")
                    //add to db
                    val url = "http://114.70.23.77:8002/v1/user/fcm/newtoken"
                    val queue = Volley.newRequestQueue(requireContext())
                    val stringRequest = object : StringRequest(
                        Method.POST,url,
                        Response.Listener<String> { response ->
                            val jsonObject = JSONObject(response)
                            val jwtToken = jsonObject.getString("jwtToken")
                            editor.putString("user_jwt",jwtToken).apply()
                            Log.d("jwtjwt",jwtToken)
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
                    val edit = prefs.edit() as SharedPreferences.Editor
                    edit.putString("pref_fcm_token", token)
                    edit.commit()
                }

            })


    }
    fun addUserToDB(bd:String){
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
                    params.put("user_gender",userGender.toString()) //나중에
                    params.put("user_birthday",bd)
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
        requireActivity().finish()
    }
}