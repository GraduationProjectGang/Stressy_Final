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
import com.android.stressy.dataclass.db.PredictedStressDatabase
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainStressGraphFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_main_stress_graph, container, false)
        var stressChart = rootView!!.findViewById(R.id.mainBarChart) as HorizontalBarChart
        makeDataToBarEntry()




        initChart(stressChart)
        return rootView
    }

    fun initChart(chart:HorizontalBarChart){

//        val data = listOf<Int>(2,4,3,4,2,3,4)
        val data = List(7) { (2..4).random() }
        val week_average = data.average()
        val entries = arrayListOf<BarEntry>()
        for (i in data.indices) {
            val value = (Math.random() * 10).toFloat()
            entries.add(
                BarEntry(i.toFloat(), data[i].toFloat())
            )
        }
        val dataSet = BarDataSet(entries,"dtd")
        dataSet.apply {
            color = resources.getColor(R.color.colorPrimary)

            setDrawValues(false)
            valueTextSize = 13f
            highLightColor = resources.getColor(R.color.colorPrimary)

        }


        val dataSets = arrayListOf<IBarDataSet>(dataSet)
        val lineData = BarData(dataSets)
        //barchart design

        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            textSize = 14f
            setDrawGridLines(false)
            granularity = 1f
            isGranularityEnabled = false
        }

        chart.axisLeft.apply {
            granularity = 1f
            textSize = 15f

            axisMinimum = 0.0f
            axisMaximum = 5.0f
            setDrawGridLines(false)

            var stressDescription = arrayListOf<String>("","낮음","보통","높음","매우\n높음")
            setValueFormatter(IndexAxisValueFormatter(stressDescription))



        }

        chart.apply {
            setBorderColor(Color.DKGRAY)
            axisRight.isEnabled = false
//            legend.apply {
//                textSize = 12f
//                verticalAlignment = Legend.LegendVerticalAlignment.TOP
//            }
        }

        chart.run {
            this.data = lineData
//            setLine(true)
            invalidate()
        }
    }

    private fun makeDataToBarEntry(): ArrayList<Entry> = runBlocking{
        val dbObject = Room.databaseBuilder(
            requireContext(),
            PredictedStressDatabase::class.java, "stressPredicted"
        ).fallbackToDestructiveMigration().allowMainThreadQueries().build().predictedStressDao()


//        for (i in 0 until 10){
//            val timestamp_rand = (1603173028..1603605028).random().toLong()
//            val predictedData_rand = (2..4).random()
//            dbObject.insert(PredictedStressData(timestamp_rand,predictedData_rand))
//        }



        val timeStampArr = makeDateArray(0)
        val dataArr = mutableMapOf<String, Double>() //날짜, 점수 맵
        for (i in 0 until timeStampArr.size-1){
            val getData = dbObject.getFromTo(timeStampArr[i],timeStampArr[i+1])//하루동안의 데이터 받아오기
            var avg = 0.0
            if (getData.isNotEmpty()){
                val df = SimpleDateFormat("MM/dd")
                val date = Date(timeStampArr[i])
                val tempDate = df.format(date)
                for (each in getData){
                    avg += each.stressPredicted
                }
                avg /= getData.size
            }
            dataArr["tempDate"] = avg
        }
        Log.d("dateArr",dataArr.toString())


        val data = arrayListOf<Long>()
        val entries = ArrayList<Entry>()
        for (i in entries.indices){
            entries.add(Entry(i.toFloat(), data[i].toFloat()))
        }
        return@runBlocking entries
    }

    fun makeDateArray(leftCount: Int): ArrayList<Long>{ //얼마만큼 왼쪽으로 swipe 하냐
        val timeStampArray = arrayListOf<Long>()
        val calFrom = Calendar.getInstance()
        val calTo = Calendar.getInstance()

        calFrom.time = Date()
        calFrom.add(Calendar.DAY_OF_MONTH,-7*(leftCount+1))
        calFrom.set(Calendar.HOUR_OF_DAY,0)
        calFrom.set(Calendar.MINUTE,0)
        calFrom.set(Calendar.SECOND,0)

        calTo.time = Date()
        calTo.add(Calendar.DAY_OF_MONTH,-1*(leftCount+1))
        calTo.set(Calendar.HOUR_OF_DAY,0)
        calTo.set(Calendar.MINUTE,0)
        calTo.set(Calendar.SECOND,0)
        calTo.add(Calendar.SECOND,-1)
        val df = SimpleDateFormat("yyyyMMddHHmmss")

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
        Log.d("current", calFrom.timeInMillis.toString())
        Log.d("current", calTo.timeInMillis.toString())
        return timeStampArray
    }
}