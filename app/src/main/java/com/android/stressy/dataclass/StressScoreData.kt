package com.android.stressy.dataclass


data class StressScoreData(val timestamp: String, val stressCount: String, val index: String, val date: String) {
    constructor() : this("", "", "", "") {
    }
}