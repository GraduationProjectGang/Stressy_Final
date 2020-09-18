package com.android.stressy.activity.sign_up

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.android.stressy.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_sign_up2.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SignUp2Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SignUp2Fragment : Fragment() {
    var flag_female = false
    var flag_male = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        initButton()
        return inflater.inflate(R.layout.fragment_sign_up2, container, false)
    }


    fun initButton(){
        female.setOnClickListener {
            flag_female = true
            flag_male = false
            female.setBackgroundResource(R.drawable.female_clicked)
            male.setBackgroundResource(R.drawable.male)

        }
        male.setOnClickListener {
            flag_female = false
            flag_male = true
            female.setBackgroundResource(R.drawable.female)
            male.setBackgroundResource(R.drawable.male_clicked)
        }
        nextButton2.setOnClickListener {
            if (!(flag_female or flag_male)){
                Snackbar.make(it,"성별을 선택해 주세요.",Snackbar.LENGTH_SHORT).show()
            }

            //true = female, false = male, 성별간 갈등 조장 아님^^
            var gender  = flag_female
            toSignUp3(gender)
        }
    }
    fun toSignUp3(userGender:Boolean){
        var bundle = bundleOf("userGender" to userGender)
        view?.findNavController()?.navigate(R.id.action_signUp2Fragment_to_signUp3Fragment, bundle)
    }
}