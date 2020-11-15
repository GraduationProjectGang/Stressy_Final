package com.android.stressy.activity

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
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
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainTimeStressGraphFragment() : Fragment() {
    lateinit var ll:LimitLine
    lateinit var timeStampArr: ArrayList<Long>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_main_time_stress_graph, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val stressChart = requireActivity().findViewById<LineChart>(R.id.mainTimeLineChart)
        val dataArr = getGraphData()
        initChart(stressChart, dataArr)
    }

    private fun getGraphData(): List<Double> = runBlocking{
        val dbObject = Room.databaseBuilder(
            requireActivity(),
            StressPredictedDatabase::class.java, "stressPredicted"
        ).fallbackToDestructiveMigration().allowMainThreadQueries().build().stressPredictedDao()


//        for (i in 0 until 10){
//            val timestamp_rand = (1603173028..1603605028).random().toLong()
//            val predictedData_rand = (2..4).random()
//            dbObject.insert(PredictedStressData(timestamp_rand,predictedData_rand))
//        }

        timeStampArr = getHours()

        val resultArr = arrayListOf<Double>()
        //지난 자정부터 데이터 받아오기
        for (timeIdx in 1 until timeStampArr.size-1){
            val getData = dbObject.getFromTo(timeStampArr[timeIdx],timeStampArr[timeIdx+1])
            Log.d("timeStr.getData",getData.size.toString())
            var zero = 0.0
            var one = 0.0
            var two = 0.0
            var three = 0.0

            var avg = 0.0
            var size = 0

            if (getData.isNotEmpty()){
                Log.d("timeStr.getdata",getData.size.toString())
                for (data in getData){
                    val predicted = data.stressPredicted
                    if (predicted == 0) zero++
                    else if (predicted == 1) one++
                    else if (predicted == 2) two++
                    else if (predicted == 3) three++
                }
                var dataArr = doubleArrayOf(zero, one, two, three)


                if (getData.isNotEmpty()){
                    size = getData.size
                    for (scoreCount in dataArr.indices){
                        avg += scoreCount*dataArr[scoreCount]
                    }
                    avg /= size
                }
            }else{
                Log.d("timeStr.getdata","empty")

            }
            resultArr.add(avg)

        }



        return@runBlocking resultArr
    }

    private fun getHours(): ArrayList<Long> { //지난 24시간
        val nowMinus24 = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY,-22)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val df = SimpleDateFormat("MM/dd hh:mm:ss")


        val timeArr = arrayListOf<Long>()
        for (i in 0 until 24){
            timeArr.add(nowMinus24.timeInMillis)
            val tempDate = df.format(nowMinus24.time)
            Log.d("timeStr",tempDate+" "+ timeArr.size.toString())

            nowMinus24.add(Calendar.HOUR_OF_DAY,1)
        }
        return timeArr
    }

    fun initChart(chart: LineChart, data: List<Double>){
        val tf = ResourcesCompat.getFont(requireContext(),R.font.noto_sans) as Typeface

        val entries = getEntries(data)

        var avg = 0f
        for (entry in entries){
            avg += entry.y
        }
        avg /= entries.size

        val dataSet = LineDataSet(entries,"stress")
        dataSet.apply {
            color = resources.getColor(R.color.colorPrimary)
            setLineWidth(2f)
//            setCircleSize(4f)
//            setCircleColor (resources.getColor(R.color.colorPrimaryDark))
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
            typeface = tf
            position = XAxis.XAxisPosition.BOTTOM
            textSize = 13f
            setDrawGridLines(false)
            granularity = 1f
            isGranularityEnabled = false

            val df = SimpleDateFormat("H시")
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
            typeface = tf
            granularity = 1f
            textSize = 14f
            val color1 = Color.parseColor("#3B60B3")

            textColor = color1
            axisMinimum = 0.0f
            axisMaximum = 4.0f

            valueFormatter = IndexAxisValueFormatter(stressDescription)

            removeAllLimitLines()
            ll = LimitLine(avg, "평균")
            ll.lineColor = resources.getColor(R.color.colorAccent)
            ll.lineWidth = 2f
            ll.textColor = resources.getColor(R.color.colorAccent)
            ll.textSize = 12f

            addLimitLine(ll)
            setDrawLimitLinesBehindData(true)
        }

        chart.axisRight.apply {
            typeface = tf
            isEnabled = false
            setDrawAxisLine(true)
        }
        chart.apply {
            setBorderColor(Color.DKGRAY)
            legend.isEnabled = false
            description.text = ""
            disableScroll()

            notifyDataSetChanged()
        }

        chart.run {
            this.data = lineData
//            setLine(true)
            Log.d("setset","invalidate")

            invalidate()
        }
    }

    fun getEntries(data:List<Double>): ArrayList<Entry>{
        val entries = ArrayList<Entry>()

        for (i in data.indices){
            entries.add(Entry(i.toFloat(), data[i].toFloat()+1))
            Log.d("mainfrag",i.toFloat().toString()+"    " +data[i].toFloat().toString())

        }

        return entries
    }

}

