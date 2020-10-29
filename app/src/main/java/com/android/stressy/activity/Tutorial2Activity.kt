package com.android.stressy.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.android.stressy.R
import kotlinx.android.synthetic.main.activity_tutorial2.*

class Tutorial2Activity : AppCompatActivity() {
    val fm: FragmentManager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial2)

        nextButton3.setOnClickListener {
            finish()
        }
    }
}
