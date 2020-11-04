package com.android.stressy.activity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.android.stressy.R
import com.android.stressy.dataclass.db.StressPredictedData
import com.android.stressy.dataclass.db.StressPredictedDatabase
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class StressGraphFragment : Fragment(), OnChartGestureListener {
    var relativeDate = 0
    lateinit var chart : LineChart
    lateinit var timeStampArr: ArrayList<Long>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_stress_graph, container, false)
        chart = rootView!!.findViewById(R.id.stressGraph) as LineChart
        initChart(chart,makeDataToBarEntry(relativeDate))

        return rootView
    }
    fun setChartData(chart: LineChart,entries:ArrayList<Entry>){


    }
    fun initChart(chart:LineChart,entries: ArrayList<Entry>){
        val dataSet = LineDataSet(entries,"stress")
        dataSet.apply {
            color = resources.getColor(R.color.colorPrimary)
            setLineWidth(2f)
            setCircleSize(4f)
            setCircleColor (resources.getColor(R.color.colorPrimaryDark))
            setDrawValues(false)
            valueTextSize = 13f
            setDrawHighlightIndicators(false)
            highLightColor = resources.getColor(R.color.colorPrimary)
        }

        val dataSets = arrayListOf<ILineDataSet>(dataSet)
        val lineData = LineData(dataSets)

        //barchart design
        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            textSize = 14f
            setDrawGridLines(false)
            granularity = 1f
            isGranularityEnabled = false

            val df = SimpleDateFormat("MM/dd")
            var dateString = arrayListOf<String>()
            for (date in timeStampArr){
                dateString.add(df.format(date))
            }
            valueFormatter = IndexAxisValueFormatter(dateString)
        }

        val stressDescription = arrayListOf("","낮음","보통","높음","매우높음")
        chart.axisLeft.apply {
            granularity = 1f
            textSize = 15f

            axisMinimum = 0.0f
            axisMaximum = 5.0f
            valueFormatter = IndexAxisValueFormatter(stressDescription)
//
//            val ll = LimitLine(week_average.toFloat(), "평균")
//            ll.lineColor = Color.RED
//            ll.lineWidth = 2f
//            ll.textColor = Color.RED
//            ll.textSize = 12f
//
//            addLimitLine(ll)

        }

        chart.apply {
            setBorderColor(Color.DKGRAY)
            axisRight.isEnabled = false
//            legend.apply {
//                textSize = 12f
//                verticalAlignment = Legend.LegendVerticalAlignment.TOP
//            }
        }

        chart.onChartGestureListener = this
        chart.run {
            this.data = lineData
//            setLine(true)
            Log.d("setset","invalidate")

            invalidate()
        }
    }

    private fun makeDataToBarEntry(relativeDate: Int): ArrayList<Entry> {
        val dbObject = Room.databaseBuilder(
            requireContext(),
            StressPredictedDatabase::class.java, "stressPredicted"
        ).fallbackToDestructiveMigration().allowMainThreadQueries().build().stressPredictedDao()

        timeStampArr = makeDateArray(relativeDate)
        val dataArr = arrayListOf<Float>() //날짜, 점수 맵
        Log.d("getdata",timeStampArr.size.toString())

        for (i in 0 until timeStampArr.size-1){
            val getData = dbObject.getFromTo(timeStampArr[i],timeStampArr[i+1])//하루동안의 데이터 받아오기
            var avg = 0f
            if (getData.isNotEmpty()){
                val df = SimpleDateFormat("MM/dd")
                val date = Date(timeStampArr[i])

                val tempDate = df.format(date)
                Log.d("getdata",tempDate+" "+ getData.size.toString())

                for (each in getData){
                    avg += each.stressPredicted
                }
                avg /= getData.size

            }
            dataArr.add(avg)
        }

        val entries = ArrayList<Entry>()
        for (i in dataArr.indices){
            entries.add(Entry(i.toFloat(), dataArr[i]))
        }
//        return@runBlocking entries
        return entries
    }
    override fun onChartFling(me1: MotionEvent?, me2: MotionEvent?, velocityX: Float, velocityY: Float) {
        if (velocityX > 0){
            relativeDate -= 1
        }else{
            relativeDate += 1
        }
        setChartData(chart,makeDataToBarEntry(relativeDate))
    }


    fun makeDateArray(relativeDate: Int): ArrayList<Long>{ //얼마만큼 왼쪽으로 swipe 하냐
        val c = Calendar.getInstance()
        val df = SimpleDateFormat("yyyyMMddHHmmss")

        val timeStampArray = arrayListOf<Long>()
        val calFrom = Calendar.getInstance()
        val calTo = Calendar.getInstance()

        calFrom.time = Date()

        calFrom.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY)

        calFrom.add(Calendar.DAY_OF_MONTH,7*(relativeDate))
        calFrom.set(Calendar.HOUR_OF_DAY,0)
        calFrom.set(Calendar.MINUTE,0)
        calFrom.set(Calendar.SECOND,0)

        calTo.time = calFrom.time
        calTo.add(Calendar.DAY_OF_YEAR,7) //calFrom 에서 7일
        calTo.set(Calendar.HOUR_OF_DAY,0)
        calTo.set(Calendar.MINUTE,0)
        calTo.set(Calendar.SECOND,0)
        calTo.add(Calendar.SECOND,-1)

        val timeFrom1 = df.format(calFrom.time).toString()
        val timeTo1 = df.format(calTo.time).toString()


        for (i in 0 until 8){
            timeStampArray.add(calFrom.timeInMillis)
            Log.d("calcal arr",df.format(calFrom.time))
            calFrom.add(Calendar.DAY_OF_MONTH,1)
        }

        Log.d("calcal arr",timeStampArray.size.toString())

        val timeFrom = df.format(calFrom.time).toString()
        val timeTo = df.format(calTo.time).toString()

        Log.d("calcal",calFrom.time.toString())
        Log.d("calcal from",timeFrom)
        Log.d("calcal to",timeTo)
        return timeStampArray
    }

    override fun onChartGestureEnd(
        me: MotionEvent?,
        lastPerformedGesture: ChartTouchListener.ChartGesture?
    ) {
        Log.d("calcal graphhhhhhhh","onChartGestureEnd")

    }



    override fun onChartSingleTapped(me: MotionEvent?) {
        Log.d("calcal graphhhhhhhh","onChartSingleTapped")

    }

    override fun onChartGestureStart(
        me: MotionEvent?,
        lastPerformedGesture: ChartTouchListener.ChartGesture?
    ) {
        Log.d("calcal graphhhhhhhh","onChartGestureStart")

    }

    override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
        Log.d("calcal graphhhhhhhh","onChartScale")

    }

    override fun onChartLongPressed(me: MotionEvent?) {
        Log.d("calcal graphhhhhhhh","onChartLongPressed")

    }

    override fun onChartDoubleTapped(me: MotionEvent?) {
        Log.d("calcal graphhhhhhhh","onChartDoubleTapped")

    }

    override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
        Log.d("calcal graphhhhhhhh","onChartTranslate")

    }
}
