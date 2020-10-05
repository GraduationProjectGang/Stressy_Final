package com.android.stressy.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.stressy.R

class MyPageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)
        makeGraphFragment()
    }

    fun makeGraphFragment(){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val graphFragment = StressGraphFragment()
        fragmentTransaction.add(R.id.graphFragment, graphFragment).commit()
    }
}