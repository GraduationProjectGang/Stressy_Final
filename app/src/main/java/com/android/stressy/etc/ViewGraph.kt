package com.android.stressy.etc

import android.view.MotionEvent
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener

class ViewGraph : OnChartGestureListener,OnChartValueSelectedListener {
    override fun onChartGestureEnd(
        me: MotionEvent?,
        lastPerformedGesture: ChartTouchListener.ChartGesture?
    ) {
        TODO("Not yet implemented")
    }

    override fun onChartFling(
        me1: MotionEvent?,
        me2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ) {

    }

    override fun onChartSingleTapped(me: MotionEvent?) {
        TODO("Not yet implemented")
    }

    override fun onChartGestureStart(
        me: MotionEvent?,
        lastPerformedGesture: ChartTouchListener.ChartGesture?
    ) {
        TODO("Not yet implemented")
    }

    override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
        TODO("Not yet implemented")
    }

    override fun onChartLongPressed(me: MotionEvent?) {
        TODO("Not yet implemented")
    }

    override fun onChartDoubleTapped(me: MotionEvent?) {
        TODO("Not yet implemented")
    }

    override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
        TODO("Not yet implemented")
    }

    override fun onNothingSelected() {
        TODO("Not yet implemented")
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        TODO("Not yet implemented")
    }
}