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
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_sign_up1.*
import org.json.JSONObject
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
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
        val url = "http://114.70.23.77:8002/v1/user/account/validemail"
        val queue = Volley.newRequestQueue(requireActivity())
        var checkFlag = false
        val param = mutableMapOf<String,String>()
        param["user_email"] = user_email
        val jsonObj = JSONObject(param as Map<String, String>)

        val myVolleyResponse = volley(requireActivity(), url, jsonObj )
        Log.d("volvolvalidemail", myVolleyResponse.toString())
        return true
    }

    fun volley(context: Context, url:String, inputJson: JSONObject):JSONObject{
        val url = "http://114.70.23.77:8002/v1/user/account/validemail"
        val queue = Volley.newRequestQueue(context)
        var checkFlag = false
        val param = mutableMapOf<String,String>()
        var response = JSONObject()
        Log.d("volvol1",inputJson.toString())

//        val future = RequestFuture.newFuture<JSONObject>()
//        val request = JsonObjectRequest(Request.Method.POST,url,params,future,future)
//        queue.add(request)
//
//        try {
//            response = future.get(10, TimeUnit.SECONDS)//wait response
//        } catch (e: InterruptedException) {
//            Log.e("Retrieve cards api call interrupted.", e.toString())
//            future.onErrorResponse(VolleyError(e))
//        } catch (e: ExecutionException) {
//            Log.e("Retrieve cards api call failed.", e.toString())
//            future.onErrorResponse(VolleyError(e))
//        }

        val stringRequest = object : StringRequest(
            Method.POST,url,
            com.android.volley.Response.Listener<String> { response ->
                Log.d("volvol2", response) },
            com.android.volley.Response.ErrorListener { error ->  Log.d("volvol", error.toString()) }
        ){
            override fun getParams(): MutableMap<String, String>? {
                val params = hashMapOf<String,String>()
                Log.d("volvol",inputJson.toString())
                params.put("user_email", inputJson["user_email"] as String)
                return params
            }
        }
        queue.add(stringRequest)


        Log.d("volvolcheckFlag", checkFlag.toString())
        return response

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