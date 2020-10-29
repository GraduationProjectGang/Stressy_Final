package com.android.stressy.activity

import android.app.AlertDialog
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.android.stressy.R
import kotlinx.android.synthetic.main.fragment_stats_permission.*
import kotlin.properties.Delegates


class StatsPermissionFragment : DialogFragment() {
    var granted = false
    lateinit var appOps: AppOpsManager
    var mode by Delegates.notNull<Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_stats_permission, container, false)
        setDisplay()
        if (ifPermitted()){
            // 권한이 있을 경우 다음 버튼
            button_permit.text = "다음"
        }
        val button_permit = rootView.findViewById<Button>(R.id.button_permit)

        button_permit.setOnClickListener {
            if (button_permit.text == "다음" ) {
                val intent = Intent(context, UserMainActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }else getPermission()
        }
        addWhiteList()
        return rootView
    }

    private fun setDisplay() {
        val w = requireActivity().windowManager
        val d = w.defaultDisplay
        val metrics = DisplayMetrics()
        d.getMetrics(metrics)

        var widthPixels = metrics.widthPixels
        var heightPixels = metrics.heightPixels
        try {
            // used when SDK_INT >= 17; includes window decorations (statusbar bar/menu bar)
            val realSize = Point()
            Display::class.java.getMethod("getRealSize", Point::class.java).invoke(d, realSize)
            widthPixels = realSize.x
            heightPixels = realSize.y
        } catch (ignored: Exception) {
        }
    }

    override fun onResume() {
        super.onResume()

        if (ifPermitted()){
            // 권한이 있을 경우 다음 버튼
            button_permit.text = "다음"
        }
    }

    override fun onPause() {
        super.onPause()
        if (ifPermitted()){
            // 권한이 없을 경우 권한 요구 페이지 이동
            button_permit.text = "다음"
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setTitle("권한요청")
    }
    fun addWhiteList() {
        val pm = requireActivity().getSystemService(Context.POWER_SERVICE) as PowerManager
        var isWhite = false
        isWhite = pm.isIgnoringBatteryOptimizations(requireActivity().packageName)

        if (!isWhite) {
            val setdialog = AlertDialog.Builder(requireActivity())
            setdialog.setTitle("추가 설정이 필요합니다.")
                .setMessage("어플을 문제없이 사용하기 위해서는 해당 어플을 \"배터리 사용량 최적화\" 목록에서 \"제외\"해야 합니다. 설정화면으로 이동하시겠습니까?")
                .setPositiveButton("네") { _, _ -> startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) }
                .setNegativeButton("아니오") { _, _ -> Toast.makeText(requireActivity(), "설정을 취소했습니다.", Toast.LENGTH_SHORT).show() }
                .create()
                .show()
        }
    }

    fun ifPermitted(): Boolean{
        appOps = requireActivity().getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,android.os.Process.myUid(), requireActivity()!!.getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
            granted = (requireActivity().checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED)
            Log.d("frafraif",granted.toString() + "1")
        } else {
            granted = (mode == AppOpsManager.MODE_ALLOWED)
            Log.d("frafraif",granted.toString() + "2")
        }
        return granted
    }

    private fun getPermission() {
        // 권한이 없을 경우 권한 요구 페이지 이동
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivity(intent)
    }
}