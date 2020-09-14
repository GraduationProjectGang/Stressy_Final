package com.android.stressy.dataclass

import android.location.Location

data class Locate(var locationList: MutableList<Location>, val timestmamp: String) {
    init{
        locationList = mutableListOf()
    }
}