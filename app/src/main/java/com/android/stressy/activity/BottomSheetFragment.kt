package com.android.stressy.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.android.stressy.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_bottom_sheet.*

/**
 * A simple [Fragment] subclass.
 * Use the [BottomSheetFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BottomSheetFragment : BottomSheetDialogFragment() {
    lateinit var user_email:String
    lateinit var user_pw : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireDialog() as BottomSheetDialog).dismissWithAnimation = true
    }
    fun init(){
        user_email = requireArguments().getString("user_email",null)
        user_pw = requireArguments().getString("user_pw",null)
        val bundle = Bundle()
        bundle.putString("user_email",user_email)
        bundle.putString("user_pw",user_pw)

        button_change_password.setOnClickListener {
            val  dialog = CheckPwDialog()
            dialog.arguments = bundle
            dialog.show(requireActivity().supportFragmentManager, "dialog")
        }
        button_withdraw.setOnClickListener {
            val dialog = WithdrawDialog()
            dialog.arguments = bundle
            dialog.show(requireActivity().supportFragmentManager,"withdraw")
        }
        button_logout.setOnClickListener {
            val dialog = LogoutDialog()
            dialog.arguments = bundle
            dialog.show(requireActivity().supportFragmentManager,"logout")
        }
    }
    companion object {
        const val TAG = "ModalBottomSheet"
    }

}