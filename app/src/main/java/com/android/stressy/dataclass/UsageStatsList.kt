package com.android.stressy.dataclass

data class UsageStatsList(var statsList: ArrayList<UsageAppData>, val index: String, val timestamp: Long, val date:String) {
    init {
        statsList = ArrayList()
    }
}