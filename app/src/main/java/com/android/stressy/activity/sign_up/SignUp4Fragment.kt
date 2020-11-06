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
import com.android.stressy.dataclass.BaseUrl
import com.android.stressy.etc.LoginManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.JsonRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.fragment_sign_up4.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.json.json
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class SignUp4Fragment : androidx.fragment.app.Fragment() {
    val mPref = "my_pref"
    lateinit var loginManager:LoginManager
    val mutex = Mutex()
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
//        val name = "김"
//        val email = "asfaasdsdf@.sdf"
//        val pw = "rlawlgy5573!!"
//        val gender = "female"
        loginManager = LoginManager(requireActivity())

        nextButton4.setOnClickListener {
            val df = SimpleDateFormat("yyyyMMdd")
            val prefs = requireActivity().getSharedPreferences(mPref,Context.MODE_PRIVATE)
            if (!textView_birth.text.toString().contains("-")){ //if user edited
                val bd = df.format(c.time)
                var token = "null"
                Log.d("su4:", "click")

//                LoginManager(requireActivity(),email,pw).login()
                val thread = Thread(Runnable {
                    run {
                        Log.d("su4 thread","in")
                        token = getFcmToken(name,email,pw,gender,bd)
                    }
                })
                thread.start()
                Thread.sleep(1000)
                thread.join()

                Log.d("su4 thread","join")
                Log.d("su4 token",token)
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

    fun getFcmToken(
        name: String,
        email: String,
        pw: String,
        gender: String,
        bd:String
    ):String{
        var token = ""
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("logman", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                token = task.result?.token.toString()
                val prefs = requireActivity().getSharedPreferences(mPref,Context.MODE_PRIVATE)
                Log.d("logman:token",token)
                Log.d("logman:original token",prefs.getString("pref_fcm_token",requireActivity().getString(R.string.pref_fcm_token)).toString())

                if (prefs.getString("pref_fcm_token",requireActivity().getString(R.string.pref_fcm_token)) != token) {
                    Log.d("logman", "new token")
                    //add to db
                    val url = BaseUrl.url + "/user/fcm/newtoken"
                    val queue = Volley.newRequestQueue(requireActivity())
                    val stringRequest = object : StringRequest(
                        Method.POST,url,
                        Response.Listener<String> { response ->
                            val jsonObject = JSONObject(response)
                            val tokenId = jsonObject.getString("id")
                            Log.d("su4:gettoken",response.toString())
                            addUserToDB(name, pw,email,gender,bd, tokenId)
                        },
                        Response.ErrorListener { error ->  Log.d("logman:error", error.toString()) }
                    ){
                        override fun getParams(): MutableMap<String, String>? {
                            val params = hashMapOf<String,String>()
                            params["user_email"] = email
                            params["fcm_token"] = token
                            return params
                        }
                    }
                    queue.add(stringRequest)

                    //add to sharedpreference
                    prefs.edit().putString("pref_fcm_token", token).apply()
                }
            })
        return token
    }
    private fun addUserToDB(name:String, pw:String, email:String, gender:String, bd:String, token:String) {
        val url = BaseUrl.url + "/user/account/signup"
//        val url = "http://192.168.104.40:8002/v1/user/account/signup"

        val queue = Volley.newRequestQueue(requireActivity().applicationContext)
        val jsonObject = JSONObject()
        jsonObject.put("user_name",name)
        jsonObject.put("user_pw",pw)
        jsonObject.put("user_email",email)
        jsonObject.put("user_gender",gender)
        jsonObject.put("user_bd",bd)
        jsonObject.put("user_token",token)
        val jsonRequest = object : JsonObjectRequest(
            Request.Method.POST, url,jsonObject,
            Response.Listener<JSONObject> { response ->
                Log.d("su4: add res", response.toString())
                if (response.getString("code")=="200"){
//                    val intent = Intent(requireActivity(), UserMainActivity::class.java)
//                    startActivity(intent)
                    loginManager.login(email,pw)
                }
            },
            Response.ErrorListener { error -> Log.d("su4: error", error.toString()) }
        ) {
        }
        queue.add(jsonRequest)
        val prefs = requireActivity().getPreferences(Context.MODE_PRIVATE)
        prefs.edit().putBoolean(getString(R.string.pref_previously_started), true).apply()
//          }

    }
}
