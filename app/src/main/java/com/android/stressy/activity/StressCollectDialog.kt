package com.android.stressy.activity

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.room.Room
import com.android.stressy.R
import com.android.stressy.dataclass.db.StressScoreData
import com.android.stressy.dataclass.db.StressScoreDatabase
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.dialog_stress_collect.*
import java.text.SimpleDateFormat


class StressCollectDialog : DialogFragment() {
    val dateFormat = SimpleDateFormat("yyyyMMdd.HH:mm:ss")
    private val stressCollectRequest = 111

    private lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_stress_collect, container, false)
    }

    override fun onResume() {
        super.onResume()
        val width = resources.getDimensionPixelSize(R.dimen.pop_up_width)
        val height = resources.getDimensionPixelSize(R.dimen.pop_up_height)
        dialog!!.window!!.setLayout(width, height)
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
            when(i){
                R.id.radioButton-> stressScore = 0
                R.id.radioButton2-> stressScore = 1
                R.id.radioButton3-> stressScore = 2
                R.id.radioButton4-> stressScore = 3
            }
        }
        stressFinishBtn.setOnClickListener {
            if (stressScore == 9) {
                Toast.makeText(requireContext(), "질문에 답해주세요", Toast.LENGTH_SHORT).show()
            } else {

                Log.d("surveyscore", stressScore.toString())
                Toast.makeText(requireActivity(), "감사합니다", Toast.LENGTH_SHORT).show()

                var stCount = prefs.getInt(getString(R.string.stress_collect_count), 0)
                //TODO:설문 인덱스 -> db AI로 수정
                Log.w("SCA_COUNT", stCount.toString())

                val timestamp = System.currentTimeMillis()

//                val edit = prefs.edit() as SharedPreferences.Editor
//                edit.putInt(getString(R.string.stress_collect_count), stCount + 1)
//                edit.commit()


                save(StressScoreData(timestamp,stressScore))
                val noti = requireArguments().getInt("notificationCode")
                if (noti == stressCollectRequest)
                    dismiss()
                else{
                    dialog?.dismiss()

                }
            }

        }
    }
    fun save(data: StressScoreData){
        val dbObject = Room.databaseBuilder(
            requireActivity().applicationContext,
            StressScoreDatabase::class.java, "stress"
        ).fallbackToDestructiveMigration().build().stressScoreDataDao()

//        dbObject.insert(data)
    }
}