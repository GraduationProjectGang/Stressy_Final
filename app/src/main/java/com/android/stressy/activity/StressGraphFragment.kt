package com.android.stressy.activity

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.android.stressy.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [StressGraphFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
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
        val dataSet = BarDataSet(makeDataToBarEntry(data),"stress")
        val dataSets = arrayListOf<IBarDataSet>(dataSet)
        val barData = BarData(dataSets)
        barData.barWidth = 0.3f
        chart.run {
            this.data = barData
            setFitBars(true)
            invalidate()
        }
        chart.setBorderColor(Color.DKGRAY)

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