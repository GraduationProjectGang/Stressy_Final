package com.android.stressy.activity.sign_up

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.android.stressy.R
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_sign_up1.*
import java.util.regex.Pattern


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SignUp1Fragment : androidx.fragment.app.Fragment() {

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
    }

    fun init(){
        nextButton1.setOnClickListener {
            //if email is valid
            val emailInput = editText_email.text.toString()
            val passwordInput = editText_password.text.toString()
            val passwordInput2 = editText_password2.text.toString()
            var flag = true

            //EMAIL 체크
            if (!("@" in emailInput) && !("." in emailInput)){
                flag = false
                guide_email.visibility = TextView.VISIBLE
            }else if(!requestEmailCheck(emailInput)) {
                flag = false
                guide_email.visibility = TextView.VISIBLE
                guide_email.text = "이미 가입되어 있는 이메일입니다."
            }else{
                guide_email.visibility = TextView.INVISIBLE
            }

            //비번 체크
            if (isValidPassword(passwordInput) or (passwordInput.length <= 8)){
                flag = false
                guide_password.visibility = TextView.VISIBLE
            }else{
                guide_password.visibility = TextView.INVISIBLE
            }

            if (passwordInput != passwordInput2){
                flag = false
                guide_password2.visibility = TextView.VISIBLE
            }else
                guide_password2.visibility = TextView.INVISIBLE

            if(flag) toSignUp2(emailInput,passwordInput)
        }
    }
    fun toSignUp2(userEmail:String, userPassword:String){
        var bundle = bundleOf("userEmail" to userEmail, "userPassword" to userPassword)
        view?.findNavController()?.navigate(R.id.action_signUp1Fragment_to_signUp2Fragment, bundle)
    }
    fun requestEmailCheck(user_email:String):Boolean{
        val url = "http://114.70.23.77:8002/v1/user/account/validemail"
        val queue = Volley.newRequestQueue(activity!!.applicationContext)
        val stringRequest = object : StringRequest(Request.Method.POST,url,
            Response.Listener<String> {response ->
                Log.d("volvol", response) },
            Response.ErrorListener {error ->  Log.d("volvol", error.toString()) }

        ){
            override fun getParams(): MutableMap<String, String>? {
                val params = hashMapOf<String,String>()
                params.put("user_email",user_email)
                return params
            }
        }
        queue.add(stringRequest)
        return true
    }

    fun isValidPassword(input:String):Boolean{
        val PASSWORD_PATTERN =
            "^(?=.*\\\\d)(?=.*[~`!@#\$%\\\\^&*()-])(?=.*[a-z])(?=.*[A-Z]).{8,}\$"
        val pattern = Pattern.compile(PASSWORD_PATTERN)
        val matcher = pattern.matcher(input)

        return matcher.matches()
    }

}