package com.android.stressy.dataclass

data class UsageStatsCollection(var statsList: ArrayList<UsageStat>, val index: String, val timestamp: Long, val date:String) {
    init {
        statsList = ArrayList()
    }
}