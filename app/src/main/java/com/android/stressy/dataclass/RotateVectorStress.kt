package com.android.stressy.dataclass


data class RotateVectorStress(var angleList: MutableList<String>, val timestamp: String,val index:Int, val time: Long) {
    init {
        angleList = mutableListOf()
    }
}