package com.android.stressy.activity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.android.stressy.R
import com.android.stressy.dataclass.db.StressPredictedDatabase
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
    var relativeDate = 0
    lateinit var chart : LineChart
    lateinit var timeStampArr: ArrayList<Long>
    lateinit var button_graph_left:Button
    lateinit var button_graph_right:Button
    lateinit var ll:LimitLine
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_stress_graph, container, false)
        chart = rootView!!.findViewById(R.id.stressGraph) as LineChart
        button_graph_left = rootView.findViewById(R.id.button_graph_left) as Button
        button_graph_right = rootView.findViewById(R.id.button_graph_right) as Button
        initChart(chart,makeDataToBarEntry(relativeDate))
        initButton()
        return rootView
    }
    fun initButton(){

        button_graph_left.setOnClickListener {
            relativeDate -= 1
            initChart(chart,makeDataToBarEntry(relativeDate))

        }
        button_graph_right.setOnClickListener {
            if (relativeDate < 0)
                relativeDate += 1
            initChart(chart,makeDataToBarEntry(relativeDate))
        }
    }

    fun initChart(chart:LineChart,entries: ArrayList<Entry>){
        var week_average = 0f
        for (entry in entries){
            week_average += entry.y
        }
        week_average /= entries.size

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
            textSize = 13f
            setDrawGridLines(false)
            granularity = 1f
            isGranularityEnabled = false

            val df = SimpleDateFormat("MM/dd")
            var dateString = arrayListOf<String>()
            for (date in timeStampArr){
                dateString.add(df.format(date))
            }
            spaceMax = 0.4f
            labelRotationAngle = -45f
            valueFormatter = IndexAxisValueFormatter(dateString)
        }

        val stressDescription = arrayListOf("","낮음","보통","높음","매우높음")
        chart.axisLeft.apply {
            granularity = 1f
            textSize = 14f
            val color1 = Color.parseColor("#3B60B3")

            textColor = color1
            axisMinimum = 0.0f
            axisMaximum = 5.0f

            valueFormatter = IndexAxisValueFormatter(stressDescription)

            ll = LimitLine(week_average, "평균")
            ll.lineColor = Color.RED
            ll.lineWidth = 2f
            ll.textColor = Color.RED
            ll.textSize = 12f
            ll.enableDashedLine(1f,1f,1f)


            addLimitLine(ll)
            setDrawLimitLinesBehindData(true)
        }

        chart.apply {
            setBorderColor(Color.DKGRAY)
            axisRight.isEnabled = false
            legend.isEnabled = false
            description.text = ""
            notifyDataSetChanged()
        }

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


        //csv data 넣기
//        val file = resources.openRawResource(R.raw.stresspredicted)
//        val br = BufferedReader(InputStreamReader(file))
//        for (line in br.lines()){
//            val arr = line.split(",")
//            val tempData = StressPredictedData(arr[0].toLong(),arr[1].toInt())
//            dbObject.insert(tempData)
//            Log.d("insert data",tempData.toString())
//        }







        timeStampArr = makeDateArray(relativeDate)
        var dataArr = arrayListOf<Float>() //날짜, 점수 맵

        val dataAll = dbObject.getAll()
        Log.d("getdata",dataAll.size.toString())

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




        ///temp로 랜덤 데이터
        for (i in dataArr.indices){
            dataArr[i] = Random().nextFloat() * (1..4).random()
        }


        val entries = ArrayList<Entry>()
        for (i in dataArr.indices){
            entries.add(Entry(i.toFloat(), dataArr[i]))
            Log.d("getdata",entries[i].toString())

        }
        return entries
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
}
