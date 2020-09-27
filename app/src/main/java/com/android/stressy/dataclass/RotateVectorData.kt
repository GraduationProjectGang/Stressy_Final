package com.android.stressy.dataclass


data class RotateVectorData(var angleList: MutableList<String>, val timestamp: String) {
    init {
        angleList = mutableListOf()
    }
}