package com.android.stressy.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.android.stressy.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.activity_my_page.*

/**
 * A simple [Fragment] subclass.
 * Use the [BottomSheetFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BottomSheetFragment : BottomSheetDialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView =  inflater.inflate(R.layout.fragment_bottom_sheet, container, false)
        init()
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireDialog() as BottomSheetDialog).dismissWithAnimation = true
    }
    fun init(){
        button_change_password.setOnClickListener {
            val  dialog = CheckPwDialog()
            dialog.show(requireActivity().supportFragmentManager, "dialog")
        }
        button_withdraw.setOnClickListener {
            val dialog = WithdrawDialog()
            dialog.show(requireActivity().supportFragmentManager,"withdraw")
        }
        button_logout.setOnClickListener {
            val dialog = LogoutDialog()
            dialog.show(requireActivity().supportFragmentManager,"logout")
        }
    }
    companion object {
        const val TAG = "ModalBottomSheet"
    }

}