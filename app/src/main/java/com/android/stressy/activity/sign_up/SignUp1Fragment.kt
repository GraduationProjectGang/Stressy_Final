package com.android.stressy.activity.sign_up

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.android.stressy.R
import com.android.stressy.etc.VolleyCallback
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_sign_up1.*
import org.json.JSONObject
import java.util.regex.Pattern


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SignUp1Fragment : androidx.fragment.app.Fragment() {
    lateinit var response:String
    lateinit var code:String
    lateinit var stringRequest:StringRequest
    lateinit var url: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        init()
        //TODO: sign up 도중 뒤로가기 방지 조치해야댐
    }

    fun init(){
        nextButton1.setOnClickListener {
            //if email is valid
            val emailInput = editText_email.text.toString()
            val passwordInput = editText_password.text.toString()
            val passwordInput2 = editText_password2.text.toString()
            var flag = true
            var emailvalidflag = requestEmailCheck(emailInput)

            //EMAIL 체크
            if (!("@" in emailInput) && !("." in emailInput)){
                flag = false
                guide_email.text= getString(R.string.error_email)
            }else{
                if(!emailvalidflag) {
                    flag = false
                    guide_email.text = "이미 가입되어 있는 이메일입니다."
                }else{
                    guide_email.text = null
                }
            }

            //비번 체크
            if (isValidPassword(passwordInput) or (passwordInput.length <= 8)){
                flag = false
                guide_password.text = getString(R.string.guide_password)
            }else{
                guide_password.text = ""
            }

            if (passwordInput != passwordInput2){
                flag = false
                guide_password.text = getString(R.string.guide_password2)

            }else{
                guide_password.text = null
            }

            if(flag) toSignUp2(emailInput,passwordInput)
        }
    }
    fun requestEmailCheck(user_email:String):Boolean{
        url = "http://192.168.104.40:8002/v1/user/account/validemail"
        val mutableMap = mutableMapOf<String,String>()
        mutableMap["user_email"] = user_email
        volley(requireActivity(),url,mutableMap)
        return true
    }


    fun volley(context: Context, url:String, inputJson: MutableMap<String,String>) {
        val queue = Volley.newRequestQueue(context)
        val request = object : JsonObjectRequest(
            Method.POST,url,null,
            Response.Listener<JSONObject> { response ->
                Log.d("volvolres",response.toString())
                doSomething()
                if (response["code"] == "200"){
                    code = "200"
                    doSomething()
                }


            },
            Response.ErrorListener { error ->  Log.d("volvol", error.toString()) }
        ){
            override fun getParams(): MutableMap<String, String>? {
                return inputJson
            }
        }
        queue.add(request)

    }
    fun doSomething(){
        Log.d("volvol","didsomething")
    }
    override fun onResume(){
        super.onResume()
        getStringRequest(object : VolleyCallback {
            override fun onSuccess(result: String) {
                response = result
            }
        })
    }

    fun getStringRequest (callback: VolleyCallback){

    }
    fun isValidPassword(input:String):Boolean{
        val PASSWORD_PATTERN =
            "^(?=.*\\\\d)(?=.*[~`!@#\$%\\\\^&*()-])(?=.*[a-z])(?=.*[A-Z]).{8,}\$"
        val pattern = Pattern.compile(PASSWORD_PATTERN)
        val matcher = pattern.matcher(input)

        return matcher.matches()
    }

    fun toSignUp2(userEmail:String, userPassword:String){
        var bundle = bundleOf("userEmail" to userEmail, "userPassword" to userPassword)
        view?.findNavController()?.navigate(R.id.action_signUp1Fragment_to_signUp2Fragment, bundle)
    }

}