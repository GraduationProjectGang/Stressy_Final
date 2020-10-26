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
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class StressGraphFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_stress_graph, container, false)
        var stressChart = rootView!!.findViewById(R.id.stressGraph) as LineChart
        makeDataToBarEntry()

        initChart(stressChart)
        return rootView
    }

    fun initChart(chart:LineChart){

//        val data = listOf<Int>(2,4,3,4,2,3,4)
        val data = List(7) { (2..4).random() }
        val week_average = data.average()
        val entries = arrayListOf<Entry>()
        for (i in data.indices) {
            val value = (Math.random() * 10).toFloat()
            entries.add(
                Entry(i.toFloat(), data[i].toFloat())
            )
        }
        val dataSet = LineDataSet(entries,"stress")
        dataSet.apply {
            color = resources.getColor(Color.DKGRAY)
            setLineWidth(2f)
            setCircleSize(5f)
            setDrawValues(false)
            valueTextSize = 13f
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
        }

        var stressDescription = arrayListOf<String>("","낮음","보통","높음","매우\n높음")
        chart.axisLeft.apply {
            granularity = 1f
            textSize = 15f

            axisMinimum = 0.0f
            axisMaximum = 5.0f
            setValueFormatter(IndexAxisValueFormatter(stressDescription))

            val ll = LimitLine(week_average.toFloat(), "평균")
            ll.lineColor = Color.RED
            ll.lineWidth = 2f
            ll.textColor = Color.RED
            ll.textSize = 12f

            addLimitLine(ll)

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

    private fun makeDataToBarEntry(): ArrayList<Entry> {
        val dbObject = Room.databaseBuilder(
            requireContext(),
            PredictedStressDatabase::class.java, "stress"
        ).fallbackToDestructiveMigration().build().predictedStressDao()

        val timeStampArr = makeDateArray(0)


        val data = arrayListOf<Long>()
        val entries = ArrayList<Entry>()
        for (i in entries.indices){
            entries.add(Entry(i.toFloat(), data[i].toFloat()))
        }
        return entries
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