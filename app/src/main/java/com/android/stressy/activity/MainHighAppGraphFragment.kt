package com.android.stressy.activity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.android.stressy.R
import com.android.stressy.dataclass.db.HighAppDatabase
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet


class MainHighAppGraphFragment() : Fragment() {
    val desArr = arrayListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_high_app_graph, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var stressChart = requireActivity().findViewById<BarChart>(R.id.mainHighBarChart)
        val entries = getData()
        initChart(stressChart, entries)
    }

    fun getData():ArrayList<BarEntry>{
        val dbObject = Room.databaseBuilder(
            requireContext(),
            HighAppDatabase::class.java, "highApp"
        ).fallbackToDestructiveMigration().allowMainThreadQueries().build().highAppDataDao()

//        dbObject.deteleAll()
//        for (i in 0..10){
//            dbObject.insert(HighAppData((1..14).random()))
//        }

        val data = dbObject.getAll()
        var startSize = 0
        if (startSize > 100) startSize == data.size - 100

        val dataArr = FloatArray(14){(0f)}

        for (idx in startSize until data.size){
            val cate = data[idx].cate
            if (cate != 0)
                dataArr[data[idx].cate-1] = dataArr[data[idx].cate-1] + 1
        }

        var map = mutableMapOf<Int,Float>()
        for (i in dataArr.indices){
            map.put(i+1,dataArr[i])
        }

        map = map.toList().sortedWith(compareByDescending {it.second}).toMap().toMutableMap()

        Log.d("dataArr",map.toString())

        val mapKey = map.keys.toIntArray()
        Log.d("dataArr.mapkey",mapKey.toString())

        val entries = arrayListOf<BarEntry>()


        for (idx in 0..3){
            desArr.add(getCate(mapKey[idx]))
            val entry = BarEntry(idx.toFloat(), map[mapKey[idx]]!!.toFloat())
            Log.d("dataArr.en",entry.toString())
            entries.add(entry)
        }

        return entries
    }

    fun getCate(idx: Int):String{
        var cate = ""
        when(idx){
            1 -> cate = "카메라"
            2 -> cate = "라이프스타일"
            3 -> cate = "SNS"
            4 -> cate = "엔터테인먼트"
            5 -> cate = "커뮤니케이션"
            6 -> cate = "게임"
            7 -> cate = "설정"
            8 -> cate = "교육"
            9 -> cate = "쇼핑"
            10 -> cate = "교통"
            11 -> cate = "건강"
            12 -> cate = "음식"
            13 -> cate = "금융"
            14 -> cate = "브라우징"
        }
        return cate
    }

    fun initChart(chart:BarChart, entries:ArrayList<BarEntry>){
//        val roundedBarChartRenderer = RoundedBarChartRenderer(
//            chart,
//            chart.getAnimator(),
//            chart.getViewPortHandler()
//        )
//        roundedBarChartRenderer.setmRadius(20f)
//        chart.setRenderer(roundedBarChartRenderer)


//        val data = listOf<Int>(2,4,3,4,2,3,4)

//        val entries = arrayListOf<BarEntry>()
//        for (i in 0 until 4) {
//            entries.add(
//                BarEntry(i.toFloat(), (3..15).random().toFloat())
//            )
//            Log.d("chacha",entries[i].toString())
//        }

//        val entries = ArrayList<BarEntry>()
//        for (i in data.indices){
//            entries.add(BarEntry(i.toFloat(), data[i].toFloat()))
//            Log.d("mainfrag",i.toFloat().toString()+"    " +data[i].toFloat().toString())
//
//        }

        val dataSet = BarDataSet(entries,"dtd")

        dataSet.apply {
            setColor(R.color.colorPrimaryDark)
            setDrawValues(false)

            valueTextSize = 10f
//            barBorderWidth = 1f

        }

        val dataSets = arrayListOf<IBarDataSet>(dataSet)
        val color1 = Color.parseColor("#9DAFD9")
        val color2 = Color.parseColor("#6C88C6")
        val color3 = Color.parseColor("#3B60B3")
        val color4 = Color.parseColor("#0A38A0")
        val colorArr = mutableListOf<Int>(color1,color2,color3,color4)
        dataSet.colors = colorArr

        val barData = BarData(dataSets)
        barData.barWidth = 0.7f
        //barchart design

        chart.axisLeft.apply {
            setDrawGridLines(false)
            isEnabled = false
        }
        chart.axisRight.apply {
            textSize = 12f
            axisMinimum = 0.0f
            setDrawGridLines(false)
//            granularity = 1f
//            isGranularityEnabled = false
        }
        chart.xAxis.apply {
            granularity = 1f
            textSize = 13f
//            axisMinimum = -0.5f
            setPosition(XAxis.XAxisPosition.BOTTOM)
            setDrawGridLines(false)
            var stressDescription = desArr
            setValueFormatter(IndexAxisValueFormatter(stressDescription))
        }
        chart.apply {
            defaultFocusHighlightEnabled = true
            description.isEnabled = false
            this.data = barData
            legend.isEnabled = false
            disableScroll()
            extraBottomOffset = 10f
            centerViewTo(chart.getXChartMax(),0f,YAxis.AxisDependency.RIGHT)
            setScaleEnabled(false)
//            setViewPortOffsets(200f, 0f, 0f, 40f)
            animateY(1000)
            invalidate()
        }
    }



}

