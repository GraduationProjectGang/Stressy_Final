package com.android.stressy.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.stressy.R
import kotlinx.android.synthetic.main.activity_tutorial1.*


class Tutorial1Activity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial1)
        nextButton1.setOnClickListener {
            val intent = Intent(this,
                Tutorial2Activity::class.java) //다음이어질 액티비티
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
    }
}
