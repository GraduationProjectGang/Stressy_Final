package com.android.stressy.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.stressy.R
import kotlinx.android.synthetic.main.activity_tutorial2.*

class Tutorial2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial2)
        nextButton2.setOnClickListener {
            val intent = Intent(this,
                Tutorial3Activity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()

        }
    }
}
