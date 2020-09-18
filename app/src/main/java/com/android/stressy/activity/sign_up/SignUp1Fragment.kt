package com.android.stressy.activity.sign_up

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.android.stressy.R
import kotlinx.android.synthetic.main.fragment_sign_up1.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SignUp1Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SignUp1Fragment : Fragment() {
    // TODO: Rename and change types of parameters

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        init()
        return inflater.inflate(R.layout.fragment_sign_up1, container, false)
    }
    fun init(){
        nextButton1.setOnClickListener {
            toSignUp2()
        }
    }
    fun toSignUp2(){
        val userName = answer_name.text.toString()
        var bundle = bundleOf("userName" to userName)
        view?.findNavController()?.navigate(R.id.action_signUp1Fragment_to_signUp2Fragment, bundle)
    }
}