package com.android.stressy.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.stressy.R
import kotlinx.android.synthetic.main.activity_my_page.*

class MyPageActivity : AppCompatActivity() {
//    lateinit var mDialogResult: DiaRe
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)
        init()
        makeGraphFragment()
    }

    fun makeGraphFragment(){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val graphFragment = StressGraphFragment()
        fragmentTransaction.add(R.id.graphFragment, graphFragment).commit()
    }

    fun init(){

        account_settings.setOnClickListener {
            val modalBottomSheet = BottomSheetFragment()
            modalBottomSheet.show(supportFragmentManager, BottomSheetFragment.TAG)
        }
    }

    fun checkOriginalPassword(pw:String):Boolean{
        return false
    }
}
