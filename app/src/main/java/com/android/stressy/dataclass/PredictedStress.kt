package com.android.stressy.dataclass

import kotlin.properties.Delegates

class PredictedStress {
    var predictedStressToday by Delegates.notNull<Double>()
    fun getStartTimestamp(dataTimestamp:Long){

    }
    fun getEndTimestamp(dataTimestamp:Long){

    }
//    fun getDataFromTo(start:Long, end:Long):ArrayList<Int>{
//    }
}