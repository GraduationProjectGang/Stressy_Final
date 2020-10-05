package com.android.stressy.activity

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.android.stressy.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet

class StressGraphFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_stress_graph, container, false)
        var stressChart = rootView!!.findViewById(R.id.stressGraph) as BarChart
        initChart(stressChart)
        return rootView
    }

    fun initChart(chart:BarChart){
        val data = listOf<Int>(3,4,3,4,3,4,3)
        val week_average = data.average()
        val dataSet = BarDataSet(makeDataToBarEntry(data),"stress")
        dataSet.apply {
            color = resources.getColor(R.color.colorAccent)
            valueTextSize = 13f

            highLightColor = resources.getColor(R.color.colorPrimary)

        }


        val dataSets = arrayListOf<IBarDataSet>(dataSet)
        val barData = BarData(dataSets)
        barData.barWidth = 0.3f
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
            textSize = 20f

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
            legend.apply {
                textSize = 14f
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
            }
        }

        chart.run {
            this.data = barData
            setFitBars(true)
            invalidate()
        }
    }

    private fun makeDataToBarEntry(data: List<Int>): ArrayList<BarEntry> {
        val entries = ArrayList<BarEntry>()
        var index = 1.0f
        for (i in data){
            entries.add(BarEntry(index,data[i].toFloat()))
            index += 1
        }
        return entries
    }


}