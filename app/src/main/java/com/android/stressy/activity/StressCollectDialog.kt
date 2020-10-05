package com.android.stressy.activity

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.RadioButton
import androidx.fragment.app.DialogFragment
import com.android.stressy.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.dialog_stress_collect.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime

class StressCollectDialog : DialogFragment() {
    val dateFormat = SimpleDateFormat("yyyyMMdd.HH:mm:ss")
    lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    fun init() {
        prefs = PreferenceManager.getDefaultSharedPreferences(activity!!.baseContext)

        val key = prefs.getString(getString(R.string.pref_previously_logined), "null")
        var stressScore = 9
        stressRadio1.setOnCheckedChangeListener { radioGroup, i ->
            //radiobutton 값 받아서 input에 저장
            stressScore = activity!!.findViewById<RadioButton>(i).text.toString().get(0).toInt() - 48
        }
        stressFinishBtn.setOnClickListener {
            if (stressScore == 9) {
                Snackbar.make(it, "질문에 답해주세요", Snackbar.LENGTH_SHORT).show()
            } else {

                Log.d("surveyscore", stressScore.toString())
                Snackbar.make(it, "감사합니다", Snackbar.LENGTH_SHORT).show()


                var stCount = prefs.getInt(getString(R.string.stress_collect_count), 0)
                //TODO:설문 인덱스 -> db AI로 수정
                Log.w("SCA_COUNT", stCount.toString())

                val timestamp = System.currentTimeMillis()

//                val edit = prefs.edit() as SharedPreferences.Editor
//                edit.putInt(getString(R.string.stress_collect_count), stCount + 1)
//                edit.commit()


            }

        }
    }
}