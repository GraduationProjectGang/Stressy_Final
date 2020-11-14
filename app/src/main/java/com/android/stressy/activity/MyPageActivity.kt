package com.android.stressy.activity

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import androidx.appcompat.app.AppCompatActivity
import com.android.stressy.R
import kotlinx.android.synthetic.main.activity_my_page.*

class MyPageActivity : AppCompatActivity() {
    lateinit var user_email:String
    lateinit var user_pw:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)
        init()
        makeGraphFragment()
    }

    fun makeGraphFragment(){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val graphFragment = WeeklyStressGraphFragment()
        fragmentTransaction.add(R.id.weeklyGraph, graphFragment)
        val fragmentTransaction2 = supportFragmentManager.beginTransaction()
        val graphFragment2 = WeeklyTimeGraphFragment()
        fragmentTransaction.add(R.id.hourlyGraph, graphFragment2).commit()
    }

    fun init(){
        user_email = intent.getStringExtra("user_email").toString()
        user_pw = intent.getStringExtra("user_pw").toString()


        account_settings.setOnClickListener {
            val modalBottomSheet = BottomSheetFragment()
            val bundle = Bundle()
            bundle.putString("user_email",user_email)
            bundle.putString("user_pw",user_pw)
            modalBottomSheet.arguments = bundle
            modalBottomSheet.show(supportFragmentManager, BottomSheetFragment.TAG)
        }
        val mystring = "프로젝트 가이드 다시보기"
        val content = SpannableString(mystring)
        content.setSpan(UnderlineSpan(), 0, mystring.length, 0)
        tutorialAgain.setText(content)
        tutorialAgain.setOnClickListener {
            val intent = Intent(this, Tutorial1Activity::class.java)
            startActivity(intent)
        }
    }

    fun checkOriginalPassword(pw:String):Boolean{
        return false
    }
}
