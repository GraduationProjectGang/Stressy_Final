package com.android.stressy.activity.sign_up

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.android.stressy.R
import com.android.stressy.dataclass.BaseUrl
import com.android.stressy.etc.LoginManager
import com.android.stressy.etc.LoginViewModel
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_sign_up1.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import java.util.regex.Pattern


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SignUp1Fragment : androidx.fragment.app.Fragment() {
    var validFlag = true
    lateinit var loginManager : LoginManager
    lateinit var loginViewModel : LoginViewModel
    lateinit var emailInput :String
    lateinit var passwordInput:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up1, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()
    }
    fun init(){
        nextButton1.setOnClickListener{
            validFlag = true
            emailInput = editText_email.text.toString()
            passwordInput = editText_password.text.toString()
            val passwordInput2 = editText_password2.text.toString()

            //EMAIL 체크
            guide_email.text= "" //초기화
            if ("@" !in emailInput || "." !in emailInput){
                validFlag = false
                guide_email.text= getString(R.string.error_email)
            }
            //비번 체크
            guide_password.text = "" //초기화
            Log.d("valval",passwordInput.length.toString())
            if (!isValidPassword(passwordInput) or (passwordInput.length <= 8)){
                validFlag = false
                guide_password.text = getString(R.string.guide_password)
            }else if (passwordInput != passwordInput2){
                validFlag = false
                guide_password.text = getString(R.string.guide_password2)
            }
            Log.d("valval validflag",validFlag.toString())


            if (validFlag)
                requestEmailCheck(emailInput)

        }

    }

    fun requestEmailCheck(user_email:String){
        val url = BaseUrl.url + "/user/account/validemail"
        val jsonObject = JSONObject().put("user_email",user_email)
        volley(requireActivity(),url,jsonObject)
    }

    fun volley(context: Context, url:String, inputJson: JSONObject) {
        val queue = Volley.newRequestQueue(context)
        val request = object : JsonObjectRequest(
            Method.POST,url,inputJson,
            Response.Listener<JSONObject> { response ->
                Log.d("volvolres",response.toString())
                toSignUp2(emailInput,passwordInput)
            },
            Response.ErrorListener { error ->
                Log.d("volvolerr", error.toString())
                Toast.makeText(context,"중복된 이메일입니다.",Toast.LENGTH_SHORT).show()
            }
        ){}
        queue.add(request)
    }

    fun isValidPassword(input:String):Boolean{
        val PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[\$@\$!%*#?&]).{8,15}.\$"
        val pattern = Pattern.compile(PASSWORD_PATTERN)
        val matcher = pattern.matcher(input)
        Log.d("valval: matcher",matcher.matches().toString())

        return matcher.matches()
    }

    fun toSignUp2(userEmail:String, userPassword:String){
        var bundle = bundleOf("userEmail" to userEmail, "userPassword" to userPassword)
        view?.findNavController()?.navigate(R.id.action_signUp1Fragment_to_signUp2Fragment, bundle)
    }

}