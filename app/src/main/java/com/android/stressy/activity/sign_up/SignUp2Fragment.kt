package com.android.stressy.activity.sign_up

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.android.stressy.R
import kotlinx.android.synthetic.main.fragment_sign_up2.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SignUp1Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SignUp2Fragment : androidx.fragment.app.Fragment() {
    // TODO: Rename and change types of parameters

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_sign_up2, container, false)
        return rootView
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        init(view)
    }
    fun init(rootView: View){
        nextButton2.setOnClickListener {
            val nameInput = editText_name.text.toString()
            toSignUp3(nameInput)
        }
    }

    fun toSignUp3(userName:String){
        var bundle = arguments
        bundle!!.putString("userName", userName)
        view?.findNavController()?.navigate(R.id.action_signUp2Fragment_to_signUp3Fragment, bundle)
    }
}