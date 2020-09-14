package com.android.stressy.dataclass


data class RotateVector(var angleList: MutableList<String>, val timestamp: String) {
    init {
        angleList = mutableListOf()
    }
}