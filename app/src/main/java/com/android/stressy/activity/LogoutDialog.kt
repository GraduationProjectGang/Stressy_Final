package com.android.stressy.activity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.android.stressy.R
import kotlinx.android.synthetic.main.dialog_logout.*


class LogoutDialog : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_withdraw, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    fun init() {
        val pref_auto_email = "mEmail"
        val pref_auto_password = "mPassword"
        button_logout_no.setOnClickListener {
//            this.dismiss()
        }
        button_logout_yes.setOnClickListener {
            Toast.makeText(requireActivity(),"로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
            val editor = requireActivity().getPreferences(Context.MODE_PRIVATE).edit()
            editor.remove(pref_auto_email)
            editor.remove(pref_auto_password)
            editor.apply()
        }
    }
}