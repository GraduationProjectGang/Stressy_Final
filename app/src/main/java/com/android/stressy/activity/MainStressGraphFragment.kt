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
import com.android.stressy.R
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet


class MainStressGraphFragment() : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_main_stress_graph, container, false)
        var stressChart = rootView!!.findViewById<HorizontalBarChart>(R.id.mainBarChart)
        val data = requireArguments().getDoubleArray("data") as DoubleArray
        initChart(stressChart, data)
        return rootView
    }

    fun initChart(chart:HorizontalBarChart, data:DoubleArray){
        val tf = ResourcesCompat.getFont(requireContext(),R.font.noto_sans) as Typeface

        val entries = ArrayList<BarEntry>()
        for (i in data.indices){
            entries.add(BarEntry(i.toFloat(), data[i].toFloat()))
            Log.d("mainfrag",i.toFloat().toString()+"    " +data[i].toFloat().toString())

        }

        val dataSet = BarDataSet(entries,"dtd")

        dataSet.apply {
            setColor(R.color.colorPrimaryDark)
            setDrawValues(false)
            valueTextSize = 10f
//            barBorderWidth = 1f
            valueTypeface = tf
        }

        val dataSets = arrayListOf<IBarDataSet>(dataSet)
        val color1 = Color.parseColor("#9DAFD9")
        val color2 = Color.parseColor("#6C88C6")
        val color3 = Color.parseColor("#3B60B3")
        val color4 = Color.parseColor("#0A38A0")
        val colorArr = mutableListOf<Int>(color1,color2,color3,color4)
        dataSet.colors = colorArr

        val barData = BarData(dataSets)
        //barchart design

        chart.axisLeft.apply {
            typeface = tf
            setDrawGridLines(false)
            isEnabled = false
        }

        chart.axisRight.apply {
            typeface = tf
            textSize = 12f
            axisMinimum = 0f
            granularity = 1f
            setDrawGridLines(false)
            isEnabled = true
            typeface = tf
        }

        chart.xAxis.apply {
            typeface = tf
            granularity = 1f
            textSize = 13f
            axisMinimum = -0.5f
            setPosition(XAxis.XAxisPosition.BOTTOM)
            setDrawGridLines(false)
            typeface = tf
            var stressDescription = arrayListOf<String>("낮음","보통","높음","매우\n높음")
            setValueFormatter(IndexAxisValueFormatter(stressDescription))
        }

        chart.apply {

            defaultFocusHighlightEnabled = false
            description.isEnabled = false
            this.data = barData
            legend.isEnabled = false
            disableScroll()
            centerViewTo(chart.getXChartMax(),0f,YAxis.AxisDependency.RIGHT)
            setScaleEnabled(false)
//            setViewPortOffsets(200f, 0f, 0f, 40f)
            animateY(1000)
            invalidate()
        }
    }


}

