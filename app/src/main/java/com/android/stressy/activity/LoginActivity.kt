package com.android.stressy.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.stressy.R
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        init()
    }

    private fun init() {
        val email = login_email.text.toString()
        val pw = login_password.text.toString()
        loginButton.setOnClickListener {

        }
    }
}