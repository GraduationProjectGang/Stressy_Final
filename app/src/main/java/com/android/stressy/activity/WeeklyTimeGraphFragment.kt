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
import kotlinx.android.synthetic.main.fragment_weekly_time_graph.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class WeeklyTimeGraphFragment : Fragment() {
    var relativeDate = 0
    lateinit var chart : LineChart
    lateinit var timeStampArr: ArrayList<Long>
    lateinit var button_graph_left:Button
    lateinit var button_graph_right:Button
    lateinit var ll:LimitLine
    lateinit var hoursArr: ArrayList<Long>
    lateinit var timeArr: ArrayList<Long>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_weekly_time_graph, container, false)
        chart = rootView!!.findViewById(R.id.hourlyStressGraph) as LineChart
        hoursArr = getHours()

        initChart(chart,makeDataToBarEntry(relativeDate))
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initButton()

    }

    private fun initButton() {
        button_weekly_time_left.setOnClickListener {
            relativeDate -= 1
            initChart(chart,makeDataToBarEntry(relativeDate))

        }
        button_weekly_time_right.setOnClickListener {
            if (relativeDate < 0){
                relativeDate += 1
                initChart(chart,makeDataToBarEntry(relativeDate))
            }

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
            setDrawCircles(false)
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

            val df = SimpleDateFormat("H시")
            var dateString = arrayListOf<String>()
            for (date in hoursArr){
                dateString.add(df.format(date))
            }

            spaceMax = 0.4f
            valueFormatter = IndexAxisValueFormatter(dateString)

        }

        val stressDescription = arrayListOf("","낮음","보통","높음","매우높음")
        chart.axisLeft.apply {
            granularity = 1f
            textSize = 14f
            val color1 = Color.parseColor("#3B60B3")

            textColor = color1
            axisMinimum = 0.0f
            axisMaximum = 4.0f

            valueFormatter = IndexAxisValueFormatter(stressDescription)

            removeAllLimitLines()
            ll = LimitLine(week_average, "평균")
            ll.lineColor = Color.RED
            ll.lineWidth = 1.5f
            ll.textColor = Color.RED
            ll.textSize = 12f
            ll.enableDashedLine(10f,0f,0f)

            addLimitLine(ll)
            setDrawLimitLinesBehindData(true)
        }

        chart.apply {
            setBorderColor(Color.DKGRAY)
            axisRight.isEnabled = false
            legend.isEnabled = false
            description.text = ""
            extraBottomOffset = 10f
            notifyDataSetChanged()
        }

        chart.run {
            this.data = lineData
//            setLine(true)

            Log.d("setset","invalidate")

            invalidate()
        }
    }

    private fun getHours(): ArrayList<Long> { //지난 24시간
        val nowMinus24 = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY,-22)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val df = SimpleDateFormat("MM/dd hh:mm:ss")


        timeArr = arrayListOf<Long>()
        for (i in 0 until 24){
            timeArr.add(nowMinus24.timeInMillis)
            val tempDate = df.format(nowMinus24.time)
            Log.d("timeStr",tempDate+" "+ timeArr.size.toString())

            nowMinus24.add(Calendar.HOUR_OF_DAY,1)
        }
        return timeArr
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







        timeStampArr = makeWeekDateArray(relativeDate)
        var dataArr = FloatArray(24) {1f * (it+1)} //날짜, 점수 맵
        var sizeArr = FloatArray(24) {1f * (it+1)} //시간별 size array



//        val dataAll = dbObject.getAll()
//        Log.d("getdata",dataAll.size.toString())


        //일주일 간의 데이터를 시간별로 match
        for (i in 0 until timeStampArr.size-1){
            val getData = dbObject.getFromTo(timeStampArr[i],timeStampArr[i+1])//하루동안의 데이터 받아오기
            var avg = 0f

            if (getData.isNotEmpty()){
                val cal = Calendar.getInstance()

                for (data in getData){
                    val tempCal = Calendar.getInstance()
                    tempCal.timeInMillis = data.timestamp
                    val h = tempCal.get(Calendar.HOUR_OF_DAY)
                    dataArr[h] = dataArr[h] + data.stressPredicted
                    sizeArr[h] = sizeArr[h] + 1
                }

                val df = SimpleDateFormat("MM/dd")
                val date = Date(timeStampArr[i])

                val tempDate = df.format(date)
                Log.d("getdata",tempDate+" "+ getData.size.toString())
            }
        }

        val entries = ArrayList<Entry>()

        Log.d("getdata22",dataArr.contentToString())
        Log.d("getdata22",sizeArr.contentToString())

        for (h in dataArr.indices){
            dataArr[h] = dataArr[h]/sizeArr[h]

            entries.add(Entry(h.toFloat(), dataArr[h]))
            Log.d("getdata2",h.toString()+"   "+entries[h].toString()+"   "+sizeArr[h].toString())
        }
        return entries
    }


    fun makeWeekDateArray(relativeDate: Int): ArrayList<Long>{ //얼마만큼 왼쪽으로 swipe 하냐
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
